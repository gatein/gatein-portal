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
    private final DescriptionStore store;

    public DescriptionServiceImpl(DescriptionStore store) {
        if (store == null) {
            throw new NullPointerException("No null persistence allowed");
        }

        //
        this.store = store;
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
        DescriptionState state = store.loadDescription(id, locale1, true);
        if (state == null && locale2 != null) {
            state = store.loadDescription(id, locale2, true);
        }
        return state;
    }

    public DescriptionState loadDescription(String id, Locale locale) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        if (locale == null) {
            throw new NullPointerException("No null locale accepted");
        }
        return store.loadDescription(id, locale, false);
    }

    public void saveDescription(String id, Locale locale, DescriptionState description) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        if (locale == null) {
            throw new NullPointerException("No null locale accepted");
        }
        validateLocale(locale);
        store.saveDescription(id, locale, description);
    }

    public Map<Locale, DescriptionState> loadDescriptions(String id) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        return store.loadDescriptions(id);
    }

    public void saveDescriptions(String id, Map<Locale, DescriptionState> descriptions) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        for (Locale locale : descriptions.keySet()) {
            validateLocale(locale);
        }
        store.saveDescriptions(id, descriptions);
    }

    private void validateLocale(Locale locale) {
        if (locale.getLanguage().length() != 2) {
            throw new IllegalArgumentException("Illegal locale");
        }
        if (locale.getCountry().length() != 0 && locale.getCountry().length() != 2) {
            throw new IllegalArgumentException("Illegal locale");
        }
        if (locale.getVariant().length() != 0 && locale.getVariant().length() != 2) {
            throw new IllegalArgumentException("Illegal locale");
        }
    }
}
