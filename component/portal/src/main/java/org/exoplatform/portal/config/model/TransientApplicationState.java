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

package org.exoplatform.portal.config.model;

import java.io.Serializable;

/**
 * The transient state of an application when it has not yet been stored in the database.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TransientApplicationState<S> extends ApplicationState<S>
{

   /** The owner type. */
   private String contentId;

   /** The owner type. */
   private String ownerType;

   /** The owner id. */
   private String ownerId;

   /** The unique id. */
   private String uniqueId;

   /** The content state. */
   private S contentState;

   public TransientApplicationState(String contentId)
   {
      this.contentId = contentId;
   }

   public TransientApplicationState(String contentId, S contentState)
   {
      this.contentId = contentId;
      this.contentState = contentState;
   }

   public TransientApplicationState(String contentId, S contentState, String ownerType, String ownerId, String uniqueId)
   {
      this.contentId = contentId;
      this.contentState = contentState;
      this.ownerType = ownerType;
      this.ownerId = ownerId;
      this.uniqueId = uniqueId;
   }

   public TransientApplicationState()
   {
      this.contentState = null;
      this.uniqueId = null;
   }

   public String getContentId()
   {
      return contentId;
   }

   public S getContentState()
   {
      return contentState;
   }

   public void setContentState(S contentState)
   {
      this.contentState = contentState;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public String getOwnerId()
   {
      return ownerId;
   }

   public void setOwnerId(String ownerId)
   {
      this.ownerId = ownerId;
   }

   public String getUniqueId()
   {
      return uniqueId;
   }
}
