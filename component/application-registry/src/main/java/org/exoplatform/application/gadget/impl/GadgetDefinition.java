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
package org.exoplatform.application.gadget.impl;

import org.chromattic.api.annotations.*;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.chromattic.ext.ntdef.NTFolder;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "app:gadgetdefinition")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("app")
public abstract class GadgetDefinition
{

   @Name
   public abstract String getName();

   @Property(name = "gtn:name")
   public abstract String getTitle();

   public abstract void setTitle(String title);

   @Property(name = "gtn:description")
   public abstract String getDescription();

   public abstract void setDescription(String description);

   @Property(name = "app:thumbnail")
   public abstract String getThumbnail();

   public abstract void setThumbnail(String thumbnail);

   @Property(name = "app:referenceurl")
   public abstract String getReferenceURL();

   public abstract void setReferenceURL(String referenceURL);

   @OneToOne
   @Owner
   @MappedBy("app:data")
   public abstract GadgetData getData();

   public abstract void setData(GadgetData data);

   @Create
   protected abstract LocalGadgetData createLocalData();

   @Create
   protected abstract RemoteGadgetData createRemoteData();

   @Create
   protected abstract NTFolder createFolder();

   public boolean isLocal()
   {
      GadgetData data = getData();
      return data instanceof LocalGadgetData;
   }

   public void setLocal(boolean local)
   {
      GadgetData data = getData();
      if (local)
      {
         if (data == null || data instanceof RemoteGadgetData)
         {
            LocalGadgetData localData = createLocalData();
            setData(localData);
            NTFolder resources = createFolder();
            localData.setResources(resources);
         }
      }
      else
      {
         if (data == null || data instanceof LocalGadgetData)
         {
            RemoteGadgetData localData = createRemoteData();
            setData(localData);
         }
      }
   }

}
