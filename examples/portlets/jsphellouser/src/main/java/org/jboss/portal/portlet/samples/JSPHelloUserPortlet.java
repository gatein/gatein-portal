/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.jboss.portal.portlet.samples;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

public class JSPHelloUserPortlet extends GenericPortlet
{
   
   public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      String sYourName = (String) request.getParameter("yourname");
      if (sYourName != null)
      {
         PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/hello.jsp");
         prd.include(request, response);
      }
      else
      {
         PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/welcome.jsp");
         prd.include(request, response);
      }
   }

   protected void doHelp(RenderRequest rRequest, RenderResponse rResponse) throws PortletException, IOException,
         UnavailableException
   {
      rResponse.setContentType("text/html");
      PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/help.jsp");
      prd.include(rRequest, rResponse);
   }

   protected void doEdit(RenderRequest rRequest, RenderResponse rResponse) throws PortletException, IOException,
         UnavailableException
   {
      rResponse.setContentType("text/html");
      PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/edit.jsp");
      prd.include(rRequest, rResponse);
   }

   public void processAction(ActionRequest aRequest, ActionResponse aResponse) throws PortletException, IOException,
         UnavailableException
   {
      String sYourname = (String) aRequest.getParameter("yourname");
      aResponse.setRenderParameter("yourname", sYourname);
   }


}
