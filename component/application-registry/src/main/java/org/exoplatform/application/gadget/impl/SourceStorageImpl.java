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
package org.exoplatform.application.gadget.impl;

import org.apache.shindig.common.uri.Uri;
import org.apache.shindig.gadgets.spec.GadgetSpec;
import org.apache.shindig.gadgets.spec.ModulePrefs;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;

import java.util.Calendar;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SourceStorageImpl implements SourceStorage
{
   /** . */
   private GadgetRegistryServiceImpl gadgetRegistryService;

   public SourceStorageImpl(GadgetRegistryService gadgetRegistryService)
   {
      this.gadgetRegistryService = (GadgetRegistryServiceImpl)gadgetRegistryService;
   }

   public Source getSource(Gadget gadget) throws Exception
   {
      GadgetDefinition def = gadgetRegistryService.getRegistry().getGadget(gadget.getName());

      //
      if (def == null)
      {
         return null;
      }

      //
      GadgetData data = def.getData();
      if (data instanceof LocalGadgetData)
      {
         LocalGadgetData localData = (LocalGadgetData)data;
         String content = localData.getSource();
         Calendar lastModified = Calendar.getInstance();
         lastModified.setTime(localData.getLastModified());

         //
         Source source = new Source(gadget.getName());
         source.setMimeType(LocalGadgetData.GADGET_MIME_TYPE);
         source.setLastModified(lastModified);
         source.setTextContent(content);

         //
         return source;
      }
      else
      {
         throw new IllegalArgumentException("The provided gadget is remote");
      }
   }

   public void saveSource(Gadget gadget, Source source) throws Exception
   {
      if (gadget == null)
      {
         throw new NullPointerException();
      }
      if (source == null)
      {
         throw new NullPointerException();
      }

      //
      GadgetRegistry registry = gadgetRegistryService.getRegistry();

      //
      GadgetDefinition def = registry.getGadget(gadget.getName());

      //
      GadgetData data = def.getData();
      if (data instanceof LocalGadgetData)
      {
         GadgetSpec spec = new GadgetSpec(Uri.parse("http://www.gatein.org"), source.getTextContent());
         ModulePrefs prefs = spec.getModulePrefs();

         //
         String description = prefs.getDescription();
         String thumbnail = prefs.getThumbnail().toString();
         String title = getGadgetTitle(prefs, gadget.getName());
         String referenceURL = prefs.getTitleUrl().toString();

         // Update state from XML
         def.setDescription(description);
         def.setThumbnail(thumbnail); // Do something better than that
         def.setTitle(title);
         def.setReferenceURL(referenceURL);

         // Save text content
         LocalGadgetData localData = (LocalGadgetData)data;
         localData.setSource(source.getTextContent());
      }
      else
      {
         throw new IllegalArgumentException("The provided gadget is remote");
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

   public void removeSource(String sourcePath) throws Exception
   {
      // No op
   }

   public String getSourceURI(String sourcePath)
   {
      throw new UnsupportedOperationException("Cannot obtain URI from source " + sourcePath);
   }
}
