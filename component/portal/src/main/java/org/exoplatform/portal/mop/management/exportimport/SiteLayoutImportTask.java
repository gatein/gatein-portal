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
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteLayoutImportTask extends AbstractImportTask<PortalConfig>
{
   private final DataStorage dataStorage;
   private PortalConfig rollbackDelete;
   private PortalConfig rollbackSave;

   public SiteLayoutImportTask(PortalConfig data, SiteKey siteKey, DataStorage dataStorage)
   {
      super(data, siteKey);
      this.dataStorage = dataStorage;
   }

   @Override
   public void importData(ImportStrategy importStrategy) throws Exception
   {
      PortalConfig dst = dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());

      switch (importStrategy)
      {
         // Really doesn't make sense to "merge" site layout data.  Really two modes, conserve (keep) and overwrite.
         case CONSERVE:
            if (dst == null)
            {
               dst = data;
               rollbackDelete = data;
            }
            else
            {
               dst = null;
            }
            break;
         case MERGE:
         case OVERWRITE:
            if (dst == null)
            {
               rollbackDelete = data;
            }
            else
            {
               rollbackSave = PageUtils.copy(dst);
            }
            dst = data;
            break;
      }

      if (dst != null)
      {
         if (rollbackDelete == null)
         {
            dataStorage.save(dst);
         }
         else
         {
            dataStorage.create(dst);
         }
      }
   }

   @Override
   public void rollback() throws Exception
   {
      if (rollbackDelete != null)
      {
         dataStorage.remove(rollbackDelete);
      }
      else if (rollbackSave != null)
      {
         dataStorage.save(rollbackSave);
      }
   }
}
