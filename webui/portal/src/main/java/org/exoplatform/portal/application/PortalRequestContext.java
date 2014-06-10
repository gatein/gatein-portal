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

package org.exoplatform.portal.application;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.exoplatform.Constants;
import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.commons.utils.PortalPrinter;
import org.exoplatform.commons.xml.DOMSerializer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.mop.user.UserPortalContext;
import org.exoplatform.portal.url.PortalURLContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.web.security.sso.SSOHelper;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLFactory;
import org.exoplatform.web.url.URLFactoryService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.url.ComponentURL;
import org.gatein.common.http.QueryStringParser;
import org.w3c.dom.Element;

/**
 * This class extends the abstract WebuiRequestContext which itself extends the RequestContext one
 *
 * <p>
 * It mainly implements the abstract methods and overide some.
 */
public class PortalRequestContext extends WebuiRequestContext {
    protected static Log log = ExoLogger.getLogger("portal:PortalRequestContext");

    public static final int PUBLIC_ACCESS = 0;

    public static final int PRIVATE_ACCESS = 1;

    public static final String UI_COMPONENT_ACTION = ComponentURL.PORTAL_COMPONENT_ACTION;

    public static final String UI_COMPONENT_ID = ComponentURL.PORTAL_COMPONENT_ID;

    public static final String TARGET_NODE = "portal:targetNode";

    public static final String CACHE_LEVEL = "portal:cacheLevel";

    public static final String REQUEST_TITLE = "portal:requestTitle".intern();

    public static final String REQUEST_METADATA = "portal:requestMetadata".intern();

    private static final String LAST_PORTAL_NAME = "prc.lastPortalName";

    private static final String DO_LOGIN_PATTERN = "login";

    /** The path decoded from the request. */
    private final String nodePath_;

    /** . */
    private final String requestURI_;

    /** . */
    private final String portalURI;

    /** . */
    private final SiteKey siteKey;

    /** The locale from the request. */
    private final Locale requestLocale;

    /** . */
    private final HttpServletRequest request_;

    /** . */
    private final HttpServletResponse response_;

    private String cacheLevel_ = "cacheLevelPortlet";

    private boolean ajaxRequest_ = true;

    private boolean forceFullUpdate = false;

    private Writer writer_;

    protected JavascriptManager jsmanager_;

    private List<Element> extraMarkupHeaders;

    private final PortalURLBuilder urlBuilder;

    private Map<String, String[]> parameterMap;

    private Locale locale = Locale.ENGLISH;

    /** . */
    private final URLFactoryService urlFactory;

    /** . */
    private final ControllerContext controllerContext;

    private UserPortalConfig userPortalConfig;

    public JavascriptManager getJavascriptManager() {
        return jsmanager_;
    }

