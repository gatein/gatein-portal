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

package org.exoplatform.portal.pom.spi.gadget;

import org.exoplatform.portal.pom.spi.ContentProviderHelper;
import org.exoplatform.portal.pom.spi.HelpableContentProvider;
import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.GetState;
import org.gatein.mop.spi.content.StateContainer;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GadgetContentProvider implements ContentProvider<Gadget>, HelpableContentProvider<GadgetState, Gadget>
{

   public GetState<Gadget> getState(String contentId)
   {
      throw new UnsupportedOperationException();
   }

   public Gadget combine(List<Gadget> states)
   {
      throw new UnsupportedOperationException();
   }

   public void setState(StateContainer container, Gadget state)
   {
      ContentProviderHelper.setState(container, state, this);
   }

   public Gadget getState(StateContainer container)
   {
      return ContentProviderHelper.getState(container, this);
   }

   public Class<Gadget> getStateType()
   {
      return Gadget.class;
   }

   public String getNodeName()
   {
      return GadgetState.MOP_NODE_NAME;
   }

   public void setInternalState(GadgetState gadgetState, Gadget gadget)
   {
      gadgetState.setUserPrefs(gadget.getUserPref());
   }

   public Gadget getState(GadgetState gadgetState)
   {
      Gadget gadget = new Gadget();
      gadget.setUserPref(gadgetState.getUserPrefs());
      return gadget;
   }
}
