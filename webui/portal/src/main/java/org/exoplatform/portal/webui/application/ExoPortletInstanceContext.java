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

package org.exoplatform.portal.webui.application;

import org.exoplatform.portal.pc.ExoPortletStateType;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.StateEvent;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoPortletInstanceContext implements InstanceContext
{

   /** . */
   private final String id;

   /** . */
   private final AccessMode accessMode;

   /** . */
   private PortletContext clonedContext;

   /** . */
   private PortletContext modifiedContext;

   public ExoPortletInstanceContext(String id)
   {
      this(id, AccessMode.READ_WRITE);
   }

   public ExoPortletInstanceContext(String id, AccessMode accessMode)
   {
      if (id == null)
      {
         throw new IllegalArgumentException();
      }
      if (accessMode == null)
      {
         throw new IllegalArgumentException();
      }

      //
      this.id = id;
      this.accessMode = accessMode;
   }

   public String getId()
   {
      return id;
   }

   public AccessMode getAccessMode()
   {
      return accessMode;
   }

   public void onStateEvent(StateEvent event)
   {
      switch (event.getType())
      {
         case PORTLET_CLONED_EVENT :
            clonedContext = event.getPortletContext();
            break;
         case PORTLET_MODIFIED_EVENT :
            modifiedContext = event.getPortletContext();
      }
   }

   public PortletContext getClonedContext()
   {
      return clonedContext;
   }

   public PortletContext getModifiedContext()
   {
      return modifiedContext;
   }

   public ExoPortletStateType getStateType()
   {
      return ExoPortletStateType.getInstance();
   }
}
