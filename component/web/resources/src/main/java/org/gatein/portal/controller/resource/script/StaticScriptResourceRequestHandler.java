/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
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

package org.gatein.portal.controller.resource.script;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.ResourceRequestFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

/**
 * A handler for non-JavaScript resources possibly distributed with third party JavaScript frameworks,
 * such as images, HTML templates, CSS, etc.
 *
 * Handles response's {@code Cache-Control} and request's {@code If-Modified-Since}
 * and {@code Last-Modified} in the same manner as {@link ResourceRequestHandler}.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class StaticScriptResourceRequestHandler extends WebRequestHandler {
    private static final Log log = ExoLogger.getLogger(StaticScriptResourceRequestHandler.class);

    public StaticScriptResourceRequestHandler() {
        super();
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#getHandlerName()
     */
    @Override
    public String getHandlerName() {
        return "staticScriptResource";
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#execute(org.exoplatform.web.ControllerContext)
     */
    @Override
    public boolean execute(ControllerContext context) throws Exception {
        String resourcePath = context.getParameter(ResourceRequestHandler.RESOURCE_QN);
        if (resourcePath != null) {
            /* prepend the slash */
            resourcePath = new StringBuilder(1+resourcePath.length()).append('/').append(resourcePath).toString();
        }
        final boolean isDebugEnabled = log.isDebugEnabled();
        if (isDebugEnabled) {
            log.debug("About to serve '"+ resourcePath +"'");
        }
        JavascriptConfigService service = (JavascriptConfigService) PortalContainer.getComponent(JavascriptConfigService.class);
        StaticScriptResource r = service.getStaticScriptResource(resourcePath);
        if (r != null) {
            if (isDebugEnabled) {
                log.debug("Found the context '"+ r.getContextPath() +"' for '"+ resourcePath +"'");
            }

            String targetContextPath = r.getContextPath();
            PortalContainer portalContainer = PortalContainer.getInstance();
            ServletContext mergedContext = portalContainer.getPortalContext();
            ServletContext targetContext = mergedContext.getContext(targetContextPath);

            String resourceURI = r.getDirectoryAndPath();
            HttpServletRequest request = context.getRequest();
            HttpServletResponse response = context.getResponse();

            /* use the same caching logic as ResourceRequestHandler */
            response.setHeader(ResourceRequestHandler.CACHE_CONTROL, ResourceRequestHandler.CACHE_CONTROL_VALUE);
            long ifModifiedSince = request.getDateHeader(ResourceRequestHandler.IF_MODIFIED_SINCE);
            long lastModified = r.getLastModified();
            if (ResourceRequestHandler.isModified(ifModifiedSince, lastModified)) {
                if (isDebugEnabled) {
                    log.debug("Sending bytes for '"+ resourcePath +"'");
                }
                response.setDateHeader(ResourceRequestFilter.LAST_MODIFIED, lastModified);
                RequestDispatcher dispatcher = targetContext.getRequestDispatcher(resourceURI);
                dispatcher.forward(request, response);
            } else {
                if (isDebugEnabled) {
                    log.debug("Sending "+ HttpServletResponse.SC_NOT_MODIFIED +" for '"+ resourcePath +"'");
                }
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.exoplatform.web.WebRequestHandler#getRequiresLifeCycle()
     */
    @Override
    protected boolean getRequiresLifeCycle() {
        return false;
    }

}
