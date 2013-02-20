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

package org.gatein.api.management;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.simple.SimpleURL;
import org.exoplatform.web.url.simple.SimpleURLContext;
import org.gatein.api.BasicPortalRequest;
import org.gatein.api.EntityAlreadyExistsException;
import org.gatein.api.EntityNotFoundException;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.Util;
import org.gatein.api.common.Attributes;
import org.gatein.api.common.URIResolver;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.security.Group;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteQuery;
import org.gatein.api.site.SiteType;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ManagedUser;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedAfter;
import org.gatein.management.api.annotations.ManagedBefore;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedRole;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationNames;

import java.util.List;
import java.util.Locale;

import static org.gatein.api.management.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@SuppressWarnings("unused")
@Managed(value = "api", description = "GateIn API Management Resource")
public class GateInApiManagementResource {
    private static final Logger log = LoggerFactory.getLogger("org.gatein.api.management");

    private static final SiteQuery SITE_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.SITE).build();
    private static final SiteQuery SPACE_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.SPACE).build();
    private static final SiteQuery DASHBOARD_QUERY = new SiteQuery.Builder().withSiteTypes(SiteType.DASHBOARD).build();

    private final Portal portal;

    @ManagedContext
    private final ModelProvider modelProvider; // gatein-management will set this field via reflection

    public GateInApiManagementResource(Portal portal) {
        this(portal, null);
    }

    // Constructor for testing to specify ModelProvider instead of gatein-management
    GateInApiManagementResource(Portal portal, ModelProvider modelProvider) {
        this.portal = portal;
        this.modelProvider = modelProvider;
    }

    @ManagedBefore
    public void before(@ManagedContext OperationContext context) {
        PortalRequest portalRequest = PortalRequest.getInstance();
        if (portalRequest == null) {
            setCurrentPortalRequest(context);
        }
    }

    @ManagedAfter
    public void after() {
        if (PortalRequest.getInstance() instanceof BasicPortalRequest) {
            BasicPortalRequest.setInstance(null);
        }
    }

    // ------------------------------------------------- Portal Sites --------------------------------------------------//
    @Managed("/sites")
    public ModelList getSites(@ManagedContext PathAddress address) {
        return _getSites(SITE_QUERY, address);
    }

    @Managed("/sites/{site-name}")
    public ModelObject getSite(@MappedPath("site-name") String siteName, @ManagedContext OperationContext context) {
        SiteId id = new SiteId(siteName);
        return _getSite(id, context);
    }

    @Managed("/sites/{site-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds a given site")
    public ModelObject addSite(@MappedPath("site-name") String siteName, @MappedAttribute("template") String template, @ManagedContext PathAddress address) {
        SiteId siteId = new SiteId(siteName);
        return _addSite(address, siteId, template);
    }

    @Managed("/sites/{site-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given site")
    public void removeSite(@MappedPath("site-name") String siteName) {
        SiteId id = new SiteId(siteName);
        _removeSite(id);
    }

    @Managed("/sites/{site-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates a given site")
    public ModelObject updateSite(@MappedPath("site-name") String siteName, @ManagedContext ModelObject siteModel, @ManagedContext PathAddress address) {
        SiteId id = new SiteId(siteName);
        return _updateSite(id, siteModel, address);
    }

    @Managed("/sites/{site-name}/pages")
    public PageManagementResource getPages(@MappedPath("site-name") String siteName) {
        SiteId id = new SiteId(siteName);
        return pagesResource(id);
    }

    @Managed("/sites/{site-name}/navigation")
    public NavigationManagementResource getNavigation(@MappedPath("site-name") String siteName) {
        SiteId id = new SiteId(siteName);
        return navigationResource(id);
    }

    // --------------------------------------------- Group Sites (Spaces) ----------------------------------------------//
    @Managed("/spaces")
    public ModelList getSpaces(@ManagedContext PathAddress address) {
        return _getSites(SPACE_QUERY, address);
    }

    @Managed("/spaces/{group-name: .*}")
    public ModelObject getSpace(@MappedPath("group-name") String groupName, @ManagedContext OperationContext context) {
        SiteId id = new SiteId(new Group(groupName));
        return _getSite(id, context);
    }

    @Managed("/spaces/{group-name: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds a given site")
    public ModelObject addSpace(@MappedPath("group-name") String groupName, @MappedAttribute("template") String template, @ManagedContext PathAddress address) {
        SiteId siteId = new SiteId(new Group(groupName));
        return _addSite(address, siteId, template);
    }

    @Managed("/spaces/{group-name: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given space")
    public void removeSpace(@MappedPath("group-name") String groupName) {
        SiteId id = new SiteId(new Group(groupName));
        _removeSite(id);
    }

    @Managed("/spaces/{group-name: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates a given space")
    public ModelObject updateSpace(@MappedPath("group-name") String groupName, @ManagedContext ModelObject siteModel, @ManagedContext PathAddress address) {
        SiteId id = new SiteId(new Group(groupName));
        return _updateSite(id, siteModel, address);
    }

    @Managed("/spaces/{group-name: .*}/pages")
    public PageManagementResource getSpacePages(@MappedPath("group-name") String groupName) {
        SiteId id = new SiteId(new Group(groupName));
        return pagesResource(id);
    }

    @Managed("/spaces/{group-name: .*}/navigation")
    public NavigationManagementResource getSpaceNavigation(@MappedPath("group-name") String groupName) {
        SiteId id = new SiteId(new Group(groupName));
        return navigationResource(id);
    }

    // -------------------------------------------- User Sites (Dashboard) ---------------------------------------------//
    @Managed("/dashboards")
    public ModelList getDashboards(@ManagedContext PathAddress address) {
        return _getSites(DASHBOARD_QUERY, address);
    }

    @Managed("/dashboards/{user-name}")
    public ModelObject getDashboard(@MappedPath("user-name") String userName, @ManagedContext OperationContext context) {
        SiteId id = new SiteId(new User(userName));
        return _getSite(id, context);
    }

    @Managed("/dashboards/{user-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds a given site")
    public ModelObject addDashboard(@MappedPath("user-name") String userName, @MappedAttribute("template") String template, @ManagedContext PathAddress address) {
        SiteId siteId = new SiteId(new User(userName));
        return _addSite(address, siteId, template);
    }

    @Managed("/dashboards/{user-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given dashboard")
    public void removeDashboard(@MappedPath("user-name") String userName) {
        SiteId id = new SiteId(new User(userName));
        _removeSite(id);
    }

    @Managed("/dashboards/{user-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates a given space")
    public ModelObject updateDashboard(@MappedPath("user-name") String userName, @ManagedContext ModelObject siteModel, @ManagedContext PathAddress address) {
        SiteId id = new SiteId(new User(userName));
        return _updateSite(id, siteModel, address);
    }

    @Managed("/dashboards/{user-name}/pages")
    public PageManagementResource getDashboardPages(@MappedPath("user-name") String userName) {
        SiteId id = new SiteId(new User(userName));
        return pagesResource(id);
    }

    @Managed("/dashboards/{user-name}/navigation")
    public NavigationManagementResource getDashboardNavigation(@MappedPath("user-name") String userName) {
        SiteId id = new SiteId(new User(userName));
        return navigationResource(id);
    }

    private NavigationManagementResource navigationResource(SiteId siteId) {
        Navigation navigation = portal.getNavigation(siteId);
        if (navigation == null) {
            throw new ResourceNotFoundException("Navigation does not exist for site " + siteId);
        }

        return new NavigationManagementResource(navigation, modelProvider);
    }

    private PageManagementResource pagesResource(SiteId siteId) {
        requireSite(siteId);
        return new PageManagementResource(portal, modelProvider, siteId);
    }

    private ModelList _getSites(SiteQuery query, PathAddress address) {
        List<Site> sites = portal.findSites(query);
        ModelList list = modelProvider.newModel(ModelList.class);
        populateModel(sites, list, address);

        return list;
    }

    private ModelObject _getSite(SiteId id, OperationContext context) {
        Site site = requireSite(id);

        // Verify current user has access to site
        verifyAccess(site, context);

        // Populate site model
        ModelObject siteModel = modelProvider.newModel(ModelObject.class);
        populateModel(site, siteModel, context.getAddress());

        return siteModel;
    }

    private ModelObject _addSite(PathAddress address, SiteId siteId, String template) {
        Site site;
        try {
            if (template == null) {
                site = portal.createSite(siteId);
            } else {
                site = portal.createSite(siteId, template);
            }
        } catch (EntityAlreadyExistsException e) {
            throw alreadyExists("Could not add site", siteId);
        }
        portal.saveSite(site);

        // Populate model
        ModelObject siteModel = modelProvider.newModel(ModelObject.class);
        populateModel(site, siteModel, address);

        return siteModel;
    }

    private void _removeSite(SiteId id) {
        requireSite(id);
        try {
            boolean removed = portal.removeSite(id);
            if (!removed) throw new RuntimeException("Could not remove site + " + id + " for unknown reasons.");
        } catch (EntityNotFoundException e) {
            throw notFound("Cannot remove site", id);
        }
    }

    private ModelObject _updateSite(SiteId id, ModelObject siteModel, PathAddress address) {
        Site site = requireSite(id);

        if (siteModel.has("displayName")) {
            String displayName = get(siteModel, ModelString.class, "displayName").getValue();
            site.setDisplayName(displayName);
        }
        if (siteModel.has("description")) {
            String description = get(siteModel, ModelString.class, "description").getValue();
            site.setDescription(description);
        }
        if (siteModel.has("skin")) {
            String skin = get(siteModel, ModelString.class, "skin").getValue();
            site.setSkin(skin);
        }
        if (siteModel.has("locale")) {
            Locale locale = getLocale(siteModel, "locale");
            site.setLocale(locale);
        }
        if (siteModel.has("access-permissions")) {
            Permission permission = getPermission(siteModel, false, "access-permissions");
            site.setAccessPermission(permission);
        }
        if (siteModel.has("edit-permissions")) {
            Permission permission = getPermission(siteModel, true, "access-permissions");
            site.setEditPermission(permission);
        }
        if (siteModel.hasDefined("attributes")) {
            ModelList list = get(siteModel, ModelList.class, "attributes");
            for (int i = 0; i < list.size(); i++) {
                ModelValue mv = list.get(i);
                String field = "attributes["+i+"]"; // Used for error reporting
                if (mv.getValueType() != ModelValue.ModelValueType.OBJECT) {
                    throw invalidType(mv, ModelValue.ModelValueType.OBJECT, field);
                }
                ModelObject attrModel = mv.asValue(ModelObject.class);
                if (!attrModel.hasDefined("key")) {
                    throw requiredField(field, "key");
                }
                String key = get(attrModel, ModelString.class, "key").getValue();
                if (!attrModel.has("value")) {
                    throw requiredField(field, "value");
                }
                String value = get(attrModel, ModelString.class, "value").getValue();
                site.getAttributes().put(key, value);
            }
        }

        portal.saveSite(site);

        ModelObject updatedSiteModel = modelProvider.newModel(ModelObject.class);
        populateModel(site, updatedSiteModel, address);

        return updatedSiteModel;
    }

    private Site requireSite(SiteId id) {
        Site site = portal.getSite(id);
        if (site == null) throw new ResourceNotFoundException("Site not found for " + id);

        return site;
    }

    private void populateModel(Site site, ModelObject siteModel, PathAddress address) {
        // Site fields
        siteModel.set("name", site.getId().getName());
        siteModel.set("type", site.getId().getType().name().toLowerCase());
        siteModel.set("displayName", site.getDisplayName());
        siteModel.set("description", site.getDescription());
        siteModel.set("skin", site.getSkin());
        populate("locale", site.getLocale(), siteModel);
        populate("access-permissions", site.getAccessPermission(), siteModel);
        populate("edit-permissions", site.getEditPermission(), siteModel);
        ModelList attrList = siteModel.get("attributes", ModelList.class);
        Attributes attributes = site.getAttributes();
        for (String key : attributes.keySet()) {
            ModelObject attr = attrList.add().setEmptyObject();
            attr.set("key", key);
            attr.set("value", attributes.get(key));
        }

        // Pages
        ModelReference pagesRef = siteModel.get("pages", ModelReference.class);
        pagesRef.set(address.append("pages"));

        // Navigation
        ModelReference navigationRef = siteModel.get("navigation", ModelReference.class);
        navigationRef.set(address.append("navigation"));
    }

    private void populateModel(List<Site> sites, ModelList list, PathAddress address) {
        for (Site site : sites) {
            if (hasPermission(site.getAccessPermission())) {
                ModelReference siteRef = list.add().asValue(ModelReference.class);
                siteRef.set("name", site.getName());
                siteRef.set("type", site.getType().getName());
                siteRef.set(address.append(site.getName()));
            }
        }
    }

    private User getUser(ManagedUser managedUser) {
        if (managedUser == null) return User.anonymous();

        return new User(managedUser.getUserName());
    }

    private void setCurrentPortalRequest(OperationContext context) {
        final ManagedUser managedUser = context.getUser();
        final PathAddress address = context.getAddress();

        // Retrieve siteId from address (can be null)
        SiteId siteId = getSiteId(address);

        // Retrieve nodePath from address (can be null)
        NodePath nodePath = getNodePath(address);

        Locale locale = context.getLocale();
        // For some HTTP requests the locale is set to *, I guess to indicate a header 'Accept-Language: *' ?
        if (locale != null && locale.getLanguage().equals("*")) {
            locale = null;
        }

        User user = (managedUser == null || managedUser.getUserName() == null) ? User.anonymous() : new User(managedUser.getUserName());

        final PortalContainer container = PortalContainer.getInstance();
        final WebAppController controller = (WebAppController) container.getComponentInstanceOfType(WebAppController.class);
        URIResolver uriResolver = new URIResolver() {
            @Override
            public String resolveURI(SiteId siteId) {
                SiteKey siteKey = Util.from(siteId);
                NavigationResource navResource = new NavigationResource(siteKey, "");
                SimpleURL url = new SimpleURL(new SimpleURLContext(container, controller));
                url.setSchemeUse(false);
                url.setAuthorityUse(false);
                String urlString = url.setResource(navResource).toString();
                return urlString.substring(0, urlString.length() - 1);
            }
        };
        BasicPortalRequest.setInstance(new BasicPortalRequest(user, siteId, nodePath, locale, portal, uriResolver));
    }

    private static SiteId getSiteId(PathAddress address) {
        String siteName = address.resolvePathTemplate("site-name");
        if (siteName != null) {
            return new SiteId(siteName);
        }

        String groupName = address.resolvePathTemplate("group-name");
        if (groupName != null) {
            return new SiteId(new Group(groupName));
        }

        String userName = address.resolvePathTemplate("user-name");
        if (userName != null) {
            return new SiteId(new User(userName));
        }

        return null;
    }

    private static NodePath getNodePath(PathAddress address) {
        String path = address.resolvePathTemplate("path");
        if (path != null) {
            return NodePath.fromString(path);
        }

        return null;
    }

    static PathAddress getSiteAddress(SiteId siteId) {
        PathAddress address = PathAddress.pathAddress("api");
        switch (siteId.getType()) {
            case SITE:
                address = address.append("sites");
                break;
            case SPACE:
                address = address.append("spaces");
                break;
            case DASHBOARD:
                address = address.append("dashboards");
                break;
            default:
                throw new AssertionError();
        }

        return address.append(siteId.getName());
    }

    static PathAddress getPagesAddress(SiteId siteId) {
        return getSiteAddress(siteId).append("pages");
    }

    static PathAddress getNavigationAddress(SiteId siteId) {
        return getSiteAddress(siteId).append("navigation");
    }
}
