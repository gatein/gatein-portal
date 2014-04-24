/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Apr 19, 2007 This service is used to load the PortalConfig, Page config and Navigation config
 * for a given user.
 */
public class UserPortalConfigService implements Startable {
    DataStorage storage_;

    UserACL userACL_;

    OrganizationService orgService_;

    private NewPortalConfigListener newPortalConfigListener_;

    /** . */
    final NavigationService navService;

    /** . */
    final DescriptionService descriptionService;

    /** . */
    final PageService pageService;

    /** . */
    boolean createUserPortal;

    /** . */
    boolean destroyUserPortal;

    /** . */
    private final ImportMode defaultImportMode;

    private Log log = ExoLogger.getLogger("Portal:UserPortalConfigService");

    public UserPortalConfigService(UserACL userACL, DataStorage storage, OrganizationService orgService,
            NavigationService navService, DescriptionService descriptionService, PageService pageService, InitParams params)
            throws Exception {

        //
        ValueParam createUserPortalParam = params == null ? null : params.getValueParam("create.user.portal");
        boolean createUserPortal = createUserPortalParam == null
                || createUserPortalParam.getValue().toLowerCase().trim().equals("true");

        //
        ValueParam destroyUserPortalParam = params == null ? null : params.getValueParam("destroy.user.portal");
        boolean destroyUserPortal = destroyUserPortalParam == null
                || destroyUserPortalParam.getValue().toLowerCase().trim().equals("true");

        //
        ValueParam defaultImportModeParam = params == null ? null : params.getValueParam("default.import.mode");
        ImportMode defaultImportMode = defaultImportModeParam == null ? ImportMode.CONSERVE : ImportMode
                .valueOf(defaultImportModeParam.getValue().toUpperCase().trim());

        //
        this.storage_ = storage;
        this.orgService_ = orgService;
        this.userACL_ = userACL;
        this.navService = navService;
        this.pageService = pageService;
        this.descriptionService = descriptionService;
        this.createUserPortal = createUserPortal;
        this.destroyUserPortal = destroyUserPortal;
        this.defaultImportMode = defaultImportMode;
    }

    public PageService getPageService() {
        return pageService;
    }

    public DataStorage getDataStorage() {
        return storage_;
    }

    public ImportMode getDefaultImportMode() {
        return defaultImportMode;
    }

    public boolean getCreateUserPortal() {
        return createUserPortal;
    }

    public void setCreateUserPortal(boolean createUserPortal) {
        this.createUserPortal = createUserPortal;
    }

    public boolean getDestroyUserPortal() {
        return destroyUserPortal;
    }

    public void setDestroyUserPortal(boolean destroyUserPortal) {
        this.destroyUserPortal = destroyUserPortal;
    }

    /**
     * Returns the navigation service associated with this service.
     *
     * @return the navigation service;
     */
    public NavigationService getNavigationService() {
        return navService;
    }

    public DescriptionService getDescriptionService() {
        return descriptionService;
    }

    public UserACL getUserACL() {
        return userACL_;
    }

    public OrganizationService getOrganizationService() {
        return orgService_;
    }

    /** Temporary until the {@link #getUserPortalConfig(String, String)} is removed. */
    private static final UserPortalContext NULL_CONTEXT = new UserPortalContext() {
        public ResourceBundle getBundle(UserNavigation navigation) {
            return null;
        }

        public Locale getUserLocale() {
            return Locale.ENGLISH;
        }
    };

