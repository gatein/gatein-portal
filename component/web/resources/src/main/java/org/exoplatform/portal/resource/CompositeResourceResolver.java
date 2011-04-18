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

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.Reader;
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

   /** . */
   private final String prefix;

   /** . */
   private final Logger log = LoggerFactory.getLogger(CompositeResourceResolver.class);

   public CompositeResourceResolver(String portalContainerName, Map<SkinKey, SkinConfig> skins)
   {
      this.portalContainerName = portalContainerName;
      this.skins = skins;
      this.prefix = "/" + portalContainerName + "/resource/";
   }

   public Resource resolve(String path)
   {
      if (path == null)
      {
         throw new NullPointerException("No null path is accepted");
      }

      //
      if (path.startsWith(prefix) && path.endsWith(".css"))
      {
         final StringBuilder sb = new StringBuilder();
         String encoded = path.substring(prefix.length());
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
               return new CharSequenceReader(sb);
            }
         };
      }
      else
      {
         if (log.isDebugEnabled())
         {
            log.debug("Could not resolve path value");
         }
         return null;
      }
   }
}
