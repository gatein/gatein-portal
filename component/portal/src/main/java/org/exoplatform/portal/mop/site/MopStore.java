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
import java.util.Set;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
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
import org.gatein.mop.core.util.Tools;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteStore;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopStore implements SiteStore {

    /** . */
    static final Set<String> portalPropertiesBlackList = Tools.set(MappedAttributes.LOCALE.getName(), MappedAttributes.SKIN.getName());

    /** . */
    final POMSessionManager manager;

    /** . */
    final DataCache dataCache;

    public MopStore(POMSessionManager manager, DataCache dataCache) {
        this.manager = manager;
        this.dataCache = dataCache;
    }

    public SiteData loadSite(SiteKey key) {
        POMSession session = manager.getSession();
        SiteData data = dataCache.getSiteData(session, key);
        return data == SiteData.EMPTY ? null : data;
    }

    public boolean saveSite(SiteKey key, SiteState state) {
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(key.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getName());
        boolean created;
        if (site == null) {
            site = workspace.addSite(Utils.objectType(key.getType()), key.getName());
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
        if (state != null) {
            Attributes attrs = site.getAttributes();
            attrs.setValue(MappedAttributes.LOCALE, state.getLocale());
            attrs.setValue(MappedAttributes.SKIN, state.getSkin());
            if (state.getProperties() != null) {
                Mapper.save(state.getProperties(), attrs, portalPropertiesBlackList);
            }
            ProtectedResource pr = site.adapt(ProtectedResource.class);
            pr.setAccessPermissions(state.getAccessPermissions());
            pr.setEditPermissions(state.getEditPermissions());
            Described described = site.adapt(Described.class);
            described.setName(state.getLabel());
            described.setDescription(state.getDescription());
        }
        dataCache.removeSite(session, key);
        dataCache.removeSites(session, key.getType());
        return created;
    }

    public boolean destroySite(SiteKey key) {
        ObjectType<Site> objectType = Utils.objectType(key.getType());
        POMSession session = manager.getSession();
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getName());
        if (site != null) {
            site.destroy();
            dataCache.removeSite(session, key);
            dataCache.removeSites(session, key.getType());
            return true;
        } else {
            return false;
        }
    }

    public Collection<SiteKey> findSites(SiteType type) {
        POMSession session = manager.getSession();
        ArrayList<SiteKey> keys = dataCache.getSites(session, type);
        if (keys == null) {
            Workspace workspace = session.getWorkspace();
            Collection<Site> sites = workspace.getSites(Utils.objectType(type));
            keys = new ArrayList<SiteKey>(sites.size());
            for (Site site : sites) {
                keys.add(new SiteKey(type, site.getName()));
            }
            dataCache.putSites(session, type, keys);
        }
        return keys;
    }
}
