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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTask extends AbstractImportTask<Page.PageSet>
{
   private final DataStorage dataStorage;
   private Page.PageSet rollbackSaves;
   private Page.PageSet rollbackDeletes;

   public PageImportTask(Page.PageSet data, SiteKey siteKey, DataStorage dataStorage)
   {
      super(data, siteKey);
      this.dataStorage = dataStorage;
   }

   @Override
   public void importData(ImportMode importMode) throws Exception
   {
      if (data == null || data.getPages() == null || data.getPages().isEmpty()) return;

      Query<Page> query = new Query<Page>(siteKey.getTypeName(), siteKey.getName(), Page.class);
      LazyPageList<Page> list = dataStorage.find(query);
      int size = list.getAvailable();

      Page.PageSet dst = null;
      switch (importMode)
      {
         case CONSERVE:
            if (size == 0)
            {
               dst = data; // No pages exist yet.
               rollbackDeletes = data;
            }
            else
            {
               dst = null;
            }
            break;
         case INSERT:
            if (size == 0)
            {
               dst = data; // No pages exist yet.
               rollbackDeletes = data;
            }
            else
            {
               dst = new Page.PageSet();
               dst.setPages(new ArrayList<Page>());
               List<Page> existingPages = list.getAll();
               rollbackDeletes = new Page.PageSet();
               rollbackDeletes.setPages(new ArrayList<Page>());
               for (Page src : data.getPages())
               {
                  Page found = findPage(existingPages, src);
                  if (found == null)
                  {
                     dst.getPages().add(src);
                     rollbackDeletes.getPages().add(src);
                  }
               }
            }
            break;
         case MERGE:
            if (size == 0) // No pages exist yet.
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               dst = new Page.PageSet();
               dst.setPages(new ArrayList<Page>(data.getPages().size()));
               List<Page> existingPages = list.getAll();
               rollbackSaves = new Page.PageSet();
               rollbackSaves.setPages(new ArrayList<Page>(size));
               rollbackDeletes = new Page.PageSet();
               rollbackDeletes.setPages(new ArrayList<Page>());
               for (Page src : data.getPages())
               {
                  dst.getPages().add(src);

                  Page found = findPage(existingPages, src);
                  if (found == null)
                  {
                     rollbackDeletes.getPages().add(src);
                  }
                  else
                  {
                     rollbackSaves.getPages().add(PageUtils.copy(found));
                  }
               }
            }
            break;
         case OVERWRITE:
            if (size == 0)
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               List<Page> existingPages = list.getAll();
               rollbackSaves = new Page.PageSet();
               rollbackSaves.setPages(new ArrayList<Page>(size));
               rollbackDeletes = new Page.PageSet();
               rollbackDeletes.setPages(new ArrayList<Page>());
               for (Page page : existingPages)
               {
                  Page copy = PageUtils.copy(page);
                  dataStorage.remove(page);
                  dataStorage.save();
                  rollbackSaves.getPages().add(copy);
               }
               for (Page src : data.getPages())
               {
                  Page found = findPage(rollbackSaves.getPages(), src);
                  if (found == null)
                  {
                     rollbackDeletes.getPages().add(src);
                  }
               }

               dst = data;
            }
            break;
      }

      if (dst != null)
      {
         for (Page page : dst.getPages())
         {
            dataStorage.save(page);
            dataStorage.save();
         }
      }
   }

   @Override
   public void rollback() throws Exception
   {
      if (rollbackDeletes != null && !rollbackDeletes.getPages().isEmpty())
      {
         for (Page page : rollbackDeletes.getPages())
         {
            dataStorage.remove(page);
            dataStorage.save();
         }
      }
      if (rollbackSaves != null && !rollbackSaves.getPages().isEmpty())
      {
         for (Page page : rollbackSaves.getPages())
         {
            dataStorage.save(page);
            dataStorage.save();
         }
      }
   }

   Page.PageSet getRollbackSaves()
   {
      return rollbackSaves;
   }

   Page.PageSet getRollbackDeletes()
   {
      return rollbackDeletes;
   }

   private Page findPage(List<Page> pages, Page src)
   {
      Page found = null;
      for (Page page : pages)
      {
         if (src.getName().equals(page.getName()))
         {
            found = page;
         }
      }
      return found;
   }
}
