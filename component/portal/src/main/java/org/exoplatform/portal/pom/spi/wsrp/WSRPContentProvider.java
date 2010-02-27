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

package org.exoplatform.portal.pom.spi.wsrp;

import org.exoplatform.commons.utils.Safe;
import org.gatein.common.util.ParameterValidation;
import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.StateContainer;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPContentProvider implements ContentProvider<WSRP, WSRPState>
{

   public WSRP combine(List<WSRP> wsrpStates)
   {
      WSRP result = null;
      if(ParameterValidation.existsAndIsNotEmpty(wsrpStates))
      {
         for (WSRP state : wsrpStates)
         {
            result = state;
            if(state.isCloned())
            {
               return state;
            }
         }
      }
      return result;
   }

   public void setState(StateContainer<WSRPState> container, WSRP state)
   {
      WSRPState wsrpState = container.getState();
      if (wsrpState != null)
      {
         if (state == null)
         {
            container.setState(null);
         }
         else
         {
            setInternalState(wsrpState, state);
         }
      }
      else
      {
         if (state != null)
         {
            wsrpState = container.create();
            setInternalState(wsrpState, state);
         }
      }
   }

   public WSRP getState(StateContainer<WSRPState> container)
   {
      WSRPState wsrpState  = container.getState();
      if (wsrpState != null)
      {
         return getState(wsrpState);
      }
      else
      {
         return null;
      }
   }

   public Class<WSRP> getExternalType()
   {
      return WSRP.class;
   }

   public Class<WSRPState> getInternalType()
   {
      return WSRPState.class;
   }

   private void setInternalState(WSRPState persistedState, WSRP updatedState)
   {
      byte[] bytes = updatedState.getState();
      if (bytes != null && bytes.length > 0)
      {
         ByteArrayInputStream is = new ByteArrayInputStream(bytes);
         persistedState.setState(is);
      }
      persistedState.setPortletId(updatedState.getPortletId());
      persistedState.setCloned(updatedState.isCloned());
   }

   private WSRP getState(WSRPState state)
   {
      WSRP wsrp = new WSRP();
      byte[] bytes = Safe.getBytes(state.getState());
      wsrp.setState(bytes);
      wsrp.setPortletId(state.getPortletId());
      wsrp.setCloned(state.getCloned());
      return wsrp;
   }
}
