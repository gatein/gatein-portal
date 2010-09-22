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

package org.exoplatform.webui.core.lifecycle;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.groovyscript.text.TemplateService;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SAS May 7, 2006
 */
public class Lifecycle<E extends UIComponent>
{

   protected static Log log = ExoLogger.getLogger("portal:Lifecycle");

   private Decorator decorator_ = new Decorator();

   // public void init(UIComponent uicomponent, WebuiRequestContext context)
   // throws Exception {}

   public void processDecode(E uicomponent, WebuiRequestContext context) throws Exception
   {
   }

   public void processAction(E uicomponent, WebuiRequestContext context) throws Exception
   {
      String action = context.getRequestParameter(context.getActionParameterName());
      if (action == null)
         return;
      Event<UIComponent> event = uicomponent.createEvent(action, Event.Phase.PROCESS, context);
      if (event != null)
         event.broadcast();
   }

   /**
    * That method is the most generic one for every UIComponent that is bound to
    * this Lifecycle object and the class that extends it without overriding the
    * method.
    * 
    * The template associated to the specified UIComponent is rendered using renderTemplate(). A WebuiBindingContext
    * context object provides the template with all the necessary objects to render. 
    * (WebuiBindingContext extends the Map class)
    * 
    */
   public void processRender(E uicomponent, WebuiRequestContext context) throws Exception
   {
      String template = uicomponent.getTemplate();

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

   // public void destroy(UIComponent uicomponent) throws Exception {}

   /**
    * The method allows to use Groovy templates to render the portal components.
    *
    * <ol>
    * <li>Add a decorator object into the context</li>
    * <li>Get a reference to the TemplateService</li>
    * <li>If the system property "exo.product.developing" is set to <code>true</code>, the templates are not cached</li>
    * <li>If the writer used to render the output is of type HtmlValidator, which is the case in the Portal
    * environment, then it is also possible to validate the generated HTML (for debugging purposes)</li>
    * <li>The template and the context are then merged to generate the HTML fragment</li>
    * </ol>
    * 
    */
   protected void renderTemplate(String template, WebuiBindingContext bcontext) throws Exception
   {      
      WebuiRequestContext context = bcontext.getRequestContext();
      bcontext.put("decorator", decorator_);
      bcontext.put("locale", context.getLocale());
      ExoContainer pcontainer = context.getApplication().getApplicationServiceContainer();
      TemplateService service = (TemplateService)pcontainer.getComponentInstanceOfType(TemplateService.class);
      ResourceResolver resolver = bcontext.getResourceResolver();

      if (PropertyManager.isDevelopping())
      {
         WebuiRequestContext rootContext = (WebuiRequestContext)context.getParentAppRequestContext();
         if (rootContext == null)
            rootContext = context;
         long lastAccess = rootContext.getUIApplication().getLastAccessApplication();
         if (resolver.isModified(template, lastAccess))
         {
            if (log.isDebugEnabled())
               log.debug("Invalidate the template: " + template);
            service.invalidateTemplate(template, resolver);
         }
      }

      try
      {
         service.merge(template, bcontext);
      }
      catch (NullPointerException e)
      {
         log.error("Template: " + template + " not found.");
      }
   }
}