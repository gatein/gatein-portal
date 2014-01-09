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

package org.exoplatform.portal.pom.config;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.transaction.Status;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.EventType;
import org.exoplatform.portal.pom.config.tasks.DashboardTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.BodyData;
import org.exoplatform.portal.pom.data.BodyType;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerAdapter;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.pom.data.RedirectData;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.transaction.JTAUserTransactionLifecycleService;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.portal.mop.QueryResult;
import org.gatein.portal.mop.customization.CustomizationContext;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteContext;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.mop.site.SiteType;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMDataStorage implements ModelDataStorage {

    private static final Logger log = LoggerFactory.getLogger(POMDataStorage.class);

    /** . */
    private final POMSessionManager pomMgr;

    /** . */
    private ConfigurationManager confManager_;

    /** . */
    private JTAUserTransactionLifecycleService jtaUserTransactionLifecycleService;

    /** . */
    private final ListenerService listenerService;

    /** . */
    private final LayoutService layoutService;

    /** . */
    private final SiteService siteService ;

    /** . */
    private final PageService pageService;

    /** . */
    private final CustomizationService customizationService;

    public POMDataStorage(
            final POMSessionManager pomMgr,
            ConfigurationManager confManager,
            LayoutService layoutService,
            SiteService siteService,
            PageService pageService,
            JTAUserTransactionLifecycleService jtaUserTransactionLifecycleService,
            ListenerService listenerService,
            CustomizationService customizationService) {

        // Invalidation bridge : listen for PageService events and invalidate the DataStorage cache
        Listener<?, org.gatein.portal.mop.page.PageKey> invalidator = new Listener<Object, org.gatein.portal.mop.page.PageKey>() {
            @Override
            public void onEvent(Event<Object, org.gatein.portal.mop.page.PageKey> event) throws Exception {
                org.gatein.portal.mop.page.PageKey key = event.getData();
                PageKey adaptedKey = new PageKey(key.getSite().getTypeName(), key.getSite().getName(), key.getName());
                pomMgr.getSession().scheduleForEviction(adaptedKey);
            }
        };
        listenerService.addListener(EventType.PAGE_UPDATED, invalidator);
        listenerService.addListener(EventType.PAGE_DESTROYED, invalidator);

        //
        this.pomMgr = pomMgr;
        this.confManager_ = confManager;
        this.jtaUserTransactionLifecycleService = jtaUserTransactionLifecycleService;
        this.listenerService = listenerService;
        this.siteService = siteService;
        this.layoutService = layoutService;
        this.pageService = pageService;
        this.customizationService = customizationService;
    }

    public PortalData getPortalConfig(PortalKey key) throws Exception {

        SiteKey siteKey = org.exoplatform.portal.mop.Utils.create(key);
        SiteContext context = siteService.loadSite(siteKey);
        if (context != null) {
            ContainerData container = loadLayout(context.getLayoutId(), BodyType.PAGE);
            return new PortalData(
                    context.getId(),
                    context.getKey().getName(),
                    context.getKey().getType().name().toLowerCase(),
                    context.getState().getLocale(),
                    context.getState().getLabel(),
                    context.getState().getDescription(),
                    context.getState().getAccessPermissions(),
                    context.getState().getEditPermissions(),
                    context.getState().getProperties(),
                    context.getState().getSkin(),
                    container,
                    new ArrayList<RedirectData>()
            );
        } else {
            return null;
        }
    }

    public void create(PortalData config) throws Exception {
        SiteKey key = org.exoplatform.portal.mop.Utils.create(config.getKey());
        SiteContext site = siteService.loadSite(key);
        if (site != null) {
            throw new IllegalArgumentException("Cannot create portal " + config.getName() + " that already exist");
        }

        // Save site
        site = new SiteContext(key, config.toState());
        siteService.saveSite(site);

        // Add layout id so it can be saved
        ContainerData layout = config.getPortalLayout();
        layout = new ContainerData(
                site.getLayoutId(),
                layout.getStorageName(),
                layout.getId(),
                layout.getName(),
                layout.getIcon(),
                layout.getTemplate(),
                layout.getFactoryId(),
                layout.getTitle(),
                layout.getDescription(),
                layout.getWidth(),
                layout.getHeight(),
                layout.getAccessPermissions(),
                layout.getChildren()
        );


        // Save layout
        save(layout);
    }

    public void save(PortalData config) throws Exception {
        SiteKey key = org.exoplatform.portal.mop.Utils.create(config.getKey());
        SiteContext site = siteService.loadSite(key);
        if (site == null) {
            throw new IllegalArgumentException("Cannot save portal " + config.getName() + " that does not exist");
        }

        // Save intrinsic state
        site.setState(config.toState());
        siteService.saveSite(site);

        // Add layout id so it can be saved
        ContainerData layout = config.getPortalLayout();
        layout = new ContainerData(
                site.getLayoutId(),
                layout.getStorageName(),
                layout.getId(),
                layout.getName(),
                layout.getIcon(),
                layout.getTemplate(),
                layout.getFactoryId(),
                layout.getTitle(),
                layout.getDescription(),
                layout.getWidth(),
                layout.getHeight(),
                layout.getAccessPermissions(),
                layout.getChildren()
        );

        // Save layout
        save(layout);
    }

    public void remove(PortalData config) throws Exception {
        SiteKey key = org.exoplatform.portal.mop.Utils.create(config.getKey());
        if (!siteService.destroySite(key)) {
            throw new NoSuchDataException("Could not remove non existing portal " + key);
        }
    }

    public PageData getPage(PageKey key) throws Exception {
        PageContext context = pageService.loadPage(new org.gatein.portal.mop.page.PageKey(
                new SiteKey(key.getType(), key.getId()),
                key.getName()
        ));
        if (context != null) {
            ContainerData container = loadLayout(context.getLayoutId(), BodyType.PAGE);
            return new PageData(
                    context.getLayoutId(),
                    null,
                    key.getName(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    Collections.<String> emptyList(),
                    container.getChildren(),
                    key.getType(),
                    key.getId(),
                    Collections.<String>emptyList(),
                    false);
        } else {
            return null;
        }
    }

    private ContainerData loadLayout(String layoutId, BodyType bodyType) {
        NodeContext<?, ElementState> layout = layoutService.loadLayout(ElementState.model(), layoutId, null);
        if (layout != null) {
            return (ContainerData)create(layout, bodyType);
        } else {
            return null;
        }
    }

    private ComponentData create(NodeContext<?, ElementState> context, BodyType bodyType) {
        ElementState state = context.getState();
        if (state instanceof ElementState.Container) {
            ElementState.Container container = (ElementState.Container) state;
            ArrayList<ComponentData> children = new ArrayList<ComponentData>();
            for (NodeContext<?, ElementState> child : context) {
                children.add(create(child, bodyType));
            }
            return new ContainerData(
                    context.getId(),
                    context.getName(),
                    container.id,
                    container.properties.get(ElementState.Container.NAME),
                    container.properties.get(ElementState.Container.ICON),
                    container.properties.get(ElementState.Container.TEMPLATE),
                    container.properties.get(ElementState.Container.FACTORY_ID),
                    container.properties.get(ElementState.Container.TITLE),
                    container.properties.get(ElementState.Container.DESCRIPTION),
                    container.properties.get(ElementState.Container.WIDTH),
                    container.properties.get(ElementState.Container.HEIGHT),
                    Collections.<String>emptyList(),
                    children
            );
        } else if (state instanceof ElementState.Window) {
            ElementState.Window window = (ElementState.Window) state;
            HashMap<String, String> properties = new HashMap<String, String>();
/*
            for (Property p : window.properties) {
                if (p instanceof Property.Raw) {
                    Property.Raw  raw = (Property.Raw) p;
                    properties.put(raw.getName(), raw.getValue());
                }
            }
*/
            return new ApplicationData(
                    context.getId(),
                    context.getName(),
                    window.type.getApplicationType(),
                    window.state,
                    null,
                    window.properties.get(ElementState.Window.TITLE),
                    window.properties.get(ElementState.Window.ICON),
                    window.properties.get(ElementState.Window.DESCRIPTION),
                    window.properties.get(ElementState.Window.SHOW_INFO_BAR),
                    window.properties.get(ElementState.Window.SHOW_APPLICATION_STATE),
                    window.properties.get(ElementState.Window.SHOW_APPLICATION_MODE),
                    window.properties.get(ElementState.Window.THEME),
                    window.properties.get(ElementState.Window.WIDTH),
                    window.properties.get(ElementState.Window.HEIGHT),
                    properties,
                    Collections.emptyList()
            );
        } else if (state instanceof ElementState.Body) {
            return new BodyData(context.getId(), bodyType);
        } else  {
            throw new UnsupportedOperationException("todo : " + state.getClass().getName());
        }
    }

    private void save(ContainerData container) throws Exception {

        // We cheat a bit with this cast
        // but well it's easier to do this way
        NodeContext<ComponentData, ElementState> ret = (NodeContext<ComponentData, ElementState>) layoutService.loadLayout(ElementState.model(), container.getStorageId(), null);

        // Save element
        layoutService.saveLayout(new ContainerAdapter(container), container, ret, null);
    }

    public List<ModelChange> save(PageData page) throws Exception {

        // Build layout context
        org.gatein.portal.mop.page.PageKey key = new org.gatein.portal.mop.page.PageKey(new SiteKey(page.getKey().getType(), page.getKey().getId()), page.getKey().getName());
        PageContext context = pageService.loadPage(key);

        //
        if (context == null) {
            context = new PageContext(key, new PageState(
                    page.getName(),
                    page.getDescription(),
                    page.isShowMaxWindow(),
                    page.getFactoryId()
            ));
            pageService.savePage(context);
        }

        // Need to use the context ID
        ContainerData container = new ContainerData(
                context.getLayoutId(),
                page.getStorageName(),
                page.getId(),
                page.getName(),
                page.getIcon(),
                page.getTemplate(),
                page.getFactoryId(),
                page.getTitle(),
                page.getDescription(),
                page.getWidth(),
                page.getHeight(),
                page.getAccessPermissions(),
                page.getChildren());

        //
        save(container);

        //
        return Collections.emptyList();
    }

    public <S extends Serializable> String getId(ApplicationState<S> state) throws Exception {
        String contentId;
        if (state instanceof TransientApplicationState) {
            TransientApplicationState tstate = (TransientApplicationState) state;
            contentId = tstate.getContentId();
        } else {
            String storageId;
            if (state instanceof CloneApplicationState) {
                storageId = ((CloneApplicationState<S>) state).getStorageId();
            } else {
                storageId = ((PersistentApplicationState<S>) state).getStorageId();
            }
            CustomizationContext context = customizationService.loadCustomization(storageId);
            contentId = context.getContentId();
        }
        return contentId;
    }

    public <S extends Serializable> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception {
        Class<S> clazz = type.getContentType().getStateClass();
        if (state instanceof TransientApplicationState) {
            TransientApplicationState<S> transientState = (TransientApplicationState<S>) state;
            S prefs = transientState.getContentState();
            return prefs != null ? prefs : null;
        } else {
            String storageId;
            if (state instanceof CloneApplicationState) {
                storageId = ((CloneApplicationState<S>) state).getStorageId();
            } else {
                storageId = ((PersistentApplicationState<S>) state).getStorageId();
            }
            CustomizationContext context = customizationService.loadCustomization(storageId);
            return (S) context.getState();
        }
    }

    public <S extends Serializable> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception {
        if (state instanceof TransientApplicationState) {
            throw new AssertionError("Does not make sense");
        } else {
            String storageId;
            if (state instanceof PersistentApplicationState) {
                storageId = ((PersistentApplicationState<S>) state).getStorageId();
            } else {
                storageId = ((CloneApplicationState<S>) state).getStorageId();
            }
            CustomizationContext context = customizationService.loadCustomization(storageId);
            context.setState((Serializable) preferences);
            customizationService.saveCustomization(context);
            return state;
        }
    }

    public <T> LazyPageList<T> find(Query<T> q) throws Exception {
        return find(q, null);
    }

    public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception {
        Class<T> type = q.getClassType();
        if (PageData.class.equals(type)) {
            throw new UnsupportedOperationException("Use PageService.findPages to instead of");
        } else if (PortletPreferences.class.equals(type)) {
            return (LazyPageList<T>) pomMgr.execute(new SearchTask.FindPortletPreferences((Query<PortletPreferences>) q));
        } else if (PortalData.class.equals(type)) {
            return (LazyPageList<T>) pomMgr.execute(new SearchTask.FindSite((Query<PortalData>) q));
        } else if (PortalKey.class.equals(type) && "portal".equals(q.getOwnerType())) {
            QueryResult<SiteKey> keys = siteService.findSites(SiteType.PORTAL);
            ArrayList ret = new ArrayList(keys.getSize());
            for (SiteKey key : keys) {
                ret.add(new PortalKey(key));
            }
            return new LazyPageList(new ListAccessImpl(PortalKey.class, ret), 10);
        } else if (PortalKey.class.equals(type) && "group".equals(q.getOwnerType())) {
            QueryResult<SiteKey> keys = siteService.findSites(SiteType.GROUP);
            ArrayList ret = new ArrayList(keys.getSize());
            for (SiteKey key : keys) {
                ret.add(new PortalKey(key));
            }
            return new LazyPageList(new ListAccessImpl(PortalKey.class, ret), 10);
        } else {
            throw new UnsupportedOperationException("Could not perform search on query " + q);
        }
    }

    /**
     * This is a hack and should be removed, it is only used temporarily. This is because the objects are loaded from files and
     * don't have name.
     */
    private void generateStorageName(ModelObject obj) {
        if (obj instanceof Container) {
            for (ModelObject child : ((Container) obj).getChildren()) {
                generateStorageName(child);
            }
        } else if (obj instanceof Application) {
            obj.setStorageName(UUID.randomUUID().toString());
        }
    }

    public DashboardData loadDashboard(String dashboardId) throws Exception {
        return pomMgr.execute(new DashboardTask.Load(dashboardId));
    }

    public void saveDashboard(DashboardData dashboard) throws Exception {
        pomMgr.execute(new DashboardTask.Save(dashboard));
    }

    public Container getSharedLayout() throws Exception {
        String path = "war:/conf/portal/portal/sharedlayout.xml";
        String out = IOUtil.getStreamContentAsString(confManager_.getInputStream(path));
        ByteArrayInputStream is = new ByteArrayInputStream(out.getBytes("UTF-8"));
        IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
        UnmarshallingContext uctx = (UnmarshallingContext) bfact.createUnmarshallingContext();
        uctx.setDocument(is, null, "UTF-8", false);
        Container container = (Container) uctx.unmarshalElement();
        generateStorageName(container);
        return container;
    }

    public void save() throws Exception {
        pomMgr.execute(new POMTask<Object>() {
            public Object run(POMSession session) {
                session.save();
                return null;
            }
        });
    }

    public <A> A adapt(ModelData modelData, Class<A> type) {
        return adapt(modelData, type, true);
    }

    public <A> A adapt(ModelData modelData, Class<A> type, boolean create) {
        try {
            POMSession pomSession = pomMgr.getSession();
            ChromatticSession chromSession = pomSession.getSession();

            // TODO: Deal with the case where modelData is not persisted before invocation to adapt
            // Get the workspace object
            Object o = pomSession.findObjectById(modelData.getStorageId());

            A a = chromSession.getEmbedded(o, type);
            if (a == null && create) {
                a = chromSession.create(type);
                chromSession.setEmbedded(o, type, a);
            }

            return a;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * If we are in JTA environment and there are pending changes in MOP, we will commit current JTA transaction and start new.
     * This will enforce that query result will contain latest persistent stuff
     */
    private void syncUserTransactionIfJTAEnabled() {
        try {
            if (jtaUserTransactionLifecycleService.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                POMSession pomSession = pomMgr.getSession();
                if (pomSession.isModified()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Active JTA transaction found. Going to sync MOP session and JTA transaction");
                    }

                    // Sync current MOP session first
                    pomSession.save();

                    jtaUserTransactionLifecycleService.finishJTATransaction();
                    jtaUserTransactionLifecycleService.beginJTATransaction();
                }
            }
        } catch (Exception e) {
            log.warn("Error during sync of JTA transaction", e);
        }
    }

    public String[] getSiteInfo(String workspaceObjectId) throws Exception {

        POMSession session = pomMgr.getSession();

        WorkspaceObject workspaceObject = session.findObjectById(workspaceObjectId);

        if (workspaceObject instanceof UIComponent) {
            Site site = ((UIComponent) workspaceObject).getPage().getSite();
            ObjectType<? extends Site> siteType = site.getObjectType();

            String[] siteInfo = new String[2];

            // Put the siteType on returned map
            if (siteType == ObjectType.PORTAL_SITE) {
                siteInfo[0] = PortalConfig.PORTAL_TYPE;
            } else if (siteType == ObjectType.GROUP_SITE) {
                siteInfo[0] = PortalConfig.GROUP_TYPE;
            } else if (siteType == ObjectType.USER_SITE) {
                siteInfo[0] = PortalConfig.USER_TYPE;
            }

            // Put the siteOwner on returned map
            siteInfo[1] = site.getName();

            return siteInfo;
        }

        throw new Exception("The provided ID is not associated with an application");
    }

    public <S extends Serializable> ApplicationData<S> getApplicationData(String applicationStorageId) {
        // TODO Auto-generated method stub

        POMSession session = pomMgr.getSession();
        WorkspaceObject workspaceObject = session.findObjectById(applicationStorageId);

        if (workspaceObject instanceof UIWindow) {
            UIWindow application = (UIWindow) workspaceObject;
            Mapper mapper = new Mapper(session);

            ApplicationData data = mapper.load(application);
            return data;
        }
        throw new NoSuchDataException("Could not load the application data specified by the ID: " + applicationStorageId);
    }
}
