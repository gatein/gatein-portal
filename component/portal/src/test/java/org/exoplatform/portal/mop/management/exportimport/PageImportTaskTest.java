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


import junit.framework.Assert;
import junit.framework.TestCase;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageServiceImpl;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTaskTest extends TestCase
{
   private PageServiceImpl pageService;
   private DataStorage dataStorage;
   private SiteKey siteKey = new SiteKey("user", "foo");

   @Override
   @SuppressWarnings("unchecked")
   protected void setUp() throws Exception
   {
      dataStorage = mock(DataStorage.class);
      pageService = mock(PageServiceImpl.class);
   }

   public void testConserve_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      QueryResult<PageContext> result = new QueryResult<PageContext>(0, 0, Collections.<PageContext>emptyList());
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);

      task.importData(ImportMode.CONSERVE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      for (Page page : importing.getPages())
      {
         PageContext context = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(context)));
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testConserve_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page1", "page2", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext pc : existing)
      {
         Page page = new Page(siteKey.getTypeName(), siteKey.getName(), pc.getState().getName());
         when(dataStorage.getPage(pc.getKey().format())).thenReturn(page);
      }

      task.importData(ImportMode.CONSERVE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testConserve_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("foo", "bar", "baz");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext pc : existing)
      {
         Page page = new Page(siteKey.getTypeName(), siteKey.getName(), pc.getState().getName());
         when(dataStorage.getPage(pc.getKey().format())).thenReturn(page);
      }

      task.importData(ImportMode.CONSERVE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testConserve_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page1", "page2", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext pc : existing)
      {
         Page page = new Page(siteKey.getTypeName(), siteKey.getName(), pc.getState().getName());
         when(dataStorage.getPage(pc.getKey().format())).thenReturn(page);
      }

      task.importData(ImportMode.CONSERVE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      assertNullOrEmpty(task.getRollbackSaves());
   }
   
   public void testInsert_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      QueryResult<PageContext> result = new QueryResult<PageContext>(0, 0, Collections.<PageContext>emptyList());
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);

      task.importData(ImportMode.INSERT);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testInsert_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page1", "page2", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
      }

      task.importData(ImportMode.INSERT);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         verify(pageService).loadPage(siteKey.page(page.getName()));
      }

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testInsert_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("foo", "bar", "baz");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
      }

      task.importData(ImportMode.INSERT);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         verify(pageService).loadPage(siteKey.page(page.getName()));

         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testInsert_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page2", "bar", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
      }

      task.importData(ImportMode.INSERT);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         verify(pageService).loadPage(siteKey.page(page.getName()));
         if (page.getName().equals("page1") || page.getName().equals("page4"))
         {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
         }
      }
      verify(dataStorage, times(2)).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testMerge_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      QueryResult<PageContext> result = new QueryResult<PageContext>(0, 0, Collections.<PageContext>emptyList());
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);

      task.importData(ImportMode.MERGE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testMerge_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page1", "page2", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
         Page p = new Page(siteKey.getTypeName(), siteKey.getName(), page.getKey().getName());
         when(dataStorage.getPage(page.getKey().format())).thenReturn(p);
      }

      task.importData(ImportMode.MERGE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         PageKey pageKey = siteKey.page(page.getName());
         verify(pageService).loadPage(pageKey);
         verify(dataStorage).getPage(pageKey.format());

         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
      comparePages(existing, task.getRollbackSaves().getPages());
   }

   public void testMerge_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("foo", "bar", "baz");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
      }

      task.importData(ImportMode.MERGE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         verify(pageService).loadPage(siteKey.page(page.getName()));

         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testMerge_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page2", "bar", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      for (PageContext page : existing)
      {
         when(pageService.loadPage(page.getKey())).thenReturn(page);
         if (page.getKey().getName().equals("page2") || page.getKey().getName().equals("page3"))
         {
            Page p = new Page(siteKey.getTypeName(), siteKey.getName(), page.getKey().getName());
            when(dataStorage.getPage(page.getKey().format())).thenReturn(p);
         }
      }

      task.importData(ImportMode.MERGE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      for (Page page : importing.getPages())
      {
         verify(pageService).loadPage(siteKey.page(page.getName()));
         if (page.getName().equals("page2") || page.getName().equals("page3"))
         {
            verify(dataStorage).getPage(siteKey.page(page.getName()).format());
         }
         if (!page.getName().equals("bar"))
         {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
         }
      }
      verify(dataStorage, times(4)).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(2, task.getRollbackSaves().getPages().size());
      comparePages(Arrays.asList(existing.get(0), existing.get(2)), task.getRollbackSaves().getPages());
   }

   public void testOverwrite_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      QueryResult<PageContext> result = new QueryResult<PageContext>(0, 0, Collections.<PageContext>emptyList());
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);

      task.importData(ImportMode.OVERWRITE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }

      verify(dataStorage, times(importing.getPages().size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testOverwrite_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page1", "page2", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      when(pageService.loadPages(siteKey)).thenReturn(existing);
      for (PageContext page : existing)
      {
         Page p = new Page(siteKey.getTypeName(), siteKey.getName(), page.getKey().getName());
         when(dataStorage.getPage(page.getKey().format())).thenReturn(p);
      }

      task.importData(ImportMode.OVERWRITE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      verify(pageService).loadPages(siteKey);

      for (PageContext page : existing)
      {
         verify(dataStorage).getPage(page.getKey().format());
         verify(pageService).destroyPage(page.getKey());
      }

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      assertNullOrEmpty(task.getRollbackDeletes());
      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
      comparePages(existing, task.getRollbackSaves().getPages());
   }

   public void testOverwrite_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("foo", "bar", "baz");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      when(pageService.loadPages(siteKey)).thenReturn(existing);
      for (PageContext page : existing)
      {
         Page p = new Page(siteKey.getTypeName(), siteKey.getName(), page.getKey().getName());
         when(dataStorage.getPage(page.getKey().format())).thenReturn(p);
      }

      task.importData(ImportMode.OVERWRITE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      verify(pageService).loadPages(siteKey);

      for (PageContext page : existing)
      {
         verify(dataStorage).getPage(page.getKey().format());
         verify(pageService).destroyPage(page.getKey());
      }

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());

      Assert.assertNotNull(task.getRollbackSaves());
      comparePages(existing, task.getRollbackSaves().getPages());
   }

   public void testOverwrite_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService);

      List<PageContext> existing = pages("page2", "bar", "page3");
      QueryResult<PageContext> result = new QueryResult<PageContext>(0, existing.size(), existing);
      when(pageService.findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null)).thenReturn(result);
      when(pageService.loadPages(siteKey)).thenReturn(existing);
      for (PageContext page : existing)
      {
         Page p = new Page(siteKey.getTypeName(), siteKey.getName(), page.getKey().getName());
         when(dataStorage.getPage(page.getKey().format())).thenReturn(p);
      }

      task.importData(ImportMode.OVERWRITE);

      verify(pageService).findPages(0, 1, siteKey.getType(), siteKey.getName(), null, null);
      verify(pageService).loadPages(siteKey);

      for (PageContext page : existing)
      {
         verify(dataStorage).getPage(page.getKey().format());
         verify(pageService).destroyPage(page.getKey());
      }

      for (Page page : importing.getPages())
      {
         PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
         verify(pageService).savePage(argThat(matches(pageContext)));
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

      verifyNoMoreInteractions(dataStorage, pageService);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

      Assert.assertNotNull(task.getRollbackSaves());
      comparePages(existing, task.getRollbackSaves().getPages());
   }

   private void assertNullOrEmpty(Page.PageSet pages)
   {
      if (pages != null)
      {
         Assert.assertTrue(pages.getPages().isEmpty());
      }
   }

   private void comparePages(List<PageContext> expected, ArrayList<Page> actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }

      assertEquals(expected.size(), actual.size());

      for (int i=0; i<expected.size(); i++)
      {
         comparePage(expected.get(i), actual.get(i));
      }
   }

   private void comparePages(ArrayList<Page> expected, ArrayList<Page> actual)
   {
      if (expected == null)
      {
         assertNull(actual);
         return;
      }

      assertEquals(expected.size(), actual.size());

      for (int i=0; i<expected.size(); i++)
      {
         comparePage(expected.get(i), actual.get(i));
      }
   }

   private void comparePage(Page expected, Page actual)
   {
      assertEquals(expected.getName(), actual.getName());
   }

   private void comparePage(PageContext expected, Page actual)
   {
      assertEquals(expected.getKey().getName(), actual.getName());
   }

   /*private Query<Page> query(String ownerType, String ownerId)
   {
      return argThat(new QueryMatcher(new Query<Page>(ownerType, ownerId, Page.class)));
   }

   private class QueryMatcher extends ArgumentMatcher<Query<Page>>
   {
      private Query<Page> query;

      public QueryMatcher(Query<Page> query)
      {
         this.query = query;
      }

      @Override
      public boolean matches(Object o)
      {
         if (query == o) return true;
         if (!(o instanceof Query)) return false;

         Query that = (Query) o;

         if (!query.getClassType().equals(that.getClassType())) return false;
         if (!query.getOwnerType().equals(that.getOwnerType())) return false;
         if (!query.getOwnerId().equals(that.getOwnerId())) return false;

         if (query.getName() != null ? !query.getName().equals(that.getName()) : that.getName() != null) return false;
         if (query.getTitle() != null ? !query.getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;

         return true;
      }
   }*/

   private List<PageContext> pages(String...names)
   {
      return new PageContextBuilder().pages(names).build();
   }

   private static ArgumentMatcher<PageContext> matches(PageContext context)
   {
      return new PageContextMatcher(context);
   }

   private static class PageContextMatcher extends ArgumentMatcher<PageContext>
   {
      private final PageContext pageContext;

      public PageContextMatcher(PageContext pageContext)
      {
         this.pageContext = pageContext;
      }

      @Override
      public boolean matches(Object o)
      {
         if (pageContext == o) return true;
         if (!(o instanceof PageContext)) return false;

         PageContext that = (PageContext) o;

         return that.getKey().equals(pageContext.getKey()) && that.getState().equals(pageContext.getState());

      }
   }

   private static class Builder
   {
      private Page.PageSet pages;
      public Builder()
      {
         pages = new Page.PageSet();
         pages.setPages(new ArrayList<Page>());
      }

      public Builder addPage(String name)
      {
         PageData page= new PageData(null, "", name, null, null, null, null, null, null, null, Collections.<String>emptyList(), Collections.<ComponentData>emptyList(), "", "", null, false);
         pages.getPages().add(new Page(page));

         return this;
      }

      public Page.PageSet build()
      {
         return pages;
      }
   }

   private class PageContextBuilder
   {
      private List<PageContext> pages = new ArrayList<PageContext>();

      PageContextBuilder pages(String... names)
      {
         for (String name : names)
         {
            Page page = new Page(siteKey.getTypeName(), siteKey.getName(), name);
            pages.add(new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page)));
         }

         return this;
      }

      List<PageContext> build()
      {
         return pages;
      }
   }
}
