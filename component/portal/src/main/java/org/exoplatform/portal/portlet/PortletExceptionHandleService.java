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

package org.exoplatform.portal.portlet;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.portletcontainer.PortletContainerException;

import java.util.ArrayList;
import java.util.List;

public class PortletExceptionHandleService
{

   private List<PortletExceptionListener> listeners;

   public void initListener(ComponentPlugin listener) throws Exception
   {
      if (listener instanceof PortletExceptionListener)
      {
         if (listeners == null)
            listeners = new ArrayList<PortletExceptionListener>();
         listeners.add((PortletExceptionListener)listener);
      }

   }

   public void handle(PortletContainerException ex)
   {
      for (PortletExceptionListener listener : listeners)
      {
         listener.handle(ex);
      }
   }
}