    /**
     * Analyze a request and split this request's URI to get useful information then keep it in following properties of
     * PortalRequestContext :<br/>
     * 1. <code>requestURI</code> : The decoded URI of this request <br/>
     * 2. <code>portalOwner</code> : The portal name ( "classic" for instance )<br/>
     * 3. <code>portalURI</code> : The URI to current portal ( "/portal/public/classic/ for instance )<br/>
     * 4. <code>nodePath</code> : The path that is used to reflect to a navigation node
     */
    public PortalRequestContext(WebuiApplication app, ControllerContext controllerContext, String requestSiteType,
            String requestSiteName, String requestPath, Locale requestLocale) {
        super(app);

        //
        this.urlFactory = (URLFactoryService) PortalContainer.getComponent(URLFactoryService.class);
        this.controllerContext = controllerContext;
        this.jsmanager_ = new JavascriptManager();

        //
        request_ = controllerContext.getRequest();
        response_ = controllerContext.getResponse();
        response_.setBufferSize(1024 * 100);
        setSessionId(request_.getSession().getId());

        // The encoding needs to be set before reading any of the parameters since the parameters's encoding
        // is set at the first access.

        // TODO use the encoding from the locale-config.xml file
        response_.setContentType("text/html; charset=UTF-8");
        try {
            request_.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Encoding not supported", e);
        }

        // Query parameters from the request will be set in the servlet container url encoding and not
        // necessarly in utf-8 format. So we need to directly parse the parameters from the query string.
        parameterMap = new HashMap<String, String[]>();
        parameterMap.putAll(request_.getParameterMap());
        String queryString = request_.getQueryString();
        if (queryString != null) {
            // The QueryStringParser currently only likes & and not &amp;
            queryString = queryString.replace("&amp;", "&");
            Map<String, String[]> queryParams = QueryStringParser.getInstance().parseQueryString(queryString);
            parameterMap.putAll(queryParams);
        }

        ajaxRequest_ = "true".equals(request_.getParameter("ajaxRequest"));
        String cache = request_.getParameter(CACHE_LEVEL);
        if (cache != null) {
            cacheLevel_ = cache;
        }

        requestURI_ = request_.getRequestURI();
        /*
         * String decodedURI = URLDecoder.decode(requestURI_, "UTF-8");
         *
         * // req.getPathInfo will already have the encoding set from the server. // We need to use the UTF-8 value since this
         * is how we store the portal name. // Reconstructing the getPathInfo from the non server decoded values. String
         * servletPath = URLDecoder.decode(request_.getServletPath(), "UTF-8"); String contextPath =
         * URLDecoder.decode(request_.getContextPath(), "UTF-8"); String pathInfo = "/"; if (requestURI_.length() >
         * servletPath.length() + contextPath.length()) pathInfo = decodedURI.substring(servletPath.length() +
         * contextPath.length());
         *
         * int colonIndex = pathInfo.indexOf("/", 1); if (colonIndex < 0) { colonIndex = pathInfo.length(); } portalOwner_ =
         * pathInfo.substring(1, colonIndex); nodePath_ = pathInfo.substring(colonIndex, pathInfo.length());
         */
        //
        this.siteKey = new SiteKey(SiteType.valueOf(requestSiteType.toUpperCase()), requestSiteName);
        this.nodePath_ = requestPath;
        this.requestLocale = requestLocale;

        //
        NodeURL url = createURL(NodeURL.TYPE);
        url.setResource(new NavigationResource(siteKey, ""));
        portalURI = url.toString();

        //
        urlBuilder = new PortalURLBuilder(this, createURL(ComponentURL.TYPE));
    }

