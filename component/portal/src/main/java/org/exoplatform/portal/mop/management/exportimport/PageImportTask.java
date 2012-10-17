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
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.operations.MOPSiteProvider;
import org.exoplatform.portal.mop.management.operations.Utils;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Site;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTask extends AbstractImportTask<Page.PageSet>
{
   private final DataStorage dataStorage;
   private final PageService pageService;
   private final MOPSiteProvider siteProvider;
   private Page.PageSet rollbackSaves;
   private Page.PageSet rollbackDeletes;

   public PageImportTask(Page.PageSet data, SiteKey siteKey, DataStorage dataStorage, PageService pageService, MOPSiteProvider siteProvider)
   {
      super(data, siteKey);
      this.dataStorage = dataStorage;
      this.pageService = pageService;
      this.siteProvider = siteProvider;
   }

   @Override
   public void importData(ImportMode importMode) throws Exception
   {
      if (data == null || data.getPages() == null || data.getPages().isEmpty()) return;

      Site site = siteProvider.getSite(siteKey);
      if (site == null) throw new Exception("Cannot import pages because site does not exist for " + siteKey);
      org.gatein.mop.api.workspace.Page pages = site.getRootPage().getChild("pages");
      int size = (pages == null) ? 0 : pages.getChildren().size();

      Page.PageSet dst = null;
      switch (importMode)
      {
         case CONSERVE:
            if (size == 0) // No pages exist yet.
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               dst = null;
            }
            break;
         case INSERT:
            if (size == 0) // No pages exist yet.
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               dst = new Page.PageSet();
               rollbackDeletes = new Page.PageSet();
               for (Page src : data.getPages())
               {
                  if (pages.getChild(src.getName()) == null)
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
               rollbackSaves = new Page.PageSet();
               rollbackDeletes = new Page.PageSet();
               for (Page src : data.getPages())
               {
                  dst.getPages().add(src);
                  PageKey pageKey = siteKey.page(src.getName());
                  if (pages.getChild(src.getName()) == null)
                  {
                     rollbackDeletes.getPages().add(src);
                  }
                  else
                  {
                     PageContext pageContext = pageService.loadPage(pageKey);
                     Page existing = dataStorage.getPage(pageKey.format());
                     pageContext.update(existing);
                     rollbackSaves.getPages().add(PageUtils.copy(existing));
                  }
               }
            }
            break;
         case OVERWRITE:
            if (size == 0) // No pages exist yet.
            {
               dst = data;
               rollbackDeletes = data;
            }
            else
            {
               List<Page> list = PageUtils.getAllPages(dataStorage, pageService, siteKey).getPages();
               rollbackSaves = new Page.PageSet();
               rollbackSaves.setPages(new ArrayList<Page>(list.size()));
               rollbackDeletes = new Page.PageSet();
               for (Page page : list)
               {
                  Page copy = PageUtils.copy(page);
                  pageService.destroyPage(siteKey.page(page.getName()));
                  dataStorage.save();
                  rollbackSaves.getPages().add(copy);
               }
               for (Page src : data.getPages())
               {
                  Page found = findPage(list, src);
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
            pageService.savePage(new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page)));
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
            pageService.destroyPage(siteKey.page(page.getName()));
            dataStorage.save();
         }
      }
      if (rollbackSaves != null && !rollbackSaves.getPages().isEmpty())
      {
         for (Page page : rollbackSaves.getPages())
         {
            pageService.savePage(new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page)));
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

   private static Page findPage(List<Page> pages, Page src)
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
