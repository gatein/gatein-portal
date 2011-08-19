/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.application;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 */
public class StaticResourceRequestHandler extends WebRequestHandler
{
   @Override
   public String getHandlerName()
   {
      return "staticResource";
   }

   @Override
   public void execute(ControllerContext context) throws Exception
   {
      PortalContainer portalContainer = PortalContainer.getInstance();
      ServletContext mergedContext = portalContainer.getPortalContext();
      
      HttpServletRequest req = context.getRequest();
      HttpServletResponse res = context.getResponse();
      mergedContext.getNamedDispatcher("default").forward(req, res);
   }
}