    @Override
    public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
        PortalURLContext urlContext = new PortalURLContext(controllerContext, siteKey);
        U url = urlFactory.newURL(resourceType, urlContext);
        if (url != null) {
            url.setAjax(false);
            url.setLocale(requestLocale);
        }
        return url;
    }

    public UserPortal getUserPortal() {
        UserPortalConfig upc = getUserPortalConfig();
        if (upc != null) {
            return upc.getUserPortal();
        } else {
            return null;
        }
    }

    public UserPortalConfig getUserPortalConfig() {
        if (userPortalConfig == null) {
            String portalName = null;
            String remoteUser = getRemoteUser();
            SiteType siteType = getSiteType();

            ExoContainer appContainer = getApplication().getApplicationServiceContainer();
            UserPortalConfigService service_ = (UserPortalConfigService) appContainer
                    .getComponentInstanceOfType(UserPortalConfigService.class);
            if (SiteType.PORTAL == siteType) {
                portalName = getSiteName();
            }

            HttpSession session = request_.getSession();
            if (portalName == null) {
                if (session != null) {
                    portalName = (String) session.getAttribute(LAST_PORTAL_NAME);
                }
            }

            if (portalName == null) {
                portalName = service_.getDefaultPortal();
            }
            try {
                userPortalConfig = service_.getUserPortalConfig(portalName, remoteUser,
                        PortalRequestContext.USER_PORTAL_CONTEXT);
                if (userPortalConfig != null) {
                    session.setAttribute(LAST_PORTAL_NAME, portalName);
                }
            } catch (Exception e) {
                return null;
            }
        }

        return userPortalConfig;
    }

    public void setUserPortalConfig(UserPortalConfig upc) {
        userPortalConfig = upc;
    }

    public String getInitialURI() {
        return request_.getRequestURI();
    }

    public ControllerContext getControllerContext() {
        return controllerContext;
    }

    public void refreshResourceBundle() throws Exception {
        appRes_ = findApplicationResourceBundle();
    }

    public void requestAuthenticationLogin() throws Exception {
        StringBuilder initialURI = new StringBuilder();
        initialURI.append(request_.getRequestURI());
        if (request_.getQueryString() != null) {
            initialURI.append("?").append(request_.getQueryString());
        }

        StringBuilder loginPath = new StringBuilder();

        //. Check SSO Enable
        ExoContainer container = getApplication().getApplicationServiceContainer();
        SSOHelper ssoHelper = (SSOHelper) container.getComponentInstanceOfType(SSOHelper.class);
        if(ssoHelper != null && ssoHelper.isSSOEnabled()) {
            loginPath.append(request_.getContextPath()).append(ssoHelper.getSSORedirectURLSuffix());
        } else {
            loginPath.append(request_.getContextPath()).append("/").append(DO_LOGIN_PATTERN);
        }

        loginPath.append("?initialURI=").append(URLEncoder.encode(initialURI.toString(), "UTF-8"));

        sendRedirect(loginPath.toString());
    }

    public String getTitle() throws Exception {
        String title = (String) request_.getAttribute(REQUEST_TITLE);

        //
        if (title == null) {
            UIPortal uiportal = Util.getUIPortal();

            //
            UserNode node = uiportal.getSelectedUserNode();
            if (node != null) {
                ExoContainer container = getApplication().getApplicationServiceContainer();
                container.getComponentInstanceOfType(UserPortalConfigService.class);
                UserPortalConfigService configService = (UserPortalConfigService) container
                        .getComponentInstanceOfType(UserPortalConfigService.class);
                PageKey pageRef = node.getPageRef();
                PageContext page = configService.getPage(pageRef);

                //
                if (page != null) {
                    title = page.getState().getDisplayName();
                    String resolvedTitle = ExpressionUtil.getExpressionValue(this.getApplicationResourceBundle(), title);
                    if (resolvedTitle != null) {
                        return resolvedTitle;
                    }
                }
                title = node.getResolvedLabel();
            }
        }

        return title == null ? "" : title;
    }

    @Override
    public URLFactory getURLFactory() {
        return urlFactory;
    }

    public Orientation getOrientation() {
        return ((UIPortalApplication) uiApplication_).getOrientation();
    }

    public Locale getRequestLocale() {
        return requestLocale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMetaInformation() {
        return (Map<String, String>) request_.getAttribute(REQUEST_METADATA);
    }

    public String getCacheLevel() {
        return cacheLevel_;
    }

    public String getRequestParameter(String name) {
        if (parameterMap.get(name) != null && parameterMap.get(name).length > 0) {
            return parameterMap.get(name)[0];
        } else {
            return null;
        }
    }

    public String[] getRequestParameterValues(String name) {
        return parameterMap.get(name);
    }

    public Map<String, String[]> getPortletParameters() {
        Map<String, String[]> unsortedParams = parameterMap;
        Map<String, String[]> sortedParams = new HashMap<String, String[]>();
        Set<String> keys = unsortedParams.keySet();
        for (String key : keys) {
            if (!key.startsWith(Constants.PARAMETER_ENCODER)) {
                sortedParams.put(key, unsortedParams.get(key));
            }
        }
        return sortedParams;
    }

    public final String getRequestContextPath() {
        return request_.getContextPath();
    }

    @Override
    public String getPortalContextPath() {
        return getRequestContextPath();
    }

    public String getActionParameterName() {
        return PortalRequestContext.UI_COMPONENT_ACTION;
    }

    public String getUIComponentIdParameterName() {
        return ComponentURL.PORTAL_COMPONENT_ID;
    }

    public SiteType getSiteType() {
        return siteKey.getType();
    }

    public String getSiteName() {
        return siteKey.getName();
    }

    public SiteKey getSiteKey() {
        return siteKey;
    }

    public String getPortalOwner() {
        UserPortalConfig userPortalConfig = getUserPortalConfig();
        if (userPortalConfig != null) {
            return userPortalConfig.getPortalName();
        } else {
            return null;
        }
    }

    public String getNodePath() {
        return nodePath_;
    }

    public String getRequestURI() {
        return requestURI_;
    }

    public String getPortalURI() {
        return portalURI;
    }

    public URLBuilder<UIComponent> getURLBuilder() {
        return urlBuilder;
    }

    public int getAccessPath() {
        return request_.getRemoteUser() != null ? PRIVATE_ACCESS : PUBLIC_ACCESS;
    }

    public final String getRemoteUser() {
        return request_.getRemoteUser();
    }

    public final boolean isUserInRole(String roleUser) {
        return request_.isUserInRole(roleUser);
    }

    public final Writer getWriter() throws Exception {
        if (writer_ == null) {
            writer_ = new PortalPrinter(response_.getOutputStream(), true, 30000);
        }
        return writer_;
    }

    public final void setWriter(Writer writer) {
        this.writer_ = writer;
    }

    public final boolean useAjax() {
        return ajaxRequest_;
    }

    @SuppressWarnings("unchecked")
    public final HttpServletRequest getRequest() {
        return request_;
    }

    @SuppressWarnings("unchecked")
    public final HttpServletResponse getResponse() {
        return response_;
    }

    /**
     *
     * @see org.exoplatform.web.application.RequestContext#getFullRender()
     */
    public final boolean getFullRender() {
        return forceFullUpdate;
    }

    /**
     * Sets a boolean value to force whether portal will be fully rendered and it is only effective to an Ajax request.
     * <p/>
     *
     * if the value is set to <code>true</code>, it means :<br/>
     *
     * 1) Only portal ui components are rendered <br/>
     * 2) Portlets will be fully rendered if are inner of the portal ui components being updated
     *
     * @param forceFullUpdate
     *
     *        This method is deprecated, ignoreAJAXUpdateOnPortlets should be used instead
     */
    @Deprecated()
    public final void setFullRender(boolean forceFullUpdate) {
        this.forceFullUpdate = forceFullUpdate;
    }

    /**
     * Call to this method makes sense only in the scope of an AJAX request.
     *
     * Invoking ignoreAJAXUpdateOnPortlets(true) as there is need to update only UI components of portal (ie: the components
     * outside portlet windows) are updated by AJAX. In the request response, all the blocks <PortletRespond > are empty. The
     * content displayed in portlet windows are retrieved by non-AJAX render request to associated portlet object.
     *
     *
     * @param ignoreAJAXUpdateOnPortlets
     */
    public final void ignoreAJAXUpdateOnPortlets(boolean ignoreAJAXUpdateOnPortlets) {
        this.forceFullUpdate = ignoreAJAXUpdateOnPortlets;
    }

    public final void sendError(int sc) throws IOException {
        setResponseComplete(true);
        response_.sendError(sc);
    }

    public final void sendRedirect(String url) throws IOException {
        setResponseComplete(true);
        response_.sendRedirect(url);
    }

    public void setHeaders(Map<String, String> headers) {
        Set<String> keys = headers.keySet();
        for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
            String key = iter.next();
            response_.setHeader(key, headers.get(key));
        }
    }

    public List<String> getExtraMarkupHeadersAsStrings() throws Exception {
        List<String> markupHeaders = new ArrayList<String>();

        if (extraMarkupHeaders != null && !extraMarkupHeaders.isEmpty()) {
            for (Element element : extraMarkupHeaders) {
                StringWriter sw = new StringWriter();
                DOMSerializer.serialize(element, sw);
                markupHeaders.add(sw.toString());
            }
        }

        return markupHeaders;
    }

    /**
     * Get the extra markup headers to add to the head of the html.
     *
     * @return The markup to be added.
     */
    public List<Element> getExtraMarkupHeaders() {
        return this.extraMarkupHeaders;
    }

    /**
     * Add an extra markup to the head of the html page.
     *
     * @param element The element to add
     * @param portletWindowId The ID of portlet window contributing markup header
     */
    public void addExtraMarkupHeader(Element element, String portletWindowId) {
        element.setAttribute("class", "ExHead-" + portletWindowId);
        if (this.extraMarkupHeaders == null) {
            this.extraMarkupHeaders = new ArrayList<Element>();
        }
        this.extraMarkupHeaders.add(element);
    }

    public static final UserPortalContext USER_PORTAL_CONTEXT = new UserPortalContext() {
        public ResourceBundle getBundle(UserNavigation navigation) {
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            ResourceBundleManager rbMgr = (ResourceBundleManager) container
                    .getComponentInstanceOfType(ResourceBundleManager.class);
            Locale locale = Util.getPortalRequestContext().getLocale();
            return rbMgr.getNavigationResourceBundle(LocaleContextInfo.getLocaleAsString(locale), navigation.getKey().getTypeName(), navigation
                    .getKey().getName());
        }

        public Locale getUserLocale() {
            return Util.getPortalRequestContext().getLocale();
        }
    };

    public RequestNavigationData getNavigationData() {
        return new RequestNavigationData(controllerContext.getParameter(RequestNavigationData.REQUEST_SITE_TYPE),
                controllerContext.getParameter(RequestNavigationData.REQUEST_SITE_NAME),
                controllerContext.getParameter(RequestNavigationData.REQUEST_PATH));
    }
}
