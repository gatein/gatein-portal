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

package org.exoplatform.commons.xml;

import org.exoplatform.commons.utils.IOUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ResourceEntityResolver implements EntityResolver
{

   /** . */
   private final Map<String, String> systemIdToResourcePath;

   /** . */
   private final ConcurrentMap<String, byte[]> systemIdToSource;

   /** . */
   private final ClassLoader loader;

   public ResourceEntityResolver(Class clazz, String systemId, String resourcePath)
   {
      this(clazz.getClassLoader(), Collections.singletonMap(systemId, resourcePath));
   }

   public ResourceEntityResolver(ClassLoader loader, String systemId, String resourcePath)
   {
      this(loader, Collections.singletonMap(systemId, resourcePath));
   }

   public ResourceEntityResolver(Class clazz, Map<String, String> systemIdToResourcePath)
   {
      this(clazz.getClassLoader(), systemIdToResourcePath);
   }

   public ResourceEntityResolver(ClassLoader loader, Map<String, String> systemIdToResourcePath)
   {
      if (loader == null)
      {
         throw new NullPointerException();
      }
      if (systemIdToResourcePath == null)
      {
         throw new NullPointerException();
      }

      // Defensive copy
      this.systemIdToResourcePath = new HashMap<String, String>(systemIdToResourcePath);
      this.systemIdToSource = new ConcurrentHashMap<String, byte[]>();
      this.loader = loader;
   }

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      if (systemId != null)
      {
         byte[] data = systemIdToSource.get(systemId);

         //
         if (data == null)
         {
            String path = systemIdToResourcePath.get(systemId);
            if (path != null)
            {
               InputStream in = loader.getResourceAsStream(path);
               if (in != null)
               {
                  data = IOUtil.getStreamContentAsBytes(in);
               }
            }

            // Black list it, we won't find it
            if (data == null)
            {
               data = new byte[0];
            }

            // Put in cache
            systemIdToSource.put(systemId, data);

            // Some basic prevention against stupid use of this class that could cause OOME
            if (systemIdToSource.size() > 1000)
            {
               systemIdToSource.clear();
            }
         }

         //
         if (data != null && data.length > 0)
         {
            return new InputSource(new ByteArrayInputStream(data));
         }
      }

      //
      return null;
   }
}
