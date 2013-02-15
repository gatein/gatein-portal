/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.api;

import static org.gatein.api.Assert.assertEquals;
import static org.gatein.api.Assert.assertFalse;
import static org.gatein.api.Assert.assertNotNull;
import static org.gatein.api.Assert.assertNull;
import static org.gatein.api.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.exoplatform.container.component.RequestLifeCycle;
import org.gatein.api.common.Attributes;
import org.gatein.api.common.Filter;
import org.gatein.api.common.Pagination;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Group;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.junit.Test;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalImplTest extends AbstractApiTest {

    @Test
    public void createPage() {
        createSite(new SiteId("create-page"));

        Page page = portal.createPage(new PageId("create-page", "baz"));
        assertNotNull(page);
        assertNull(portal.getPage(new PageId("create-page", "baz")));
    }

    @Test(expected = ApiException.class)
    public void createPage_Faulty() {
        createSite(new SiteId("create-page-exists"), "bar");

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.createPage(new PageId("create-page-exists", "bar"));
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPage_NullPageId() {
        portal.createPage(null);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void createPage_PageExists() {
        createSite(new SiteId("create-page-exists"), "bar");
        portal.createPage(new PageId("create-page-exists", "bar"));
    }

    @Test(expected = EntityNotFoundException.class)
    public void createPage_NoSite() {
        portal.createPage(new PageId("no-site", "baz"));
    }

    @Test
    public void createSite() {
        portal.createSite(new SiteId("newsite"), "basic");
        assertNull(portal.getSite(new SiteId("newsite")));
    }

    @Test(expected = ApiException.class)
    public void createSite_Faulty() {
        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.createSite(new SiteId("newsite"), "basic");
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void createSite_NullSiteId() {
        portal.createSite(null, "basic");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createSite_NullTemplateName() {
        portal.createSite(new SiteId("newsite"), null);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void createSite_SiteExists() {
        saveSite();
        portal.createSite(new SiteId("newsite"), "basic");
    }

    @Test
    public void findPages() {
        createSite(new SiteId("find-pages"), "page3", "page1", "page5", "page2", "page4");

        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(new SiteId("find-pages")).build());
        assertNotNull(pages);
        assertEquals(5, pages.size());
    }

    @Test
    public void findPages_Filter() {
        createSite(new SiteId("find-pages"), "page3", "page1", "page5", "page2", "page4");

        Filter<Page> filter = new Filter<Page>() {
            public boolean accept(Page element) {
                return element.getName().equals("page1") || element.getName().equals("page4");
            }
        };

        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(new SiteId("find-pages")).withFilter(filter)
                .build());
        assertNotNull(pages);
        assertEquals(2, pages.size());
    }

    @Test
    public void findPages_BySiteName() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteName("find-pages").build();
        List<Page> pages = portal.findPages(query);
        assertEquals(6, pages.size());
    }

    @Test
    public void findPages_BySiteType() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteType(SiteType.SITE).build();
        List<Page> pages = portal.findPages(query);
        assertEquals(2, pages.size());

        query = new PageQuery.Builder().withSiteType(SiteType.SPACE).build();
        pages = portal.findPages(query);
        assertEquals(1, pages.size());

        query = new PageQuery.Builder().withSiteType(SiteType.DASHBOARD).build();
        pages = portal.findPages(query);
        assertEquals(3, pages.size());
    }

    @Test
    public void findPages_BySiteType_And_SiteName() {
        createSite(new SiteId("find-pages"), "page1", "page2");
        createSite(new SiteId(new Group("find-pages")), "find-pages", "page3");
        createSite(new SiteId(new User("find-pages")), "page4", "page5", "page6");

        PageQuery query = new PageQuery.Builder().withSiteType(SiteType.DASHBOARD).withSiteName("find-pages").build();
        List<Page> pages = portal.findPages(query);
        assertEquals(3, pages.size());
    }

    @Test
    public void findPages_ByTitle() {
        createSite(new SiteId("find-pages"), "page3", "page1", "page5", "page2", "page4");
        Page page = portal.getPage(new PageId("find-pages", "page1"));
        page.setDisplayName("FooTitle");
        portal.savePage(page);

        page = portal.getPage(new PageId("find-pages", "page4"));
        page.setDisplayName("FooTitle");
        portal.savePage(page);

        List<Page> pages = portal.findPages(new PageQuery.Builder().withSiteId(new SiteId("find-pages"))
                .withDisplayName("FooTitle").build());
        assertEquals(2, pages.size());
        for (Page p : pages) {
            assertEquals("FooTitle", p.getDisplayName());
        }
    }

    @Test
    public void findPages_Pagination() {
        createSite(new SiteId("find-pages"), "page1", "page2", "page3", "page4", "page5", "page6", "page7");

        PageQuery query = new PageQuery.Builder().withSiteId(new SiteId("find-pages")).withPagination(0, 5).build();
        List<Page> pages = portal.findPages(query);
        assertEquals(5, pages.size());

        pages = portal.findPages(query.nextPage());
        assertEquals(2, pages.size());
    }

    @Test
    public void findSites() {
        createSite(defaultSiteId);

        List<Site> sites = portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());

        assertNotNull(sites);
        assertEquals(1, sites.size());
        assertEquals("classic", sites.get(0).getId().getName());
    }

    @Test
    public void findSites_Ascending() {
        createSite(new SiteId("z"));
        createSite(new SiteId("a"));
        createSite(new SiteId("f"));
        createSite(new SiteId("b"));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).ascending().build());
        assertEquals(4, sites.size());
        assertEquals("a", sites.get(0).getId().getName());
        assertEquals("b", sites.get(1).getId().getName());
        assertEquals("f", sites.get(2).getId().getName());
        assertEquals("z", sites.get(3).getId().getName());
    }

    @Test(expected = ApiException.class)
    public void findSites_Faulty() {
        createSite(defaultSiteId);

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());
            }
        });
    }

    @Test
    public void findSites_Filtered() {
        createSite(new SiteId("c"));
        createSite(new SiteId("a"));
        createSite(new SiteId("d"));
        createSite(new SiteId("b"));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withFilter(new Filter<Site>() {
            @Override
            public boolean accept(Site site) {
                return site.getName().equals("a") || site.getName().equals("b");
            }
        }).build());

        Iterator<Site> iter = sites.iterator();
        assertEquals(2, sites.size());
        Site site = iter.next();
        assertEquals("a", site.getId().getName());
        site = iter.next();
        assertEquals("b", site.getId().getName());
    }

    @Test
    public void findSites_NaturalOrdering() {
        createSite(new SiteId("z"));
        createSite(new SiteId("a"));
        createSite(new SiteId("f"));
        createSite(new SiteId("b"));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).build());
        assertEquals(4, sites.size());
        assertEquals("z", sites.get(0).getId().getName());
        assertEquals("a", sites.get(1).getId().getName());
        assertEquals("f", sites.get(2).getId().getName());
        assertEquals("b", sites.get(3).getId().getName());
    }

    @Test
    public void findSites_NonHidden() {
        createSite(new SiteId("test-site"), false);

        List<Site> sites = portal.findSites(new SiteQuery.Builder().withSiteTypes(SiteType.SITE).build());
        assertTrue(sites.isEmpty());
    }

    @Test
    public void findSites_NoResults() {
        List<Site> sites = portal.findSites(new SiteQuery.Builder().withAllSiteTypes().build());

        assertNotNull(sites);
        assertEquals(0, sites.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findSites_NullQuery() {
        portal.findSites(null);
    }

    @Test
    public void findSites_Paged() {
        for (int i = 0; i < 10; i++) {
            createSite(new SiteId("site" + (i + 1)));
        }

        SiteQuery query = new SiteQuery.Builder().includeEmptySites(true).withPagination(0, 5).build();
        List<Site> sites = portal.findSites(query);
        assertEquals(5, sites.size());
        // check bounds
        assertEquals("site1", sites.get(0).getName());
        assertEquals("site5", sites.get(4).getName());

        query = query.nextPage();
        sites = portal.findSites(query);
        assertEquals(5, sites.size());
        // check bounds
        assertEquals("site6", sites.get(0).getName());
        assertEquals("site10", sites.get(4).getName());

        query = query.nextPage();
        sites = portal.findSites(query);
        assertEquals(0, sites.size());

        query = query.previousPage();
        sites = portal.findSites(query);
        assertEquals(5, sites.size());

        query = new SiteQuery.Builder().from(query).withPagination(2, 5).withNextPage().build();
        sites = portal.findSites(query);
        assertEquals(3, sites.size());
        assertEquals("site8", sites.get(0).getName());
        assertEquals("site10", sites.get(2).getName());
    }

    @Test
    public void findSites_PagedWithMultipleSiteTypes() {
        // Add more sites and check
        createSite(new SiteId("site1"));
        createSite(new SiteId("site2"));
        createSite(new SiteId("site3"));
        createSite(new SiteId("site4"));

        createSite(new SiteId(new Group("/platform/users")));
        createSite(new SiteId(new Group("/foo/bar")));
        createSite(new SiteId(new Group("blah")));

        createSite(new SiteId(new User("root")));
        createSite(new SiteId(new User("john")));
        createSite(new SiteId(new User("mary")));

        List<Site> sites = portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withAllSiteTypes().build());
        assertEquals(10, sites.size());

        // Range

        assertEquals(10, portal.findSites(new SiteQuery.Builder().withAllSiteTypes().withPagination(0, 10).build()).size());
        assertEquals(5, portal.findSites(new SiteQuery.Builder().withAllSiteTypes().withPagination(0, 5).build()).size());

        Pagination pagination = new Pagination(0, 3);
        SiteQuery query = new SiteQuery.Builder().withAllSiteTypes().withPagination(pagination).build();
        assertEquals(3, portal.findSites(query).size());

        query = query.nextPage();
        assertEquals(3, portal.findSites(query).size());

        query = query.nextPage();
        assertEquals(3, portal.findSites(query).size());

        query = query.nextPage();
        assertEquals(1, portal.findSites(query).size());

        query = query.nextPage();
        assertEquals(0, portal.findSites(query).size());

        query = query.previousPage();
        assertEquals(1, portal.findSites(query).size());

        query = query.previousPage();
        assertEquals(3, portal.findSites(query).size());

        // By type
        assertEquals(4, portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).build())
                .size());
        assertEquals(3, portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).build())
                .size());
        assertEquals(3,
                portal.findSites(new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).build())
                        .size());

        // By type and range
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).withPagination(0, 2)
                                .build()).size());
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).withPagination(0, 2)
                                .build()).size());
        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2)
                                .build()).size());

        assertEquals(
                2,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SITE).withPagination(0, 2)
                                .withNextPage().build()).size());
        assertEquals(
                1,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.SPACE).withPagination(0, 2)
                                .withNextPage().build()).size());
        assertEquals(
                1,
                portal.findSites(
                        new SiteQuery.Builder().includeEmptySites(true).withSiteTypes(SiteType.DASHBOARD).withPagination(0, 2)
                                .withNextPage().build()).size());
    }

    @Test
    public void getPage() {
        createSite(new SiteId("get-page"), "bar");

        assertNotNull(portal.getPage(new PageId("get-page", "bar")));
    }

    @Test(expected = ApiException.class)
    public void getPage_Faulty() {
        createSite(new SiteId("get-page"), "bar");

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.getPage(new PageId("get-page", "bar"));
            }
        });
    }

    @Test
    public void getPage_NonExisting() {
        assertNull(portal.getPage(new PageId("get-page", "blah")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPage_Null() {
        portal.getPage(null);
    }

    @Test
    public void getSite() {
        createSite(defaultSiteId);

        assertNotNull(portal.getSite(new SiteId("classic")));
    }

    @Test(expected = ApiException.class)
    public void getSite_Faulty() {
        createSite(defaultSiteId);

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.getSite(new SiteId("classic"));
            }
        });
    }

    @Test
    public void getSite_Group() {
        createSite(new SiteId(new Group("/platform/something")));

        Site space = portal.getSite(new SiteId(new Group("platform", "something")));
        assertNotNull(space);
    }

    @Test
    public void getSite_NonExisting() {
        assertNull(portal.getSite(new SiteId("nosuch")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSite_NullSiteId() {
        portal.getSite(null);
    }

    @Test
    public void getSite_User() {
        createSite(new SiteId(new User("user10")));
        Site dashboard = portal.getSite(new SiteId(new User("user10")));
        assertNotNull(dashboard);
    }

    @Test
    public void hasPermission() {
        createSite(new SiteId("permissions"));

        Page page = portal.createPage(new PageId("permissions", "page1"));
        page.setAccessPermission(Permission.everyone());
        page.setEditPermission(new Permission("*", new Group("/platform/administrators")));
        portal.savePage(page);

        page = portal.getPage(page.getId());

        assertTrue(portal.hasPermission(new User("root"), page.getAccessPermission()));
        assertTrue(portal.hasPermission(new User("root"), page.getEditPermission()));

        assertTrue(portal.hasPermission(User.anonymous(), page.getAccessPermission()));
        assertFalse(portal.hasPermission(User.anonymous(), page.getEditPermission()));
    }

    @Test
    public void hasPermission_User() {
        createSite(new SiteId("permissions"));

        Page page = portal.createPage(new PageId("permissions", "page1"));
        page.setAccessPermission(new Permission(new User("root")));
        portal.savePage(page);

        assertTrue(portal.hasPermission(new User("root"), page.getAccessPermission()));
        assertFalse(portal.hasPermission(new User("john"), page.getAccessPermission()));
    }

    @Test
    public void removePage() {
        createSite(new SiteId("removePage"), "page1", "page2");

        assertNotNull(portal.getPage(new PageId("removePage", "page1")));
        assertNotNull(portal.getPage(new PageId("removePage", "page2")));

        assertTrue(portal.removePage(new PageId("removePage", "page1")));

        assertNull(portal.getPage(new PageId("removePage", "page1")));
        assertNotNull(portal.getPage(new PageId("removePage", "page2")));

        assertTrue(portal.removePage(new PageId("removePage", "page2")));

        assertNull(portal.getPage(new PageId("removePage", "page1")));
        assertNull(portal.getPage(new PageId("removePage", "page2")));
    }

    @Test(expected = ApiException.class)
    public void removePage_Faulty() {
        createSite(new SiteId("test1"), "page1");

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.removePage(new PageId("removePage", "page1"));
            }
        });
    }

    @Test
    public void removePage_NonExisting() {
        createSite(new SiteId("test1"), "page1");

        assertFalse(portal.removePage(new PageId("test1", "page2")));
    }

    @Test(expected = EntityNotFoundException.class)
    public void removePage_SiteNonExisting() {
        createSite(new SiteId("test1"), "page1");

        assertFalse(portal.removePage(new PageId("test2", "page1")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePage_NullSiteId() {
        portal.removePage(null);
    }

    @Test
    public void removeSite() {
        createSite(new SiteId("test1"));
        createSite(new SiteId("test2"));
        createSite(new SiteId("test3"));

        assertNotNull(portal.getSite(new SiteId("test1")));
        assertNotNull(portal.getSite(new SiteId("test2")));
        assertNotNull(portal.getSite(new SiteId("test3")));

        assertTrue(portal.removeSite(new SiteId("test1")));

        assertNull(portal.getSite((new SiteId("test1"))));
        assertNotNull(portal.getSite(new SiteId("test2")));
        assertNotNull(portal.getSite(new SiteId("test3")));

        assertTrue(portal.removeSite(new SiteId(SiteType.SITE, "test2")));

        assertNull(portal.getSite(new SiteId("te")));
        assertNull(portal.getSite(new SiteId("test2")));
        assertNotNull(portal.getSite(new SiteId("test3")));
    }

    @Test(expected = ApiException.class)
    public void removeSite_Faulty() {
        createSite(new SiteId("test1"));

        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.removeSite(new SiteId("test1"));
            }
        });
    }

    @Test
    public void removeSite_NonExisting() {
        assertFalse(portal.removeSite(new SiteId("test1")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeSite_NullSiteId() {
        portal.removeSite(null);
    }

    @Test
    public void savePage() {
        createSite(new SiteId("create-page"));

        Permission access = new Permission("*", new Group("access"));
        Permission edit = new Permission("*", new Group("edit"));

        Page page = portal.createPage(new PageId("create-page", "baz"));
        page.setAccessPermission(access);
        page.setDescription("description");
        page.setDisplayName("displayName");
        page.setEditPermission(edit);

        portal.savePage(page);

        page = portal.getPage(new PageId("create-page", "baz"));
        assertNotNull(page);
        assertEquals(access, page.getAccessPermission());
        assertEquals("description", page.getDescription());
        assertEquals("displayName", page.getDisplayName());
        assertEquals(edit, page.getEditPermission());
        assertEquals(new PageId("create-page", "baz"), page.getId());
        assertEquals("baz", page.getName());
        assertEquals(new SiteId("create-page"), page.getSiteId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void savePage_NoSite() {
        Site site = portal.createSite(new SiteId("save-page"), "basic");
        portal.saveSite(site);

        Page page = portal.createPage(new PageId("no-site", "baz"));

        portal.removeSite(site.getId());

        portal.savePage(page);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void savePage_Exists() {
        createSite(new SiteId("create-page"));

        Page pageA = portal.createPage(new PageId("create-page", "baz"));
        Page pageB = portal.createPage(new PageId("create-page", "baz"));

        portal.savePage(pageA);
        portal.savePage(pageB);
    }

    @Test(expected = ApiException.class)
    public void savePage_Faulty() {
        createSite(new SiteId("create-page"));

        final Page page = portal.createPage(new PageId("create-page", "baz"));
        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.savePage(page);
            }
        });
    }

    @Test
    public void savePage_Modify() {
        savePage();

        Permission access = new Permission("*", new Group("newAccess"));
        Permission edit = new Permission("*", new Group("newEdit"));

        Page page = portal.getPage(new PageId("create-page", "baz"));
        page.setAccessPermission(access);
        page.setDescription("newDescription");
        page.setDisplayName("newDisplayName");
        page.setEditPermission(edit);

        portal.savePage(page);

        page = portal.getPage(new PageId("create-page", "baz"));
        assertNotNull(page);
        assertEquals(access, page.getAccessPermission());
        assertEquals("newDescription", page.getDescription());
        assertEquals("newDisplayName", page.getDisplayName());
        assertEquals(edit, page.getEditPermission());
        assertEquals(new PageId("create-page", "baz"), page.getId());
        assertEquals("baz", page.getName());
        assertEquals(new SiteId("create-page"), page.getSiteId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void savePage_Null() {
        portal.savePage(null);
    }

    @Test
    public void saveSite() {
        Permission access = new Permission("*", new Group("access"));
        Permission edit = new Permission("*", new Group("edit"));

        Site site = portal.createSite(new SiteId("newsite"), "basic");
        site.setAccessPermission(access);
        site.setDisplayName("displayName");
        site.setDescription("description");
        site.setEditPermission(edit);
        site.setLocale(Locale.ENGLISH);
        site.setSkin("skin");

        site.getAttributes().put(Attributes.key("attributeKey", String.class), "attributeValue");

        portal.saveSite(site);

        site = portal.getSite(new SiteId("newsite"));
        assertNotNull(site);
        assertEquals("displayName", site.getDisplayName());
        assertEquals("description", site.getDescription());
        assertEquals(access, site.getAccessPermission());
        assertEquals(edit, site.getEditPermission());
        assertEquals(new SiteId("newsite"), site.getId());
        assertEquals(Locale.ENGLISH, site.getLocale());
        assertEquals("newsite", site.getName());
        assertEquals("skin", site.getSkin());
        assertEquals(SiteType.SITE, site.getType());

        assertEquals(1, site.getAttributes().size());
        assertEquals("attributeValue", site.getAttributes().get(Attributes.key("attributeKey", String.class)));

        assertNull(portal.getSite(new SiteId("xxx")));
    }

    @Test(expected = ApiException.class)
    public void saveSite_Exists() {
        Site siteA = portal.createSite(new SiteId("newsite"), "basic");
        Site siteB = portal.createSite(new SiteId("newsite"), "basic");

        portal.saveSite(siteA);
        portal.saveSite(siteB);
    }

    @Test(expected = ApiException.class)
    public void saveSite_Faulty() {
        final Site site = portal.createSite(new SiteId("newsite"), "basic");
        runWithFault(new Runnable() {
            @Override
            public void run() {
                portal.saveSite(site);
            }
        });
    }

    @Test
    public void saveSite_Modify() {
        saveSite();

        Permission access = new Permission("*", new Group("newAccess"));
        Permission edit = new Permission("*", new Group("newEdit"));

        Site site = portal.getSite(new SiteId("newsite"));
        site.setAccessPermission(access);
        site.setDisplayName("newDisplayName");
        site.setDescription("newDescription");
        site.setEditPermission(edit);
        site.setLocale(Locale.SIMPLIFIED_CHINESE);
        site.setSkin("newSkin");

        site.getAttributes().put(Attributes.key("attributeKey", String.class), "newAttributeValue");
        site.getAttributes().put(Attributes.key("attributeKey2", String.class), "attributeValue2");

        portal.saveSite(site);

        site = portal.getSite(new SiteId("newsite"));

        assertNotNull(site);
        assertEquals("newDisplayName", site.getDisplayName());
        assertEquals("newDescription", site.getDescription());
        assertEquals(access, site.getAccessPermission());
        assertEquals(edit, site.getEditPermission());
        assertEquals(new SiteId("newsite"), site.getId());
        assertEquals(Locale.SIMPLIFIED_CHINESE, site.getLocale());
        assertEquals("newsite", site.getName());
        assertEquals("newSkin", site.getSkin());
        assertEquals(SiteType.SITE, site.getType());

        assertEquals(2, site.getAttributes().size());
        assertEquals("newAttributeValue", site.getAttributes().get(Attributes.key("attributeKey", String.class)));
        assertEquals("attributeValue2", site.getAttributes().get(Attributes.key("attributeKey2", String.class)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveSite_Null() {
        portal.saveSite(null);
    }

    @Test
    public void serializePage() throws Exception {
        createSite(new SiteId("classic"));

        Permission access = Permission.any("/platform/administrators");
        Permission edit = Permission.everyone();

        Page page = portal.createPage(new PageId("classic", "page1"));
        page.setAccessPermission(access);
        page.setDescription("description");
        page.setDisplayName("displayName");
        page.setEditPermission(edit);

        Page serialized = SerializationUtils.serializeDeserialize(page);

        assertNotNull(serialized);
        assertEquals(access, serialized.getAccessPermission());
        assertEquals("description", serialized.getDescription());
        assertEquals("displayName", serialized.getDisplayName());
        assertEquals(edit, serialized.getEditPermission());
        assertEquals(new PageId("classic", "page1"), serialized.getId());
        assertEquals("page1", serialized.getName());
        assertEquals(new SiteId("classic"), serialized.getSiteId());

        portal.savePage(page);

        Page saved = portal.getPage(page.getId());

        Page savedSerialized = SerializationUtils.serializeDeserialize(saved);
        assertNotNull(savedSerialized);
        assertEquals(access, savedSerialized.getAccessPermission());
        assertEquals("description", savedSerialized.getDescription());
        assertEquals("displayName", savedSerialized.getDisplayName());
        assertEquals(edit, savedSerialized.getEditPermission());
        assertEquals(new PageId("classic", "page1"), savedSerialized.getId());
        assertEquals("page1", savedSerialized.getName());
        assertEquals(new SiteId("classic"), savedSerialized.getSiteId());
    }

    @Test
    public void serializeSite() throws Exception {
        Permission access = new Permission("*", new Group("access"));
        Permission edit = new Permission("*", new Group("edit"));

        Site site = portal.createSite(new SiteId("newsite"), "basic");
        site.setAccessPermission(access);
        site.setDisplayName("displayName");
        site.setDescription("description");
        site.setEditPermission(edit);
        site.setLocale(Locale.ENGLISH);
        site.setSkin("skin");

        site.getAttributes().put(Attributes.key("attributeKey", String.class), "attributeValue");

        site = SerializationUtils.serializeDeserialize(site);
        assertNotNull(site);
        assertEquals("displayName", site.getDisplayName());
        assertEquals("description", site.getDescription());
        assertEquals(access, site.getAccessPermission());
        assertEquals(edit, site.getEditPermission());
        assertEquals(new SiteId("newsite"), site.getId());
        assertEquals(Locale.ENGLISH, site.getLocale());
        assertEquals("newsite", site.getName());
        assertEquals("skin", site.getSkin());
        assertEquals(SiteType.SITE, site.getType());

        assertEquals(1, site.getAttributes().size());
        assertEquals("attributeValue", site.getAttributes().get(Attributes.key("attributeKey", String.class)));

        portal.saveSite(site);
        site = portal.getSite(new SiteId("newsite"));

        site = SerializationUtils.serializeDeserialize(site);
        assertNotNull(site);
        assertEquals("displayName", site.getDisplayName());
        assertEquals("description", site.getDescription());
        assertEquals(access, site.getAccessPermission());
        assertEquals(edit, site.getEditPermission());
        assertEquals(new SiteId("newsite"), site.getId());
        assertEquals(Locale.ENGLISH, site.getLocale());
        assertEquals("newsite", site.getName());
        assertEquals("skin", site.getSkin());
        assertEquals(SiteType.SITE, site.getType());

        assertEquals(1, site.getAttributes().size());
        assertEquals("attributeValue", site.getAttributes().get(Attributes.key("attributeKey", String.class)));
    }

    private void runWithFault(Runnable r) {
        RequestLifeCycle.end();
        try {
            r.run();
        } finally {
            RequestLifeCycle.begin(container);
        }
    }
}
