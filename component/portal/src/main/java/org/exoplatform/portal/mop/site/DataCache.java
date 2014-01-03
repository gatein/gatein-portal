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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Templatized;
import org.gatein.portal.mop.site.SiteData;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteState;
import org.gatein.portal.mop.site.SiteType;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class DataCache {

    protected abstract SiteData getSite(POMSession session, SiteKey key);

    protected abstract void removeSite(POMSession session, SiteKey key);

    protected abstract void putSite(SiteData data);

    protected abstract ArrayList<SiteKey> getSites(POMSession session, SiteType key);

    protected abstract void putSites(POMSession session, SiteType key, ArrayList<SiteKey> sites);

    protected abstract void removeSites(POMSession session, SiteType key);

    protected abstract void clear();

    final SiteData getSiteData(POMSession session, SiteKey key) {
        SiteData data;
        if (session.isModified()) {
            data = loadSite(session, key);
        } else {
            data = getSite(session, key);
        }

        //
        return data;
    }

    protected final SiteData loadSite(POMSession session, SiteKey key) {
        Workspace workspace = session.getWorkspace();
        ObjectType<Site> objectType = Utils.objectType(key.getType());
        Site site = workspace.getSite(objectType, key.getName());
        if (site != null) {
            Attributes attrs = site.getAttributes();
            List<String> accessPermissions = Collections.emptyList();
            List<String> editPermissions = null;
            if (site.isAdapted(ProtectedResource.class)) {
                ProtectedResource pr = site.adapt(ProtectedResource.class);
                accessPermissions = pr.getAccessPermissions();
                editPermissions = pr.getEditPermissions();
            }
            Described described = site.adapt(Described.class);
            Map<String, String> properties = new HashMap<String, String>();
            Mapper.load(attrs, properties, MopStore.portalPropertiesBlackList);
            Templatized templatized = site.getRootNavigation().getTemplatized();
            org.gatein.mop.api.workspace.Page layout = templatized.getTemplate();
            SiteState state = new SiteState(
                    attrs.getValue(MappedAttributes.LOCALE),
                    described.getName(),
                    described.getDescription(),
                    accessPermissions,
                    editPermissions,
                    properties,
                    attrs.getValue(MappedAttributes.SKIN)
            );
            return new SiteData(
                    key,
                    site.getObjectId(),
                    layout.getRootComponent().getObjectId(),
                    state);
        } else {
            return SiteData.EMPTY;
        }
    }
}
