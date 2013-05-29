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

package org.exoplatform.portal.webui.portal;

import org.exoplatform.portal.account.UIAccountSetting;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.ChangeApplicationListActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.ChangeLanguageActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.ChangeSkinActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.EditPortalPropertiesActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.MoveChildActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.RecoveryPasswordAndUsernameActionListener;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.ShowLoginFormActionListener;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.login.LoginServlet;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.web.security.impersonation.ImpersonatedIdentity;
import org.gatein.web.security.impersonation.ImpersonationServlet;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentConfig(lifecycle = UIPortalLifecycle.class, template = "system:/groovy/portal/webui/portal/UIPortal.gtmpl", events = {
        @EventConfig(listeners = ChangeApplicationListActionListener.class),
        @EventConfig(listeners = MoveChildActionListener.class), @EventConfig(listeners = UIPortal.LogoutActionListener.class),
        @EventConfig(listeners = ShowLoginFormActionListener.class),
        @EventConfig(listeners = ChangeLanguageActionListener.class),
        @EventConfig(listeners = EditPortalPropertiesActionListener.class),
        @EventConfig(listeners = ChangeSkinActionListener.class),
        @EventConfig(listeners = RecoveryPasswordAndUsernameActionListener.class),
        @EventConfig(listeners = UIPortal.AccountSettingsActionListener.class),
        @EventConfig(listeners = UIPortalActionListener.PingActionListener.class) })
public class UIPortal extends UIContainer {
    private SiteKey siteKey;

    private String locale;

    private String label;

    private String description;

    private String editPermission;

    private String skin;

    private Properties properties;

    private UserNode navPath;

    private Map<String, UIPage> all_UIPages;

    private Map<String, String[]> publicParameters_ = new HashMap<String, String[]>();

    private UIComponent maximizedUIComponent;

    private ArrayList<PortalRedirect> portalRedirects;

    public SiteKey getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(SiteKey key) {
        siteKey = key;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String s) {
        locale = s;
    }

    public String getEditPermission() {
        return editPermission;
    }

    public void setEditPermission(String editPermission) {
        this.editPermission = editPermission;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String s) {
        skin = s;
    }

    /**
     * @deprecated Use {@link #getSiteType()} instead
     *
     * @return
     */
    @Deprecated
    public String getOwnerType() {
        return siteKey.getTypeName();
    }

    public SiteType getSiteType() {
        return siteKey.getType();
    }

    public Map<String, String[]> getPublicParameters() {
        return publicParameters_;
    }

    public void setPublicParameters(Map<String, String[]> publicParams) {
        publicParameters_ = publicParams;
    }

    public UserNode getNavPath() {
        if (navPath == null) {
            PortalRequestContext prc = Util.getPortalRequestContext();
            navPath = prc.getUserPortalConfig().getUserPortal().getDefaultPath(null);
        }
        return navPath;
    }

    public void setNavPath(UserNode nav) {
        this.navPath = nav;
    }

    /**
     * Return cached UIPage associated to the specified pageReference
     *
     * @param pageReference key whose associated UIPage is to be returned
     * @return the UIPage associated to the specified pageReference or null if not any
     */
    public UIPage getUIPage(String pageReference) {
        if (all_UIPages == null) {
            return null;
        }
        return this.all_UIPages.get(pageReference);
    }

    public void setUIPage(String pageReference, UIPage uiPage) {
        if (this.all_UIPages == null) {
            this.all_UIPages = new HashMap<String, UIPage>(5);
        }
        this.all_UIPages.put(pageReference, uiPage);
    }

    public void clearUIPage(String pageReference) {
        if (this.all_UIPages != null)
            this.all_UIPages.remove(pageReference);
    }

    public UserNavigation getUserNavigation() {
        PortalRequestContext prc = Util.getPortalRequestContext();
        return prc.getUserPortalConfig().getUserPortal().getNavigation(siteKey);
    }

    /**
     * Refresh the UIPage under UIPortal
     *
     * @throws Exception
     */
    public void refreshUIPage() throws Exception {
        UIPageBody uiPageBody = findFirstComponentOfType(UIPageBody.class);
        if (uiPageBody == null) {
            return;
        }

        uiPageBody.setPageBody(getSelectedUserNode(), this);
    }

    public UserNode getSelectedUserNode() throws Exception {
        return getNavPath();
    }

    public UIComponent getMaximizedUIComponent() {
        return maximizedUIComponent;
    }

    public void setMaximizedUIComponent(UIComponent maximizedReferenceComponent) {
        this.maximizedUIComponent = maximizedReferenceComponent;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties props) {
        properties = props;
    }

    public String getProperty(String name) {
        if (name == null)
            throw new NullPointerException();
        if (properties == null)
            return null;
        return properties.get(name);
    }

    public String getProperty(String name, String defaultValue) {
        String value = getProperty(name);
        if (value == null)
            value = defaultValue;
        return value;
    }

    public void setProperty(String name, String value) {
        if (name == null || properties == null)
            throw new NullPointerException();
            properties.setProperty(name, value);
    }

    public void removeProperty(String name) {
        if (name == null)
            throw new NullPointerException();
        properties.setProperty(name, null);
    }

    public String getSessionAlive() {
        return getProperty(PortalProperties.SESSION_ALIVE, PortalProperties.SESSION_ON_DEMAND);
    }

    public void setSessionAlive(String type) {
        setProperty(PortalProperties.SESSION_ALIVE, type);
    }

