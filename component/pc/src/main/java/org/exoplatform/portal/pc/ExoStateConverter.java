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

package org.exoplatform.portal.pc;

import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.pc.portlet.state.StateConversionException;
import org.gatein.pc.portlet.state.StateConverter;
import org.gatein.pc.portlet.state.producer.PortletState;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoStateConverter implements StateConverter
{

   public <S extends Serializable> S marshall(PortletStateType<S> stateType, PortletState state)
      throws StateConversionException, IllegalArgumentException
   {
      if (stateType.getJavaType().equals(ExoPortletState.class))
      {
         ExoPortletState map = marshall(state);
         return (S)map;
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   public ExoPortletState marshall(PortletState state)
   {
      if (state == null)
      {
         throw new IllegalArgumentException("No null state");
      }
      ExoPortletState map = new ExoPortletState("local." + state.getPortletId());
      map.getState().putAll(state.getProperties());
      return map;
   }

   public <S extends Serializable> PortletState unmarshall(PortletStateType<S> stateType, S marshalledState)
      throws StateConversionException, IllegalArgumentException
   {
      if (stateType.getJavaType().equals(ExoPortletState.class))
      {
         ExoPortletState map = (ExoPortletState)marshalledState;
         return unmarshall(map);
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   public PortletState unmarshall(ExoPortletState marshalledState)
   {
      if (marshalledState == null)
      {
         throw new IllegalArgumentException("No null map");
      }
      PropertyMap properties = new SimplePropertyMap(marshalledState.getState());
      String portletID = marshalledState.getPortletId().substring("local.".length());
      return new PortletState(portletID, properties);
   }

}
