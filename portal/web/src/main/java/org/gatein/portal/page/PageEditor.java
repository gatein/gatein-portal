/*
 * Copyright (C) 2013 eXo Platform SAS.
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

package org.gatein.portal.page;

import javax.inject.Inject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import juzu.Param;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.request.ResourceContext;

import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.GenericScope;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.page.spi.portlet.PortletContentProvider;

public class PageEditor {

    @Inject
    NavigationService navigationService;

    @Inject
    PageService pageService;

    @Inject
    LayoutService layoutService;

    @Inject
    CustomizationService customizationService;

    @Inject
    PortletContentProvider contentProvider;

    @Resource
    @Route(value = "/{javax.portlet.path}", priority = 2)
    public Response edit(ResourceContext context, @Param(name = "javax.portlet.path", pattern = ".*") String path)
            throws Exception {
        NodeContext<org.gatein.portal.page.NodeState, ElementState> pageStructure = null;

        // Parse path
        List<String> names = new ArrayList<String>();
        for (String name : Tools.split(path, '/')) {
            if (name.length() > 0) {
                names.add(name);
            }
        }

        //
        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));
        NodeContext<?, NodeState> root = navigationService.loadNode(NodeState.model(), navigation,
                GenericScope.branchShape(names), null);
        if (root != null) {

            // Get our node from the navigation
            NodeContext<?, NodeState> current = root;
            for (String name : names) {
                current = current.get(name);
                if (current == null) {
                    break;
                }
            }

            if (current != null) {
                // Page builder
                PageContext.Builder pageBuilder = new PageContext.Builder(contentProvider, customizationService, path);

                // Load page windows
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                org.gatein.portal.mop.page.PageContext page = pageService.loadPage(pageKey);
                pageStructure = layoutService.loadLayout(pageBuilder, page.getLayoutId(), null);
            }
        }

        String body = null;
        if (pageStructure != null) {
            JSON action = getAction(context);

            NodeContext<org.gatein.portal.page.NodeState, ElementState> window = find(action.getString("id"),
                    ElementState.Window.class, pageStructure);

            String targetParent = action.getString("containerID");
            NodeContext parent = window.getParent();
            if (!parent.getName().equals(targetParent)) {
                parent = find(targetParent, ElementState.Container.class, pageStructure);
            }

            if (parent != null) {
                NodeContext prev = find(action.getString("prev"), ElementState.Window.class, parent);
                if (prev != null) {
                    parent.add(prev.getIndex() + 1, window);
                } else {
                    parent.add(0, window);
                }
                try {
                    layoutService.saveLayout(pageStructure, null);
                } catch (Exception ex) {
                }

                List<JSON> apps = new LinkedList<JSON>();
                buildResponse(pageStructure, apps);
                body = apps.toString();
            }
        }

        if (body != null) {
            return Response.status(200).body(body).withCharset(Charset.forName("UTF-8"))
                    .withMimeType("application/json");
        } else {
            return Response.status(400).body("Can't edit");
        }
    }

    private void buildResponse(NodeContext<org.gatein.portal.page.NodeState, ElementState> node, List<JSON> apps)
            throws Exception {
        ElementState state = node.getState();
        if (state instanceof ElementState.Window) {
            JSON window = new JSON();
            window.set("id", node.getName());
            window.set("containerID", node.getParent().getName());
            NodeContext prev = node.getPrevious();
            window.set("prev", prev != null ? prev.getName() : null);
            apps.add(window);
        } else if (state instanceof ElementState.Container) {
            for (NodeContext child : node) {
                buildResponse(child, apps);
            }
        }
    }

    private NodeContext<org.gatein.portal.page.NodeState, ElementState> find(String id, Class type,
            NodeContext<org.gatein.portal.page.NodeState, ElementState> target) {
        ElementState state = target.getState();

        if (type.isAssignableFrom(state.getClass()) && target.getName().equals(id)) {
            return target;
        } else if (state instanceof ElementState.Container) {
            for (NodeContext<org.gatein.portal.page.NodeState, ElementState> child : target) {
                NodeContext<org.gatein.portal.page.NodeState, ElementState> tmp = find(id, type, child);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

    private JSON getAction(ResourceContext context) throws Exception {
        InputStream content = context.getClientContext().getInputStream();

        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            if (content != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(content));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            }
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }
        String body = stringBuilder.toString();
        return (JSON) JSON.parse(body);
    }
}
