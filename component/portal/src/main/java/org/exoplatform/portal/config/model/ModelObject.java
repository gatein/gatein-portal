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

import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ModelObject
{

   /** Storage id. */
   String storageId;

   /** The storage name that is unique among a container context. */
   String storageName;

   /**
    * Create a new object.
    *
    * @param storageId if the storage id is null
    */
   public ModelObject(String storageId)
   {
      this.storageId = storageId;
   }

   protected ModelObject()
   {
      this.storageId = null;
   }

   public String getStorageId()
   {
      return storageId;
   }

   public String getStorageName()
   {
      return storageName;
   }

   public void setStorageName(String storageName)
   {
      this.storageName = storageName;
   }

   public abstract ModelData build();

   public static ModelObject build(ModelData data)
   {
      if (data instanceof ContainerData)
      {
         return new Container((ContainerData)data);
      }
      else if (data instanceof PageData)
      {
         return new Page((PageData)data);
      }
      else if (data instanceof BodyData)
      {
         BodyData bodyData = (BodyData)data;
         switch (bodyData.getType())
         {
            case PAGE:
               return new PageBody(data.getStorageId());
            case SITE:
               return new SiteBody(data.getStorageId());
            default:
               throw new AssertionError();
         }
      }
      else if (data instanceof ApplicationData)
      {
         ApplicationData applicationData = (ApplicationData)data;
         ApplicationType type = applicationData.getType();
         if (ApplicationType.PORTLET == type)
         {
            return Application.createPortletApplication((ApplicationData<Portlet>)applicationData);
         }
         else if (ApplicationType.GADGET == type)
         {
            return Application.createGadgetApplication((ApplicationData<Gadget>)applicationData);
         }
         else if (ApplicationType.WSRP_PORTLET == type)
         {
            return Application.createWSRPApplication((ApplicationData<WSRP>)applicationData);
         }
         else
         {
            throw new AssertionError();
         }
      }
      else
      {
         throw new UnsupportedOperationException("todo " + data);
      }
   }
}
