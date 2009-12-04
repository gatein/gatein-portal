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
package org.exoplatform.application.gadget;

import org.exoplatform.application.gadget.impl.GadgetRegistry;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletLocalImporter extends LocalImporter
{

   /** . */
   private final Logger log = LoggerFactory.getLogger(ServletLocalImporter.class);

   /** . */
   private final ServletContext servletContext;

   public ServletLocalImporter(
      String name,
      GadgetRegistry registry,
      String gadgetPath,
      ServletContext servletContext,
      boolean local)
   {
      super(name, registry, gadgetPath, local);

      //
      this.servletContext = servletContext;
   }

   @Override
   public String getName(String resourcePath) throws IOException
   {
      // It's a directory, remove the trailing '/'
      if (resourcePath.endsWith("/"))
      {
         resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
      }

      // Get index of last '/'
      int index = resourcePath.lastIndexOf('/');

      // Return name
      return resourcePath.substring(index + 1);
   }

   @Override
   public String getParent(String resourcePath) throws IOException
   {
      // It's a directory, remove the trailing '/'
      if (resourcePath.endsWith("/"))
      {
         resourcePath = resourcePath.substring(0, resourcePath.length() - 1);
      }

      // Get index of last '/'
      int index = resourcePath.lastIndexOf('/');

      // Return the parent that ends with a '/'
      return resourcePath.substring(0, index + 1);
   }

   @Override
   public byte[] getContent(String filePath) throws IOException
   {
      InputStream in = servletContext.getResourceAsStream(filePath);
      if (in == null)
      {
         log.error("Could not obtain input stream for file " + filePath);
         return null;
      }
      else
      {
         return IOTools.getBytes(in);
      }
   }

   @Override
   public Iterable<String> getChildren(String folderPath) throws IOException
   {
      @SuppressWarnings("unchecked") Set resourcePaths = servletContext.getResourcePaths(folderPath);
      return resourcePaths;
   }

   @Override
   public boolean isFile(String resourcePath) throws IOException
   {
      return !resourcePath.endsWith("/");
   }

   @Override
   public String getMimeType(String fileName)
   {
      return servletContext.getMimeType(fileName);
   }
}
