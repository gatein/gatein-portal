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

package org.gatein.portal.impl.mop.ram;

import java.io.Serializable;

import org.exoplatform.portal.config.model.TransientApplicationState;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.customization.CustomizationData;
import org.gatein.portal.mop.customization.CustomizationStore;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class RamCustomizationStore implements CustomizationStore {

    /** . */
    private final Store store;

    public RamCustomizationStore(RamStore store) {
        this.store = store.store;
    }

    @Override
    public <S extends Serializable> CustomizationData<S> loadCustomization(String id) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        Node customization = current.getNode(id);
        ElementState.Window windowState = (ElementState.Window) customization.getState();
        TransientApplicationState appState = (TransientApplicationState) windowState.state;
        return new CustomizationData(id, windowState.type, appState.getContentId(), appState.getContentState());
    }

    @Override
    public <S extends Serializable> S saveCustomization(String id, S state) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        Node customization = current.getNode(id);
        ElementState.Window windowState = (ElementState.Window) customization.getState();
        TransientApplicationState appState = (TransientApplicationState) windowState.state;
        appState = new TransientApplicationState(appState.getContentId(), state, appState.getOwnerType(), appState.getOwnerId());
        windowState = new ElementState.Window(windowState.type, appState, windowState.properties);
        current.update(id, windowState);
        return state;
    }

    @Override
    public String cloneCustomization(String id) {
        throw new UnsupportedOperationException();
    }
}
