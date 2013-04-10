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

package org.exoplatform.portal.mop.description;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.i18n.I18NAdapter;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.portal.mop.description.DescriptionStore;
import org.gatein.portal.mop.description.DescriptionState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopStore implements DescriptionStore {

    /** . */
    private final POMSessionManager manager;

    /** . */
    private DataCache cache;

    public MopStore(POMSessionManager manager, DataCache cache) {
        this.manager = manager;
        this.cache = cache;
    }

    @Override
    public DescriptionState loadDescription(String id, Locale locale, boolean resolve) {
        if (resolve) {
            POMSession session = manager.getSession();
            return cache.getState(session, new CacheKey(locale, id));
        } else {
            POMSession session = manager.getSession();
            WorkspaceObject obj = session.findObjectById(id);
            I18NAdapter able = obj.adapt(I18NAdapter.class);
            Described desc;
            if (locale != null) {
                desc = able.getI18NMixin(Described.class, locale, false);
            } else {
                desc = able.getMixin(Described.class, false);
            }
            return desc != null ? desc.getState() : null;
        }
    }

    @Override
    public void saveDescription(String id, Locale locale, DescriptionState description) {
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        I18NAdapter able = obj.adapt(I18NAdapter.class);
        Described desc = able.getI18NMixin(Described.class, locale, true);
        cache.removeState(new CacheKey(locale, id));
        desc.setState(description);
    }

    @Override
    public void loadDescription(String id, DescriptionState description) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        I18NAdapter able = obj.adapt(I18NAdapter.class);
        if (description != null) {
            Described desc = able.getMixin(Described.class, true);
            desc.setState(description);
        } else {
            able.removeMixin(Described.class);
        }
    }

    @Override
    public Map<Locale, DescriptionState> loadDescriptions(String id) {
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        I18NAdapter able = obj.adapt(I18NAdapter.class);
        Map<Locale, Described> mixins = able.getI18NMixin(Described.class);
        Map<Locale, DescriptionState> names = null;
        if (mixins != null) {
            names = new HashMap<Locale, DescriptionState>(mixins.size());
            for (Map.Entry<Locale, Described> entry : mixins.entrySet()) {
                names.put(entry.getKey(), entry.getValue().getState());
            }
        }
        return names;
    }

    @Override
    public void saveDescriptions(String id, Map<Locale, DescriptionState> descriptions) {
        if (id == null) {
            throw new NullPointerException("No null id accepted");
        }
        POMSession session = manager.getSession();
        WorkspaceObject obj = session.findObjectById(id);
        I18NAdapter able = obj.adapt(I18NAdapter.class);
        Collection<Locale> locales = able.removeI18NMixin(Described.class);
        for (Locale locale : locales) {
            cache.removeState(new CacheKey(locale, id));
        }

        // Interface specifies it allows a null description map
        if (descriptions != null) {
            for (Map.Entry<Locale, DescriptionState> entry : descriptions.entrySet()) {
                Described described = able.addI18NMixin(Described.class, entry.getKey());
                described.setState(entry.getValue());
            }
        }
    }
}
