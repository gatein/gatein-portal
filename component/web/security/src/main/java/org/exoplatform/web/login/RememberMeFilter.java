/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.login;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.controller.router.PercentEncoding;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.text.FastURLEncoder;
import org.gatein.wci.security.Credentials;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * The remember me filter performs a send redirect on a portal private servlet mapping when the current request
 * is a GET request, the user is not authenticated and there is a remember me token cookie in the request.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RememberMeFilter extends AbstractFilter
{
   /** . */
   private static final FastURLEncoder CONVERTER = FastURLEncoder.getUTF8Instance();

   /** . */
   private static final Logger log = LoggerFactory.getLogger(RememberMeFilter.class);
   
   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      doFilter((HttpServletRequest)req, (HttpServletResponse)resp, chain);
   }

   private void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      if (req.getRemoteUser() == null && "GET".equals(req.getMethod()))
      {
         String token = InitiateLoginServlet.getRememberMeTokenCookie(req);
         if (token != null)
         {
            
            ExoContainer container = getContainer();
            Object o =
               ((CookieTokenService)container.getComponentInstanceOfType(CookieTokenService.class)).validateToken(
               token, false);
            if (o instanceof Credentials)
            {
               req.getSession().setAttribute(Credentials.CREDENTIALS, o);
               resp.sendRedirect(resp.encodeRedirectURL(
                     loginUrl(
                           req.getContextPath(),
                           privateUri(req)
                     )
               ));
               resp.flushBuffer();
            }
         }
      }

      //
      if (!resp.isCommitted())
      {
         chain.doFilter(req, resp);
      }
   }

   public void destroy()
   {
   }

   private String privateUri(HttpServletRequest req)
   {
      StringBuilder builder = new StringBuilder(req.getRequestURI());
      char sep = '?';
      for (Enumeration<String> e = req.getParameterNames();e.hasMoreElements();)
      {
         String parameterName = e.nextElement();
         for (String parameteValue : req.getParameterValues(parameterName))
         {
            builder.append(sep);
            sep = '&';
            builder.append(CONVERTER.encode(parameterName));
            builder.append('=');
            builder.append(CONVERTER.encode(parameteValue));
         }
      }
      return PercentEncoding.QUERY_PARAM.encode(builder);
   }

   private String loginUrl(String context, String initUrl)
   {
      return String.format(
            "%s/login?initialURI=%s",
            context, initUrl
      );
   }
}
