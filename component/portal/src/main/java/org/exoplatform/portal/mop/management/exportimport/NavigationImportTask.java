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

package org.exoplatform.portal.mop.management.exportimport;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.importer.NavigationImporter;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.Locale;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationImportTask extends AbstractImportTask<PageNavigation>
{
   private static final Logger log = LoggerFactory.getLogger(NavigationImportTask.class);

   private NavigationService navigationService;
   private DescriptionService descriptionService;
   private DataStorage dataStorage;
   private RollbackTask rollbackTask;

   public NavigationImportTask(PageNavigation data, SiteKey siteKey,
                               NavigationService navigationService, DescriptionService descriptionService, DataStorage dataStorage)
   {
      super(data, siteKey);
      this.navigationService = navigationService;
      this.descriptionService = descriptionService;
      this.dataStorage = dataStorage;
   }

   @Override
   public void importData(ImportStrategy importStrategy) throws Exception
   {
      ImportMode mode;
      switch (importStrategy)
      {
         case CONSERVE:
            mode = ImportMode.INSERT;
            break;
         case MERGE:
            mode = ImportMode.MERGE;
            break;
         case OVERWRITE:
            mode = ImportMode.OVERWRITE;
            break;
         default:
            throw new Exception("Could not map import strategy " + importStrategy.getName() + " to import mode.");
      }

      PortalConfig portalConfig = dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
      if (portalConfig == null) throw new Exception("Cannot import navigation because site does not exist for " + siteKey);

      Locale locale = (portalConfig.getLocale() == null) ? Locale.ENGLISH : new Locale(portalConfig.getLocale());

      final NavigationContext navContext = navigationService.loadNavigation(siteKey);
      if (navContext == null)
      {
         rollbackTask = new RollbackTask()
         {
            @Override
            public void rollback() throws Exception
            {
               navigationService.destroyNavigation(navContext);
            }
         };
      }
      else
      {
         //TODO: Rollback updates.
         rollbackTask = new RollbackTask()
         {
            @Override
            public void rollback() throws Exception
            {
               log.warn("Rollback for existing navigation not supported at the moment.");
            }
         };
      }

      NavigationImporter importer = new NavigationImporter(locale, mode, data, navigationService, descriptionService);
      importer.perform();
   }

   @Override
   public void rollback() throws Exception
   {
      if (rollbackTask != null)
      {
         rollbackTask.rollback();
      }
   }

   private static interface RollbackTask
   {
      void rollback() throws Exception;
   }
}
