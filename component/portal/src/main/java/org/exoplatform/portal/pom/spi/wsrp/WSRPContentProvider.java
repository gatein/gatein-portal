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
import org.gatein.common.io.IOTools;
import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.GetState;
import org.gatein.mop.spi.content.StateContainer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPContentProvider implements ContentProvider<WSRP>, HelpableContentProvider<WSRPState, WSRP>
{

   public GetState<WSRP> getState(String s)
   {
      throw new UnsupportedOperationException("todo");
   }

   public WSRP combine(List<WSRP> wsrpStates)
   {
      throw new UnsupportedOperationException("todo");
   }

   public void setState(StateContainer stateContainer, WSRP wsrpState)
   {
      ContentProviderHelper.setState(stateContainer, wsrpState, this);
   }

   public WSRP getState(StateContainer stateContainer)
   {
      return ContentProviderHelper.getState(stateContainer, this);
   }

   public Class<WSRP> getStateType()
   {
      return WSRP.class;
   }

   public String getNodeName()
   {
      return WSRPState.MOP_NODE_NAME;
   }

   public void setInternalState(WSRPState persistedState, WSRP updatedState)
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

   private byte[] getBytes(InputStream is)
   {
      byte[] bytes;

      if (is == null)
      {
         return null;
      }

      try
      {
         bytes = IOTools.getBytes(is);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Couldn't get bytes from WSRPState", e); // todo: log instead?
      }
      IOTools.safeClose(is);
      return bytes;
   }

   public WSRP getState(WSRPState state)
   {
      WSRP wsrp = new WSRP();
      byte[] bytes = getBytes(state.getState());
      wsrp.setState(bytes);
      wsrp.setPortletId(state.getPortletId());
      wsrp.setCloned(state.getCloned());
      return wsrp;
   }
}
