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

import org.chromattic.ext.ntdef.NTFolder;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.application.gadget.impl.GadgetDefinition;
import org.exoplatform.application.gadget.impl.LocalGadgetData;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ServletLocalImporter extends GadgetImporter
{

   /** . */
   private final Logger log = LoggerFactory.getLogger(ServletLocalImporter.class);

   /** . */
   private final ServletContext servletContext;

   /** Used temporarily when importing resources. */
   private NTFolder folder;

   public ServletLocalImporter(
      String name,
      String gadgetPath,
      ServletContext servletContext)
   {
      super(name, gadgetPath);

      //
      this.servletContext = servletContext;
   }

   @Override
   protected byte[] getGadgetBytes(String gadgetURI) throws IOException
   {
      return getContent(gadgetURI);
   }

   @Override
   protected String getGadgetURL(String gadgetURI) throws Exception
   {
      return gadgetURI;
   }

   @Override
   protected void process(String gadgetURI, GadgetDefinition def) throws Exception
   {
      def.setLocal(true);

      //
      LocalGadgetData data = (LocalGadgetData)def.getData();

      //
      String fileName = getName(gadgetURI);
      data.setFileName(fileName);

      // Import resource
      folder = data.getResources();
      String folderPath = getParent(gadgetURI);
      visitChildren(gadgetURI, folderPath);
      folder = null;
   }

   private void visit(String uri, String resourcePath) throws Exception
   {
      String name = getName(resourcePath);
      if (isFile(resourcePath))
      {
         byte[] content = getContent(resourcePath);

         //
         if (content != null)
         {
            String mimeType = getMimeType(name);

            //
            if (mimeType == null)
            {
               mimeType = "application/octet-stream";
            }

            // We can detect encoding for XML files
            String encoding = null;
            if ("application/xml".equals(mimeType))
            {
               encoding = EncodingDetector.detect(new ByteArrayInputStream(content));
            }

            // Correct mime type for gadgets
            if (resourcePath.equals(uri)) {
               mimeType = LocalGadgetData.GADGET_MIME_TYPE;
            }

            //
            folder.createFile(name, new Resource(mimeType, encoding, content));
         }
      }
      else
      {
         folder = folder.createFolder(name);
         visitChildren(uri, resourcePath);
         folder = folder.getParent();
      }
   }

   private void visitChildren(String gadgetURI, String folderPath) throws Exception
   {
      for (String childPath : getChildren(folderPath))
      {
         visit(gadgetURI, childPath);
      }
   }

   private String getName(String resourcePath) throws IOException
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

   private String getParent(String resourcePath) throws IOException
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

   private byte[] getContent(String filePath) throws IOException
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

   private Iterable<String> getChildren(String folderPath) throws IOException
   {
      @SuppressWarnings("unchecked") Set<String> resourcePaths = servletContext.getResourcePaths(folderPath);
      return resourcePaths;
   }

   private boolean isFile(String resourcePath) throws IOException
   {
      return !resourcePath.endsWith("/");
   }

   private String getMimeType(String fileName)
   {
      return servletContext.getMimeType(fileName);
   }
}
