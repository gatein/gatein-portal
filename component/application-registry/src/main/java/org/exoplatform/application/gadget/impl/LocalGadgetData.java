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
package org.exoplatform.application.gadget.impl;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.NodeMapping;
import org.chromattic.api.annotations.Property;
import org.exoplatform.application.gadget.EncodingDetector;
import org.exoplatform.application.registry.impl.NodeAware;
import org.gatein.common.io.IOTools;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;


/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@NodeMapping(name = "app:localgadgetdata")
public abstract class LocalGadgetData extends GadgetData implements NodeAware
{

   /** Temporary hack, use with care. */
   private Node node;

   @ManyToOne
   public abstract GadgetDefinition getDefinition();

   @Property(name = "filename")
   public abstract String getFileName();

   public abstract void setFileName(String fileName);

   public void setNode(Node node)
   {
      this.node = node;
   }

   public Node getNode()
   {
      return node;
   }

   public void setSource(String gadgetXML) throws Exception
   {

      // Get the definition
      GadgetDefinition def = getDefinition();

      // Get the related content
      GadgetSpec spec = new GadgetSpec(Uri.parse("http://www.gatein.org"), gadgetXML);
      ModulePrefs prefs = spec.getModulePrefs();
      String fileName = getFileName();
      Node contentNode = node.getNode("resources/" + fileName + "/jcr:content");
      byte[] bytes = gadgetXML.getBytes();
      String encoding = EncodingDetector.detect(new ByteArrayInputStream(bytes));

      // Update def
      def.setDescription(prefs.getDescription());
      def.setThumbnail(prefs.getThumbnail().toString()); // Do something better than that
      def.setTitle(prefs.getTitle());
      def.setReferenceURL(prefs.getTitleUrl().toString());

      // Update content
      contentNode.setProperty("jcr:encoding", encoding);
      contentNode.setProperty("jcr:data", new ByteArrayInputStream(bytes));
      contentNode.setProperty("jcr:mimeType", "application/xml");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
   }

   public String getSource() throws Exception
   {
      String fileName = getFileName();
      Node contentNode = node.getNode("resources/" + fileName + "/jcr:content");
      InputStream in = contentNode.getProperty("jcr:data").getStream();
      String encoding = contentNode.getProperty("jcr:encoding").getString();
      byte[] bytes = IOTools.getBytes(in);
      return new String(bytes, encoding);
   }

   public Calendar getLastModified()
   {
      try
      {
         String fileName = getFileName();
         Node contentNode = node.getNode("resources/" + fileName + "/jcr:content");
         return contentNode.getProperty("jcr:lastModified").getDate();
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
   }

   private static String getProperty(Node node, String name, String defaultValue) throws Exception
   {
      if (node.hasProperty(name))
      {
         return node.getProperty(name).getString();
      }
      else
      {
         return defaultValue;
      }
   }

   private static Calendar getCalendarProperty(Node node, String name) throws Exception
   {
      if (node.hasProperty(name))
      {
         return node.getProperty(name).getDate();
      }
      else
      {
         return null;
      }
   }


/*
   @OneToOne
   @MappedBy("folder")
   public abstract NTFolder getFolder();

   public abstract void setFolder(NTFolder folder);
*/
}
