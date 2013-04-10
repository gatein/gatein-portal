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
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ui.UIWindow;
import org.gatein.portal.mop.customization.CustomizationData;
import org.gatein.portal.mop.customization.CustomizationStore;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopStore implements CustomizationStore {

    /** . */
    final POMSessionManager manager;

    /** . */
    final DataCache dataCache;

    public MopStore(POMSessionManager manager) {
        this(manager, new SimpleDataCache());
    }

    public MopStore(POMSessionManager manager, DataCache dataCache) {
        this.manager = manager;
        this.dataCache = dataCache;
    }

    @Override
    public <S extends Serializable> CustomizationData<S> loadCustomization(String id) {
        POMSession session = manager.getSession();
        return dataCache.getCustomizationData(session, id);
    }

    @Override
    public <S extends Serializable> CustomizationData<S> saveCustomization(String id, S state) {
        POMSession session = manager.getSession();
        UIWindow window = (UIWindow) session.findObjectById(id);
        Customization customization = window.getCustomization();
/*
        if (state.getContentType() == PortletState.CONTENT_TYPE) {
            Portlet portlet = new Portlet();
            for (Preference pref : ((PortletState)state.getState())) {
                portlet.putPreference(new org.exoplatform.portal.pom.spi.portlet.Preference(pref.getName(), pref.getValues(), pref.isReadOnly()));
            }
            toSave = portlet;
        } else {
            throw new UnsupportedOperationException("Not yet implemented");
        }
*/
        if (customization == null) {
            throw new UnsupportedOperationException("todo");
        } else {
            customization.setState(state);
        }
        return dataCache.loadCustomization(session, id);
    }

    @Override
    public String cloneCustomization(String id) {
        throw new UnsupportedOperationException("Implement me");
    }
}
