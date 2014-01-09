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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.hierarchy.ModelAdapter;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ContainerAdapter implements ModelAdapter<ComponentData, ElementState> {

    /** . */
    final ContainerData root;

    /** . */
    final IdentityHashMap<ComponentData, String> handles = new IdentityHashMap<ComponentData, String>();

    public ContainerAdapter(ContainerData root) {
        this.root = root;
    }

    @Override
    public String getName(ComponentData node) {
        String name = node.getStorageName();
        if (name == null) {
            // For now we generate a name
            // however the name should be fully provided by the node (possibly randomly generated)
            name = UUID.randomUUID().toString();
        }
        return name;
    }

    @Override
    public String getId(ComponentData node) {
        return node.getStorageId();
    }

    @Override
    public ElementState getState(ComponentData node) {
        return create(node);
    }

    @Override
    public ContainerData getParent(ComponentData node) {
        return getParent(root, node);
    }

    private ContainerData getParent(ContainerData container, ComponentData node) {
        for (ComponentData child : container.getChildren()) {
            if (child == node) {
                return container;
            } else if (child instanceof ContainerData) {
                ContainerData parent = getParent((ContainerData) child, node);
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }

    @Override
    public ComponentData getPrevious(ComponentData parent, ComponentData node) {
        ContainerData container = (ContainerData) parent;
        int index = container.getChildren().indexOf(node);
        return index > 0 ? container.getChildren().get(index - 1) : null;
    }

    @Override
    public Iterator<ComponentData> getChildren(ComponentData node, boolean reverse) {
        if (node instanceof ContainerData) {
            List<ComponentData> list = ((ContainerData) node).getChildren();
            if (reverse) {
                final ListIterator<ComponentData> iterator = list.listIterator(list.size());
                return new Iterator<ComponentData>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasPrevious();
                    }
                    @Override
                    public ComponentData next() {
                        return iterator.previous();
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            } else {
                return list.iterator();
            }
        } else {
            return Collections.<ComponentData>emptyList().iterator();
        }
    }

    @Override
    public int size(ComponentData node) {
        if (node instanceof ContainerData) {
            return ((ContainerData) node).getChildren().size();
        } else {
            return 0;
        }
    }

    private ElementState create(ComponentData data) {
        if (data instanceof ApplicationData) {
            ApplicationData application = (ApplicationData) data;
            return new ElementState.Window(
                    ContentType.forApplicationType(application.getType()),
                    application.getState(),
                    application.getTitle(),
                    application.getIcon(),
                    application.getDescription(),
                    application.isShowInfoBar(),
                    application.isShowApplicationState(),
                    application.isShowApplicationMode(),
                    application.getTheme(),
                    application.getWidth(),
                    application.getHeight(),
                    application.getProperties()
            );
        } else if (data instanceof BodyData) {
            return new ElementState.Body();
        } else if (data instanceof ContainerData) {
            ContainerData container = (ContainerData) data;
            return new ElementState.Container(
                    container.getId(),
                    container.getName(),
                    container.getIcon(),
                    container.getTemplate(),
                    container.getFactoryId(),
                    container.getTitle(),
                    container.getDescription(),
                    container.getWidth(),
                    container.getHeight(),
                    container instanceof DashboardData
            );
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
