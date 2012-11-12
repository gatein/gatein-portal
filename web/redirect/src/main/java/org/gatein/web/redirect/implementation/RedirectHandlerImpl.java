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
package org.gatein.web.redirect.implementation;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gatein.web.redirect.RedirectRequestHandler;
import org.gatein.web.redirect.api.RedirectHandler;
import org.gatein.web.redirect.api.SiteRedirectService;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectHandlerImpl implements RedirectHandler, Startable
{
   SiteRedirectService siteRedirectService;
   
   public RedirectHandlerImpl(SiteRedirectService siteRedirectService)
   {
      this.siteRedirectService = siteRedirectService;
   }
   
   @Override
   public Map<String, String> getAlternativeRedirects(String siteName, String URI, boolean setPreference)
   {
      Map<String, String> redirectNames = siteRedirectService.getAlternativeSites(siteName);
      
      Map<String, String> redirects = new LinkedHashMap<String, String>(); //use LinkedHashMap to keep the List order intact
      for (String redirectName: redirectNames.keySet())
      {
         String redirectURI = createRedirectURI(redirectNames.get(redirectName), URI);
         redirects.put(redirectName, redirectURI);
      }
      
      return redirects;
   }
   
   protected String createRedirectURI(String redirectSite, String URI)
   {
      String redirectURI = URI;
      
      if (redirectURI.contains("&"))
      {
         redirectURI += "?";
      }
      else
      {
         if (redirectURI.endsWith("/"))
         {
            redirectURI = redirectURI.substring(0, redirectURI.length() - 1);
         }
         redirectURI += "?";
      }
      
      redirectURI += RedirectRequestHandler.REDIRECT_FLAG + "=" + redirectSite;
      
      return redirectURI;
   }
   
   @Override
   public void start()
   {
      // only needed because exo kernel requires this method (really its the underlying picocontianer that needs it)
   }

   @Override
   public void stop()
   {
      // only needed because exo kernel requires this method (really its the underlying picocontianer that needs it)
   }
}