    /**
     * <p>
     * Build and returns an instance of <tt>UserPortalConfig</tt>.
     * </p>
     * <p/>
     * <p>
     * To return a valid config, the current thread must be associated with an identity that will grant him access to the portal
     * as returned by the {@link UserACL#hasPermission(org.exoplatform.portal.config.model.PortalConfig)} method.
     * </p>
     * <p/>
     * <p>
     * The navigation loaded on the <tt>UserPortalConfig<tt> object are obtained according to the specified user
     * argument. The portal navigation is always loaded. If the specified user is null then the navigation of the guest
     * group as configured by {@link org.exoplatform.portal.config.UserACL#getGuestsGroup()} is also loaded, otherwise
     * the navigations are loaded according to the following rules:
     * <p/>
     * <ul> <li>The navigation corresponding to the user is loaded.</li> <li>When the user is root according to the value
     * returned by {@link org.exoplatform.portal.config.UserACL#getSuperUser()} then the navigation of all groups are
     * loaded.</li> <li>When the user is not root, then all its groups are added except the guest group as configued per
     * {@link org.exoplatform.portal.config.UserACL#getGuestsGroup()}.</li> </ul>
     * <p/>
     * All the navigations are sorted using the value returned by {@link org.exoplatform.portal.config.model.PageNavigation#getPriority()}.
     * </p>
     *
     * @param portalName the portal name
     * @param accessUser the user name
     * @return the config
     * @throws Exception any exception
     * @deprecated the method {@link #getUserPortalConfig(String, String, org.exoplatform.portal.mop.user.UserPortalContext)}
     *             should be used instead
     */
    @Deprecated
    public UserPortalConfig getUserPortalConfig(String portalName, String accessUser) throws Exception {
        return getUserPortalConfig(portalName, accessUser, NULL_CONTEXT);
    }

    public UserPortalConfig getUserPortalConfig(String portalName, String accessUser, UserPortalContext userPortalContext)
            throws Exception {
        PortalConfig portal = storage_.getPortalConfig(portalName);
        if (portal == null || !userACL_.hasPermission(portal)) {
            return null;
        }

        return new UserPortalConfig(portal, this, portalName, accessUser, userPortalContext);
    }

    /**
     * Compute and returns the list that the specified user can manage. If the user is root then all existing groups are
     * returned otherwise the list is computed from the groups in which the user has a configured membership. The membership is
     * configured from the value returned by {@link org.exoplatform.portal.config.UserACL#getMakableMT()}
     *
     * @param remoteUser the user to get the makable navigations
     * @param withSite true if a site must exist
     * @return the list of groups
     * @throws Exception any exception
     */
    public List<String> getMakableNavigations(String remoteUser, boolean withSite) throws Exception {
        Collection<Group> groups;
        if (remoteUser.equals(userACL_.getSuperUser())) {
            groups = orgService_.getGroupHandler().getAllGroups();
        } else {
            groups = orgService_.getGroupHandler().resolveGroupByMembership(remoteUser, userACL_.getMakableMT());
        }

        //
        List<String> list = new ArrayList<String>();
        if (groups != null) {
            Set<String> existingNames = null;
            if (withSite) {
                existingNames = new HashSet<String>();
                Query<PortalConfig> q = new Query<PortalConfig>("group", null, PortalConfig.class);
                LazyPageList<PortalConfig> lpl = storage_.find(q);
                for (PortalConfig groupSite : lpl.getAll()) {
                    existingNames.add(groupSite.getName());
                }
            }

            //
            for (Group group : groups) {
                String groupId = group.getId().trim();
                if (existingNames == null || existingNames.contains(groupId)) {
                    list.add(groupId);
                }
            }
        }

        //
        return list;
    }

    /**
     * Create a user site for the specified user. It will perform the following:
     * <ul>
     * <li>create the user site by calling {@link #createUserPortalConfig(String, String, String)} which may create a site or
     * not according to the default configuration</li>
     * <li>if not site exists then it creates a site then it creates an empty site</li>
     * <li>if not navigation exists for the user site then it creates an empty navigation</li>
     * </ul>
     *
     * @param userName the user name
     * @throws Exception a nasty exception
     */
    public void createUserSite(String userName) throws Exception {
        // Create the portal from the template
        createUserPortalConfig(PortalConfig.USER_TYPE, userName, "user");

        // Need to insert the corresponding user site if needed
        PortalConfig cfg = storage_.getPortalConfig(PortalConfig.USER_TYPE, userName);
        if (cfg == null) {
            cfg = new PortalConfig(PortalConfig.USER_TYPE);
            cfg.setPortalLayout(new Container());
            cfg.setName(userName);
            storage_.create(cfg);
        }

        // Create a blank navigation if needed
        SiteKey key = SiteKey.user(userName);
        NavigationContext nav = navService.loadNavigation(key);
        if (nav == null) {
            nav = new NavigationContext(key, new NavigationState(5));
            navService.saveNavigation(nav);
        }
    }

