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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.namespace.QName;

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
import org.gatein.pc.api.invocation.response.ContentResponse;
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

    /** . */
    private static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();

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
                PageContext.Builder pageBuilder = new PageContext.Builder(path);
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                org.gatein.portal.mop.page.PageContext page = pageService.loadPage(pageKey);
                NodeContext<org.gatein.portal.page.NodeState, ElementState> pageLayout = layoutService.loadLayout(pageBuilder, page.getLayoutId(), null);

                // Decode from request
                Map<String, String[]> parameters = NO_PARAMETERS;
                for (RequestParameter parameter : requestParameters.values()) {
                    String name = parameter.getName();
                    if (name.startsWith("javax.portlet.")) {
                        if (name.equals("javax.portlet.p")) {
                            Decoder decoder = new Decoder(parameter.getRaw(0));
                            HashMap<QName, String[]> prp = new HashMap<QName, String[]>();
                            for (Map.Entry<String, String[]> p : decoder.decode().getParameters().entrySet()) {
                                prp.put(new QName(p.getKey()), p.getValue());
                            }
                            pageBuilder.setParameters(prp);
                        } else if (name.startsWith("javax.portlet.p.")) {
                            String id = name.substring("javax.portlet.p.".length());
                            Decoder decoder = new Decoder(parameter.getRaw(0));
                            WindowState window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.parameters = decoder.decode().getParameters();
                            }
                        } else if (name.startsWith("javax.portlet.w.")) {
                            String id = name.substring("javax.portlet.w.".length());
                            WindowState window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.windowState = org.gatein.pc.api.WindowState.create(parameter.getValue());
                            }
                        } else if (name.startsWith("javax.portlet.m.")) {
                            String id = name.substring("javax.portlet.m.".length());
                            WindowState window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.mode = org.gatein.pc.api.Mode.create(parameter.getValue());
                            }
                        }
                    } else {
                        if (parameters == NO_PARAMETERS) {
                            parameters = new HashMap<String, String[]>();
                        }
                        parameters.put(name, parameter.toArray());
                    }
                }

                //
                if (action != null) {

                    //
                    PageContext pageContext = pageBuilder.build(customizationService, manager);

                    // Going to invoke process action
                    if (target != null) {
                        WindowContext window = pageContext.get(target);
                        if (window != null) {

                            if ("action".equals(action)) {

                                //
                                org.gatein.pc.api.WindowState windowState = window.state.windowState;
                                if (targetWindowState != null) {
                                    windowState = org.gatein.pc.api.WindowState.create(targetWindowState);
                                }
                                Mode mode = window.state.mode;
                                if (targetMode != null) {
                                    mode = org.gatein.pc.api.Mode.create(targetMode);
                                }

                                //
                                PortletInvocationResponse response = window.processAction(windowState, mode, parameters);
                                if (response instanceof UpdateNavigationalStateResponse) {
                                    UpdateNavigationalStateResponse update = (UpdateNavigationalStateResponse) response;
                                    PageContext.Builder clone = pageContext.builder();
                                    WindowState windowClone = clone.getWindow(window.state.name);
                                    ParametersStateString s = (ParametersStateString) update.getNavigationalState();
                                    if (s != null && s.getSize() > 0) {
                                        windowClone.parameters = s.getParameters();
                                    }
                                    if (update.getWindowState() != null) {
                                        windowClone.windowState = update.getWindowState();
                                    }
                                    if (update.getMode() != null) {
                                        windowClone.mode = update.getMode();
                                    }
                                    Map<String, String[]> changes = update.getPublicNavigationalStateUpdates();
                                    if (changes != null && changes.size() > 0) {
                                        clone.apply(window.getPublicParametersChanges(changes));
                                    }
                                    return clone.build(customizationService, manager).getDispatch().with(PropertyType.REDIRECT_AFTER_ACTION);
                                } else {
                                    throw new UnsupportedOperationException("Not yet handled " + response);
                                }
                            } else if ("resource".equals(action)) {

                                //
                                PortletInvocationResponse response = window.serveResource(parameters);

                                //
                                if (response instanceof ContentResponse) {
                                    ContentResponse content = (ContentResponse) response;

                                    Response.Render render;
                                    if (content.getBytes() != null) {
                                        throw new UnsupportedOperationException("todo");
                                    } else if (content.getChars() != null) {
                                        return Response.ok(content.getChars());
                                    } else {
                                        render = Response.ok("");
                                    }

                                    // Set content type
                                    if (content.getContentType() != null) {
                                        render.withMimeType(content.getContentType());
                                    }

                                    //
                                    if (content.getEncoding() != null) {
//                                        render.withHeader()
                                    }

                                    //
                                    return render;
                                } else {
                                    throw new UnsupportedOperationException("No yet handled " + response);
                                }
                            } else {
                                throw new AssertionError("should not be here");
                            }
                        } else {
                            return Response.error("Target " + target + " not found");
                        }
                    } else {
                        return Response.error("No target");
                    }
                } else {

                    // Set page parameters
                    HashMap<QName, String[]> prp = new HashMap<QName, String[]>(parameters.size());
                    for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                        prp.put(new QName(parameter.getKey()), parameter.getValue());
                    }
                    pageBuilder.setParameters(prp);

                    //
                    PageContext pageContext = pageBuilder.build(customizationService, manager);

                    // Render all windows in a map
                    HashMap<String, String> fragments = new HashMap<String, String>();
                    for (Map.Entry<String, WindowContext> entry : pageContext) {
                        try {
                            WindowContext window = entry.getValue();
                            PortletInvocationResponse response = window.render();
                            if (response instanceof FragmentResponse) {
                                FragmentResponse fragment = (FragmentResponse) response;
                                fragments.put(window.state.name, fragment.getContent());
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
                    Response.Render ok = Response.ok(buffer);
                    layout.render(fragments, pageContext, ok.getProperties(), buffer);
                    return ok;
                }
            }
        } else {
            return Response.notFound("Page for navigation " + path + " could not be located");
        }
    }
}
