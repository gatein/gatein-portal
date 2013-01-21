/*
 * Copyright (C) 2011 eXo Platform SAS.
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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DescriptionServiceImpl implements DescriptionService {

    /** . */
    private final DescriptionPersistence persistence;

    public DescriptionServiceImpl(DescriptionPersistence persistence) {
        if (persistence == null) {
            throw new NullPointerException("No null persistence allowed");
        }

        //
        this.persistence = persistence;
    }

    public DescriptionState resolveDescription(String id, Locale locale) throws NullPointerException {
        return resolveDescription(id, null, locale);
    }

    public DescriptionState resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        if (locale1 == null) {
            throw new NullPointerException("No null locale accepted");
        }

        //
        DescriptionState state = persistence.resolveDescription(id, locale1);
        if (state == null && locale2 != null) {
            state = persistence.resolveDescription(id, locale2);
        }
        return state;
    }

    public DescriptionState getDescription(String id, Locale locale) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        if (locale == null) {
            throw new NullPointerException("No null locale accepted");
        }
        return persistence.getDescription(id, locale);
    }

    public DescriptionState getDescription(String id) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        return persistence.getDescription(id, null);
    }

    public void setDescription(String id, Locale locale, DescriptionState description) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        if (locale == null) {
            throw new NullPointerException("No null locale accepted");
        }
        persistence.setDescription(id, locale, description);
    }

    public void setDescription(String id, DescriptionState description) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        persistence.setDescription(id, description);
    }

    public Map<Locale, DescriptionState> getDescriptions(String id) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        return persistence.getDescriptions(id);
    }

    public void setDescriptions(String id, Map<Locale, DescriptionState> descriptions) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        persistence.setDescriptions(id, descriptions);
    }
}
