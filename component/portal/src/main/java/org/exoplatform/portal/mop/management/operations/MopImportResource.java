/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.operations;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.exportimport.NavigationExportTask;
import org.exoplatform.portal.mop.management.exportimport.NavigationImportTask;
import org.exoplatform.portal.mop.management.exportimport.PageExportTask;
import org.exoplatform.portal.mop.management.exportimport.PageImportTask;
import org.exoplatform.portal.mop.management.exportimport.SiteLayoutExportTask;
import org.exoplatform.portal.mop.management.exportimport.SiteLayoutImportTask;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.mop.api.workspace.Workspace;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopImportResource implements OperationHandler
{
   private static final Logger log = LoggerFactory.getLogger(MopImportResource.class);

   //TODO: Would like to see the step operations be handled by mgmt core.

   //TODO: Clean this up when we have time
   @Override
   public void execute(final OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      final String operationName = operationContext.getOperationName();

      OperationAttachment attachment = operationContext.getAttachment(true);
      if (attachment == null) throw new OperationException(operationContext.getOperationName(), "No attachment available for MOP import.");

      InputStream inputStream = attachment.getStream();
      if (inputStream == null) throw new OperationException(operationContext.getOperationName(), "No data stream available for import.");

      POMSessionManager mgr = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
      POMSession session = mgr.getSession();
      if (session == null) throw new OperationException(operationName, "MOP session was null");

      Workspace workspace = session.getWorkspace();
      if (workspace == null) throw new OperationException(operationName, "MOP workspace was null");

      DataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);
      if (dataStorage == null) throw new OperationException(operationName, "DataStorage was null");

      NavigationService navigationService = operationContext.getRuntimeContext().getRuntimeComponent(NavigationService.class);
      if (navigationService == null) throw new OperationException(operationName, "Navigation service was null");

      DescriptionService descriptionService = operationContext.getRuntimeContext().getRuntimeComponent(DescriptionService.class);
      if (descriptionService == null) throw new OperationException(operationName, "Description service was null");

      String mode = operationContext.getAttributes().getValue("importMode");
      if (mode == null || "".equals(mode)) mode = "merge";

      ImportMode importMode;
      try
      {
         importMode = ImportMode.valueOf(mode.trim().toUpperCase());
      }
      catch (Exception e)
      {
         throw new OperationException(operationName, "Unknown importMode " + mode);
      }

      Map<SiteKey, MopImport> importMap = new HashMap<SiteKey, MopImport>();
      final NonCloseableZipInputStream zis = new NonCloseableZipInputStream(inputStream);
      ZipEntry entry;
      boolean empty = false;
      try
      {
         log.info("Preparing data for import.");
         while ( (entry = zis.getNextEntry()) != null)
         {
            // Skip directories
            if (entry.isDirectory()) continue;
            // Skip empty entries (this allows empty zip files to not cause exceptions).
            empty = entry.getName().equals("");
            if (empty) continue;

            // Parse zip entry
            String[] parts = parseEntry(entry);
            SiteKey siteKey = Utils.siteKey(parts[0], parts[1]);
            String file = parts[2];
            
            MopImport mopImport = importMap.get(siteKey);
            if (mopImport == null)
            {
               mopImport =  new MopImport();
               importMap.put(siteKey, mopImport);
            }

            if (file.equals(SiteLayoutExportTask.FILE))
            {
               // Unmarshal site layout data
               Marshaller<PortalConfig> marshaller = operationContext.getBindingProvider().getMarshaller(PortalConfig.class, ContentType.XML);
               PortalConfig portalConfig = marshaller.unmarshal(zis);
               portalConfig.setType(siteKey.getTypeName());
               if (!portalConfig.getName().equals(siteKey.getName()))
               {
                  throw new OperationException(operationName, "Name of site does not match that of the zip entry site name.");
               }

               // Add import task to run later
               mopImport.siteTask = new SiteLayoutImportTask(portalConfig, siteKey, dataStorage);
            }
            else if (file.equals(PageExportTask.FILE))
            {
               // Unmarshal page data
               Marshaller<Page.PageSet> marshaller = operationContext.getBindingProvider().getMarshaller(Page.PageSet.class, ContentType.XML);
               Page.PageSet pages = marshaller.unmarshal(zis);
               for (Page page : pages.getPages())
               {
                  page.setOwnerType(siteKey.getTypeName());
                  page.setOwnerId(siteKey.getName());
               }

               // Add import task to run later.
               mopImport.pageTask = new PageImportTask(pages, siteKey, dataStorage);
            }
            else if (file.equals(NavigationExportTask.FILE))
            {
               // Unmarshal navigation data
               Marshaller<PageNavigation> marshaller = operationContext.getBindingProvider().getMarshaller(PageNavigation.class, ContentType.XML);
               PageNavigation navigation = marshaller.unmarshal(zis);
               navigation.setOwnerType(siteKey.getTypeName());
               navigation.setOwnerId(siteKey.getName());

               // Add import task to run later
               mopImport.navigationTask = new NavigationImportTask(navigation, siteKey, navigationService, descriptionService, dataStorage);
            }
         }

         resultHandler.completed(NoResultModel.INSTANCE);
      }
      catch (Throwable t)
      {
         throw new OperationException(operationContext.getOperationName(), "Exception reading data for import.", t);
      }
      finally
      {
         try
         {
            zis.reallyClose();
         }
         catch (IOException e)
         {
            log.warn("Exception closing underlying data stream from import.");
         }
      }

      if (empty)
      {
         log.info("Nothing to import, zip file empty.");
         return;
      }

      // Perform import
      Map<SiteKey, MopImport> importsRan = new HashMap<SiteKey, MopImport>();
      try
      {
         log.info("Performing import using importMode '" + mode + "'");
         for (Map.Entry<SiteKey, MopImport> mopImportEntry : importMap.entrySet())
         {
            SiteKey siteKey = mopImportEntry.getKey();
            MopImport mopImport = mopImportEntry.getValue();
            MopImport ran = new MopImport();

            if (importsRan.containsKey(siteKey))
            {
               throw new IllegalStateException("Multiple site imports for same operation.");
            }
            importsRan.put(siteKey, ran);

            log.debug("Importing data for site " + siteKey);

            // Site layout import
            if (mopImport.siteTask != null)
            {
               log.debug("Importing site layout data.");
               ran.siteTask = mopImport.siteTask;
               mopImport.siteTask.importData(importMode);
            }

            // Page import
            if (mopImport.pageTask != null)
            {
               log.debug("Importing page data.");
               ran.pageTask = mopImport.pageTask;
               mopImport.pageTask.importData(importMode);
            }

            // Navigation import
            if (mopImport.navigationTask != null)
            {
               log.debug("Importing navigation data.");
               ran.navigationTask = mopImport.navigationTask;
               mopImport.navigationTask.importData(importMode);
            }
         }
         log.info("Import successful !");
      }
      catch (Throwable t)
      {
         boolean rollbackSuccess = true;
         log.error("Exception importing data.", t);
         log.info("Attempting to rollback data modified by import.");
         for (Map.Entry<SiteKey, MopImport> mopImportEntry : importsRan.entrySet())
         {
            SiteKey siteKey = mopImportEntry.getKey();
            MopImport mopImport = mopImportEntry.getValue();

            log.debug("Rolling back imported data for site " + siteKey);
            if (mopImport.navigationTask != null)
            {
               log.debug("Rolling back navigation modified during import...");
               try
               {
                  mopImport.navigationTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back navigation data for site " + siteKey, t1);
               }
            }
            if (mopImport.pageTask != null)
            {
               log.debug("Rolling back pages modified during import...");
               try
               {
                  mopImport.pageTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back page data for site " + siteKey, t1);
               }
            }
            if (mopImport.siteTask != null)
            {
               log.debug("Rolling back site layout modified during import...");
               try
               {
                  mopImport.siteTask.rollback();
               }
               catch (Throwable t1) // Continue rolling back even though there are exceptions.
               {
                  rollbackSuccess = false;
                  log.error("Error rolling back site layout for site " + siteKey, t1);
               }
            }
         }

         String message = (rollbackSuccess) ?
            "Error during import. Tasks successfully rolled back. Portal should be back to consistent state." :
            "Error during import. Errors in rollback as well. Portal may be in an inconsistent state.";

         throw new OperationException(operationName, message, t);
      }
      finally
      {
         importMap.clear();
         importsRan.clear();
      }
   }

   private static String[] parseEntry(ZipEntry entry) throws IOException
   {
      String name = entry.getName();
      if (name.endsWith(SiteLayoutExportTask.FILE) || name.endsWith(PageExportTask.FILE) || name.endsWith(NavigationExportTask.FILE))
      {
         String[] parts = new String[3];
         parts[0] = name.substring(0, name.indexOf("/"));
         parts[1] = name.substring(parts[0].length() + 1, name.lastIndexOf("/"));
         parts[2] = name.substring(name.lastIndexOf("/") + 1);
         return parts;
      }
      else
      {
         throw new IOException("Unknown entry " + name + " in zip file.");
      }
   }

   // Bug in SUN's JDK XMLStreamReader implementation closes the underlying stream when
   // it finishes reading an XML document. This is no good when we are using a ZipInputStream.
   // See http://bugs.sun.com/view_bug.do?bug_id=6539065 for more information.
   private static class NonCloseableZipInputStream extends ZipInputStream
   {
      private NonCloseableZipInputStream(InputStream inputStream)
      {
         super(inputStream);
      }

      @Override
      public void close() throws IOException
      {
      }

      private void reallyClose() throws IOException
      {
         super.close();
      }
   }

   private static class MopImport
   {
      private SiteLayoutImportTask siteTask;
      private PageImportTask pageTask;
      private NavigationImportTask navigationTask;
   }
}
