/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.gatein.portal.page.spi.portlet;

import java.io.IOException;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.MimeResponse;

import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.page.Result;
import org.gatein.portal.page.WindowContext;
import org.gatein.portal.page.spi.RenderTask;
import org.gatein.portal.servlet.Context;
import org.w3c.dom.Element;

/**
 * @author Julien Viet
 */
class PortletRenderTask extends RenderTask implements PortletInvocationContext {

    /** . */
    private final WindowContext context;

    /** . */
    private final PortletInvoker invoker;

    /** . */
    private final RenderInvocation invocation;

    /** . */
    private ContextLifeCycle lifeCycle;

    /** . */
    private final GateInPortletInvocationContext invocationContext;

    PortletRenderTask(PortletContentProvider provider, WindowContext context) {

        PortletContent pc = (PortletContent) context.state;

        //
        RenderInvocation invocation = new RenderInvocation(this);
        invocation.setClientContext(new GateInClientContext());
        invocation.setPortalContext(new AbstractPortalContext());
        invocation.setInstanceContext(new AbstractInstanceContext(pc.name, AccessMode.READ_ONLY));
        invocation.setWindowContext(new AbstractWindowContext(pc.name));
        invocation.setUserContext(new AbstractUserContext());
        invocation.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        invocation.setRequest(Context.getCurrentRequest());
        invocation.setResponse(Context.getCurrentResponse());
        invocation.setTarget(pc.portlet.getContext());
        invocation.setMode(pc.mode != null ? pc.mode : Mode.VIEW);
        invocation.setWindowState(pc.windowState != null ? pc.windowState : org.gatein.pc.api.WindowState.NORMAL);
        invocation.setNavigationalState(pc.parameters != null ? ParametersStateString.create(pc.parameters) : null);
        invocation.setPublicNavigationalState(context.computePublicParameters());

        //
        this.context = context;
        this.invoker = provider.portletManager.getInvoker();
        this.invocation = invocation;
        this.invocationContext = new GateInPortletInvocationContext(provider, context);
    }

    @Override
    public Result execute(Locale locale) {
        Request request = Request.getCurrent();
        lifeCycle = request.suspend();
        PortletInvocationResponse response = null;
        PortletInvokerException failure = null;
        try {
            response = invoker.invoke(invocation);
        } catch (PortletInvokerException e) {
            failure = e;
        } finally {
            lifeCycle.resume();
        }
        Result result;
        if (failure != null) {
            failure.printStackTrace();
            result = new Result.Error(true, failure);
        } else {
            if (response instanceof FragmentResponse) {
                FragmentResponse fragment = (FragmentResponse) response;
                ResponseProperties properties = fragment.getProperties();
                List<Map.Entry<String, String>> headers = Collections.emptyList();
                List<Element> headerTags = Collections.emptyList();
                if (properties != null) {
                    if (properties.getTransportHeaders() != null) {
                        headers = new LinkedList<Map.Entry<String, String>>();
                        for (String headerName : properties.getTransportHeaders().keySet()) {
                            String headerValue = properties.getTransportHeaders().getValue(headerName);
                            headers.add(new AbstractMap.SimpleEntry<String, String>(headerName, headerValue));
                        }
                    }
                    if (properties.getMarkupHeaders() != null) {
                        headerTags = new LinkedList<Element>();
                        for (String headerName : properties.getMarkupHeaders().keySet()) {
                            if (MimeResponse.MARKUP_HEAD_ELEMENT.equals(headerName)) {
                                for (Element headerValue : properties.getMarkupHeaders().getValues(headerName)) {
                                    headerTags.add(headerValue);
                                }
                            }
                        }
                    }
                }
                String title = fragment.getTitle();
                if (title == null) {
                    title = context.resolveTitle(locale);
                }
                result = new Result.Fragment(headers, headerTags, title, fragment.getContent());
            } else {
                throw new UnsupportedOperationException("Not yet handled " + response);
            }
        }
        return result;
    }

    @Override
    public MediaType getResponseContentType() {
        return invocationContext.getResponseContentType();
    }

    @Override
    public String encodeResourceURL(String url) throws IllegalArgumentException {
        return invocationContext.encodeResourceURL(url);
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        invocationContext.renderURL(writer, containerURL, format);
    }

    @Override
    public String renderURL(ContainerURL containerURL, URLFormat format) {
        Request current = Request.getCurrent();
        if (current != null) {
            ContextLifeCycle currentLF = current.suspend();
            try {
                return doRenderURL(containerURL, format);
            } finally {
                currentLF.resume();
            }
        } else {
            return doRenderURL(containerURL, format);
        }
    }

    private String doRenderURL(ContainerURL containerURL, URLFormat format) {
        lifeCycle.resume();
        try {
            return invocationContext.renderURL(containerURL, format);
        } finally {
            Request.getCurrent().suspend();
        }
    }
}
