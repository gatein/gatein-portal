/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.webui.workspace;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.application.StandaloneAppRequestContext;
import org.exoplatform.portal.resource.Skin;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.url.URLWriter;
import org.exoplatform.portal.webui.application.UIStandaloneAppContainer;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.url.MimeType;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.url.ComponentURL;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.FetchMap;
import org.gatein.portal.controller.resource.script.FetchMode;
import org.gatein.portal.controller.resource.script.Module;
import org.gatein.portal.controller.resource.script.ScriptResource;
import org.json.JSONObject;

@ComponentConfig(lifecycle = UIStandaloneApplicationLifecycle.class, template = "system:/groovy/portal/webui/workspace/UIStandaloneApplication.gtmpl")
public class UIStandaloneApplication extends UIApplication {
    public static final int NORMAL_MODE = 0;

    // modeState, locale, skin_, orientation will be used when we display portlet in standalone mode
    private int modeState = NORMAL_MODE;

    private Locale locale_ = Locale.ENGLISH;

    private String skin_ = "Default";

    private Orientation orientation_ = Orientation.LT;

    private boolean isSessionOpen = false;

    public static final UIComponent EMPTY_COMPONENT = new UIComponent() {
        public String getId() {
            return "{portal:componentId}";
        };
    };

    public UIStandaloneApplication() throws Exception {
        addChild(UIStandaloneAppContainer.class, null, null);
    }

    // Temporary need this, don't want to render UIPopupMessage
    public void renderChildren() throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        super.renderChildren(context);
    }

    @Override
    public void processDecode(WebuiRequestContext context) throws Exception {
        String storageId = ((StandaloneAppRequestContext) context).getStorageId();

        UIStandaloneAppContainer staContaner = getChild(UIStandaloneAppContainer.class);
        if (!storageId.equals(staContaner.getCurrStorageId())) {
            staContaner.setCurrStorageId(storageId);
        }
        super.processDecode(context);
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        Writer w = context.getWriter();

        //
        if (!context.useAjax()) {
            super.processRender(context);
        } else {
            Set<UIComponent> list = context.getUIComponentToUpdateByAjax();

            w.write("<div class=\"PortalResponse\">");
            w.write("<div class=\"PortalResponseData\">");
            if (list != null) {
                for (UIComponent uicomponent : list) {
                    if (log.isDebugEnabled()) {
                        log.debug("AJAX call: Need to refresh the UI component " + uicomponent.getName());
                    }
                    renderBlockToUpdate(uicomponent, context, w);
                }
            }
            w.write("</div>");
            w.write("<div class=\"LoadingScripts\">");
            writeLoadingScripts(context);
            w.write("</div>");
            w.write("<div class=\"PortalResponseScript\">");
            JavascriptManager jsManager = context.getJavascriptManager();
            w.write(jsManager.getJavaScripts());
            w.write("</div>");
            w.write("</div>");
        }
    }

    private void writeLoadingScripts(WebuiRequestContext context) throws Exception {
        Writer w = context.getWriter();
        Map<String, Boolean> scriptURLs = getScripts();
        w.write("<div class=\"ImmediateScripts\">");
        w.write(StringUtils.join(scriptURLs.keySet(), ","));
        w.write("</div>");
    }

    public Map<String, Boolean> getScripts() {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        JavascriptManager jsMan = context.getJavascriptManager();

        // Need to add bootstrap as immediate since it contains the loader
        jsMan.loadScriptResource(ResourceScope.SHARED, "bootstrap");

        //
        FetchMap<ResourceId> requiredResources = jsMan.getScriptResources();
        log.debug("Resource ids to resolve: {}", requiredResources);

        //
        JavascriptConfigService service = getApplicationComponent(JavascriptConfigService.class);
        LinkedHashMap<String, Boolean> ret = new LinkedHashMap<String, Boolean>();
        Map<ScriptResource, FetchMode> tmp = service.resolveIds(requiredResources);
        for (ScriptResource rs : tmp.keySet()) {
            ResourceId id = rs.getId();
            boolean isRemote = !rs.isEmpty() && rs.getModules().get(0) instanceof Module.Remote;
            ret.put(id.toString(), isRemote);
        }
        for (String url : jsMan.getExtendedScriptURLs()) {
            ret.put(url, true);
        }

        //
        log.debug("Resolved resources for page: " + ret);

        return ret;
    }

    public JSONObject getJSConfig() throws Exception {
        JavascriptConfigService service = getApplicationComponent(JavascriptConfigService.class);
        StandaloneAppRequestContext context = WebuiRequestContext.getCurrentInstance();
        return service.getJSConfig(context.getControllerContext(), context.getLocale());
    }

    public Collection<Skin> getPortalSkins() {
        SkinService skinService = getApplicationComponent(SkinService.class);
        Collection<Skin> skins = new ArrayList<Skin>(skinService.getPortalSkins(skin_));
        return skins;
    }

    public Set<Skin> getPortletSkins() {
        Set<Skin> skins = new HashSet<Skin>();
        return skins;
    }

    public boolean isSessionOpen() {
        return isSessionOpen;
    }

    public void setSessionOpen(boolean isSessionOpen) {
        this.isSessionOpen = isSessionOpen;
    }

    public String getSkin() {
        return skin_;
    }

    public Orientation getOrientation() {
        return orientation_;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation_ = orientation;
    }

    public Locale getLocale() {
        return locale_;
    }

    public void setLocale(Locale locale) {
        locale_ = locale;
    }

    public void setModeState(int mode) {
        this.modeState = mode;
    }

    public int getModeState() {
        return modeState;
    }

    /**
     * Return the portal url template which will be sent to client ( browser ) and used for JS based portal url generation.
     *
     * <p>
     * The portal url template are calculated base on the current request and site state. Something like :
     * <code>"/portal/g/:platform:administrators/administration/registry?portal:componentId={portal:uicomponentId}&portal:action={portal:action}" ;</code>
     *
     * @return return portal url template
     * @throws java.io.UnsupportedEncodingException
     */
    public String getPortalURLTemplate() throws UnsupportedEncodingException {
        StandaloneAppRequestContext context = WebuiRequestContext.getCurrentInstance();
        ComponentURL urlTemplate = context.createURL(ComponentURL.TYPE);
        if (URLWriter.isUrlEncoded()) {
            urlTemplate.setMimeType(MimeType.XHTML);
        } else {
            urlTemplate.setMimeType(MimeType.PLAIN);
        }
        urlTemplate.setPath(context.getNodePath());
        urlTemplate.setResource(EMPTY_COMPONENT);
        urlTemplate.setAction("{portal:action}");

        return URLDecoder.decode(urlTemplate.toString(), "UTF-8");
    }
}