    public Boolean isShowInfobar() {
        String value = getProperty(PortalProperties.SHOW_PORTLET_INFO, "1");
        if (Integer.parseInt(value) == 1) {
            return true;
        }
        return false;
    }

    public void setShowInfobar(Boolean value) {
        if (value) {
            setProperty(PortalProperties.SHOW_PORTLET_INFO, "1");
        } else {
            setProperty(PortalProperties.SHOW_PORTLET_INFO, "0");
        }
    }

    public String getCacheControl() {
        return getProperty(PortalProperties.CACHE_CONTROL);
    }

    public void setCacheControl(String cacheControl) {
        setProperty(PortalProperties.CACHE_CONTROL, cacheControl);
    }

    public String getViewport() {
        return getProperty(PortalProperties.VIEWPORT);
    }

    public void setViewport(String viewport) {
        setProperty(PortalProperties.VIEWPORT, viewport);
    }

    public String getSharedLayout() {
        return getProperty(PortalProperties.SHARED_LAYOUT, PortalProperties.SHARED_LAYOUT_ALL);
    }

    public void setSharedLayout(String type) {
        if (type.equals(PortalProperties.SHARED_LAYOUT_ADMINS)) {
            setProperty(PortalProperties.SHARED_LAYOUT, PortalProperties.SHARED_LAYOUT_ADMINS);
        } else {
            setProperty(PortalProperties.SHARED_LAYOUT, PortalProperties.SHARED_LAYOUT_ALL);
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static class LogoutActionListener extends EventListener<UIComponent> {
        public void execute(Event<UIComponent> event) throws Exception {
            PortalRequestContext prContext = Util.getPortalRequestContext();
            HttpServletRequest req = prContext.getRequest();

            // Check if we are in the middle of impersonation and want to cancel it
            Identity identity = ConversationState.getCurrent().getIdentity();
            if (identity instanceof ImpersonatedIdentity) {

                // Redirect to ImpersonationServlet and trigger stop of Impersonation session
                String redirectURI = req.getContextPath() + ImpersonationServlet.IMPERSONATE_URL_SUFIX;

                redirectURI = new StringBuilder(redirectURI)
                        .append("?")
                        .append(ImpersonationServlet.PARAM_ACTION)
                        .append("=")
                        .append(ImpersonationServlet.PARAM_ACTION_STOP_IMPERSONATION)
                        .toString();

                prContext.sendRedirect(redirectURI);
                return;
            }

            // Delete the token from JCR
            String token = getTokenCookie(req);
            if (token != null) {
                AbstractTokenService<GateInToken, String> tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
                tokenService.deleteToken(token);
            }

            String portalName = prContext.getPortalOwner();
            NodeURL createURL = prContext.createURL(NodeURL.TYPE);
            createURL.setResource(new NavigationResource(SiteType.PORTAL, portalName, null));

            LogoutControl.wantLogout();
            Cookie cookie = new Cookie(LoginServlet.COOKIE_NAME, "");
            cookie.setPath(req.getContextPath());
            cookie.setMaxAge(0);
            prContext.getResponse().addCookie(cookie);

            prContext.sendRedirect(createURL.toString());
        }

        private String getTokenCookie(HttpServletRequest req) {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (LoginServlet.COOKIE_NAME.equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }

    }

    public static class AccountSettingsActionListener extends EventListener<UIPortal> {
        public void execute(Event<UIPortal> event) throws Exception {
            UIPortal uiPortal = event.getSource();
            UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

            // Modified by nguyenanhkien2a@gmail.com
            // We should check account for existing
            String username = Util.getPortalRequestContext().getRemoteUser();
            OrganizationService service = uiPortal.getApplicationComponent(OrganizationService.class);
            User useraccount = service.getUserHandler().findUserByName(username);

            if (useraccount != null) {
                UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null);
                uiMaskWS.setUIComponent(uiAccountForm);
                uiMaskWS.setShow(true);
                event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
            } else {
                // Show message detail to user and then logout if user press ok button
                JavascriptManager jsManager = Util.getPortalRequestContext().getJavascriptManager();
                jsManager.require("SHARED/base").addScripts(
                        "if(confirm('"
                                + Util.getPortalRequestContext().getApplicationResourceBundle()
                                        .getString("UIAccountProfiles.msg.NotExistingAccount") + "')) {eXo.portal.logout();}");
            }
        }
    }

    public void setRedirects(ArrayList<PortalRedirect> portalRedirects) {
        this.portalRedirects = portalRedirects;
    }

    public ArrayList<PortalRedirect> getPortalRedirects() {
        return portalRedirects;
    }

    @Override
    public String getPermissionClasses() {
        StringBuilder permissionClasses = new StringBuilder();
        /* here we use ProtectedContainer to mark the situation when header and footer
         * should not be editable in during fullPreview. ProtectedContainer is used by JavaScript
         * on the client side. */
        if (UIPage.isFullPreviewInPageEditor()) {
            permissionClasses.append(" ProtectedContainer");
        }

        if (!hasMoveAppsPermission()) {
            permissionClasses.append(" CannotMoveApps");
        }
        if (!hasMoveContainersPermission()) {
            permissionClasses.append(" CannotMoveContainers");
        }
        return permissionClasses.toString();
    }

    public void setHeaderAndFooterRendered(boolean headerAndFooterRendered) {
        List<UIComponent> list = getChildren();
        for (UIComponent child : list) {
            if (child instanceof UIPageBody) {
                /* do not touch the page body */
            } else if (child.isRendered() != headerAndFooterRendered) {
                child.setRendered(headerAndFooterRendered);
            }
        }
    }
}
