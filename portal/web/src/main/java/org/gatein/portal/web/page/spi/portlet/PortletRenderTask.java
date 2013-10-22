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
package org.gatein.portal.web.page.spi.portlet;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.MimeResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.web.page.Result;
import org.gatein.portal.web.page.WindowContext;
import org.gatein.portal.web.page.spi.RenderTask;
import org.gatein.portal.web.servlet.Context;
import org.gatein.portal.web.page.spi.RenderTask;
import org.gatein.portal.web.servlet.Context;
import org.w3c.dom.Element;

/**
 * @author Julien Viet
 */
class PortletRenderTask extends RenderTask {

    /** . */
    private final PortletContentProvider provider;

    /** . */
    private final WindowContext windowContext;

    /** . */
    private final PortletInvoker invoker;

    /** . */
    private final PortletContent content;

    /** . */
    private final  HttpServletRequest servletReq;

    /** . */
    private final  HttpServletResponse servletResp;

    PortletRenderTask(PortletContentProvider provider, WindowContext windowContext) {
        this.windowContext = windowContext;
        this.invoker = provider.portletManager.getInvoker();
        this.provider = provider;
        this.content = (PortletContent) windowContext.state;
        this.servletReq = Context.getCurrentRequest();
        this.servletResp = Context.getCurrentResponse();
    }

    @Override
    public Result execute(Locale locale) {

        //
        ContextLifeCycle lifeCycle = Request.getCurrent().suspend();
        PortletInvocationResponse response = null;
        PortletInvokerException failure = null;
        try {

            //
            GateInPortletInvocationContext context = new GateInPortletInvocationContext(provider, windowContext, lifeCycle);
            RenderInvocation invocation = new RenderInvocation(context);
            invocation.setClientContext(new GateInClientContext());
            invocation.setPortalContext(new AbstractPortalContext());
            invocation.setInstanceContext(new AbstractInstanceContext(windowContext.name, AccessMode.READ_ONLY));
            invocation.setWindowContext(new AbstractWindowContext(windowContext.name));
            invocation.setUserContext(new AbstractUserContext());
            invocation.setSecurityContext(new AbstractSecurityContext(servletReq));
            invocation.setRequest(servletReq);
            invocation.setResponse(servletResp);
            invocation.setTarget(content.portlet.getContext());
            invocation.setMode(content.mode != null ? content.mode : Mode.VIEW);
            invocation.setWindowState(content.windowState != null ? content.windowState : org.gatein.pc.api.WindowState.NORMAL);
            invocation.setNavigationalState(content.parameters != null ? ParametersStateString.create(content.parameters) : null);
            invocation.setPublicNavigationalState(windowContext.computePublicParameters());

            //
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
                    title = windowContext.resolveTitle(locale);
                }
                result = new Result.Fragment(headers, headerTags, title, fragment.getContent());
            } else {
                throw new UnsupportedOperationException("Not yet handled " + response);
            }
        }
        return result;
    }
}
