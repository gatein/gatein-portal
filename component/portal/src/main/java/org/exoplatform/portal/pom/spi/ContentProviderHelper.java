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

package org.exoplatform.portal.pom.spi;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.gatein.mop.core.api.workspace.content.AbstractCustomization;
import org.gatein.mop.spi.content.StateContainer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ContentProviderHelper
{
   /** . */
   private static final String STATE_NODE_NAME = "mop:state";

   public static <InternalState, State> void setState(StateContainer container, State state,
      HelpableContentProvider<InternalState, State> provider)
   {
      try
      {
         ChromatticSession session = ((AbstractCustomization)container).session;
         String containerId = session.getId(container);
         Node node = session.getJCRSession().getNodeByUUID(containerId);

         //
         InternalState internalState;
         if (node.hasNode(STATE_NODE_NAME))
         {
            Node stateNode = node.getNode(STATE_NODE_NAME);
            internalState = (InternalState)session.findById(Object.class, stateNode.getUUID());
            if (state == null)
            {
               session.remove(internalState);
               return;
            }
         }
         else
         {
            if (state == null)
            {
               return;
            }
            else
            {
               Node stateNode = node.addNode(STATE_NODE_NAME, provider.getNodeName());
               internalState = (InternalState)session.findById(Object.class, stateNode.getUUID());
            }
         }

         //
         provider.setInternalState(internalState, state);
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
   }

   public static <InternalState, State> State getState(StateContainer container,
      HelpableContentProvider<InternalState, State> provider)
   {
      try
      {
         ChromatticSession session = ((AbstractCustomization)container).session;
         String containerId = session.getId(container);
         Node node = session.getJCRSession().getNodeByUUID(containerId);

         //
         InternalState prefs;
         if (node.hasNode(STATE_NODE_NAME))
         {
            Node stateNode = node.getNode(STATE_NODE_NAME);
            prefs = (InternalState)session.findById(Object.class, stateNode.getUUID());
            return provider.getState(prefs);
         }
         else
         {
            return null;
         }
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
   }
}
