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

package org.exoplatform.portal.pom.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LayoutModelImpl implements NodeModel<ComponentData, ElementState> {

    @Override
    public NodeContext<ComponentData, ElementState> getContext(ComponentData node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ComponentData create(final NodeContext<ComponentData, ElementState> context) {
        ElementState state = context.getState();
        if (state instanceof ElementState.Container) {
            ElementState.Container container = (ElementState.Container) state;
            return new ContainerData(
                    context.getId(),
                    container.id,
                    container.name,
                    container.icon,
                    container.template,
                    container.factoryId,
                    container.title,
                    container.description,
                    container.width,
                    container.height,
                    container.accessPermissions,
                    null
            ) {
                @Override
                public List<ComponentData> getChildren() {
                    // For now we copy it and make it unmodifiable
                    return Collections.unmodifiableList(new ArrayList<ComponentData>(context.getNodes()));
                }
            };


        } else if (state instanceof ElementState.Window) {
            ElementState.Window window = (ElementState.Window) state;
            return new ApplicationData(
                    context.getId(),
                    context.getName(),
                    window.type,
                    window.state,
                    null,
                    window.title,
                    window.icon,
                    window.description,
                    window.showInfoBar,
                    window.showApplicationState,
                    window.showApplicationMode,
                    window.theme,
                    window.width,
                    window.height,
                    window.properties,
                    window.accessPermissions
            );
        } else  {
            throw new UnsupportedOperationException("todo : " + state.getClass().getName());
        }



    }
}
