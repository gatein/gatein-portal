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

package org.exoplatform.portal.mop.customization;

import java.io.Serializable;

import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.portal.mop.customization.CustomizationData;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class DataCache {

    protected abstract CustomizationData getCustomization(POMSession session, String key);

    protected abstract void removeCustomization(POMSession session, String key);

//    protected abstract void putCustomization(CustomizationData data);

    protected abstract void clear();

    final <S extends Serializable> CustomizationData<S> getCustomizationData(POMSession session, String key) {
        CustomizationData data;
        if (session.isModified()) {
            data = loadCustomization(session, key);
        } else {
            data = getCustomization(session, key);
        }

        //
        return data;
    }

    protected final <S extends Serializable> CustomizationData<S> loadCustomization(POMSession session, String key) {
        UIWindow window = (UIWindow) session.findObjectById(key);
        Customization<?> customization = window.getCustomization();
        if (customization == null) {
            return null;
        } else {
            ContentType<?> contentType = customization.getType();
            Serializable intrinsicState = (Serializable) customization.getState();

            // State adaptation
            S adaptedState = (S) intrinsicState;
//            if (intrinsicState != null) {
//                if (contentType.getMimeType().equals("application/portlet")) {
//                    PortletState.Builder ps = PortletState.EMPTY.builder();
//                    Portlet portlet = (Portlet) intrinsicState;
//                    for (Preference pref : portlet) {
//                        ps.put(pref.getName(), pref.getValues(), pref.isReadOnly());
//                    }
//                    adaptedState = (S) ps.build();
//                } else {
//                    throw new UnsupportedOperationException("Not yet supported");
//                }
//            } else {
//                adaptedState = null;
//            }

            //
            org.gatein.portal.mop.customization.ContentType<S> adapted = org.gatein.portal.mop.customization.ContentType.forValue(contentType.getMimeType());
            return new CustomizationData<S>(key, adapted, customization.getContentId(), adaptedState);
        }



/*
        ObjectType<Site> objectType = Utils.objectType(key.getType());
        Site site = workspace.getSite(objectType, key.getName());
        if (site != null) {
            Attributes attrs = site.getAttributes();
            List<String> accessPermissions = Collections.emptyList();
            String editPermission = null;
            if (site.isAdapted(ProtectedResource.class)) {
                ProtectedResource pr = site.adapt(ProtectedResource.class);
                accessPermissions = pr.getAccessPermissions();
                editPermission = pr.getEditPermission();
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
                    editPermission,
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
*/
    }
}
