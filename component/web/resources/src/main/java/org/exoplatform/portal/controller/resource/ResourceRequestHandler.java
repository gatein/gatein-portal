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

package org.exoplatform.portal.controller.resource;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.common.io.IOTools;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ResourceRequestHandler extends WebRequestHandler
{

   /** . */
   public static final QualifiedName RESOURCE = QualifiedName.create("gtn", "resource");

   /** . */
   public static final QualifiedName SCOPE = QualifiedName.create("gtn", "scope");

   /** . */
   public static final QualifiedName MODULE = QualifiedName.create("gtn", "module");

   @Override
   public String getHandlerName()
   {
      return "script";
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {

      JavascriptConfigService service = (JavascriptConfigService)PortalContainer.getComponent(JavascriptConfigService.class);

      String resourceParam = context.getParameter(RESOURCE);
      String scopeParam = context.getParameter(SCOPE);
      String moduleParam = context.getParameter(MODULE);

      //
      if (scopeParam != null && resourceParam != null)
      {
         try
         {
            ResourceScope scope = ResourceScope.valueOf(ResourceScope.class, scopeParam);
            ResourceId resource = new ResourceId(scope, resourceParam);
            
            //
            InputStream script;
            if (moduleParam != null)
            {
               script = service.getScript(resource, moduleParam);
            }
            else
            {
               script = service.getScript(resource);
            }

            //
            if (script != null)
            {
               HttpServletResponse response = context.getResponse();
               response.setContentType("application/x-javascript");
               OutputStream out = response.getOutputStream();
               IOTools.copy(script, out);
               return true;
            }
            else
            {
               // What should we do ?
            }
         }
         catch (IllegalArgumentException e)
         {
            // Not found
         }
      }
      
      //
      return false;
   }
   
   @Override
   protected boolean getRequiresLifeCycle()
   {
      return false;
   }
}
