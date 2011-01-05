/**
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

package org.exoplatform.portal.webui.workspace;

import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.commons.utils.PortalPrinter;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.application.UIStandaloneAppContainer;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;

/**
 * Created by The eXo Platform SAS
 * May 8, 2006
 */
public class UIStandaloneApplicationLifecycle extends Lifecycle<UIStandaloneApplication>
{

   public void processDecode(UIStandaloneApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId == null)
      {
         return;
      }
      UIComponent uiTarget = uicomponent.findComponentById(componentId);
      if (uiTarget == null)
      {
         context.addUIComponentToUpdateByAjax(uicomponent.getChild(UIStandaloneAppContainer.class));
         return;
      }
      if (uiTarget == uicomponent)
      {
         super.processDecode(uicomponent, context);
      }
      uiTarget.processDecode(context);
   }

   public void processAction(UIStandaloneApplication uicomponent, WebuiRequestContext context) throws Exception
   {
      String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
      if (componentId == null)
      {
         return;
      }
      UIComponent uiTarget = uicomponent.findComponentById(componentId);
      if (uiTarget == null)
      {
         return;
      }
      if (uiTarget == uicomponent)
      {
         super.processAction(uicomponent, context);
      }
      uiTarget.processAction(context);
   }

   public void processRender(UIStandaloneApplication uicomponent,
                             WebuiRequestContext context) throws Exception
   {

      PortalRequestContext prc = (PortalRequestContext)context;
      OutputStream responseOutputStream = prc.getResponse().getOutputStream();

      PortalPrinter parentWriter = new PortalPrinter(responseOutputStream, true, 5000);
      PortalPrinter childWriter = new PortalPrinter(responseOutputStream, true, 25000, true);

      context.setWriter(childWriter);
      processRender(uicomponent, context, "system:/groovy/portal/webui/workspace/UIStandaloneApplicationChildren.gtmpl");

      context.setWriter(parentWriter);
      processRender(uicomponent, context, "system:/groovy/portal/webui/workspace/UIStandaloneApplication.gtmpl");

      try
      {
         //flush the parent writer to the output stream so that we are really to accept the child content
         parentWriter.flushOutputStream();
         //now that the parent has been flushed, we can flush the contents of the child to the output
         childWriter.flushOutputStream();
      }
      catch (IOException ioe)
      {
         //We want to ignore the ClientAbortException since this is caused by the users
         //browser closing the connection and is not something we should be logging.
         if (!ioe.getClass().toString().contains("ClientAbortException"))
         {
            throw ioe;
         }

      }
   }

   public void processRender(UIStandaloneApplication uicomponent, WebuiRequestContext context, String template) throws Exception
   {
      // Fail if we have no template
      if (template == null)
      {
         throw new IllegalStateException("uicomponent " + uicomponent + " with class " + uicomponent.getClass().getName() +
            " has no template for rendering");
      }

      //
      ResourceResolver resolver = uicomponent.getTemplateResourceResolver(context, template);
      WebuiBindingContext bcontext = new WebuiBindingContext(resolver, context.getWriter(), uicomponent, context);
      bcontext.put(UIComponent.UICOMPONENT, uicomponent);
      bcontext.put(uicomponent.getUIComponentName(), uicomponent);
      renderTemplate(template, bcontext);
   }

}
