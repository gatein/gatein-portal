/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.gatein.wci.security.Credentials;
import org.jboss.web.tomcat.security.login.WebAuthentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class ClusteredSSOFilter extends AbstractFilter
{

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {
      if (ExoContainer.getProfiles().contains("cluster"))
      {
         HttpServletRequest httpRequest = (HttpServletRequest)request;

         Credentials credentials  = (Credentials)httpRequest.getSession().getAttribute(Credentials.CREDENTIALS);
         
         // Make programatic login if authenticated credentials are present in session - they were set in another cluster node
         if (credentials != null && httpRequest.getRemoteUser() == null)
         {
            WebAuthentication pwl = new WebAuthentication();
            pwl.login(credentials.getUsername(), credentials.getPassword());

         }

         chain.doFilter(request, response);

         // TODO:
         // This is a workaround... without this code this attr will vanish from session after first request - don't ask...
         if (credentials != null && httpRequest.getSession(false) != null)
         {
            httpRequest.getSession(false).setAttribute(Credentials.CREDENTIALS, credentials);
         }
      }
      else
      {
         chain.doFilter(request, response);
      }
   }

   public void destroy()
   {
   }
}