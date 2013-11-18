/*
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
package org.gatein.portal.web.page;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import juzu.impl.common.JSON;

import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.hierarchy.ModelAdapter;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Nguyen Thanh Hai</a>
 * @version $Id$
 *
 */
public class JSONContainerAdapter implements ModelAdapter<JSON, ElementState>{
    
    final JSON root;
    
    final NodeContext<JSON, ElementState> pageStructure;
    
    public JSONContainerAdapter(JSON root, NodeContext<JSON, ElementState> pageStructure) {
        this.root = root;
        this.pageStructure = pageStructure;
    }

    @Override
    public String getId(JSON node) {
        String type = node.getString("type");
        if ("container".equals(type)) {
            return node.getJSON("container").getString("storageId");
        } else if ("application".equals(type)) {
            return node.getJSON("application").getString("storageId");
        }
        return null;
    }

    @Override
    public String getName(JSON node) {
        String type = node.getString("type");
        JSON data = "container".equalsIgnoreCase(type) ? node.getJSON("container") : node.getJSON("application");
        String name = data.getString("storageName");
        if (name == null) {
            // For now we generate a name
            // however the name should be fully provided by the node (possibly randomly generated)
            name = UUID.randomUUID().toString();
        }
        return name;
    }

    @Override
    public ElementState getState(JSON node) {
        return create(node);
    }

    @Override
    public JSON getPrevious(JSON parent, JSON node) {
        List<JSON> children = (List<JSON>)parent.getJSON("container").getList("children");
        int index = children.indexOf(node);
        return index > 0 ? children.get(index - 1) : null;
    }

    @Override
    public Iterator<JSON> getChildren(JSON node, boolean reverse) {
        if ("container".equals(node.getString("type"))) {
            List<JSON> list = (List<JSON>) node.getJSON("container").getList("children");
            if (reverse) {
                final ListIterator<JSON> iterator = list.listIterator(list.size());
                return new Iterator<JSON>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasPrevious();
                    }
                    @Override
                    public JSON next() {
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
            return Collections.<JSON>emptyList().iterator();
        }
    }

    @Override
    public int size(JSON node) {
        if ("container".equals(node.getString("type"))) {
            return node.getJSON("container").getList("children").size();
        } else {
            return 0;
        }
    }

    @Override
    public JSON getParent(JSON node) {
        return getParent(root, node);
    }
    
    private JSON getParent(JSON container, JSON node) {
        for (JSON child : (List<JSON>)container.getJSON("container").getList("children")) {
            if (child.equals(node)) {
                return container;
            } else if ("container".equals(child.getString("type"))) {
                JSON parent = getParent(child, node);
                if (parent != null) {
                    return parent;
                }
            }
        }
        return null;
    }
    
    private Map<String, String> createMap(JSON data) {
        if (data == null) {
            return new HashMap<String, String>();
        }
        Map<String, String> map = new HashMap<String, String>();
        for (String name : data.names()) {
            map.put(name, data.getString(name));
        }
        return map;
    }
    
    private NodeContext<JSON, ElementState> find(String id, NodeContext<JSON, ElementState> target) {
        if (target.getName().equals(id)) {
            return target;
        } else {
            for (NodeContext<JSON, ElementState> child : target) {
                NodeContext<JSON, ElementState> tmp = this.find(id, child);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }
    
    private ElementState create(JSON data) {
        String id = data.getString("id");
        String type = data.getString("type");
        if ("application".equalsIgnoreCase(type)) {
            JSON application = data.getJSON("application");
            NodeContext<JSON, ElementState> appCtx = find(id, pageStructure);
            ApplicationState state = null;
            if (appCtx == null) {
                String contentId = data.getString("contentId");
                state = new TransientApplicationState(contentId);
            } else {
                ElementState.Window windowState = (ElementState.Window) appCtx.getState();
                state = windowState.state;
            }
            
            return new ElementState.Window(
                    ContentType.forValue(application.getString("type")),
                    state,
                    application.getString(ElementState.Window.TITLE.getName()),
                    application.getString(ElementState.Window.ICON.getName()),
                    application.getString(ElementState.Window.DESCRIPTION.getName()),
                    application.getBoolean(ElementState.Window.SHOW_INFO_BAR.getName()),
                    application.getBoolean(ElementState.Window.SHOW_APPLICATION_STATE.getName()),
                    application.getBoolean(ElementState.Window.SHOW_APPLICATION_MODE.getName()),
                    application.getString(ElementState.Window.THEME.getName()),
                    application.getString(ElementState.Window.WIDTH.getName()),
                    application.getString(ElementState.Window.HEIGHT.getName()),
                    createMap(data.getJSON("properties")),
                    application.getList("accessPermissions")
            );
        } else if ("body".equalsIgnoreCase(type)) {
            return new ElementState.Body();
        } else if ("container".equalsIgnoreCase(type)) {
            JSON container = data.getJSON("container");
            return new ElementState.Container(
                    container.getString("id"),
                    container.getString(ElementState.Container.NAME.getName()),
                    container.getString(ElementState.Container.ICON.getName()),
                    container.getString(ElementState.Container.TEMPLATE.getName()),
                    container.getString(ElementState.Container.FACTORY_ID.getName()),
                    container.getString(ElementState.Container.TITLE.getName()),
                    container.getString(ElementState.Container.DESCRIPTION.getName()),
                    container.getString(ElementState.Container.WIDTH.getName()),
                    container.getString(ElementState.Container.HEIGHT.getName()),
                    (List<String>)container.getList("accessPermissions"),
                    false);
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
