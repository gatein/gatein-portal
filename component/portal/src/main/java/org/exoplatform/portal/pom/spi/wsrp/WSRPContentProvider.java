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

import org.exoplatform.portal.pom.spi.ContentProviderHelper;
import org.exoplatform.portal.pom.spi.HelpableContentProvider;
import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.GetState;
import org.gatein.mop.spi.content.StateContainer;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPContentProvider implements ContentProvider<WSRPState>, HelpableContentProvider<byte[], WSRPState>
{
   public GetState<WSRPState> getState(String s)
   {
      throw new UnsupportedOperationException("todo");
   }

   public WSRPState combine(List<WSRPState> wsrpStates)
   {
      throw new UnsupportedOperationException("todo");
   }

   public void setState(StateContainer stateContainer, WSRPState wsrpState)
   {
      ContentProviderHelper.setState(stateContainer, wsrpState, this);
   }

   public WSRPState getState(StateContainer stateContainer)
   {
      return ContentProviderHelper.getState(stateContainer, this);
   }

   public Class<WSRPState> getStateType()
   {
      return WSRPState.class;
   }

   public String getNodeName()
   {
      return "mop:wsrpState";
   }

   public void setInternalState(byte[] bytes, WSRPState wsrpState)
   {
      wsrpState.setState(bytes);
   }

   public WSRPState getState(byte[] bytes)
   {
      WSRPState wsrpState = new WSRPState();
      wsrpState.setState(bytes);
      return wsrpState;
   }
}
