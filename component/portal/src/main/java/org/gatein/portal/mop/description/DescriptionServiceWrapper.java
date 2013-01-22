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

package org.gatein.portal.mop.description;

import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.description.ExoDataCache;
import org.exoplatform.portal.mop.description.MopPersistence;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.cache.CacheService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DescriptionServiceWrapper implements DescriptionService {

    /** . */
    private final DescriptionServiceImpl service;

    public DescriptionServiceWrapper(POMSessionManager manager, CacheService cacheService) {
        this.service = new DescriptionServiceImpl(new MopPersistence(manager, new ExoDataCache(cacheService)));
    }

    @Override
    public DescriptionState resolveDescription(String id, Locale locale) throws NullPointerException {
        return service.resolveDescription(id, locale);
    }

    @Override
    public DescriptionState resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException {
        return service.resolveDescription(id, locale2, locale1);
    }

    @Override
    public DescriptionState getDescription(String id, Locale locale) {
        return service.getDescription(id, locale);
    }

    @Override
    public DescriptionState getDescription(String id) {
        return service.getDescription(id);
    }

    @Override
    public void setDescription(String id, Locale locale, DescriptionState description) {
        service.setDescription(id, locale, description);
    }

    @Override
    public void setDescription(String id, DescriptionState description) {
        service.setDescription(id, description);
    }

    @Override
    public Map<Locale, DescriptionState> getDescriptions(String id) {
        return service.getDescriptions(id);
    }

    @Override
    public void setDescriptions(String id, Map<Locale, DescriptionState> descriptions) {
        service.setDescriptions(id, descriptions);
    }
}
