/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.portlet.responsive.header;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.PortalURLBuilder;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.security.sso.SSOHelper;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class HeaderBean
{
   private final SSOHelper ssoHelper;
   
   public HeaderBean()
   {
      ssoHelper = (SSOHelper)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SSOHelper.class);
   }

   public String generateLoginLink(String defaultAction)
   {  
      if (ssoHelper != null)
      {
         PortalRequestContext pContext = Util.getPortalRequestContext();
         String ssoRedirectURL = pContext.getRequest().getContextPath() + ssoHelper.getSSORedirectURLSuffix();
         return ssoRedirectURL;
      }
      else
      {
         return defaultAction;
      }
   }
   
   public String generateRegisterLink()
   {
      PortalRequestContext pContext = Util.getPortalRequestContext();
      NavigationResource resource = new NavigationResource(SiteType.PORTAL, pContext.getPortalOwner(), "register");
      return resource.getNodeURI();
   }
   
   public String generateHomePageLink() throws Exception
   {
      PortalRequestContext pContext = Util.getPortalRequestContext();
      NodeURL nodeURL = pContext.createURL(NodeURL.TYPE).setResource(new NavigationResource(SiteType.PORTAL, pContext.getPortalOwner(), null));
      return nodeURL.toString();
   }
   
   public String generateDashboardLink() throws Exception
   {
      PortalRequestContext pContext = Util.getPortalRequestContext();
      NodeURL nodeURL = pContext.createURL(NodeURL.TYPE);
      nodeURL.setResource(new NavigationResource(SiteType.USER, pContext.getRemoteUser(), null));
      return nodeURL.toString();
   }
   
   public String generateGroupPagesLink() 
   {
      return "#";
   }
}

