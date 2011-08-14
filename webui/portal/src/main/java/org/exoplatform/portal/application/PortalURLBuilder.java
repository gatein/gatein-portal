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

package org.exoplatform.portal.application;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.url.ComponentURL;

import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Apr 3, 2007  
 */
public class PortalURLBuilder extends URLBuilder<UIComponent>
{

   /** . */
   private final ComponentURL url;

   public PortalURLBuilder(PortalRequestContext ctx, ComponentURL url)
   {
      String path = ctx.getNodePath();
      url.setPath(path);

      //
      this.url = url;
   }

   @Override
   public String createAjaxURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      return createURL(true, targetComponent, action, confirm, targetBeanId, params);
   }

   @Override
   public String createURL(UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      return createURL(false, targetComponent, action, confirm, targetBeanId, params);
   }

   private String createURL(boolean ajax, UIComponent targetComponent, String action, String confirm, String targetBeanId, Parameter[] params)
   {
      Map<String,String[]> queryParameters = url.getQueryParameters();
      if (queryParameters != null)
      {
         queryParameters.clear();
      }

      //
      url.setAjax(ajax);
      url.setConfirm(confirm);
      url.setResource(targetComponent);

      //
      url.setAction(action);
      url.setTargetBeanId(targetBeanId);

      //
      if (params != null)
      {
         for (Parameter param : params)
         {
            url.setQueryParameterValue(param.getName(), param.getValue());
         }
      }

      //
      if (removeLocale)
      {
         url.setLocale(null);
      }
      else if (locale != null)
      {
         url.setLocale(locale);
      }

      //
      return url.toString();
   }
}
