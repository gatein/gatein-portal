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

package org.gatein.portal.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import juzu.Param;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.RenderContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.portal.mop.customization.CustomizationContext;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.GenericScope;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageContext;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.portlet.PortletAppManager;

/**
 * The controller for aggregation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Aggregator {

    /** . */
    private static final Pattern PORTLET_PATTERN = Pattern.compile("^([^/]+)/([^/]+)$");

    @Inject
    PortalContainer current;

    @Inject
    PortletAppManager manager;

    @Inject
    NavigationService navigationService;

    @Inject
    PageService pageService;

    @Inject
    LayoutService layoutService;

    @Inject
    CustomizationService customizationService;

    @View()
    @Route("/{path}")
    public Response.Content index(RenderContext context, @Param(pattern = ".*") String path) throws Exception {

        // Parse path
        List<String> names = new ArrayList<String>();
        for (String name : Tools.split(path, '/')) {
            if (name.length() > 0) {
                names.add(name);
            }
        }

        //
        NodeModel<?, NodeState> a =  NodeState.model();
        NodeModel<?, ElementState> b =  ElementState.model();

        //
        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));
        NodeContext<?, NodeState> root =  navigationService.loadNode(a, navigation, GenericScope.branchShape(names), null);
        if (root != null) {

            // Get our node from the navigation
            NodeContext<?, NodeState> current = root;
            for (String name : names) {
                current = current.get(name);
                if (current == null) {
                    break;
                }
            }
            if (current == null) {
                return Response.notFound("Page for navigation " + path + " could not be located");
            } else {
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                PageContext page = pageService.loadPage(pageKey);
                NodeContext<?, ElementState> pageLayout = layoutService.loadLayout(b, page.getLayoutId(), null);
                StringBuilder buffer = new StringBuilder();
                render(context, pageLayout, buffer);
                return Response.ok(buffer);
            }
        } else {
            return Response.notFound("Page for navigation " + path + " could not be located");
        }
    }

    private <N> void render(RenderContext context, NodeContext<N, ElementState> node, StringBuilder to) {
        ElementState state = node.getState();
        if (state instanceof ElementState.Container) {
            ElementState.Container containerState = (ElementState.Container) state;
            to.append("<div>");
            for (NodeContext<N, ElementState> child : node) {
                render(context, child, to);
            }
            to.append("</div>");
        } else if (state instanceof ElementState.Window) {
            ElementState.Window windowState = (ElementState.Window) state;
            CustomizationContext<Portlet> portletCustomization = customizationService.loadCustomization(node.getId());

            PortletInvoker invoker = manager.getInvoker();
            org.gatein.pc.api.Portlet portlet = null;
            try {
                String contentId = portletCustomization.getContentId();
                Matcher matcher = PORTLET_PATTERN.matcher(contentId);
                if (matcher.matches()) {
                    portlet = invoker.getPortlet(PortletContext.createPortletContext(matcher.group(1), matcher.group(2)));
                } else {
                    throw new Exception("Could not handle " + contentId);
                }
            } catch (Exception e) {
                // Handle me
                e.printStackTrace();
            }

            //
            if (portlet != null) {
                try {
                    RenderInvocation render = manager.render(node.getId(), portlet);
                    render.setMode(Mode.VIEW);
                    render.setWindowState(WindowState.NORMAL);
                    PortletInvocationResponse response = invoker.invoke(render);
                    if (response instanceof FragmentResponse) {
                        FragmentResponse fragment = (FragmentResponse) response;
                        to.append("<div>");
                        to.append("<div>").append(windowState.properties.get(ElementState.Window.TITLE)).append("</div>");
                        to.append("<div>").append(fragment.getContent()).append("</div>");
                        to.append("</div>");
                    } else {
                        throw new UnsupportedOperationException("Not yet handled " + response);
                    }
                } catch (PortletInvokerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else {
                to.append("<div>").append(windowState.properties.get(ElementState.Window.TITLE) + " : " + portletCustomization.getContentId()).append("</div>");
            }
        }
    }
}
