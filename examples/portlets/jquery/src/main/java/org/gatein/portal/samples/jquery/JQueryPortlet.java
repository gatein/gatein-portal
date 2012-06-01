/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.samples.jquery;

import java.io.IOException;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 5/31/12
 */
public class JQueryPortlet extends GenericPortlet
{
   @Override
   public void render(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      //Two lines of code below ensure that ON-LOAD resources jquery is available in the rendering phase
      //We need this as the JQueryPortlet resource does not declare dependency on jquery resource
      response.addProperty("org.gatein.javascript.dependency", "jquery");

      String view = request.getParameter("view");
      if(view == null || !view.endsWith(".jsp"))
      {
         view = "index.jsp";
      }
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/jsp/views/" + view);
      dispatcher.forward(request, response);
   }
}
