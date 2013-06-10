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
package org.gatein.portal.page;

import java.io.IOException;
import java.io.Writer;

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
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.servlet.Context;

/**
 * @author Julien Viet
 */
public class RenderTask implements Runnable, PortletInvocationContext {

    /** . */
    PortletInvocationResponse response;

    /** . */
    PortletInvokerException failure;

    /** . */
    private final WindowContext context;

    /** . */
    private final PortletInvoker invoker;

    /** . */
    private final RenderInvocation invocation;

    /** . */
    private ContextLifeCycle lifeCycle;

    public RenderTask(WindowContext context) {

        //
        RenderInvocation invocation = new RenderInvocation(this);
        invocation.setClientContext(new GateInClientContext());
        invocation.setPortalContext(new AbstractPortalContext());
        invocation.setInstanceContext(new AbstractInstanceContext(context.state.name, AccessMode.READ_ONLY));
        invocation.setWindowContext(new AbstractWindowContext(context.state.name));
        invocation.setUserContext(new AbstractUserContext());
        invocation.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
        invocation.setRequest(Context.getCurrentRequest());
        invocation.setResponse(Context.getCurrentResponse());
        invocation.setTarget(context.getPortlet().getContext());
        invocation.setMode(context.state.mode != null ? context.state.mode : Mode.VIEW);
        invocation.setWindowState(context.state.windowState != null ? context.state.windowState : org.gatein.pc.api.WindowState.NORMAL);
        invocation.setNavigationalState(context.state.parameters != null ? ParametersStateString.create(context.state.parameters) : null);
        invocation.setPublicNavigationalState(context.computePublicParameters());

        //
        this.context = context;
        this.invoker = context.page.portletManager.getInvoker();
        this.invocation = invocation;
    }

    @Override
    public void run() {
        Request request = Request.getCurrent();
        lifeCycle = request.suspend();
        try {
            response = invoker.invoke(invocation);
        } catch (PortletInvokerException e) {
            failure = e;
        } finally {
            lifeCycle.resume();
        }
    }

    @Override
    public MediaType getResponseContentType() {
        return context.getResponseContentType();
    }

    @Override
    public String encodeResourceURL(String url) throws IllegalArgumentException {
        return context.encodeResourceURL(url);
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        context.renderURL(writer, containerURL, format);
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
            return context.renderURL(containerURL, format);
        } finally {
            Request.getCurrent().suspend();
        }
    }
}
