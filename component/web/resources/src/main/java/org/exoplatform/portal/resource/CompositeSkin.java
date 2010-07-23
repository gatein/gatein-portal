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

package org.exoplatform.portal.resource;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.resources.Orientation;

import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

/**
 * A composite skin.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CompositeSkin implements Skin
{

   /** . */
   private final SkinService service;

   /** . */
   private final String id;

   /** . */
   private final String urlPrefix;

   CompositeSkin(SkinService service, Collection<SkinConfig> skins)
   {
      TreeMap<String, SkinConfig> urlSkins = new TreeMap<String, SkinConfig>();
      for (SkinConfig skin : skins)
      {
         urlSkins.put(skin.getCSSPath(), skin);
      }

      //
      final StringBuilder builder = new StringBuilder();
      builder.append("/").append(service.portalContainerName).append("/resource");

      //
      final StringBuilder id = new StringBuilder();

      //
      try
      {
         for (SkinConfig cfg : urlSkins.values())
         {
            StringBuilder encodedName = new StringBuilder();
            Codec.encode(encodedName, cfg.getName());
            StringBuilder encodedModule = new StringBuilder();
            Codec.encode(encodedModule, cfg.getModule());

            //
            id.append(encodedName).append(encodedModule);
            builder.append("/").append(encodedName).append("/").append(encodedModule);
         }
      }
      catch (IOException e)
      {
         throw new Error(e);
      }

      //
      this.service = service;
      this.id = id.toString();
      this.urlPrefix = builder.toString();
   }

   public String getId()
   {
      return id;
   }

   public SkinURL createURL()
   {
      return new SkinURL()
      {

         Orientation orientation;

         public void setOrientation(Orientation orientation)
         {
            this.orientation = orientation;
         }

         @Override
         public String toString()
         {
            return urlPrefix + "/" + (PropertyManager.isDevelopping() ? "style" : service.id)
               + service.getSuffix(orientation);
         }
      };
   }
}
