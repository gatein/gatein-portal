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
package org.gatein.portlet.redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.gatein.web.redirect.api.RedirectHandler;

/**
 * @author <a href="mailto:theute@jboss.org">Thomas Heute</a>
 * @version $Revision$
 */
public class RedirectBean
{
   protected RedirectHandler redirectHandler;
   
   public RedirectBean()
   {
      redirectHandler = (RedirectHandler)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RedirectHandler.class);
   }
   
   public List<RedirectLink> getAlternativeSites()
   {
      PortalRequestContext prc = (PortalRequestContext)PortalRequestContext.getCurrentInstance();
      
      String siteName = ((PortalRequestContext)PortalRequestContext.getCurrentInstance()).getSiteName();
      String portalName = PortalContainer.getCurrentPortalContainerName();
      
      Map<String, String> redirects = redirectHandler.getAlternativeRedirects(siteName, prc.getRequestURI(), true);
      
      List<RedirectLink> redirectLinks = new ArrayList<RedirectLink>();
      if (redirects != null)
      {
         for (String siteNames : redirects.keySet())
         {
            RedirectLink redirectLink = new RedirectLink(siteNames, redirects.get(siteNames));
            redirectLinks.add(redirectLink);
         }
      }
  
      return redirectLinks;
   }
}

