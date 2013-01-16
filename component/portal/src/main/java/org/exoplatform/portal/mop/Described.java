/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.mop;

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;
import org.gatein.portal.mop.description.DescriptionState;

/**
 * Something having a human readable name and a description. The semantic of the name is to be human readable, it can be
 * expressed under various ways : name, display name, label, title, etc...
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@MixinType(name = "gtn:described")
public abstract class Described {

    /**
     * Returns the namet.
     *
     * @return the name
     */
    @Property(name = "gtn:name")
    public abstract String getName();

    /**
     * Update the name.
     *
     * @param name the new name
     */
    public abstract void setName(String name);

    /**
     * Returns the description.
     *
     * @return the description
     */
    @Property(name = "gtn:description")
    public abstract String getDescription();

    /**
     * Update the description.
     *
     * @param description the new description
     */
    public abstract void setDescription(String description);

    /**
     * Return the state.
     *
     * @return the state
     */
    public DescriptionState getState() {
        String name = getName();
        String description = getDescription();
        return new DescriptionState(name, description);
    }

    /**
     * Update the state.
     *
     * @param state the new state
     * @throws NullPointerException if the new state is null
     */
    public void setState(DescriptionState state) throws NullPointerException {
        if (state == null) {
            throw new NullPointerException("No null state accepted");
        }

        //
        setName(state.getName());
        setDescription(state.getDescription());
    }

}
