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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import juzu.Action;
import juzu.Param;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.RenderContext;
import juzu.request.RequestParameter;
import juzu.template.Template;
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
import org.gatein.portal.mop.site.SiteContext;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.page.spi.WindowContent;
import org.gatein.portal.page.spi.portlet.PortletContentProvider;
import org.gatein.portal.servlet.Context;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.security.Credentials;

/**
 * The controller for aggregation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Controller {


    /** . */
    private static final int AUTHENTICATED = 1;

    /** . */
    private static final int FAILED = 2;

    @Inject
    @Path("login.gtmpl")
    private Template login;

    @View
    @Route(value = "/dologin", priority = 10)
    public Response doLogin() {
        try {
            HttpServletRequest req = Context.getCurrentRequest();
            int status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

            if (status == AUTHENTICATED) {
                System.out.println("Authenticated :D");
                return Response.ok("doLogin");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //return Controller_.login();
        return Response.ok("test");
    }

    @Action
    @Route(value = "/actionLogin", priority = 10)
    public Response actionLogin(String username, String password) {
        System.out.println("====================== LOGIN ======================");
        System.out.println("                      Username: " + username);
        System.out.println("                      Password: " + password);
        System.out.println("===================================================");

        Credentials credentials = new Credentials(username, password);
        ServletContainer container = ServletContainerFactory.getServletContainer();

        try {
            container.login(Context.getCurrentRequest(), Context.getCurrentResponse(), credentials);
            return Controller_.doLogin();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException ex) {
            ex.printStackTrace();
        }

        return Controller_.login();
    }

    @View
    @Route(value = "/login", priority = 10)
    public Response login() {
        return login.with().ok();
    }




    /** . */
    private static final Map<String, String[]> NO_PARAMETERS = Collections.emptyMap();

    @Inject
    SiteService siteService;

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

    @Inject
    PortletContentProvider contentProvider;

    @Inject
    @Path("not_found.gtmpl")
    Template notFound;

    @View()
    @Route(value = "/{javax.portlet.path}", priority = 100)
    public Response index(
            RenderContext context,
            @Param(name = "javax.portlet.path", pattern = ".*")
            String path,
            @Param(name = "javax.portlet.a")
            String phase,
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
                return notFound.with().set("path", path).notFound();
            } else {

                // Page builder
                PageContext.Builder pageBuilder = new PageContext.Builder(contentProvider, customizationService, path);

                // Load site windows
                SiteContext site = siteService.loadSite(SiteKey.portal("classic"));
                NodeContext<org.gatein.portal.page.NodeState, ElementState> siteStructure = layoutService.loadLayout(pageBuilder, site.getLayoutId(), null);

                // Load page windows
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                org.gatein.portal.mop.page.PageContext page = pageService.loadPage(pageKey);
                NodeContext<org.gatein.portal.page.NodeState, ElementState> pageStructure = layoutService.loadLayout(pageBuilder, page.getLayoutId(), null);

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
                            WindowContent window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.setParameters(parameter.getRaw(0));
                            }
                        } else if (name.startsWith("javax.portlet.w.")) {
                            String id = name.substring("javax.portlet.w.".length());
                            WindowContent window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.setWindowState(parameter.getValue());
                            }
                        } else if (name.startsWith("javax.portlet.m.")) {
                            String id = name.substring("javax.portlet.m.".length());
                            WindowContent window = pageBuilder.getWindow(id);
                            if (window != null) {
                                window.setMode(parameter.getValue());
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
                if (phase != null) {

                    //
                    PageContext pageContext = pageBuilder.build();

                    // Going to invoke process action
                    if (target != null) {
                        WindowContext window = pageContext.get(target);
                        if (window != null) {

                            if ("action".equals(phase)) {

                                //
                                String windowState = window.state.getWindowState();
                                if (targetWindowState != null) {
                                    windowState = targetWindowState;
                                }
                                String mode = window.state.getMode();
                                if (targetMode != null) {
                                    mode = targetMode;
                                }

                                //
                                return window.processAction(windowState, mode, parameters);
                            } else if ("resource".equals(phase)) {

                                //
                                String id;
                                RequestParameter resourceId = requestParameters.get("javax.portlet.r");
                                if (resourceId != null) {
                                    id = resourceId.getValue();
                                } else {
                                    id = null;
                                }

                                //
                                return window.serveResource(id, parameters);
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

                    // Build page
                    PageContext pageContext = pageBuilder.build();

                    // Render page
                    String layoutId = page.getState().getFactoryId();
                    if (layoutId == null) {
                        layoutId = "1";
                    }
                    Layout pageLayout = layoutFactory.build(layoutId, pageStructure);
                    Layout siteLayout = layoutFactory.build("site", siteStructure);

                    //
                    ReactivePage rp = new ReactivePage(pageContext, context.getUserContext().getLocale());

                    //
                    return rp.execute(siteLayout, pageLayout, context);
                }
            }
        } else {
            return Response.notFound("Page for navigation " + path + " could not be located");
        }
    }
}