    /**
     * Create a group site for the specified group. It will perform the following:
     * <ul>
     * <li>create the group site by calling {@link #createUserPortalConfig(String, String, String)} which may create a site or
     * not according to the default configuration</li>
     * <li>if not site exists then it creates a site then it creates an empty site</li>
     * </ul>
     *
     * @param groupId the group id
     * @throws Exception a nasty exception
     */
    public void createGroupSite(String groupId) throws Exception {
        // Create the portal from the template
        createUserPortalConfig(PortalConfig.GROUP_TYPE, groupId, "group");

        // Need to insert the corresponding group site
        PortalConfig cfg = storage_.getPortalConfig(PortalConfig.GROUP_TYPE, groupId);
        if (cfg == null) {
            cfg = new PortalConfig(PortalConfig.GROUP_TYPE);
            cfg.setPortalLayout(new Container());
            cfg.setName(groupId);
            storage_.create(cfg);
        }
    }

    /**
     * This method should create a the portal config, pages and navigation according to the template name.
     *
     * @param siteType the site type
     * @param siteName the Site name
     * @param template the template to use
     * @throws Exception any exception
     */
    public void createUserPortalConfig(String siteType, String siteName, String template) throws Exception {
        String templatePath = newPortalConfigListener_.getTemplateConfig(siteType, template);

        NewPortalConfig portalConfig = new NewPortalConfig(templatePath);
        portalConfig.setTemplateName(template);
        portalConfig.setOwnerType(siteType);
        newPortalConfigListener_.createPortalConfig(portalConfig, siteName);
        newPortalConfigListener_.createPage(portalConfig, siteName);
        newPortalConfigListener_.createPageNavigation(portalConfig, siteName);
    }

    /**
     * This method removes the PortalConfig, Page and PageNavigation that belong to the portal in the database.
     *
     * @param portalName the portal name
     * @throws Exception any exception
     */
    public void removeUserPortalConfig(String portalName) throws Exception {
        removeUserPortalConfig(PortalConfig.PORTAL_TYPE, portalName);
    }

    /**
     * This method removes the PortalConfig, Page and PageNavigation that belong to the portal in the database.
     *
     * @param ownerType the owner type
     * @param ownerId the portal name
     * @throws Exception any exception
     */
    public void removeUserPortalConfig(String ownerType, String ownerId) throws Exception {
        PortalConfig config = storage_.getPortalConfig(ownerType, ownerId);
        if (config != null) {
            storage_.remove(config);
        }
    }

    /**
     * Use {@link PageService} to load metadata of specify page
     *
     * @param pageRef the PageKey
     * @return the PageContext
     */
    public PageContext getPage(PageKey pageRef) {
        if (pageRef == null) {
            return null;
        }

        PageContext page = pageService.loadPage(pageRef);
        if (page == null || !userACL_.hasPermission(page)) {
            return null;
        }
        return page;
    }

    /**
     * Creates a page from an existing template.
     *
     * @param temp the template name
     * @param ownerType the new owner type
     * @param ownerId the new owner id
     * @return the page
     * @throws Exception any exception
     */
    public Page createPageTemplate(String temp, String ownerType, String ownerId) throws Exception {
        Page page = newPortalConfigListener_.createPageFromTemplate(ownerType, ownerId, temp);
        updateOwnership(page, ownerType, ownerId);
        return page;
    }

    /**
     * Returns the list of all portal site names.
     *
     * @return the list of all portal site names
     * @throws Exception any exception
     */
    public List<String> getAllPortalNames() throws Exception {
        return getAllSiteNames(SiteType.PORTAL);
    }

    /**
     * Returns the list of all group site names.
     *
     * @return the list of all group site names
     * @throws Exception any exception
     */
    public List<String> getAllGroupNames() throws Exception {
        return getAllSiteNames(SiteType.GROUP);
    }

    private List<String> getAllSiteNames(SiteType siteType) throws Exception {
        List<String> list;
        switch (siteType) {
            case PORTAL:
                list = storage_.getAllPortalNames();
                break;
            case GROUP:
                list = storage_.getAllGroupNames();
                break;
            default:
                throw new AssertionError();
        }
        for (Iterator<String> i = list.iterator(); i.hasNext();) {
            String name = i.next();
            PortalConfig config = storage_.getPortalConfig(siteType.getName(), name);
            if (config == null || !userACL_.hasPermission(config)) {
                i.remove();
            }
        }
        return list;
    }

