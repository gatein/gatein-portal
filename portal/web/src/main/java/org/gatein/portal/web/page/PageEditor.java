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

package org.gatein.portal.web.page;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.Cookie;

import juzu.Param;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Request;
import juzu.request.ResourceContext;

import org.exoplatform.portal.config.model.ApplicationType;
import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.SimpleMultiValuedPropertyMap;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.ClientContext;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.spi.AbstractInstanceContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractUserContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.mop.Properties;
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
import org.gatein.portal.web.layout.RenderingContext;
import org.gatein.portal.web.layout.ZoneLayout;
import org.gatein.portal.web.layout.ZoneLayoutFactory;
import org.gatein.portal.web.page.spi.portlet.PortletContentProvider;
import org.gatein.portal.web.portlet.PortletAppManager;
import org.gatein.portal.web.servlet.Context;

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

    @Inject
    ZoneLayoutFactory layoutFactory;

    @Inject
    PortletAppManager portletAppManager;

    @Resource
    @Route(value = "/switchto/{javax.portlet.z}")
    public Response switchLayout(@Param(name = "javax.portlet.z") String id) throws Exception {
        ZoneLayout layout = (ZoneLayout) layoutFactory.builder(id).build();
        StringBuilder sb = new StringBuilder();
        layout.render(new RenderingContext(null, true), Collections.<String, Result.Fragment>emptyMap(), null, null, sb);

        JSON result = new JSON();
        result.set("code", 200);
        result.set("status", "success");

        JSON data = new JSON();
        data.set("layout_id", id);
        data.set("html", sb.toString());

        result.set("data", data);

        return Response.status(200).body(result.toString());
    }

    @Resource
    @Route(value = "/portlets")
    public Response getAllPortlets() throws Exception {
        List<JSON> items = new LinkedList<JSON>();
        Set<Portlet> portlets = this.portletAppManager.getAllPortlets();
        for(Portlet portlet : portlets) {
            PortletInfo info = portlet.getInfo();
            MetaInfo meta = info.getMeta();

            JSON item = new JSON();
            item.set("name", info.getName());
            item.set("title", meta.getMetaValue("title").getDefaultString());
            item.set("applicationName", info.getApplicationName());

            items.add(item);
        }

        JSON data = new JSON();
        data.set("portlets", items.toArray(new JSON[0]));
        JSON result = new JSON();
        result.set("code", 200);
        result.set("status", "success");
        result.set("data", data);
        return Response.status(200).body(result.toString());
    }

    @Resource
    @Route(value = "/{javax.portlet.path}", priority = 2)
    public Response edit(ResourceContext context, @Param(name = "javax.portlet.path", pattern = ".*") String path) {
        NodeContext<JSON, ElementState> pageStructure = null;
        org.gatein.portal.mop.page.PageContext page = null;

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
                // Load page windows
                NodeState state = current.getState();
                PageKey pageKey = state.getPageRef();
                page = pageService.loadPage(pageKey);
                pageStructure = (NodeContext<JSON, ElementState>) layoutService.loadLayout(ElementState.model(), page.getLayoutId(), null);
            }
        }

        JSON result = new JSON();
        if (pageStructure != null) {
            try {
                //
                JSON action = getAction(context);
                JSON rootContainer = this.buildComponentData(pageStructure);
                List<JSON> rootChildren = (List<JSON>)rootContainer.getJSON("container").getList("children");

                //
                JSON[] childrens = action.getArray("childrens", JSON.class);
                for (NodeContext<JSON, ElementState> sel : pageStructure) {
                    NodeContext<JSON, ElementState> ctx = this.find(sel.getName(), pageStructure);
                    rootChildren.add(this.buildComponentData(ctx));
                }
                rootContainer.getJSON("container").set("children", rootChildren);
                
                //
                for (JSON j : childrens) {
                    this.buildComponentTree(pageStructure, j, rootContainer);
                }

                layoutService.saveLayout(new JSONContainerAdapter(rootContainer, pageStructure), rootContainer, pageStructure, null);

                //Update layout
                String layoutId = action.getString("layout_id");
                if (layoutId != null && !layoutId.isEmpty()) {
                    page.setState(page.getState().builder().factoryId(layoutId).build());
                    pageService.savePage(page);
                }


                result.set("code", 200);
                result.set("status", "success");
                result.set("message", "OK");
            } catch (Exception ex) {
                ex.printStackTrace();
                result.set("code", 400);
                result.set("status", "error");
                result.set("message", "can not edit");
                result.set("data", Collections.EMPTY_MAP);
            }
        }

        return Response.status(200).body(result.toString()).withCharset(Charset.forName("UTF-8"))
                    .withMimeType("application/json");
    }

    /**
     * Temporary implement to render portlet content without full page context
     */
    @Resource
    @Route(value = "/getContent")
    public Response getContent(@Param(name = "javax.portlet.content") String contentId) {
        JSON result = new JSON();

        ContextLifeCycle lifeCycle = Request.getCurrent().suspend();
        PortletInvocationResponse response = null;
        PortletInvokerException failure = null;
        try {

            //            
            RenderInvocation invocation = new RenderInvocation(new PortletInvocationContext() {                
                @Override
                public void renderURL(Writer arg0, ContainerURL arg1, URLFormat arg2) throws IOException {                    
                }

                @Override
                public String renderURL(ContainerURL containerURL, URLFormat format) {
                    return "";
                }

                @Override
                public MediaType getResponseContentType() {
                    return MediaType.TEXT_HTML;
                }

                @Override
                public String encodeResourceURL(String arg0) throws IllegalArgumentException {
                    return null;
                }
            });

            invocation.setClientContext(new ClientContext() {

                @Override
                public MultiValuedPropertyMap<String> getProperties() {
                    return new SimpleMultiValuedPropertyMap<String>();
                }

                @Override
                public String getMethod() {
                    return "GET";
                }

                @Override
                public List<Cookie> getCookies() {
                    return Collections.emptyList();
                }
            });

            org.gatein.pc.api.Portlet portlet;
            PortletInvoker invoker = portletAppManager.getInvoker();
            try {
                String[] id = contentId.split("/");
                if (id.length == 2) {
                    portlet = invoker.getPortlet(PortletContext.createPortletContext(id[0], id[1]));
                } else {
                    throw new Exception("Could not handle " + contentId);
                }
            } catch (Exception e) {
                e.printStackTrace();
                portlet = null;
            }

            invocation.setPortalContext(new AbstractPortalContext());
            invocation.setInstanceContext(new AbstractInstanceContext(contentId, AccessMode.READ_ONLY));
            invocation.setWindowContext(new AbstractWindowContext(contentId));
            invocation.setUserContext(new AbstractUserContext());
            invocation.setSecurityContext(new AbstractSecurityContext(Context.getCurrentRequest()));
            invocation.setRequest(Context.getCurrentRequest());
            invocation.setResponse(Context.getCurrentResponse());
            invocation.setTarget(portlet.getContext());
            invocation.setMode(Mode.VIEW);
            invocation.setWindowState(org.gatein.pc.api.WindowState.NORMAL);
            invocation.setNavigationalState(null);
            invocation.setPublicNavigationalState(null);

            //
            response = invoker.invoke(invocation);
        } catch (PortletInvokerException e) {
            failure = e;
        } finally {
            lifeCycle.resume();
        }

        if (failure != null) {
            failure.printStackTrace();
            result.set("error", "Can't render portlet");
        } else {
            if (response instanceof FragmentResponse) {
                FragmentResponse fragment = (FragmentResponse) response;    
                String title = fragment.getTitle();
                result.set("title", title);
                result.set("content", fragment.getContent());
            } else {
                throw new UnsupportedOperationException("Not yet handled " + response);
            }
        }

        return Response.status(200).body(result.toString()).withCharset(Charset.forName("UTF-8"))
                    .withMimeType("application/json");
    }

    private void buildComponentTree(NodeContext<JSON, ElementState> context, JSON json, JSON parent) {
        String id = json.getString("id");
        String type = json.getString("type");

        NodeContext<JSON, ElementState> ctx = this.find(id, context);

        JSON componentData = null;
        if (ctx == null) {
            if ("container".equalsIgnoreCase(type)) {
                JSON data = new JSON().set("storageName", id)
                            .set("accessPermission", new ArrayList<String>())
                            .set("children", new ArrayList<JSON>());
                componentData = new JSON().set("id", id).set("type", "container").set("container", data);
                
                if (parent != null) {
                    List<JSON> children = (List<JSON>)parent.getJSON("container").getList("children");
                    children.add(componentData);
                    parent.getJSON("container").set("children", children);
                }
            } else if ("application".equalsIgnoreCase(type)) {
                String name = json.getString("name");
                String applicationName = json.getString("applicationName");
                String contentId = applicationName + "/" + name;
                JSON data = new JSON();
                data.set("storageName", id);
                data.set("type", ApplicationType.PORTLET.getName());
                data.set(ElementState.Window.SHOW_INFO_BAR.getName(), new Boolean(false));
                data.set(ElementState.Window.SHOW_APPLICATION_STATE.getName(), new Boolean(false));
                data.set(ElementState.Window.SHOW_APPLICATION_MODE.getName(), new Boolean(false));
                componentData = new JSON().set("id", id).set("type", "application").set("contentId", contentId).set("application", data);
                
                if (parent != null) {
                    List<JSON> children = (List<JSON>)parent.getJSON("container").getList("children");
                    children.add(componentData);
                    parent.getJSON("container").set("children", children);
                }
            }
        } else {
            if ("container".equalsIgnoreCase(type)) {
                for (JSON child : (List<JSON>)parent.getJSON("container").getList("children")) {
                    if (child.getJSON("container").getString("storageName").equals(id)) {
                        componentData = child;
                        break;
                    }
                }
            } else if ("application".equalsIgnoreCase(type)) {
                componentData = this.buildComponentData(ctx);
                if (parent != null) {
                    List<JSON> children = (List<JSON>)parent.getJSON("container").getList("children");
                    children.add(componentData);
                    parent.getJSON("container").set("children", children);
                }
            }
        }

        if (componentData == null) {
            return;
        }

        //Process children of this node
        JSON[] childrens = json.getArray("childrens", JSON.class);
        for (JSON j : childrens) {
            this.buildComponentTree(context, j, componentData);
        }
    }
    
    private JSON buildComponentData(NodeContext<JSON, ElementState> context) {
        ElementState state = context.getState();
        if (state instanceof ElementState.Container) {
            ElementState.Container containerState = (ElementState.Container) state;
            Properties properties = containerState.properties;
            JSON data = new JSON().set("storageId", context.getId())
                        .set("storageName", context.getName())
                        .set("id", context.getId())
                        .set(ElementState.Container.NAME.getName(), properties.get(ElementState.Container.NAME))
                        .set(ElementState.Container.ICON.getName(), properties.get(ElementState.Container.ICON))
                        .set(ElementState.Container.TEMPLATE.getName(), properties.get(ElementState.Container.TEMPLATE))
                        .set(ElementState.Container.FACTORY_ID.getName(), properties.get(ElementState.Container.FACTORY_ID))
                        .set(ElementState.Container.TITLE.getName(), properties.get(ElementState.Container.TITLE))
                        .set(ElementState.Container.DESCRIPTION.getName(), properties.get(ElementState.Container.DESCRIPTION))
                        .set(ElementState.Container.WIDTH.getName(), properties.get(ElementState.Container.WIDTH))
                        .set(ElementState.Container.HEIGHT.getName(), properties.get(ElementState.Container.HEIGHT))
                        .set("accessPermissions", containerState.getAccessPermissions())
                        .set("children", new ArrayList<JSON>());
            
            return new JSON().set("id", context.getName()).set("type", "container").set("container", data);
        } else if (state instanceof ElementState.Window) {
            ElementState.Window winState = (ElementState.Window) state;
            Properties properties = winState.properties;
            
            JSON data = new JSON().set("storageId", context.getId())
                        .set("storageName", context.getName())
                        .set("type", winState.type.getName())
                        .set("id", context.getId())
                        .set(ElementState.Window.TITLE.getName(), properties.get(ElementState.Window.TITLE))
                        .set(ElementState.Window.ICON.getName(), properties.get(ElementState.Window.ICON))
                        .set(ElementState.Window.DESCRIPTION.getName(), properties.get(ElementState.Window.DESCRIPTION))
                        .set(ElementState.Window.SHOW_INFO_BAR.getName(), properties.get(ElementState.Window.SHOW_INFO_BAR))
                        .set(ElementState.Window.SHOW_APPLICATION_STATE.getName(), properties.get(ElementState.Window.SHOW_APPLICATION_STATE))
                        .set(ElementState.Window.SHOW_APPLICATION_MODE.getName(), properties.get(ElementState.Window.SHOW_APPLICATION_MODE))
                        .set(ElementState.Window.THEME.getName(), properties.get(ElementState.Window.THEME))
                        .set(ElementState.Window.WIDTH.getName(), properties.get(ElementState.Window.WIDTH))
                        .set(ElementState.Window.HEIGHT.getName(), properties.get(ElementState.Window.HEIGHT))
                        .set("properties", new JSON())
                        .set("accessPermissions", new ArrayList<String>());
            return new JSON().set("id", context.getName()).set("type", "application").set("application", data);
        }
        return null;
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
