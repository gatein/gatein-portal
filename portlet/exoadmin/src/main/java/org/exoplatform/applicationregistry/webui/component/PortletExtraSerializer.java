/*
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

package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.commons.serialization.api.TypeConverter;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.federation.FederatingPortletInvoker;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletExtraSerializer extends TypeConverter<UIPortletManagement.PortletExtra, PortletContext>
{

   @Override
   public PortletContext write(UIPortletManagement.PortletExtra input)
   {
      return input.context;
   }

   @Override
   public UIPortletManagement.PortletExtra read(PortletContext output) throws Exception
   {
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      FederatingPortletInvoker portletInvoker = (FederatingPortletInvoker)manager.getComponentInstance(FederatingPortletInvoker.class);
      Portlet portlet = portletInvoker.getPortlet(output);
      return new UIPortletManagement.PortletExtra(portlet);
   }
}
