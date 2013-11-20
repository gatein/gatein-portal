/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.footer;

import java.io.IOException;
import java.util.Map;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.gatein.web.redirect.api.RedirectHandler;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class FooterPortlet extends GenericPortlet {

    protected RedirectHandler redirectHandler;

    public FooterPortlet() {
        redirectHandler = (RedirectHandler) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
                RedirectHandler.class);
    }

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        request.setAttribute("alternativeSites", getAlternativeSites());
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/footer.jsp");
        prd.include(request, response);
    }

    protected Map<String, String> getAlternativeSites() {
        PortalRequestContext prc = (PortalRequestContext) PortalRequestContext.getCurrentInstance();

        String siteName = ((PortalRequestContext) PortalRequestContext.getCurrentInstance()).getSiteName();

        return redirectHandler.getAlternativeRedirects(siteName, prc.getRequestURI(), true);
    }
}
