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

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.exoplatform.application.gadget.impl.GadgetDefinition;
import org.exoplatform.application.gadget.impl.GadgetRegistry;
import org.exoplatform.application.gadget.impl.LocalGadgetData;
import org.exoplatform.application.gadget.impl.RemoteGadgetData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.net.URLTools;

import javax.jcr.Node;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class LocalImporter
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(LocalImporter.class);

   /** The gadget name as seen by GateIn. */
   private String name;

   /** The gadget registry. */
   private GadgetRegistry registry;

   /** The gadget path. */
   private String gadgetPath;

   /** . */
   private boolean local;

   /** Used temporarily when importing resources. */
   private Node folder;

   protected LocalImporter(
      String name,
      GadgetRegistry registry,
      String gadgetPath,
      boolean local)
   {
      this.name = name;
      this.registry = registry;
      this.gadgetPath = gadgetPath;
      this.local = local;
   }

   private byte[] getGadgetBytes() throws IOException
   {
      if (local)
      {
         return getContent(gadgetPath);
      }
      else
      {
         URL url = new URL(gadgetPath);
         return URLTools.getContent(url, 5000, 5000);
      }
   }

   private String getGadgetURL() throws Exception
   {
      if (local)
      {
         return "http://www.gatein.org";
      }
      else
      {
         return gadgetPath;
      }
   }

   private String getGadgetTitle(ModulePrefs prefs, String defaultValue)
   {
      String title = prefs.getDirectoryTitle();
      if (title == null || title.trim().length() < 1)
      {
         title = prefs.getTitle();
      }
      if (title == null || title.trim().length() < 1)
      {
         return defaultValue;
      }
      return title;
   }

   public void doImport() throws Exception
   {
      if (registry.getGadget(name) != null)
      {
         System.out.println("Cannot import existing gagdet " + name);
         return;
      }

      // Get bytes
      byte[] bytes = getGadgetBytes();
      if (bytes == null)
      {
         System.out.println("Coult not import gadget " + gadgetPath);
         return;
      }

      // Get encoding
      String encoding = EncodingDetector.detect(new ByteArrayInputStream(bytes));

      //
      String gadget = new String(bytes, encoding);

      //
      String gadgetURL = getGadgetURL();
      GadgetSpec spec = new GadgetSpec(Uri.parse(gadgetURL), gadget);
      ModulePrefs prefs = spec.getModulePrefs();

      //
      GadgetDefinition def = registry.addGadget(name);

      //
      String description = prefs.getDescription();
      String thumbnail = prefs.getThumbnail().toString();
      String title = getGadgetTitle(prefs, name);
      String referenceURL = prefs.getTitleUrl().toString();

      //
      log.info("Importing gadget name=" + name + " description=" + description + " thumbnail=" + thumbnail + " title=" +
               thumbnail + " title=" + title);

      //
      def.setDescription(description);
      def.setThumbnail(thumbnail); // Do something better than that
      def.setTitle(title);
      def.setReferenceURL(referenceURL);
      def.setLocal(local);

      //
      if (local)
      {
         LocalGadgetData data = (LocalGadgetData)def.getData();

         //
         String fileName = getName(gadgetPath);
         data.setFileName(fileName);

         // Import resource
         folder = data.getNode().addNode("resources", "nt:folder");
         String folderPath = getParent(gadgetPath);
         visitChildren(folderPath);
         folder = null;
      }
      else
      {
         RemoteGadgetData data = (RemoteGadgetData)def.getData();

         // Set remote URL
         data.setURL(gadgetPath);
      }
   }

   private void visit(String resourcePath) throws Exception
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
            Node file = folder.addNode(name, "nt:file");
            Node resource = file.addNode("jcr:content", "nt:resource");
            resource.setProperty("jcr:data", new ByteArrayInputStream(content));
            resource.setProperty("jcr:lastModified", Calendar.getInstance());
            resource.setProperty("jcr:mimeType", mimeType);

            // We can detect encoding for XML files
            if ("application/xml".equals(mimeType))
            {
               String encoding = EncodingDetector.detect(new ByteArrayInputStream(content));
               resource.setProperty("jcr:encoding", encoding);
            }
         }
      }
      else
      {
         folder = folder.addNode(name, "nt:folder");
         visitChildren(resourcePath);
         folder = folder.getParent();
      }
   }

   private void visitChildren(String folderPath) throws Exception
   {
      for (String childPath : getChildren(folderPath))
      {
         visit(childPath);
      }
   }

   public abstract String getName(String resourcePath) throws IOException;

   public abstract String getParent(String resourcePath) throws IOException;

   public abstract byte[] getContent(String filePath) throws IOException;

   public abstract Iterable<String> getChildren(String folderPath) throws IOException;

   public abstract boolean isFile(String resourcePath) throws IOException;

   public abstract String getMimeType(String fileName);
}
