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

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIContainer;

/**
 * Jul 10, 2006
 */
public class UIContainerLifecycle extends Lifecycle<UIContainer>
{

   public void processRender(UIContainer uicomponent, WebuiRequestContext context) throws Exception
   {
      context.getWriter().append("<div class=\"").append(uicomponent.getId()).append("\" id=\"").append(
         uicomponent.getId()).append("\">");
      uicomponent.renderChildren(context);
      context.getWriter().append("</div>");
   }
}
