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
import org.chromattic.api.annotations.*;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.chromattic.ext.ntdef.NTFile;
import org.chromattic.ext.ntdef.Resource;
import org.chromattic.ext.ntdef.NTFolder;
import org.exoplatform.application.gadget.EncodingDetector;


import java.io.ByteArrayInputStream;
import java.util.Date;


/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "app:localgadgetdata")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("app")
public abstract class LocalGadgetData extends GadgetData
{

   /** Mime type for gadgets. */
   public static final String GADGET_MIME_TYPE = "application/x-google-gadget";

   @ManyToOne
   public abstract GadgetDefinition getDefinition();

   @Property(name = "app:filename")
   public abstract String getFileName();

   public abstract void setFileName(String fileName);

   @OneToOne
   @Owner
   @MappedBy("app:resources")
   public abstract NTFolder getResources();

   protected abstract void setResources(NTFolder resources);

   private NTFile getGadgetContent() {
      String fileName = getFileName();
      NTFolder resources = getResources();
      return resources.getFile(fileName);
   }

   public void setSource(String gadgetXML) throws Exception
   {
      // Get the definition
      GadgetDefinition def = getDefinition();

      // Get the related content
      GadgetSpec spec = new GadgetSpec(Uri.parse("http://www.gatein.org"), gadgetXML);
      ModulePrefs prefs = spec.getModulePrefs();
      byte[] bytes = gadgetXML.getBytes();
      String encoding = EncodingDetector.detect(new ByteArrayInputStream(bytes));

      // Update def
      def.setDescription(prefs.getDescription());
      def.setThumbnail(prefs.getThumbnail().toString()); // Do something better than that
      def.setTitle(prefs.getTitle());
      def.setReferenceURL(prefs.getTitleUrl().toString());

      // Update content
      NTFile content = getGadgetContent();
      content.setContentResource(new Resource(GADGET_MIME_TYPE, encoding, bytes));
   }

   public String getSource() throws Exception
   {
      NTFile content = getGadgetContent();
      Resource res = content.getContentResource();
      String encoding = res.getEncoding();
      byte[] bytes = res.getData();
      return new String(bytes, encoding);
   }

   public Date getLastModified()
   {
      NTFile content = getGadgetContent();
      return content.getLastModified();
   }
}
