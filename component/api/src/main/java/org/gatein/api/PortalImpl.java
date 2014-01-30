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
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
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
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.services.security.IdentityRegistry;
import org.gatein.api.application.Application;
import org.gatein.api.application.ApplicationImpl;
import org.gatein.api.application.ApplicationRegistry;
import org.gatein.api.application.ApplicationRegistryImpl;
import org.gatein.api.common.Filter;
import org.gatein.api.common.Pagination;
import org.gatein.api.internal.Parameters;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.NavigationImpl;
import org.gatein.api.oauth.OAuthProvider;
import org.gatein.api.oauth.OAuthProviderAccessor;
import org.gatein.api.composition.BareContainer;
import org.gatein.api.composition.Container;
import org.gatein.api.composition.ContainerImpl;
import org.gatein.api.composition.ContainerItem;
import org.gatein.api.page.Page;
import org.gatein.api.composition.PageBuilder;
import org.gatein.api.composition.PageBuilderImpl;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageImpl;
import org.gatein.api.page.PageQuery;
import org.gatein.api.security.Membership;
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
    private final OAuthProviderAccessor oauthProviderAccessor;
    private ApplicationRegistry applicationRegistry;

    public PortalImpl(DataStorage dataStorage, PageService pageService, NavigationService navigationService,
                      DescriptionService descriptionService, ResourceBundleManager bundleManager, Authenticator authenticator,
                      IdentityRegistry identityRegistry, UserACL acl, UserPortalConfigService userPortalConfigService,
                      OAuthProviderAccessor oauthProviderAccessor) {
        this.dataStorage = dataStorage;
        this.pageService = pageService;
        this.navigationService = navigationService;
        this.descriptionService = descriptionService;
        this.bundleManager = bundleManager;
        this.authenticator = authenticator;
        this.identityRegistry = identityRegistry;
        this.acl = acl;
        this.userPortalConfigService = userPortalConfigService;
        this.oauthProviderAccessor = oauthProviderAccessor;
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
        boolean includeAll = query.isIncludeEmptySites();

        List<Site> sites = new ArrayList<Site>();
        for (SiteType type : query.getSiteTypes()) {
            List<Site> sitesFound;
            switch (type) {
                case SITE:
                    sitesFound = findSites(pagination, SITES, Comparators.site(query.getSorting()), includeAll);
                    break;
                case SPACE:
                    sitesFound = findSites(pagination, SPACES, Comparators.site(query.getSorting()), includeAll);
                    break;
                case DASHBOARD:
                    sitesFound = findSites(pagination, DASHBOARDS, Comparators.site(query.getSorting()), includeAll);
                    break;
                default:
                    throw new AssertionError();
            }

            sites.addAll(sitesFound);

            // No reason to fetch anymore
            if (pagination != null && sites.size() >= pagination.getLimit()) {
                break;
            }
        }

        filter(sites, query.getFilter());

        // Manually do paging for multiple site types.
        if (query.getSiteTypes().size() > 1) {
            sites = paginate(sites, query.getPagination());
        }

        return sites;
    }

    private List<Site> findSites(Pagination pagination, Query<PortalConfig> query, Comparator<PortalConfig> comparator, boolean includeAllSites) {
        try {
            if (pagination != null) {
                ListAccess<PortalConfig> access = dataStorage.find2(query, comparator);
                int size = access.getSize();
                int offset = pagination.getOffset();
                int limit = pagination.getLimit();
                if (offset >= size) {
                    return Collections.emptyList();
                }

                PortalConfig[] sites = loadSites(includeAllSites, access, size, offset, limit);
                return fromList(Arrays.asList(sites).subList(pagination.getOffset(), sites.length));
            } else {
                return fromList(dataStorage.find(query, comparator).getAll());
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
    public PageBuilder newPageBuilder() {
        return new PageBuilderImpl();
    }

    @Override
    public ApplicationRegistry getApplicationRegistry() {
        if (null == applicationRegistry) {
            applicationRegistry = new ApplicationRegistryImpl();
        }
        return applicationRegistry;
    }

    @Override
    public Page getPage(PageId pageId) {
        Parameters.requireNonNull(pageId, "pageId");

        try {
            PageContext context = pageService.loadPage(Util.from(pageId));

            if (context == null) {
                // Page not found
                return null;
            }
            return new PageImpl(this, context);
        } catch (Throwable e) {
            throw new ApiException("Failed to get page", e);
        }
    }

    public List<ContainerItem> getPageRootContainer(PageContext pageContext) {
        org.exoplatform.portal.config.model.Page pageModel;
        try {
            pageModel = dataStorage.getPage(pageContext.getKey().format());
        } catch (Throwable e) {
            throw new ApiException("Failed to load the containers for page", e);
        }

        return getContainerItemsFor(pageModel.getChildren());
    }

    private List<ContainerItem> getContainerItemsFor(ArrayList<ModelObject> children) {

        List<ContainerItem> items = new ArrayList<ContainerItem>(children.size());

        for (ModelObject child : children) {
            items.add(getContainerItemFor(child));
        }
        return items;
    }

    private ContainerItem getContainerItemFor(ModelObject child) {
        if (child instanceof org.exoplatform.portal.config.model.Container) {
            return getContainerItemFor((org.exoplatform.portal.config.model.Container) child);
        } else if (child instanceof org.exoplatform.portal.config.model.Application<?>) {
            return getContainerItemFor((org.exoplatform.portal.config.model.Application<?>) child);
        } else {
            log.warn("Unrecognized ModelObject type for a page: " + child.getClass());
            return null;
        }
    }

    private ContainerItem getContainerItemFor(org.exoplatform.portal.config.model.Container src) {
        List<ContainerItem> children = null;
        ArrayList<ModelObject> srcChildren = src.getChildren();
        if (srcChildren != null) {
            children = getContainerItemsFor(srcChildren);
        }

        ContainerImpl container = new ContainerImpl(children);
        container.setTemplate(src.getTemplate());
        container.setMoveAppsPermission(Util.from(src.getMoveAppsPermissions()));
        container.setMoveContainersPermission(Util.from(src.getMoveContainersPermissions()));
        container.setAccessPermission(Util.from(src.getAccessPermissions()));
        return container;
    }

    private ContainerItem getContainerItemFor(org.exoplatform.portal.config.model.Application<?> src) {
        ApplicationImpl dst = new ApplicationImpl();

        dst.setAccessPermission(Util.from(src.getAccessPermissions()));
        dst.setDescription(src.getDescription());
        dst.setIconURL(src.getIcon());
        dst.setId(src.getId());
        dst.setApplicationName(src.getType().getName());

        ApplicationType<?> type = src.getType();
        if (type == ApplicationType.GADGET) {
            dst.setType(org.gatein.api.application.ApplicationType.GADGET);
        } else if (type == ApplicationType.PORTLET) {
            dst.setType(org.gatein.api.application.ApplicationType.PORTLET);
        } else if (type == ApplicationType.WSRP_PORTLET) {
            dst.setType(org.gatein.api.application.ApplicationType.WSRP);
        } else {
            throw new IllegalStateException("Application Type is not of any recognized type.");
        }

        return dst;
    }

    @Override
    public Page createPage(PageId pageId) throws EntityAlreadyExistsException {
        if (getPage(pageId) != null) {
            throw new EntityAlreadyExistsException("Cannot create page. Page " + pageId + " already exists.");
        }

        if (getSite(pageId.getSiteId()) == null) {
            throw new EntityNotFoundException("Site " + pageId.getSiteId() + " doesn't exist");
        }

        /* Fulfill the contract of Page and Container interfaces. */
        Permission accessPermissions = Container.DEFAULT_ACCESS_PERMISSION;
        Permission edit = Page.DEFAULT_EDIT_PERMISSION;
        Permission moveAppsPermissions = Container.DEFAULT_MOVE_APPS_PERMISSION;
        Permission moveContainersPermissions = Container.DEFAULT_MOVE_CONTAINERS_PERMISSION;

        PageState pageState = new PageState(pageId.getPageName(), null, false, null,
                Arrays.asList(Util.from(accessPermissions)),
                Util.from(edit)[0], Arrays.asList(Util.from(moveAppsPermissions)),
                Arrays.asList(Util.from(moveContainersPermissions)));

        PageImpl p = new PageImpl(this, new PageContext(Util.from(pageId), pageState));
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
            pages.add(new PageImpl(this, iterator.next()));
        }

        filter(pages, query.getFilter());

        return pages;
    }

    @Override
    public void savePage(Page page) {
        PageImpl pageImpl = (PageImpl) page;
        Parameters.requireNonNull(pageImpl, "page");

        if (getSite(pageImpl.getSiteId()) == null) {
            throw new EntityNotFoundException("Site " + pageImpl.getSiteId() + " doesn't exist");
        }

        if (pageImpl.isCreate() && getPage(pageImpl.getId()) != null) {
            // There is still a small chance someone else creates the page, but this is currently the best we can do
            throw new EntityAlreadyExistsException("Cannot create page. Page " + pageImpl.getId() + " already exists.");
        }

        PageContext context = pageImpl.getPageContext();

        try {
            pageService.savePage(context);

            if (!pageImpl.isChildrenSet()) {
                // it seems that the page was created without a container, and it wasn't loaded,
                // so, we are done
                return;
            }

            // Added as required by the Compose Page API work:
            // In addition to the pageService, which stores only metadata about the page, we also send it
            // to the dataStorage for persistence, because this one takes care of persisting the children as well.
            // As such, we need to get the ModelObject representation of the Page, which is the only representation
            // that the dataStorage would accept.
            dataStorage.save(getModelObjectFor(pageImpl));
            dataStorage.save();
        } catch (Throwable t) {
            throw new ApiException("Failed to save page " + pageImpl.getId(), t);
        }
    }

    /**
     * This method returns the org.exoplatform.portal.config.model.Page object that is ready to be persisted by the
     * data storage.
     *
     * It first tries to find an existing page with the given PageKey and returns it, with the attributes from the
     * source page already set on the existing page, making the resulting Page ready to be persisted.
     *
     * If the page doesn't exists, returns a newly created instance, ready to be persisted.
     *
     * @param page the org.exoplatform.api.page.Page to be queries/converted
     * @return the org.exoplatform.portal.config.model.Page ready to be persisted
     */
    private org.exoplatform.portal.config.model.Page getModelObjectFor(PageImpl page) throws Exception {
        // first, let's try to retrieve a possible existing page from the permanent storage:
        org.exoplatform.portal.config.model.Page model = dataStorage.getPage(page.getPageContext().getKey().format());

        // now, let's start the conversion, with the permissions
        List<String> moveAppsPermissions = page.getPageContext().getState().getMoveAppsPermissions();
        List<String> moveContainersPermissions = page.getPageContext().getState().getMoveContainersPermissions();
        List<String> accessPermissions = page.getPageContext().getState().getAccessPermissions();

        // and now, override the values from the stored model with the ones we've received
        model.setName(page.getName());
        model.setDescription(page.getDescription());
        model.setTitle(page.getTitle());

        model.setAccessPermissions(accessPermissions.toArray(new String[accessPermissions.size()]));
        model.setEditPermission(page.getEditPermission().toString());
        model.setMoveAppsPermissions(moveAppsPermissions.toArray(new String[moveAppsPermissions.size()]));
        model.setMoveContainersPermissions(moveContainersPermissions.toArray(new String[moveContainersPermissions.size()]));

        model.setShowMaxWindow(page.getPageContext().getState().getShowMaxWindow());

        // converts all the children objects into ModelObjects, and set them as children of the persisted page
        // note that this effectively overrides the current children from the persisted page
        model.setChildren(getModelObjectsFor(page));

        // and finally, return the persisted model, with the changes applies
        return model;
    }

    /**
     * Converts a generic container generated by the API into a list of ModelObject. This means that a top-level
     * container from the API will then be converted into a list of children containers. This effectively "flattens"
     * the first level, taking only the top-level children into consideration.
     *
     * @param container the Container from the API to be converted
     * @return an ArrayList instead of a generic List, as it's required by org.exoplatform.portal.config.model.Container#setChildren
     */
    private ArrayList<ModelObject> getModelObjectsFor(BareContainer container) {
        // basically, what this method does is:
        // 1) iterates over the children of the provided container: each of them should be a ModelObject item on a list
        //    at the end of the execution
        // 2) determines the type of the object being iterated
        // 2.1) if it's a container, converts the meta-data into a ModelObject and then make a recursive call to convert
        //      the child objects into ModelObjects
        // 2.2) if it's an application, just convert it into a ModelObject
        // 3) return the list of converted objects

        ArrayList<ModelObject> children = new ArrayList<ModelObject>();
        List<ContainerItem> containerChildren = container.getChildren();

        // this will almost certainly never be null, but better check...
        if (null != containerChildren) {

            // for each application or container on the top-level container, convert it into a ModelObject and add
            // to the list of children to return
            for (ContainerItem containerItem : containerChildren) {

                if (containerItem instanceof Container) {
                    Container containerItemContainer = (Container) containerItem;
                    // it's a container, so, we might have yet another level of children to convert
                    // also, we need information about which type of container it is, and this is expressed by the
                    // template property, as there's pretty much no difference between a column container and a row
                    // container at the storage level
                    org.exoplatform.portal.config.model.Container child = new org.exoplatform.portal.config.model.Container();
                    child.setTemplate(containerItemContainer.getTemplate());
                    child.setAccessPermissions(Util.from(containerItemContainer.getAccessPermission()));
                    child.setMoveAppsPermissions(Util.from(containerItemContainer.getMoveAppsPermission()));
                    child.setMoveContainersPermissions(Util.from(containerItemContainer.getMoveContainersPermission()));

                    // recursive call, to convert all the children into model objects for this container
                    child.setChildren(getModelObjectsFor(containerItemContainer));

                    // and finally, add to the list of children
                    children.add(child);
                } else if (containerItem instanceof Application) {

                    // this child is an application, so, get a ModelObject representation for it and add to children
                    children.add(getModelObjectFor((Application)containerItem));
                } else {

                    // how come it's not a Container nor an Application???
                    throw new IllegalStateException("An unrecognized ContainerItem was found: " + containerItem.getClass());
                }

            }
        }

        // might be an empty list!
        return children;
    }

    /**
     * Converts an Application object from the API into an org.exoplatform.portal.config.model.Application<S>
     * @param application the Application object from the API to be converted
     * @return an appropriate org.exoplatform.portal.config.model.Application<S>, as a ModelObject or null, if
     * a valid application type wasn't available on the original Application object.
     * @throws java.lang.IllegalStateException if the application type is none of the known types (Gadget, Portlet, WSRP)
     */
    private ModelObject getModelObjectFor(Application application) {
        // for our purposes, the handling of applications is pretty much the same accross applications,
        // *but*, we need to set the correct application type... so, we need to check it, and get an appropriate
        // Application<S>... otherwise, we could have made a generic call to convertIntoModelObject and kept it simpler
        switch (application.getType()) {
            case GADGET:
                return convertIntoModelObject(
                        application,
                        new org.exoplatform.portal.config.model.Application<Gadget>(ApplicationType.GADGET)
                );
            case PORTLET:
                return convertIntoModelObject(
                        application,
                        new org.exoplatform.portal.config.model.Application<Portlet>(ApplicationType.PORTLET)
                );
            case WSRP:
                return convertIntoModelObject(
                        application,
                        new org.exoplatform.portal.config.model.Application<WSRP>(ApplicationType.WSRP_PORTLET)
                );
            default:
                throw new IllegalStateException("Application type was not recognized: " + application.getType());
        }
    }

    /**
     * Converts a given API Application into a ModelObject, using a model Application<S> object provided as destination.
     * @param src the source API Application
     * @param dst the object to receive the properties from the src
     * @return the dst object, populated with the properties from src
     */
    private <S> ModelObject convertIntoModelObject(Application src,
                                               org.exoplatform.portal.config.model.Application<S> dst) {

        // here's the key part on converting an application from the API into an application that is going to be persisted
        // as a component of the page. The risk is that any new properties added to the Application<S> will need
        // a new property at both the API level and here.
        TransientApplicationState<S> state = new TransientApplicationState<S>(src.getApplicationName());
        dst.setAccessPermissions(Util.from(src.getAccessPermission()));
        dst.setDescription(src.getDescription());
        dst.setIcon(src.getIconURL());
        dst.setId(src.getId());
        dst.setState(state);
        return dst;
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
        if(permission.isAccessibleToEveryone()) {
            return true;
        }

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
            for(Membership membership : permission.getMemberships()) {
                if(acl.hasPermission(identity, membership.toString())) {
                    return true;
                }
            }
            return false;

        } catch (Throwable t) {
            throw new ApiException("Failed to check permissions", t);
        }
    }

    @Override
    public OAuthProvider getOAuthProvider(String oauthProviderKey) {
        return oauthProviderAccessor.getOAuthProvider(oauthProviderKey);
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

    private static List<Site> fromList(List<PortalConfig> internalSites) {
        List<Site> sites = new ArrayList<Site>(internalSites.size());
        for (PortalConfig internalSite : internalSites) {
            if (internalSite == null) continue;

            sites.add(new SiteImpl(internalSite));
        }
        return sites;
    }

    private PortalConfig[] loadSites(boolean includeAllSites, ListAccess<PortalConfig> access, int size, int offset, int limit) throws Exception {
        PortalConfig[] sites = new PortalConfig[Math.min(limit + offset, size)];
        PortalConfig[] loaded;
        int loadIndex = 0;
        int loadLength = sites.length;
        int index = 0;
        int length = 0;
        while (index < sites.length && loadIndex < size) {
            // Load sites from backend filtering empty sites if needed (includeAllSites=false)
            loaded = load(access, loadIndex, loadLength, includeAllSites);

            // Copy contents to sites array
            int copyLength = Math.min(loaded.length, sites.length - index);
            System.arraycopy(loaded, 0, sites, index, copyLength);

            // Update what has been copied
            index = index + loaded.length;
            length = length + copyLength;
            if (length == sites.length) {
                break;
            }

            // Update what has been loaded
            loadIndex = loadIndex + sites.length;
            loadLength = loadLength + limit;
            if (loadLength + loadIndex > size) {
                loadLength = size - loadIndex;
            }
        }
        return sites;
    }

    private PortalConfig[] load(ListAccess<PortalConfig> access, int start, int end, boolean includeAllSites) throws Exception {
        PortalConfig[] loaded = access.load(start, end);
        List<PortalConfig> list = new ArrayList<PortalConfig>(loaded.length);
        for (PortalConfig pc : loaded) {
            if (pc == null) continue;

            NavigationContext ctx = null;
            if (!includeAllSites) {
                try {
                    ctx = navigationService.loadNavigation(new SiteKey(pc.getType(), pc.getName()));
                } catch (Throwable t) {
                    throw new ApiException("Failed to find sites", t);
                }
            }

            if (includeAllSites || ctx != null) {
                list.add(pc);
            }
        }

        return list.toArray(new PortalConfig[list.size()]);
    }
}
