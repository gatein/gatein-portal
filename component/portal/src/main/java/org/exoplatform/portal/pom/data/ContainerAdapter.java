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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.gatein.portal.mop.hierarchy.NodeAdapter;
import org.exoplatform.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ContainerAdapter implements NodeAdapter<List<ComponentData>, ComponentData, ElementState> {

    /** . */
    final ContainerData root;

    /** . */
    final IdentityHashMap<ComponentData, String> handles = new IdentityHashMap<ComponentData, String>();

    public ContainerAdapter(ContainerData root) {
        this.root = root;
    }

    @Override
    public String getHandle(ComponentData node) {
        String handle = node.getStorageId();
        if (handle == null) {
            handle = handles.get(node);
            if (handle == null) {
                handles.put(node, handle = UUID.randomUUID().toString());
            }
        }
        return handle;
    }
    @Override
    public List<ComponentData> getChildren(ComponentData node) {
        if (node instanceof ContainerData) {
            return ((ContainerData)node).getChildren();
        } else {
            return Collections.emptyList();
        }
    }
    @Override
    public ComponentData getDescendant(ComponentData node, String handle) {
        String h = getHandle(node);
        if (h.equals(handle)) {
            return node;
        } else if (node instanceof ContainerData) {
            ContainerData container = (ContainerData) node;
            for (ComponentData child : container.getChildren()) {
                ComponentData descendant = getDescendant(child, handle);
                if (descendant != null) {
                    return descendant;
                }
            }
            return null;
        } else {
            return null;
        }
    }
    @Override
    public int size(List<ComponentData> list) {
        return list.size();
    }
    @Override
    public Iterator<String> iterator(List<ComponentData> list, boolean reverse) {
        ArrayList<String> ret = new ArrayList<String>();
        for (ComponentData c : list) {
            ret.add(getHandle(c));
        }
        if (reverse) {
            Collections.reverse(ret);
        }
        return ret.iterator();
    }
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
    public ElementState getState(ComponentData node) {
        return create(node);
    }

    @Override
    public ComponentData getPrevious(ComponentData parent, ComponentData node) {
        ContainerData container = (ContainerData) parent;
        int index = container.getChildren().indexOf(node);
        return index > 0 ? container.getChildren().get(index - 1) : null;
    }

    @Override
    public void setHandle(ComponentData node, String handle) {
        handles.put(node, handle);
    }

    private ElementState create(ComponentData data) {
        if (data instanceof ApplicationData) {
            ApplicationData application = (ApplicationData) data;
            return new ElementState.Window(
                    application.getType(),
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
                    application.getProperties(),
                    application.getAccessPermissions()
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
                    container.getAccessPermissions(),
                    container instanceof DashboardData
            );
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
