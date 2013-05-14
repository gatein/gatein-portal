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

package org.gatein.portal.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import juzu.Param;
import juzu.PropertyType;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.RenderContext;
import juzu.request.RequestParameter;
import org.exoplatform.container.PortalContainer;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.portal.layout.Layout;
import org.gatein.portal.layout.ZoneLayoutFactory;
import org.gatein.portal.mop.customization.CustomizationService;
import org.gatein.portal.mop.hierarchy.GenericScope;
import org.gatein.portal.mop.hierarchy.NodeContext;
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
public class Controller {

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

    @Inject
    ZoneLayoutFactory layoutFactory;

    @View()
    @Route("/{javax.portlet.path}")
    public Response index(
            RenderContext context,
            @Param(name = "javax.portlet.path", pattern = ".*")
            String path,
            @Param(name = "javax.portlet.a")
            String action,
            @Param(name = "javax.portlet.t")
            String target,
            @Param(name = "javax.portlet.w")
            String targetWindowState,
            @Param(name = "javax.portlet.m")
            String targetMode) throws Exception {

        // Parse path
        List<String> names = new ArrayList<String>();
        for (String name : Tools.split(path, '/')) {
            if (name.length() > 0) {
                names.add(name);
            }
        }

        //
        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));
        NodeContext<?, NodeState> root =  navigationService.loadNode(NodeState.model(), navigation, GenericScope.branchShape(names), null);
        if (root != null) {

            //
            Map<String, RequestParameter> requestParameters = context.getParameters();

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

                //
                PageState pageState = new PageState(customizationService, manager, path);
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                PageContext page = pageService.loadPage(pageKey);
                NodeContext<org.gatein.portal.page.NodeState, ElementState> pageLayout = layoutService.loadLayout(pageState, page.getLayoutId(), null);

                // Decode from request
                Map<String, String[]> targetParameters = null;
                for (RequestParameter parameter : requestParameters.values()) {
                    String name = parameter.getName();
                    if (name.startsWith("javax.portlet.")) {
                        if (name.startsWith("javax.portlet.p.")) {
                            String id = name.substring("javax.portlet.p.".length());
                            Decoder decoder = new Decoder(parameter.getRaw(0));
                            WindowState window = pageState.get(id);
                            if (window != null) {
                                window.parameters = decoder.decode().getParameters();
                            }
                        }
                        if (name.startsWith("javax.portlet.w.")) {
                            String id = name.substring("javax.portlet.w.".length());
                            WindowState window = pageState.get(id);
                            if (window != null) {
                                window.windowState = org.gatein.pc.api.WindowState.create(parameter.getValue());
                            }
                        }
                        if (name.startsWith("javax.portlet.m.")) {
                            String id = name.substring("javax.portlet.m.".length());
                            WindowState window = pageState.get(id);
                            if (window != null) {
                                window.mode = org.gatein.pc.api.Mode.create(parameter.getValue());
                            }
                        }
                    } else {
                        if (targetParameters == null) {
                            targetParameters = new HashMap<String, String[]>();
                        }
                        targetParameters.put(name, parameter.toArray());
                    }
                }

                //
                if ("action".equals(action)) {

                    // Going to invoke process action
                    if (target != null) {
                        WindowState window = pageState.get(target);
                        if (window != null) {

                            //
                            org.gatein.pc.api.WindowState windowState = window.windowState;
                            if (targetWindowState != null) {
                                windowState = org.gatein.pc.api.WindowState.create(targetWindowState);
                            }
                            Mode mode = window.mode;
                            if (targetMode != null) {
                                mode = org.gatein.pc.api.Mode.create(targetMode);
                            }

                            //
                            PortletInvocationResponse response = window.processAction(windowState, mode, targetParameters);
                            if (response instanceof UpdateNavigationalStateResponse) {
                                UpdateNavigationalStateResponse update = (UpdateNavigationalStateResponse) response;
                                pageState = new PageState(pageState);
                                window = pageState.get(window.name);
                                ParametersStateString s = (ParametersStateString) update.getNavigationalState();
                                if (s != null && s.getSize() > 0) {
                                    window.parameters = s.getParameters();
                                }
                                if (update.getWindowState() != null) {
                                    window.windowState = update.getWindowState();
                                }
                                if (update.getMode() != null) {
                                    window.mode = update.getMode();
                                }
                                return pageState.getDispatch().with(PropertyType.REDIRECT_AFTER_ACTION);
                            } else {
                                throw new UnsupportedOperationException("Not yet handled " + response);
                            }

                        } else {
                            return Response.error("Target " + target + " not found");
                        }
                    } else {
                        return Response.error("No target");
                    }
                } else {

                    // Find if we need to update the window parameters
                    if (target != null) {
                        WindowState windowState = pageState.get(target);
                        if (targetParameters != null) {
                            windowState.parameters = targetParameters;
                        }
                        if (targetWindowState != null) {
                            windowState.windowState = org.gatein.pc.api.WindowState.create(targetWindowState);
                        }
                        if (targetMode != null) {
                            windowState.mode = org.gatein.pc.api.Mode.create(targetMode);
                        }
                    }

                    // Render all windows in a map
                    HashMap<String, String> fragments = new HashMap<String, String>();
                    for (Map.Entry<String, WindowState> entry : pageState) {
                        try {
                            WindowState window = entry.getValue();
                            PortletInvocationResponse response = window.render();
                            if (response instanceof FragmentResponse) {
                                FragmentResponse fragment = (FragmentResponse) response;
                                fragments.put(window.name, fragment.getContent());
                            } else {
                                throw new UnsupportedOperationException("Not yet handled " + response);
                            }
                        } catch (PortletInvokerException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //
                    StringBuilder buffer = new StringBuilder();
                    Layout layout = layoutFactory.build(pageLayout);
                    layout.render(fragments, pageState, buffer);
                    return Response.ok(buffer);
                }
            }
        } else {
            return Response.notFound("Page for navigation " + path + " could not be located");
        }
    }
}
