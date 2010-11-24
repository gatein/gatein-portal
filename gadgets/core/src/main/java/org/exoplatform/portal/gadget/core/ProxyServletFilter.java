/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.gadget.core;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.security.proxy.ProxyFilterService;

import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * The proxy servlet filter is a servlet filter placed in front of Shindig proxy servlet.
 * It filters a request and allows only the request satisfying the following:
 * <ul>
 * <li>the request has an URL parameter</li>
 * <li>the request can locate a container associated with the request</li>
 * <li>the request can locate the {@link ProxyFilterService} within the container</li>
 * <li>the method {@link ProxyFilterService#accept(HttpServletRequest, PortalContainer, URI)} invocation returns true</li>
 * </ul>
 *
 * This service is located in front and does not use Shindig integration point (such as org.apache.shindig.gadgets.GadgetBlacklist}
 * as the execution is performed by a thread that is not associated with the portal request precluding any access to
 * information enabling contextual filtering.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ProxyServletFilter implements Filter
{

   /** . */
   private ServletContext ctx;

   /** . */
   private static final Logger logger = LoggerFactory.getLogger(ProxyServletFilter.class);

   public void init(FilterConfig cfg) throws ServletException
   {
      this.ctx = cfg.getServletContext();
   }

   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      HttpServletRequest hreq = (HttpServletRequest)req;
      HttpServletResponse hresp = (HttpServletResponse)resp;

      // Get URL
      String url = hreq.getParameter("url");
      if (url == null)
      {
         hresp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No URL");
      }
      else
      {
         // Get container
         PortalContainer container = PortalContainer.getInstance(ctx);
         if (container == null)
         {
            hresp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not access container for servlet context " + ctx.getContextPath());
         }
         else
         {
            ProxyFilterService service = (ProxyFilterService)container.getComponentInstanceOfType(ProxyFilterService.class);
            if (service == null)
            {
               hresp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not access proxy filter service " + ctx.getContextPath());
            }
            else
            {
               try
               {
                  URI uri = URI.create(url);
                  if (!service.accept(hreq, container, uri))
                  {
                     hresp.sendError(HttpServletResponse.SC_FORBIDDEN, "Gadget " + url + " is blacklisted");
                  }
                  else
                  {
                     chain.doFilter(req, resp);
                  }

               }
               catch (java.lang.IllegalArgumentException e)
               {
                  // It happens that some URLs can be wrong, I've seen this with "http://" as URL in one of the Google Gadgets
                  logger.debug("Invalid URL: " + url);
               }
            }
         }
      }
   }

   public void destroy()
   {
   }
}
