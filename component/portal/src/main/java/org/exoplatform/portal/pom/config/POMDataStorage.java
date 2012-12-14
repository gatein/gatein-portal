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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.transaction.Status;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.LazyPageList;
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
import org.exoplatform.portal.pom.config.tasks.PageTask;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.PreferencesTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
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

    public POMDataStorage(final POMSessionManager pomMgr, ConfigurationManager confManager,
            JTAUserTransactionLifecycleService jtaUserTransactionLifecycleService, ListenerService listenerService) {

        // Invalidation bridge : listen for PageService events and invalidate the DataStorage cache
        Listener<?, org.exoplatform.portal.mop.page.PageKey> invalidator = new Listener<Object, org.exoplatform.portal.mop.page.PageKey>() {
            @Override
            public void onEvent(Event<Object, org.exoplatform.portal.mop.page.PageKey> event) throws Exception {
                org.exoplatform.portal.mop.page.PageKey key = event.getData();
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
    }

    public PortalData getPortalConfig(PortalKey key) throws Exception {
        return pomMgr.execute(new PortalConfigTask.Load(key));
    }

    public void create(PortalData config) throws Exception {
        pomMgr.execute(new PortalConfigTask.Save(config, false));
    }

    public void save(PortalData config) throws Exception {
        pomMgr.execute(new PortalConfigTask.Save(config, true));
    }

    public void remove(PortalData config) throws Exception {
        pomMgr.execute(new PortalConfigTask.Remove(config.getKey()));
    }

    public PageData getPage(PageKey key) throws Exception {
        return pomMgr.execute(new PageTask.Load(key));
    }

    public List<ModelChange> save(PageData page) throws Exception {
        PageTask.Save task = new PageTask.Save(page);
        pomMgr.execute(task);
        return task.getChanges();
    }

    public <S> String getId(ApplicationState<S> state) throws Exception {
        String contentId;
        if (state instanceof TransientApplicationState) {
            TransientApplicationState tstate = (TransientApplicationState) state;
            contentId = tstate.getContentId();
        } else if (state instanceof PersistentApplicationState) {
            PersistentApplicationState pstate = (PersistentApplicationState) state;
            contentId = pomMgr.execute(new PreferencesTask.GetContentId<S>(pstate.getStorageId()));
        } else if (state instanceof CloneApplicationState) {
            CloneApplicationState cstate = (CloneApplicationState) state;
            contentId = pomMgr.execute(new PreferencesTask.GetContentId<S>(cstate.getStorageId()));
        } else {
            throw new AssertionError();
        }

        //
        return contentId;
    }

    public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception {
        Class<S> clazz = type.getContentType().getStateClass();
        if (state instanceof TransientApplicationState) {
            TransientApplicationState<S> transientState = (TransientApplicationState<S>) state;
            S prefs = transientState.getContentState();
            return prefs != null ? prefs : null;
        } else if (state instanceof CloneApplicationState) {
            PreferencesTask.Load<S> load = new PreferencesTask.Load<S>(((CloneApplicationState<S>) state).getStorageId(), clazz);
            return pomMgr.execute(load);
        } else {
            PreferencesTask.Load<S> load = new PreferencesTask.Load<S>(((PersistentApplicationState<S>) state).getStorageId(),
                    clazz);
            return pomMgr.execute(load);
        }
    }

    public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception {
        if (state instanceof TransientApplicationState) {
            throw new AssertionError("Does not make sense");
        } else {
            if (state instanceof PersistentApplicationState) {
                PreferencesTask.Save<S> save = new PreferencesTask.Save<S>(
                        ((PersistentApplicationState<S>) state).getStorageId(), preferences);
                pomMgr.execute(save);
            } else {
                PreferencesTask.Save<S> save = new PreferencesTask.Save<S>(((CloneApplicationState<S>) state).getStorageId(),
                        preferences);
                pomMgr.execute(save);
            }
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
            return (LazyPageList<T>) pomMgr.execute(new SearchTask.FindSiteKey((Query<PortalKey>) q));
        } else if (PortalKey.class.equals(type) && "group".equals(q.getOwnerType())) {
            return (LazyPageList<T>) pomMgr.execute(new SearchTask.FindSiteKey((Query<PortalKey>) q));
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

    public <S> ApplicationData<S> getApplicationData(String applicationStorageId) {
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
