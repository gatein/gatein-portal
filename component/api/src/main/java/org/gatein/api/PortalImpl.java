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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageError;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageServiceException;
import org.exoplatform.portal.mop.page.PageServiceImpl;
import org.exoplatform.portal.mop.page.PageServiceWrapper;
import org.exoplatform.portal.mop.page.PageState;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.gatein.api.common.Filter;
import org.gatein.api.common.Pagination;
import org.gatein.api.internal.Parameters;
import org.gatein.api.internal.StringJoiner;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.NavigationImpl;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageImpl;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteImpl;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalImpl implements Portal {
    private static final Query<PortalConfig> SITES = new Query<PortalConfig>(
        org.exoplatform.portal.mop.SiteType.PORTAL.getName(), null, PortalConfig.class);
    private static final Query<PortalConfig> SPACES = new Query<PortalConfig>(
        org.exoplatform.portal.mop.SiteType.GROUP.getName(), null, PortalConfig.class);
    private static final Query<PortalConfig> DASHBOARDS = new Query<PortalConfig>(
        org.exoplatform.portal.mop.SiteType.USER.getName(), null, PortalConfig.class);

    static final Logger log = LoggerFactory.getLogger("org.gatein.api");

    private final DataStorage dataStorage;
    private final PageService pageService;
    private final NavigationService navigationService;
    private final DescriptionService descriptionService;
    private final ResourceBundleManager bundleManager;
    private final UserACL acl;
    private final Authenticator authenticator;
    private final IdentityRegistry identityRegistry;
    private final UserPortalConfigService userPortalConfigService;

    public PortalImpl(DataStorage dataStorage, PageService pageService, NavigationService navigationService,
                      DescriptionService descriptionService, ResourceBundleManager bundleManager, Authenticator authenticator,
                      IdentityRegistry identityRegistry, UserACL acl, UserPortalConfigService userPortalConfigService) {
        this.dataStorage = dataStorage;
        this.pageService = pageService;
        this.navigationService = navigationService;
        this.descriptionService = descriptionService;
        this.bundleManager = bundleManager;
        this.authenticator = authenticator;
        this.identityRegistry = identityRegistry;
        this.acl = acl;
        this.userPortalConfigService = userPortalConfigService;
    }

    @Override
    public Site getSite(SiteId siteId) {
        Parameters.requireNonNull(siteId, "siteId");
        SiteKey siteKey = Util.from(siteId);

        try {
            PortalConfig portalConfig = dataStorage.getPortalConfig(siteKey.getTypeName(), siteKey.getName());
            return (portalConfig == null) ? null : new SiteImpl(portalConfig);
        } catch (Throwable e) {
            throw new ApiException("Failed to get site", e);
        }
    }

    @Override
    public Site createSite(SiteId siteId) {
        String template = userPortalConfigService.getDefaultPortalTemplate();
        if (template == null) {
            template = ""; // This is valid if we're being executed within a test environment
        }
        return createSite(siteId, template);
    }

    @Override
    public Site createSite(SiteId siteId, String templateName) throws IllegalArgumentException, EntityAlreadyExistsException {
        if (getSite(siteId) != null) {
            throw new EntityAlreadyExistsException("Cannot create site. Site " + siteId + " already exists.");
        }
        Parameters.requireNonNull(templateName, "templateName");

        // Create new site
        return new SiteImpl(siteId, templateName);
    }

    @Override
    public List<Site> findSites(SiteQuery query) {
        Parameters.requireNonNull(query, "query");

        Pagination pagination = query.getPagination();
        if (pagination != null && query.getSiteTypes().size() > 1) {
            pagination = null; // set it to null so the internal DataStorage doesn't use it, and we manually page later
            log.warn("Pagination is not supported internally for SiteQuery's with multiple site types. Therefore this query has the possibility to perform poorly.");
        }

        List<Site> sites = new ArrayList<Site>();
        for (SiteType type : query.getSiteTypes()) {
            List<PortalConfig> internalSites;
            switch (type) {
                case SITE:
                    internalSites = findSites(pagination, SITES, Comparators.site(query.getSorting()));
                    break;
                case SPACE:
                    internalSites = findSites(pagination, SPACES, Comparators.site(query.getSorting()));
                    break;
                case DASHBOARD:
                    internalSites = findSites(pagination, DASHBOARDS, Comparators.site(query.getSorting()));
                    break;
                default:
                    throw new AssertionError();
            }

            sites.addAll(fromList(internalSites, navigationService, query.isIncludeEmptySites()));
        }

        filter(sites, query.getFilter());

        // Manually do paging for multiple site types.
        if (query.getSiteTypes().size() > 1) {
            sites = paginate(sites, query.getPagination());
        }

        return sites;
    }

    private <T> List<T> findSites(Pagination pagination, Query<T> query, Comparator<T> comparator) {
        try {
            if (pagination != null) {
                ListAccess<T> access = dataStorage.find2(query, comparator);
                int size = access.getSize();
                int offset = pagination.getOffset();
                int limit = pagination.getLimit();
                if (offset >= size) {
                    return Collections.emptyList();
                } else if (offset + limit > size) {
                    return Arrays.asList(access.load(offset, size - offset));
                } else {
                    return Arrays.asList(access.load(offset, limit));
                }
            } else {
                return dataStorage.find(query, comparator).getAll();
            }
        } catch (Throwable e) {
            throw new ApiException("Failed to query for sites", e);
        }
    }

    @Override
    public void saveSite(Site site) {
        Parameters.requireNonNull(site, "site");
        ((SiteImpl) site).save(dataStorage, userPortalConfigService);
    }

    @Override
    public boolean removeSite(SiteId siteId) {
        Site site = getSite(siteId);
        if (site == null) {
            return false;
        }
        SiteKey siteKey = Util.from(Parameters.requireNonNull(siteId, "siteId"));
        PortalConfig data = new PortalConfig(siteKey.getTypeName(), siteKey.getName());
        try {
            dataStorage.remove(data);
            return true;
        } catch (Throwable t) {
            throw new ApiException("Failed to remove site " + siteId, t);
        }
    }

    @Override
    public Navigation getNavigation(SiteId siteId) {
        Parameters.requireNonNull(siteId, "siteId");

        try {
            NavigationContext ctx = navigationService.loadNavigation(Util.from(siteId));
            if (ctx == null) return null;

            return new NavigationImpl(siteId, navigationService, ctx, descriptionService, bundleManager);
        } catch (Throwable t) {
            throw new ApiException("Failed to load navigation", t);
        }
    }

    @Override
    public Page getPage(PageId pageId) {
        Parameters.requireNonNull(pageId, "pageId");

        try {
            PageContext context = pageService.loadPage(Util.from(pageId));
            return (context == null) ? null : new PageImpl(context);
        } catch (Throwable e) {
            throw new ApiException("Failed to get page", e);
        }
    }

    @Override
    public Page createPage(PageId pageId) throws EntityAlreadyExistsException {
        if (getPage(pageId) != null) {
            throw new EntityAlreadyExistsException("Cannot create page. Page " + pageId + " already exists.");
        }

        if (getSite(pageId.getSiteId()) == null) {
            throw new EntityNotFoundException("Site " + pageId.getSiteId() + " doesn't exist");
        }

        Permission access = Permission.everyone();
        Permission edit = Permission.any("platform", "administrators");
        PageState pageState = new PageState(pageId.getPageName(), null, false, null, Arrays.asList(Util.from(access)),
            Util.from(edit)[0]);

        PageImpl p = new PageImpl(new PageContext(Util.from(pageId), pageState));
        p.setCreate(true);
        return p;
    }

    @Override
    public List<Page> findPages(PageQuery query) {
        Pagination pagination = query.getPagination();
        Iterator<PageContext> iterator;
        if (pagination == null) {
            if (query.getSiteType() == null || query.getSiteName() == null)
                throw new IllegalArgumentException("Pagination is required when site type or site name is null.");

            SiteKey siteKey = Util.from(new SiteId(query.getSiteType(), query.getSiteName()));
            if (pageService instanceof PageServiceImpl) {
                iterator = ((PageServiceImpl) pageService).loadPages(siteKey).iterator();
            } else if (pageService instanceof PageServiceWrapper) {
                iterator = ((PageServiceWrapper) pageService).loadPages(siteKey).iterator();
            } else {
                throw new RuntimeException("Unable to retrieve all pages for " + siteKey);
            }
        } else {
            QueryResult<PageContext> result = pageService.findPages(pagination.getOffset(), pagination.getLimit(),
                Util.from(query.getSiteType()), query.getSiteName(), null, query.getDisplayName());

            iterator = result.iterator();
        }

        List<Page> pages = new ArrayList<Page>();
        while (iterator.hasNext()) {
            pages.add(new PageImpl(iterator.next()));
        }

        filter(pages, query.getFilter());

        return pages;
    }

    @Override
    public void savePage(Page page) {
        Parameters.requireNonNull(page, "page");

        if (getSite(page.getSiteId()) == null) {
            throw new EntityNotFoundException("Site " + page.getSiteId() + " doesn't exist");
        }

        if (((PageImpl) page).isCreate() && getPage(page.getId()) != null) {
            // There is still a small chance someone else creates the page, but this is currently the best we can do
            throw new EntityAlreadyExistsException("Cannot create page. Page " + page.getId() + " already exists.");
        }

        PageContext context = ((PageImpl) page).getPageContext();

        try {
            pageService.savePage(context);
        } catch (Throwable t) {
            throw new ApiException("Failed to save page " + page.getId(), t);
        }
    }

    @Override
    public boolean removePage(PageId pageId) {
        Parameters.requireNonNull(pageId, "pageId");

        try {
            return pageService.destroyPage(Util.from(pageId));
        } catch (PageServiceException e) {
            if (e.getError() == PageError.NO_SITE) {
                throw new EntityNotFoundException("Cannot remove page '" + pageId.getPageName() + "'. Site " + pageId.getSiteId() + " does not exist.");
            } else {
                throw new ApiException("Failed to remove page " + pageId, e);
            }
        } catch (Throwable t) {
            throw new ApiException("Failed to remove page " + pageId, t);
        }
    }

    @Override
    public boolean hasPermission(User user, Permission permission) {
        String expPerm = StringJoiner.joiner(",").join(Util.from(permission));

        Identity identity;
        if (user == User.anonymous()) {
            identity = new Identity(IdentityConstants.ANONIM);
        } else {
            try {
                identity = identityRegistry.getIdentity(user.getId());
            } catch (Throwable t) {
                throw new ApiException("Failed top retrieve identity", t);
            }
        }

        if (identity == null) {
            try {
                identity = authenticator.createIdentity(user.getId());
            } catch (Throwable t) {
                throw new ApiException("Failed to retrieve user identity", t);
            }

            if (identity == null) {
                throw new EntityNotFoundException("User not found");
            }

            try {
                identityRegistry.register(identity);
            } catch (Throwable t) {
                throw new ApiException("Failed to register identity", t);
            }
        }

        try {
            return acl.hasPermission(identity, expPerm);
        } catch (Throwable t) {
            throw new ApiException("Failed to check permissions", t);
        }
    }

    private static <T> void filter(List<T> list, Filter<T> filter) {
        if (filter == null)
            return;

        for (Iterator<T> iterator = list.iterator(); iterator.hasNext(); ) {
            if (!filter.accept(iterator.next())) {
                iterator.remove();
            }
        }
    }

    private static <T> List<T> paginate(List<T> list, Pagination pagination) {
        if (pagination == null)
            return list;
        if (pagination.getOffset() >= list.size())
            return Collections.emptyList();

        if (pagination.getOffset() + pagination.getLimit() > list.size()) {
            return new ArrayList<T>(list.subList(pagination.getOffset(), list.size()));
        } else {
            return new ArrayList<T>(list.subList(pagination.getOffset(), pagination.getOffset() + pagination.getLimit()));
        }
    }

    private static List<Site> fromList(List<PortalConfig> internalSites, NavigationService service, boolean includeAllSites) {
        List<Site> sites = new ArrayList<Site>(internalSites.size());
        for (PortalConfig internalSite : internalSites) {
            NavigationContext ctx = null;
            if (!includeAllSites) {
                try {
                    ctx = service.loadNavigation(new SiteKey(internalSite.getType(), internalSite.getName()));
                } catch (Throwable t) {
                    throw new ApiException("Failed to find sites", t);
                }
            }

            if (includeAllSites || ctx != null) {
                sites.add(new SiteImpl(internalSite));
            }
        }
        return sites;
    }
}
