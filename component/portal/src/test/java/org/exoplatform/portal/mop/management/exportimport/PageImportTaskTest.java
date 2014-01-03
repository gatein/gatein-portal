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

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import junit.framework.TestCase;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.gatein.portal.mop.site.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.operations.MOPSiteProvider;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageServiceImpl;
import org.gatein.portal.mop.page.PageState;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.mop.api.workspace.Site;
import org.mockito.ArgumentMatcher;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageImportTaskTest extends TestCase {
    private PageServiceImpl pageService;
    private DataStorage dataStorage;
    private POMSession session;
    private Site site;
    private org.gatein.mop.api.workspace.Page pages;
    private org.gatein.mop.api.workspace.Page rootPage;
    private org.gatein.mop.api.workspace.Page mockPage;
    private SiteKey siteKey = new SiteKey("user", "foo");

    private MOPSiteProvider siteProvider;

    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        dataStorage = mock(DataStorage.class);
        pageService = mock(PageServiceImpl.class);
        rootPage = mock(org.gatein.mop.api.workspace.Page.class);
        pages = mock(org.gatein.mop.api.workspace.Page.class);
        mockPage = mock(org.gatein.mop.api.workspace.Page.class);
        site = mock(Site.class);
        siteProvider = new MOPSiteProvider() {
            @Override
            public Site getSite(SiteKey siteKey) {
                return site;
            }
        };
    }

    private org.gatein.mop.api.workspace.Page getRootPage() {
        return null;
    }

    public void testConserve_NoPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> emptyList());

        task.importData(ImportMode.CONSERVE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            PageContext context = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(context)));
            verify(dataStorage).save(page);
        }
        verify(dataStorage, times(3)).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing, task.getRollbackDeletes());
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testConserve_SamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page1", "page2", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.CONSERVE);

        verify(pages).getChildren();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        assertNullOrEmpty(task.getRollbackSaves());
    }

    public void testConserve_NewPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("foo", "bar", "baz");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.CONSERVE);

        verify(pages).getChildren();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        assertNullOrEmpty(task.getRollbackSaves());
    }

    public void testConserve_NewAndSamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page1", "page2", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.CONSERVE);

        verify(pages).getChildren();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        assertNullOrEmpty(task.getRollbackSaves());
    }

    public void testInsert_NoPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> emptyList());

        task.importData(ImportMode.INSERT);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing, task.getRollbackDeletes());
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testInsert_SamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page1", "page2", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.INSERT);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());
        }

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        assertNullOrEmpty(task.getRollbackSaves());
    }

    public void testInsert_NewPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("foo", "bar", "baz");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.INSERT);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());

            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testInsert_NewAndSamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page2", "bar", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.INSERT);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());

            if (page.getName().equals("page1") || page.getName().equals("page4")) {
                PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
                verify(pageService).savePage(argThat(matches(pageContext)));
                verify(dataStorage).save(page);
            }
        }
        verify(dataStorage, times(2)).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
        Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
        Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testMerge_NoPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> emptyList());

        task.importData(ImportMode.MERGE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing, task.getRollbackDeletes());
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testMerge_SamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page1", "page2", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
            when(pageService.loadPage(siteKey.page(name))).thenReturn(new PageContextBuilder().page(name));
            when(dataStorage.getPage(siteKey.page(name).format())).thenReturn(
                    new Page(siteKey.getTypeName(), siteKey.getName(), name));
        }

        task.importData(ImportMode.MERGE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());

            PageKey pageKey = siteKey.page(page.getName());
            verify(pageService).loadPage(pageKey);
            verify(dataStorage).getPage(pageKey.format());

            verify(pageService).savePage(argThat(matches(new PageContextBuilder().page(page.getName()))));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        Assert.assertNotNull(task.getRollbackSaves());
        Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
        compareNames(existing, task.getRollbackSaves().getPages());
    }

    public void testMerge_NewPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("foo", "bar", "baz");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
        }

        task.importData(ImportMode.MERGE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());

            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());
        assertNullOrEmpty(task.getRollbackSaves());
    }

    public void testMerge_NewAndSamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page2", "bar", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
            when(pageService.loadPage(siteKey.page(name))).thenReturn(new PageContextBuilder().page(name));
            when(dataStorage.getPage(siteKey.page(name).format())).thenReturn(
                    new Page(siteKey.getTypeName(), siteKey.getName(), name));
        }

        task.importData(ImportMode.MERGE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            verify(pages).getChild(page.getName());
            if (page.getName().equals("page2") || page.getName().equals("page3")) {
                verify(pageService).loadPage(siteKey.page(page.getName()));
                verify(dataStorage).getPage(siteKey.page(page.getName()).format());
            }
            if (!page.getName().equals("bar")) {
                PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
                verify(pageService).savePage(argThat(matches(pageContext)));
                verify(dataStorage).save(page);
            }
        }
        verify(dataStorage, times(4)).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
        Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
        Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

        Assert.assertNotNull(task.getRollbackSaves());
        Assert.assertEquals(2, task.getRollbackSaves().getPages().size());
        compareNames(Arrays.asList(existing.get(0), existing.get(2)), task.getRollbackSaves().getPages());
    }

    public void testOverwrite_NoPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> emptyList());

        task.importData(ImportMode.OVERWRITE);

        verify(pages).getChildren();

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }

        verify(dataStorage, times(importing.getPages().size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing, task.getRollbackDeletes());
        Assert.assertNull(task.getRollbackSaves());
    }

    public void testOverwrite_SamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page1", "page2", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        when(pageService.loadPages(siteKey)).thenReturn(new PageContextBuilder().pages(existing));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
            when(dataStorage.getPage(siteKey.page(name).format())).thenReturn(
                    new Page(siteKey.getTypeName(), siteKey.getName(), name));
        }

        task.importData(ImportMode.OVERWRITE);

        verify(pages).getChildren();
        verify(pageService).loadPages(siteKey);

        for (String name : existing) {
            verify(dataStorage).getPage(siteKey.page(name).format());
            verify(pageService).destroyPage(siteKey.page(name));
        }

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }
        verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        assertNullOrEmpty(task.getRollbackDeletes());
        Assert.assertNotNull(task.getRollbackSaves());
        Assert.assertEquals(3, task.getRollbackSaves().getPages().size());
        compareNames(existing, task.getRollbackSaves().getPages());
    }

    public void testOverwrite_NewPages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("foo", "bar", "baz");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        when(pageService.loadPages(siteKey)).thenReturn(new PageContextBuilder().pages(existing));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
            when(dataStorage.getPage(siteKey.page(name).format())).thenReturn(
                    new Page(siteKey.getTypeName(), siteKey.getName(), name));
        }

        task.importData(ImportMode.OVERWRITE);

        verify(pages).getChildren();
        verify(pageService).loadPages(siteKey);

        for (String name : existing) {
            verify(dataStorage).getPage(siteKey.page(name).format());
            verify(pageService).destroyPage(siteKey.page(name));
        }

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }
        verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(importing.getPages(), task.getRollbackDeletes().getPages());

        Assert.assertNotNull(task.getRollbackSaves());
        compareNames(existing, task.getRollbackSaves().getPages());
    }

    public void testOverwrite_NewAndSamePages() throws Exception {
        Page.PageSet importing = new Builder().addPage("page1").addPage("page2").addPage("page3").addPage("page4").build();
        PageImportTask task = new PageImportTask(importing, siteKey, dataStorage, pageService, siteProvider);

        List<String> existing = pages("page2", "bar", "page3");
        when(site.getRootPage()).thenReturn(rootPage);
        when(rootPage.getChild("pages")).thenReturn(pages);
        when(pages.getChildren()).thenReturn(Collections.<org.gatein.mop.api.workspace.Page> singleton(mockPage));
        when(pageService.loadPages(siteKey)).thenReturn(new PageContextBuilder().pages(existing));
        for (String name : existing) {
            when(pages.getChild(name)).thenReturn(mockPage);
            when(dataStorage.getPage(siteKey.page(name).format())).thenReturn(
                    new Page(siteKey.getTypeName(), siteKey.getName(), name));
        }

        task.importData(ImportMode.OVERWRITE);

        verify(pages).getChildren();
        verify(pageService).loadPages(siteKey);

        for (String name : existing) {
            verify(dataStorage).getPage(siteKey.page(name).format());
            verify(pageService).destroyPage(siteKey.page(name));
        }

        for (Page page : importing.getPages()) {
            PageContext pageContext = new PageContext(siteKey.page(page.getName()), PageUtils.toPageState(page));
            verify(pageService).savePage(argThat(matches(pageContext)));
            verify(dataStorage).save(page);
        }
        verify(dataStorage, times(importing.getPages().size() + existing.size())).save();

        verifyNoMoreInteractions(dataStorage, pageService, pages);

        Assert.assertNotNull(task.getRollbackDeletes());
        Assert.assertEquals(2, task.getRollbackDeletes().getPages().size());
        Assert.assertEquals(importing.getPages().get(0), task.getRollbackDeletes().getPages().get(0));
        Assert.assertEquals(importing.getPages().get(3), task.getRollbackDeletes().getPages().get(1));

        Assert.assertNotNull(task.getRollbackSaves());
        compareNames(existing, task.getRollbackSaves().getPages());
    }

    private void assertNullOrEmpty(Page.PageSet pages) {
        if (pages != null) {
            Assert.assertTrue(pages.getPages().isEmpty());
        }
    }

    private void compareNames(List<String> expected, ArrayList<Page> actual) {
        if (expected == null) {
            assertNull(actual);
            return;
        }

        assertEquals(expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            compareName(expected.get(i), actual.get(i));
        }
    }

    private void compareName(String name, Page actual) {
        assertEquals(name, actual.getName());
    }

    private List<String> pages(String... names) {
        return Arrays.asList(names);
    }

    private static ArgumentMatcher<PageContext> matches(PageContext context) {
        return new PageContextMatcher(context);
    }

    private static class PageContextMatcher extends ArgumentMatcher<PageContext> {
        private final PageContext pageContext;

        public PageContextMatcher(PageContext pageContext) {
            this.pageContext = pageContext;
        }

        @Override
        public boolean matches(Object o) {
            if (pageContext == o)
                return true;
            if (!(o instanceof PageContext))
                return false;

            PageContext that = (PageContext) o;

            return that.getKey().equals(pageContext.getKey()) && that.getState().equals(pageContext.getState());

        }
    }

    private static class Builder {
        private Page.PageSet pages;

        public Builder() {
            pages = new Page.PageSet();
            pages.setPages(new ArrayList<Page>());
        }

        public Builder addPage(String name) {
            PageData page = new PageData(null, "", name, null, null, null, null, null, null, null,
                    Collections.<String> emptyList(), Collections.<ComponentData> emptyList(), "", "", Collections.<String>emptyList(), false);
            pages.getPages().add(new Page(page));

            return this;
        }

        public Page.PageSet build() {
            return pages;
        }
    }

    private class PageContextBuilder {
        PageContext page(String name) {
            return new PageContext(siteKey.page(name), new PageState(null, null, false, null, Collections.<String> emptyList(),
                    Collections.<String>emptyList()));
        }

        List<PageContext> pages(Iterable<String> names) {
            List<PageContext> pages = new ArrayList<PageContext>();
            for (String name : names) {
                pages.add(page(name));
            }

            return pages;
        }

        List<PageContext> pages(String... names) {
            return pages(Arrays.asList(names));
        }
    }
}
