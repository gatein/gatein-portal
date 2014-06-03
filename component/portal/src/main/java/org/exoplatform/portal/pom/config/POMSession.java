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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.commons.chromattic.SynchronizationListener;
import org.exoplatform.commons.chromattic.SynchronizationStatus;
import org.exoplatform.portal.config.NoSuchDataException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.Model;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.mop.core.api.ModelImpl;
import org.gatein.mop.core.api.workspace.NavigationImpl;
import org.gatein.mop.core.api.workspace.PageImpl;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class POMSession {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(POMSession.class);

    /** . */
    private static final Map<ObjectType<?>, Class> mapping = new HashMap<ObjectType<?>, Class>();

    static {
        mapping.put(ObjectType.PAGE, PageImpl.class);
        mapping.put(ObjectType.NAVIGATION, NavigationImpl.class);
    }

    /** . */
    final POMSessionManager mgr;

    /** . */
    private ModelImpl model;

    /** . */
    private boolean isInTask;

    /** . */
    private boolean markedForRollback;

    /** . */
    private List<Serializable> staleKeys;

    /** . */
    private boolean modified;

    /** . */
    private SessionContext context;

    /** . */
    private MOPChromatticLifeCycle configurator;

    public POMSession(POMSessionManager mgr, MOPChromatticLifeCycle configurator, SessionContext context) {
        // Register for cache eviction
        context.addSynchronizationListener(listener);

        //
        this.mgr = mgr;
        this.isInTask = false;
        this.markedForRollback = false;
        this.staleKeys = null;
        this.configurator = configurator;
        this.context = context;
    }

    public Object getFromCache(Serializable key) {
        if (isModified()) {
            throw new IllegalStateException("Cannot read object in shared cache from a modified session");
        }
        return mgr.cacheGet(key);
    }

    public void putInCache(Serializable key, Object value) {
        if (isModified()) {
            throw new IllegalStateException("Cannot put object in shared cache from a modified session");
        }
        mgr.cachePut(key, value);
    }

    public void scheduleForEviction(Serializable key) {
        if (key == null) {
            throw new NullPointerException();
        }
        if (staleKeys == null) {
            staleKeys = new LinkedList<Serializable>();
        }
        staleKeys.add(key);
    }

    private Model getModel() {
        if (model == null) {
            model = mgr.getPOMService().getModel();
        }
        return model;
    }

    public boolean isModified() {
        if (modified) {
            return true;
        }
        try {
            ChromatticSession session = getSession();
            Session jcrSession = session.getJCRSession();
            modified = jcrSession.hasPendingChanges();
        } catch (RepositoryException e) {
            throw new UndeclaredRepositoryException(e);
        }
        return modified;
    }

    protected ChromatticSession getSession() {
        return context.getSession();
    }

    public Workspace getWorkspace() {
        return getModel().getWorkspace();
    }

    public boolean isMarkedForRollback() {
        return markedForRollback;
    }

    public String pathOf(WorkspaceObject o) {
        return getModel().pathOf(o);
    }

    public <O extends WorkspaceObject> Iterator<O> findObject(ObjectType<O> ownerType, String statement) {
        this.save();
        return getModel().findObject(ownerType, statement);
    }

    public <O extends WorkspaceObject> O findObjectById(ObjectType<O> ownerType, String id) {
        return getModel().findObjectById(ownerType, id);
    }

    public WorkspaceObject findObjectById(String id) {
        return findObjectById(ObjectType.ANY, id);
    }

    public Customization<?> findCustomizationById(String id) {
        Customization<?> customization = getModel().findCustomizationById(id);
        if (customization == null) {
            throw new NoSuchDataException("Can not find " + id);
        }
        return customization;
    }

    public POMSessionManager getManager() {
        return mgr;
    }

    private static final BaseEncodingObjectFormatter formatter = new BaseEncodingObjectFormatter();

    public <O extends WorkspaceObject> QueryResult<O> findObjects(ObjectType<O> type, ObjectType<Site> siteType,
            String ownerId, String title, int offset, int limit) {
        this.save();
        //
        String ownerIdChunk = "%";
        if (ownerId != null) {
            ownerId = ownerId.trim();
            if (!ownerId.isEmpty()) {
                ownerIdChunk = "mop:" + formatter.encodeNodeName(null, ownerId);
            }
        }

        //
        String ownerTypeChunk;
        if (siteType != null) {
            if (siteType == ObjectType.PORTAL_SITE) {
                ownerTypeChunk = "mop:portalsites";
            } else if (siteType == ObjectType.GROUP_SITE) {
                ownerTypeChunk = "mop:groupsites";
            } else {
                ownerTypeChunk = "mop:usersites";
            }
        } else {
            ownerTypeChunk = "%";
        }

        //
        Workspace workspace = getWorkspace();
        String workspaceChunk = model.pathOf(workspace);

        //
        String statement;
        try {
            if (title != null) {
                title = Utils.queryEscape(title);
                if (type == ObjectType.PAGE) {
                    statement = "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                            + "/mop:rootpage/mop:children/mop:pages/mop:children/%' AND " + "(" + "LOWER(gtn:name) LIKE '%"
                            + title.trim().toLowerCase() + "%' ESCAPE '\\')";
                } else {
                    throw new UnsupportedOperationException();
                }
            } else {
                if (type == ObjectType.PAGE) {
                    statement = "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                            + "/mop:rootpage/mop:children/mop:pages/mop:children/%'";
                } else {
                    statement = "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                            + "/mop:rootnavigation/mop:children/mop:default'";
                }
            }
        } catch (IllegalArgumentException e) {
            if (type == ObjectType.PAGE) {
                statement = "jcr:path LIKE ''";
            } else {
                statement = "jcr:path LIKE ''";
            }
        }

        String defaultOrderBy = "gtn:name";

        // Temporary work around, to fix in MOP and then remove
        ChromatticSession session = context.getSession();
        Class<O> mappedClass = (Class<O>) mapping.get(type);

        QueryBuilder<O> queryBuilder;
        if (type == ObjectType.PAGE) {
            queryBuilder = session.createQueryBuilder(mappedClass).where(statement).orderBy(defaultOrderBy);
        } else {
            queryBuilder = session.createQueryBuilder(mappedClass).where(statement);
        }

        return queryBuilder.get().objects((long) offset, (long) limit);
    }

    private final SynchronizationListener listener = new SynchronizationListener() {
        public void beforeSynchronization() {
        }

        public void afterSynchronization(SynchronizationStatus status) {
            if (status == SynchronizationStatus.SAVED) {
                reset();
            }
        }
    };

    /**
     * Reset the session and set its state like it was a newly created session.
     */
    private void reset() {
        // Evict entries from the shared cache if any
        if (staleKeys != null && staleKeys.size() > 0) {
            if (log.isTraceEnabled()) {
                log.trace("About to evict entries " + staleKeys);
            }
            for (Serializable key : staleKeys) {
                mgr.cacheRemove(key);
            }
            staleKeys.clear();
        }

        // Reset modified flag
        if (log.isTraceEnabled()) {
            log.trace("Setting modified flag to false");
        }
        modified = false;
    }

    public <V> V execute(POMTask<V> task) throws Exception {
        if (isInTask) {
            throw new IllegalStateException();
        }

        //
        boolean needRollback = true;
        try {
            isInTask = true;
            V v = task.run(this);
            needRollback = false;
            return v;
        } catch (Exception e) {
            throw e;
        } finally {
            isInTask = false;
            markedForRollback = needRollback;
        }
    }

    public void save() {
        if (model != null) {
            if (!markedForRollback) {
                // Trigger persistent save
                model.save();

                // Reset modified state
                reset();
            } else {
                log.debug("Will not save session that is marked for rollback");
            }
        }
    }

    /**
     * <p>
     * Closes the current session and discard the changes done during the session.
     * </p>
     *
     * @see #close(boolean)
     */
    public void close() {
        close(false);
    }

    /**
     * <p>
     * Closes the current session and optionally saves its content. If no session is associated then this method has no effects
     * and returns false.
     * </p>
     *
     * @param save if the session must be saved
     */
    public void close(boolean save) {
        if (save) {
            save();
        }

        // Close model
        if (model != null) {
            model.close();
        }

        //
        configurator.closeContext(save & markedForRollback);
    }
}
