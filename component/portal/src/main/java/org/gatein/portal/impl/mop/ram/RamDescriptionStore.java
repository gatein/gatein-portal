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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.I18N;
import org.gatein.portal.mop.description.DescriptionStore;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.navigation.NodeState;

/**
* @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
*/
public class RamDescriptionStore implements DescriptionStore {

    /** . */
    private final Store store;

    public RamDescriptionStore(RamStore persistence) {
        this.store = persistence.store;
    }

    private Locale parent(Locale locale) {
        if (locale.getVariant() != null && !locale.getVariant().isEmpty()) {
            return new Locale(locale.getLanguage(), locale.getCountry());
        } else if (locale.getCountry() != null && !locale.getCountry().isEmpty()) {
            return new Locale(locale.getLanguage());
        } else {
            return null;
        }
    }

    /**
     * todo : move this code to DescriptionService
     */
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

    @Override
    public DescriptionState loadDescription(String id, Locale locale, boolean resolve) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        Node node = current.getNode(id);
        if (locale == null) {
            NodeState nodeState = (NodeState)node.getState();
            if (resolve || nodeState.getLabel() != null) {
                return new DescriptionState(nodeState.getLabel(), null);
            }
        } else {
            for (Locale l = locale; l != null; l = parent(l)) {
                String descriptions = current.getChild(id, "descriptions");
                if (descriptions != null) {
                    String description = current.getChild(descriptions, l.toString());
                    if (description != null) {
                        Node descriptionNode = current.getNode(description);
                        return (DescriptionState) descriptionNode.getState();
                    }
                }
                if (!resolve) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public void saveDescription(String id, Locale locale, DescriptionState state) {
        validateLocale(locale);
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String descriptions = current.getChild(id, "descriptions");
        if (descriptions == null) {
            descriptions = current.addChild(id, "descriptions", "not-yet-used");
        }
        String description = current.getChild(descriptions, locale.toString());
        if (description == null) {
            current.addChild(descriptions, locale.toString(), state);
        } else {
            current.update(description, state);
        }
    }

    @Override
    public void loadDescription(String id, DescriptionState description) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        NodeState state = (NodeState) current.getNode(id).getState();
        String label = description != null ? description.getName() : null;
        current.update(id, state.builder().label(label).build());
    }

    @Override
    public Map<Locale, DescriptionState> loadDescriptions(String id) {
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String descriptions = current.getChild(id, "descriptions");
        if (descriptions == null) {
            return null;
        } else {
            HashMap<Locale, DescriptionState> states = new HashMap<Locale, DescriptionState>();
            for (String child : current.getChildren(descriptions)) {
                Node node = current.getNode(child);
                Locale locale = I18N.parseJavaIdentifier(node.getName());
                DescriptionState state = (DescriptionState) node.getState();
                states.put(locale, state);
            }
            return states;
        }
    }

    @Override
    public void saveDescriptions(String id, Map<Locale, DescriptionState> states) {
        for (Locale locale : states.keySet()) {
            validateLocale(locale);
        }
        Tx tx = Tx.associate(store);
        Store current = tx.getContext();
        String descriptions = current.getChild(id, "descriptions");
        if (descriptions != null) {
            current.remove(descriptions);
        }
        if (states.size() > 0) {
            descriptions = current.addChild(id, "descriptions", "not-yet-used");
            for (Map.Entry<Locale, DescriptionState> state : states.entrySet()) {
                current.addChild(descriptions, state.getKey().toString(), state.getValue());
            }
        }
    }
}