    /**
     * Update the ownership recursively on the model graph.
     *
     * @param object the model object graph root
     * @param ownerType the new owner type
     * @param ownerId the new owner id
     */
    private void updateOwnership(ModelObject object, String ownerType, String ownerId) {
        if (object instanceof Container) {
            Container container = (Container) object;
            if (container instanceof Page) {
                Page page = (Page) container;
                page.setOwnerType(ownerType);
                page.setOwnerId(ownerId);
            }
            for (ModelObject child : container.getChildren()) {
                updateOwnership(child, ownerType, ownerId);
            }
        } else if (object instanceof Application) {
            Application<?> application = (Application<?>) object;
            TransientApplicationState<?> applicationState = (TransientApplicationState<?>) application.getState();
            if (applicationState != null && (applicationState.getOwnerType() == null || applicationState.getOwnerId() == null)) {
                applicationState.setOwnerType(ownerType);
                applicationState.setOwnerId(ownerId);
            }
        }
    }

    public void initListener(ComponentPlugin listener) {
        if (listener instanceof NewPortalConfigListener) {
            synchronized (this) {
                if (newPortalConfigListener_ == null) {
                    this.newPortalConfigListener_ = (NewPortalConfigListener) listener;
                } else {
                    newPortalConfigListener_.mergePlugin((NewPortalConfigListener) listener);
                }
            }
        }
    }

    public void deleteListenerElements(ComponentPlugin listener) {
        if (listener instanceof NewPortalConfigListener) {
            synchronized (this) {
                if (newPortalConfigListener_ != null) {
                    newPortalConfigListener_.deleteListenerElements((NewPortalConfigListener) listener);
                }
            }
        }
    }

    public void start() {
        try {
            if (newPortalConfigListener_ == null) {
                return;
            }

            //
            newPortalConfigListener_.run();
        } catch (Exception e) {
            log.error("Could not import initial data", e);
        }
    }

    public void stop() {
    }

    public String getDefaultPortal() {
        return newPortalConfigListener_.getDefaultPortal();
    }

    /**
     * Returns the default portal template to be used when creating a site
     *
     * @return the default portal template name
     */
    public String getDefaultPortalTemplate() {
        return newPortalConfigListener_.getDefaultPortalTemplate();
    }

    public Set<String> getPortalTemplates() {
        return newPortalConfigListener_.getTemplateConfigs(PortalConfig.PORTAL_TYPE);
    }

    public PortalConfig getPortalConfigFromTemplate(String templateName) {
        return newPortalConfigListener_.getPortalConfigFromTemplate(PortalConfig.PORTAL_TYPE, templateName);
    }

    /**
     * Sets the default access, edit, move apps and move containers permissions on the given {@link Page}
     * depending on the the value of {@link Page#getOwnerType()}.
     * <p>
     * For {@code ownerType} equal to {@link SiteType#PORTAL#getName()}, the portal is loaded
     * and the permissions are overtaken from it.
     * <p>
     * Otherwise, i.e. for {@link SiteType#GROUP} and {@link SiteType#USER} owner types,
     * {@link Page#getOwnerId()} is used as a value to set access, move apps and move containers
     * permissions.
     * <p>
     * All other cases throw an {@link IllegalStateException}.
     *
     * @param page the page on which the permissions will be set
     * @throws Exception
     */
    public void setDefaultPermissions(Page page) throws Exception {
        String ownerType = page.getOwnerType();
        if (SiteType.PORTAL.getName().equals(ownerType)) {
            PortalConfig portalConfig = storage_.getPortalConfig(page.getOwnerType(), page.getOwnerId());
            page.setAccessPermissions(portalConfig.getAccessPermissions());
            page.setEditPermission(portalConfig.getEditPermission());
        } else if (SiteType.GROUP.getName().equals(ownerType)) {
            String ownerId = page.getOwnerId();
            String siteName = ownerId.startsWith("/") ? ownerId : "/" + ownerId;
            String[] accessPermissions = new String[] { "*:" + siteName };
            page.setAccessPermissions(accessPermissions);
            page.setEditPermission(userACL_.getMakableMT() + ":" + siteName);
        } else if (SiteType.USER.getName().equals(ownerType)) {
            /* Nothing can be done for users */
        }
    }

}
