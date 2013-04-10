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

package org.gatein.portal.mop.customization;

import java.io.Serializable;

import org.exoplatform.portal.mop.customization.MopStore;
import org.exoplatform.portal.mop.customization.SimpleDataCache;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CustomizationServiceWrapper implements CustomizationService {

    /** . */
    private final CustomizationServiceImpl service;

    public CustomizationServiceWrapper(POMSessionManager manager) {
        this.service = new CustomizationServiceImpl(new MopStore(manager, new SimpleDataCache()));
    }

    @Override
    public <S extends Serializable> CustomizationContext<S> loadCustomization(String id) throws NullPointerException {
        return service.loadCustomization(id);
    }

    @Override
    public <S extends Serializable> void saveCustomization(CustomizationContext<S> customization) {
        service.saveCustomization(customization);
    }

    @Override
    public <S extends Serializable> CustomizationContext<S> cloneCustomization(String srcId, String dstId) {
        return service.cloneCustomization(srcId, dstId);
    }

    @Override
    public void destroyCustomization(String id) {
        service.destroyCustomization(id);
    }
}
