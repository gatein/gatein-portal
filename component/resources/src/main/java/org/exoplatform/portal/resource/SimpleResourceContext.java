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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class SimpleResourceContext
{

   private final String contextPath;

   private final ServletContext context;

   public SimpleResourceContext(String contextPath, ServletContext context)
   {
      this.contextPath = contextPath;
      this.context = context;
   }

   public Resource getResource(String path)
   {
      int i2 = path.lastIndexOf("/") + 1;
      String targetedParentPath = path.substring(0, i2);
      String targetedFileName = path.substring(i2);
      try
      {
         final URL url = context.getResource(path);
         if (url != null)
         {
            return new Resource(contextPath, targetedParentPath, targetedFileName)
            {
               @Override
               public Reader read() throws IOException
               {
                  return new InputStreamReader(url.openStream());
               }
            };
         }
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   public String getContextPath()
   {
      return contextPath;
   }
}
