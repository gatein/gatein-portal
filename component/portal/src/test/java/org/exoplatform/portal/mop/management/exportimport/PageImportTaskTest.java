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
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTaskTest extends TestCase
{
   private DataStorage dataStorage;
   private LazyPageList<Page> list;
   private SiteKey siteKey = new SiteKey("user", "foo");

   @Override
   @SuppressWarnings("unchecked")
   protected void setUp() throws Exception
   {
      dataStorage = mock(DataStorage.class);
      list = mock(LazyPageList.class);
   }

   public void testConserve_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(0); // no pages exist

      task.importData(ImportStrategy.CONSERVE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list, never()).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testConserve_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.CONSERVE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      verifyNoMoreInteractions(dataStorage, list);

      assertNullOrEmpty(task.getRollbackDeletes());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testConserve_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("foo").addPage("bar").addPage("baz").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.CONSERVE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testConserve_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      Page.PageSet existing = new Builder().addPage("page2").addPage("bar").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.CONSERVE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      verify(dataStorage).save(importing.getPages().get(0));
      verify(dataStorage).save(importing.getPages().get(3));

      verify(dataStorage, times(2)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testMerge_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(0); // no pages exist

      task.importData(ImportStrategy.MERGE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list, never()).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testMerge_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.MERGE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      assertNullOrEmpty(task.getRollbackDeletes());
      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
      comparePages(existing.getPages(), task.getRollbackSaves().getPages());
   }

   public void testMerge_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("foo").addPage("bar").addPage("baz").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.MERGE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
      assertNullOrEmpty(task.getRollbackSaves());
   }

   public void testMerge_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      Page.PageSet existing = new Builder().addPage("page2").addPage("bar").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.MERGE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(4)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(2, task.getRollbackSaves().getPages().size());
      comparePage(existing.getPages().get(0), task.getRollbackSaves().getPages().get(0));
      comparePage(existing.getPages().get(2), task.getRollbackSaves().getPages().get(1));
   }

   public void testOverwrite_NoPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(0); // no pages exist

      task.importData(ImportStrategy.OVERWRITE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list, never()).getAll();

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(3)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing, task.getRollbackDeletes());
      Assert.assertNull(task.getRollbackSaves());
   }

   public void testOverwrite_SamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.OVERWRITE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      int saveCount = importing.getPages().size() + existing.getPages().size();
      for (Page page : existing.getPages())
      {
         verify(dataStorage).remove(page);
      }

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(saveCount)).save();

      verifyNoMoreInteractions(dataStorage, list);

      assertNullOrEmpty(task.getRollbackDeletes());
      Assert.assertNotNull(task.getRollbackSaves());
      Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
      comparePages(existing.getPages(), task.getRollbackSaves().getPages());
   }

   public void testOverwrite_NewPages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
      Page.PageSet existing = new Builder().addPage("foo").addPage("bar").addPage("baz").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.OVERWRITE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      int saveCount = importing.getPages().size() + existing.getPages().size();
      for (Page page : existing.getPages())
      {
         verify(dataStorage).remove(page);
      }

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(saveCount)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());

      Assert.assertNotNull(task.getRollbackSaves());
      comparePages(existing.getPages(), task.getRollbackSaves().getPages());
   }

   public void testOverwrite_NewAndSamePages() throws Exception
   {
      Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
      Page.PageSet existing = new Builder().addPage("page2").addPage("bar").addPage("page3").build();
      PageImportTask task = new PageImportTask(importing, siteKey, dataStorage);

      when(dataStorage.find(Matchers.<Query<Page>>any())).thenReturn(list);
      when(list.getAvailable()).thenReturn(3);
      when(list.getAll()).thenReturn(existing.getPages());

      task.importData(ImportStrategy.OVERWRITE);

      verify(dataStorage).find(query("user", "foo"));
      verify(list).getAvailable();
      verify(list).getAll();

      int saveCount = importing.getPages().size() + existing.getPages().size();
      for (Page page : existing.getPages())
      {
         verify(dataStorage).remove(page);
      }

      for (Page page : importing.getPages())
      {
         verify(dataStorage).save(page);
      }
      verify(dataStorage, times(saveCount)).save();

      verifyNoMoreInteractions(dataStorage, list);

      Assert.assertNotNull(task.getRollbackDeletes());
      Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
      Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
      Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

      Assert.assertNotNull(task.getRollbackSaves());
      comparePages(existing.getPages(), task.getRollbackSaves().getPages());
   }

   private void assertNullOrEmpty(Page.PageSet pages)
   {
      if (pages != null)
      {
         Assert.assertTrue(pages.getPages().isEmpty());
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

   private Query<Page> query(String ownerType, String ownerId)
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
}
