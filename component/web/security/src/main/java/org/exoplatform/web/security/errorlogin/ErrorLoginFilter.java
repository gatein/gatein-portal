/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
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
package org.exoplatform.web.security.errorlogin;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.web.AbstractFilter;

/**
 * Filter should be called to detect invalid login attempt to portal.
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public class ErrorLoginFilter extends AbstractFilter
{
   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      
      // Get informations about user
      String clientIPAddress = request.getRemoteAddr();
      String sessionID = httpRequest.getSession().getId();
      String username = httpRequest.getParameter("j_username");
      
      // Call InvalidLoginService, which can perform some actions (aka send mail to portal administrator)
      InvalidLoginAttemptsService invalidLoginService = (InvalidLoginAttemptsService)getContainer().getComponentInstanceOfType(InvalidLoginAttemptsService.class);
      invalidLoginService.badLoginAttempt(sessionID, username, clientIPAddress);
      
      // Continue with request
      chain.doFilter(request, response);
   }
   
   @Override
   public void destroy()
   {            
   }   
   
}

