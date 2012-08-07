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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.security.ConversationState;

/**
 * Filter should be called to detect successful login of user into portal and call InvalidLoginAttemptsService.
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public class LoginDetectorFilter extends AbstractFilter
{   
   private static final String ATTR_LOGIN_DETECTED = "LoginDetectorFilter.loginDetected";

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      HttpServletRequest httpRequest = (HttpServletRequest)request;
      ConversationState state = ConversationState.getCurrent();

      if (state != null)
      {
         if (state.getAttribute(ATTR_LOGIN_DETECTED) == null)
         {
            String clientIPAddress = request.getRemoteAddr();
            String sessionID = httpRequest.getSession().getId();
            String username = httpRequest.getRemoteUser();
            state.setAttribute(ATTR_LOGIN_DETECTED, true);
            
            InvalidLoginAttemptsService invalidLoginService = (InvalidLoginAttemptsService)getContainer().getComponentInstanceOfType(InvalidLoginAttemptsService.class);
            invalidLoginService.successfulLoginAttempt(sessionID, username, clientIPAddress);            
         }
      }
      chain.doFilter(request, response);
   }

   @Override
   public void destroy()
   {
   }

}
