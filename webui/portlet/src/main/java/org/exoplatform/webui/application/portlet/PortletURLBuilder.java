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

package org.exoplatform.webui.application.portlet;

import org.exoplatform.web.application.Parameter;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;

/**
 * julien todo : use PortletURL parameter instead of appending them to the url returned by the PortletURL
 *
 * Created by The eXo Platform SAS
 * Apr 3, 2007  
 */
public class PortletURLBuilder extends URLBuilder<UIComponent>
{
    private static final String AMP = "&amp;";
    private static final char EQUALS = '=';

    public PortletURLBuilder()
   {
      super(null);
   }

   public PortletURLBuilder(String baseURL)
   {
      super(baseURL);
   }

   public String createURL(String action, Parameter[] params)
   {
      return null;
   }

   public String createURL(String action, String objectId, Parameter[] params)
   {
      return null;
   }

   protected void createURL(StringBuilder builder, UIComponent targetComponent, String action, String targetBeanId,
      Parameter[] params)
   {
      String baseUrl = getBaseURL();
      builder.append(baseUrl).append(AMP).append(UIComponent.UICOMPONENT).append(EQUALS).append(
         targetComponent.getId());

      if (action != null && action.trim().length() > 0)
      {
         builder.append(AMP).append(WebuiRequestContext.ACTION).append(EQUALS).append(action);
      }

      if (targetBeanId != null && targetBeanId.trim().length() > 0)
      {
         builder.append(AMP).append(UIComponent.OBJECTID).append(EQUALS).append(targetBeanId);
      }

      if (params == null || params.length < 1)
         return;
      for (Parameter param : params)
      {
         builder.append(AMP).append(param.getName()).append(EQUALS).append(param.getValue());
      }

   }

}
