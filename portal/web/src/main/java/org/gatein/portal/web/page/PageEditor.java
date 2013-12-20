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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import juzu.Param;
import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.Route;
import juzu.impl.common.JSON;
import juzu.impl.common.Tools;
import juzu.impl.request.Request;
import juzu.request.ClientContext;
import juzu.request.RequestContext;
import juzu.request.UserContext;
import juzu.template.Template;
import org.gatein.portal.content.ContentDescription;
import org.gatein.portal.content.ContentProvider;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.content.ProviderRegistry;
import org.gatein.portal.content.Result;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.hierarchy.GenericScope;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.Scope;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.ui.navigation.UserNode;
import org.gatein.portal.web.layout.RenderingContext;
import org.gatein.portal.web.layout.ZoneLayout;
import org.gatein.portal.web.layout.ZoneLayoutFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PageEditor {

    @Inject
    PageService pageService;

    @Inject
    LayoutService layoutService;

    @Inject
    ZoneLayoutFactory layoutFactory;

    @Inject
    ProviderRegistry providers;
    
    @Inject
    NavigationService navigationService;

    @Inject
    DescriptionService descriptionService;
    
    @Inject
    @Path("add_new_page_modal.gtmpl")
    Template modalBody;
    
    @Resource
    @Route(value = "/modalbody")
    public Response getModalBody(UserContext userContext) {
        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));
        UserNode.Model model = new UserNode.Model(descriptionService, userContext.getLocale());
        NodeContext<UserNode, NodeState> root = navigationService.loadNode(model, navigation, Scope.ALL, null);
        List<String> holder = new ArrayList<String>();
        collectPaths(root, holder);
        return modalBody.with().set("parents", holder).ok();
    }
    private List<String> collectPaths(NodeContext<UserNode, NodeState> context, List<String> holder) {
        UserNode userNode = context.getNode();
        holder.add(userNode.getLink());
        for (Iterator<NodeContext<UserNode, NodeState>> i = context.iterator(); i.hasNext();) {
            collectPaths(i.next(), holder);
        }
        return holder;
    }
    
    @Resource
    @Route(value = "/nextstep")
    public Response nextStepToEdit(String pageName, String label, String parent, String factoryId) {
        PageKey pageKey = new PageKey(SiteKey.portal("classic"), pageName);
        ZoneLayout layout = (ZoneLayout) layoutFactory.builder(factoryId).build();
        StringBuilder sb = new StringBuilder();
        layout.render(new RenderingContext(null, null), Collections.<String, Result.Fragment>emptyMap(), null, null, sb);

        JSON data = new JSON();
        data.set("factoryId", factoryId);
        data.set("html", sb.toString());
        data.set("pageKey", pageKey.format());
        data.set("label", label);
        data.set("parent", parent);
        return Response.status(200).body(data.toString());
    }
    
    @Resource
    @Route(value = "/checkpage")
    public Response checkPageExisted(String pageName) {
        PageKey pageKey = new PageKey(SiteKey.portal("classic"), pageName);
        org.gatein.portal.mop.page.PageContext pageContext = pageService.loadPage(pageKey);
        return pageContext == null ? Response.status(200) : Response.status(500);
    }

    @Resource
    @Route(value = "/switchto/{javax.portlet.z}")
    public Response switchLayout(@Param(name = "javax.portlet.z") String id) throws Exception {
        ZoneLayout layout = (ZoneLayout) layoutFactory.builder(id).build();
        StringBuilder sb = new StringBuilder();
        layout.render(new RenderingContext(null, null), Collections.<String, Result.Fragment>emptyMap(), null, null, sb);

        JSON data = new JSON();
        data.set("factoryId", id);
        data.set("html", sb.toString());

        return Response.status(200).body(data.toString());
    }
    
    @Resource
    @Route(value = "/upload")
    public Response upload(ClientContext context) throws Exception {
        return Response.status(200).body("uploaded");
    }

    @Resource
    @Route(value = "/contents")
    public Response getAllContents() throws Exception {        
        JSONArray result = new JSONArray();
        for (ContentProvider<?> provider : providers.getProviders()) {
            JSONObject contentType = new JSONObject();
            ContentType type = provider.getContentType();
            contentType.put("value", type.getValue());
            contentType.put("tagName", type.getTagName());
            contentType.put("displayName", type.getTagName());
            JSONArray contents = new JSONArray();

            Iterable<ContentDescription> descriptions = provider.findContents("", 0, 30);
            for (ContentDescription description : descriptions) {
                JSONObject item = new JSONObject();
                item.put("contentId", description.id);
                item.put("contentType", type.getValue());
                item.put("title", description.displayName);
                item.put("description", description.markup);
                //result.put(item);
                contents.put(item);
            }
            contentType.put("contents", contents);
            result.put(contentType);
        }
        return Response.status(200).body(result.toString());
    }

    private org.gatein.portal.mop.page.PageContext createPage(PageKey pageKey, String label, String parent, String factoryId, UserContext userContext) {
        parent = parent.substring("/portal".length());
        label = label != null && !label.isEmpty() ? label : pageKey.getName();
        // Parse path
        List<String> names = new ArrayList<String>();
        for (String name : Tools.split(parent, '/')) {
            if (name.length() > 0) {
                names.add(name);
            }
        }

        NavigationContext navigation = navigationService.loadNavigation(SiteKey.portal("classic"));
        NodeContext<?, NodeState> root =  navigationService.loadNode(NodeState.model(), navigation, GenericScope.branchShape(names), null);
        // Get our node from the navigation
        NodeContext<?, NodeState> current = root;
        for (String name : names) {
            current = current.get(name);
            if (current == null) {
                break;
            }
        }

        //
        NodeContext<?, NodeState> pageNode = root.get(pageKey.getName());
        if (pageNode == null) {
            pageNode = current.add(null, pageKey.getName(), new NodeState.Builder().label(label).pageRef(pageKey).build());
            navigationService.saveNode(pageNode, null);
        } else {
            NodeState sate = pageNode.getState();
            pageNode.setState(new NodeState.Builder(sate).pageRef(pageKey).build());
            navigationService.saveNode(pageNode, null);
        }
        org.gatein.portal.mop.page.PageContext page = new org.gatein.portal.mop.page.PageContext(
                pageKey, 
                new PageState.Builder().displayName(label).factoryId(factoryId).build());

        pageService.savePage(page);
        return page;
    }

    @Resource
    @Route(value = "/savelayout/{javax.portlet.layoutid}")
    public Response saveLayout(RequestContext context, @Param(name = "javax.portlet.layoutid") String layoutId) throws Exception {
        JSONObject requestData = getRequestData(context);
        JSON result = new JSON();
        if ("newpage".equals(layoutId)) {
            PageKey pageKey = PageKey.parse(requestData.getString("pageKey"));
            String label = requestData.getString("label");
            String parent = requestData.getString("parent");
            String factoryId = requestData.getString("factoryId");
            org.gatein.portal.mop.page.PageContext pageContext = createPage(pageKey, label, parent, factoryId, Request.getCurrent().getUserContext());
            layoutId = pageContext.getLayoutId();
            result.set("redirect", parent + "/" + pageKey.getName());
        }
        
        NodeContext<JSONObject, ElementState> pageStructure = buildPageStructure(layoutId, requestData);
        if(requestData != null && pageStructure != null) {
            return Response.status(200).body(result.toString()).withCharset(Charset.forName("UTF-8")).withMimeType("application/json");

        } else if(pageStructure== null) {
            return Response.notFound("Can not edit because can not load layout with id " + layoutId);

        } else {
            return Response.status(400).body("Data is null");
        }
    }
    
    private NodeContext<JSONObject, ElementState> buildPageStructure(String layoutId, JSONObject requestData) throws JSONException {
        NodeContext<JSONObject, ElementState> pageStructure = null;
        pageStructure = (NodeContext<JSONObject, ElementState>) layoutService.loadLayout(ElementState.model(), layoutId, null);

        if(requestData != null && pageStructure != null) {
            org.exoplatform.portal.pom.data.JSONContainerAdapter adapter = new org.exoplatform.portal.pom.data.JSONContainerAdapter(requestData, pageStructure);

            layoutService.saveLayout(adapter, requestData, pageStructure, null);

            //Update layout
            String factoryId = requestData.getString("factoryId");
            String pageKey = requestData.getString("pageKey");
            if (factoryId != null && pageKey != null && !factoryId.isEmpty() && !pageKey.isEmpty()) {
                PageKey key = PageKey.parse(pageKey);
                org.gatein.portal.mop.page.PageContext page = pageService.loadPage(key);
                page.setState(page.getState().builder().factoryId(factoryId).build());
                pageService.savePage(page);
            }
        }
        return pageStructure;
    }

    private JSONObject getRequestData(RequestContext context) throws Exception {
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
        return stringBuilder.length() > 0 ? new JSONObject(stringBuilder.toString()) : null;
    }
}
