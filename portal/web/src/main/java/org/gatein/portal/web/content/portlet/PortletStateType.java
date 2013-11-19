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
package org.gatein.portal.web.content.portlet;

import org.gatein.pc.portlet.state.producer.PortletState;

/**
 * @author Julien Viet
 */
class PortletStateType extends org.gatein.pc.api.PortletStateType<PortletState> {

    /** . */
    static final PortletStateType INSTANCE = new PortletStateType();

    @Override
    public Class<PortletState> getJavaType() {
        return PortletState.class;
    }

    @Override
    public boolean equals(PortletState state1, PortletState state2) {
        return state1.getProperties().equals(state2.getProperties());
    }

    @Override
    public int hashCode(PortletState state) {
        return state.getProperties().hashCode();
    }

    @Override
    public String toString(PortletState state) {
        return state.getProperties().toString();
    }
}
