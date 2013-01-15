/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop.site;

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Templatized;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SiteServiceImpl implements SiteService {

    /** . */
    final POMSessionManager manager;

    /** . */
    private final DataCache dataCache;

    public SiteServiceImpl(POMSessionManager manager) {
        this(manager, new SimpleDataCache());
    }

    public SiteServiceImpl(POMSessionManager manager, DataCache dataCache) throws NullPointerException {
        if (manager == null) {
            throw new NullPointerException("No null pom session manager allowed");
        }
        if (dataCache == null) {
            throw new NullPointerException("No null data cache allowed");
        }
        this.manager = manager;
        this.dataCache = dataCache;
    }

    @Override
    public SiteContext loadSite(SiteKey key) throws NullPointerException, SiteServiceException {
        if (key == null) {
            throw new NullPointerException();
        }

        //
        POMSession session = manager.getSession();
        SiteData data = dataCache.getSiteData(session, key);
        return data != null && data != SiteData.EMPTY ? new SiteContext(data) : null;
    }

    @Override
    public boolean saveSite(SiteContext context) throws NullPointerException, SiteServiceException {
        if (context == null) {
            throw new NullPointerException();
        }

        //
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(context.key.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, context.key.getName());

        //
        boolean created;
        if (site == null) {
            site = workspace.addSite(Utils.objectType(context.key.getType()), context.key.getName());
            Page root = site.getRootPage();
            root.addChild("pages");
            Page templates = root.addChild("templates");
            Page template = templates.addChild("default");
            Navigation navigation = site.getRootNavigation();
            Templatized templatized = navigation.getTemplatized();
            if (templatized != null) {
                templatized.setTemplate(template);
            } else {
                template.templatize(navigation);
            }
            created = true;
        } else {
            created = false;
        }

        //
        SiteState state = context.state;
        if (state != null) {
            //
            Attributes attrs = site.getAttributes();
            attrs.setValue(MappedAttributes.LOCALE, state.getLocale());
            attrs.setValue(MappedAttributes.SKIN, state.getSkin());
            if (state.getProperties() != null) {
                Mapper.save(state.getProperties(), attrs, SiteState.portalPropertiesBlackList);
            }

            ProtectedResource pr = site.adapt(ProtectedResource.class);
            pr.setAccessPermissions(state.getAccessPermissions());
            pr.setEditPermission(state.getEditPermission());

            Described described = site.adapt(Described.class);
            described.setName(state.getLabel());
            described.setDescription(state.getDescription());
        }

        //
        dataCache.removeSite(session, context.key);
        dataCache.removeSites(session, context.key.getType());

        // Update state
        context.data = dataCache.getSiteData(session, context.key);
        context.state = null;

        //
        return created;
    }

    @Override
    public boolean destroySite(SiteKey key) throws NullPointerException, SiteServiceException {
        if (key == null) {
            throw new NullPointerException("No null page argument");
        }

        //
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(key.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getName());

        //
        if (site != null) {
            site.destroy();
            dataCache.removeSite(session, key);
            dataCache.removeSites(session, key.getType());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public QueryResult<SiteKey> findSites(SiteType siteType) throws SiteServiceException {
        if (siteType == null) {
            throw new NullPointerException("No null site type accepted");
        }
        if (siteType == SiteType.USER) {
            throw new IllegalArgumentException("No site type user accepted");
        }

        //
        POMSession session = manager.getSession();
        ArrayList<SiteKey> keys = dataCache.getSites(session, siteType);
        if (keys == null) {
            Workspace workspace = session.getWorkspace();
            Collection<Site> sites = workspace.getSites(Utils.objectType(siteType));
            keys = new ArrayList<SiteKey>(sites.size());
            for (Site site : sites) {
                keys.add(new SiteKey(siteType, site.getName()));
            }
            dataCache.putSites(session, siteType, keys);
        }

        //
        return new QueryResult<SiteKey>(0, keys.size(), keys);
    }
}
