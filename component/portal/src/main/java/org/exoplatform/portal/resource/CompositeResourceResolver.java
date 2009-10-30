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

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class CompositeResourceResolver implements ResourceResolver
{

   /** . */
   private final Map<SkinKey, SkinConfig> skins;

   /** 
    * The name of the portal container 
    */
   private final String portalContainerName;

   public CompositeResourceResolver(String portalContainerName, Map<SkinKey, SkinConfig> skins)
   {
      this.portalContainerName = portalContainerName;
      this.skins = skins;
   }

   public Resource resolve(String path)
   {
      if (path.startsWith("/" + portalContainerName + "/resource/") && path.endsWith(".css"))
      {
         final StringBuffer sb = new StringBuffer();
         String encoded = path.substring(("/" + portalContainerName + "/resource/").length());
         String blah[] = encoded.split("/");
         int len = (blah.length >> 1) << 1;
         for (int i = 0; i < len; i += 2)
         {
            String name = Codec.decode(blah[i]);
            String module = Codec.decode(blah[i + 1]);
            SkinKey key = new SkinKey(module, name);
            SkinConfig skin = skins.get(key);
            if (skin != null)
            {
               sb.append("@import url(").append(skin.getCSSPath()).append(");").append("\n");
            }
         }
         return new Resource(path)
         {
            @Override
            public Reader read()
            {
               return new StringReader(sb.toString());
            }
         };
      }
      else
      {
         return null;
      }
   }
}
