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

package org.exoplatform.portal.webui.application;

import org.exoplatform.Constants;
import org.exoplatform.commons.utils.Text;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.UserProfileLifecycle;
import org.exoplatform.portal.application.state.ContextualPropertyManager;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.portlet.PortletExceptionHandleService;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ChangePortletModeActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ChangeWindowStateActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.EditPortletActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ProcessActionActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ProcessEventsActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.RenderActionListener;
import org.exoplatform.portal.webui.application.UIPortletActionListener.ServeResourceActionListener;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComponentActionListener.DeleteComponentActionListener;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.portletcontainer.PortletContainerException;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event.Phase;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.ModeInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.EventInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.portlet.impl.spi.AbstractClientContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractRequestContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractWindowContext;
import org.gatein.portal.controller.resource.ResourceScope;
import org.w3c.dom.Element;

import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 * This UI component represent a portlet window on a page. <br/>
 * Each user request to a portlet will be passed through this class then delegate call to the portlet container<br/>
 * UIPortletLifecycle do the main request router: delegate the job to portlet action listeners according to the url parameters<br/>
 *
 * ProcessAction, ServeResource, Render action listeners will receive event if request url contain parameter
 * point to them, those event will delegate call to portlet container to call JSR 286 portlet lifecycle method<br/>
 *
 * ProcessEvents, ChangePortletMode, ChangeWindowState listener will receive event after the portlet action invocation response.
 * (dispatched during the process of ProcessActionListener)<br/>
 *
 * DeleteComponent, EditPortlet action listener is portal specific listener, come from the UI of portal
 *
 * @see UIPortletLifecycle
 * @see UIPortletActionListener
 */
@ComponentConfig(lifecycle = UIPortletLifecycle.class, template = "system:/groovy/portal/webui/application/UIPortlet.gtmpl", events = {
        @EventConfig(listeners = RenderActionListener.class), @EventConfig(listeners = ChangePortletModeActionListener.class),
        @EventConfig(listeners = ChangeWindowStateActionListener.class),
        @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIPortlet.deletePortlet"),
        @EventConfig(listeners = EditPortletActionListener.class),
        @EventConfig(phase = Phase.PROCESS, listeners = ProcessActionActionListener.class),
        @EventConfig(phase = Phase.PROCESS, listeners = ServeResourceActionListener.class),
        @EventConfig(phase = Phase.PROCESS, listeners = ProcessEventsActionListener.class) })
public class UIPortlet<S, C extends Serializable> extends UIApplication {

    protected static final Log log = ExoLogger.getLogger("portal:UIPortlet");

    public static final String DEFAULT_THEME = "Default:DefaultTheme::Vista:VistaTheme::Mac:MacTheme";
    public static final String JAVASCRIPT_DEPENDENCY = "org.gatein.javascript.dependency";
    private static final String WSRP_URL = "wsrp-url";
    private static final String WSRP_PREFER_OPERATION = "wsrp-preferOperation";
    private static final String WSRP_REQUIRES_REWRITE = "wsrp-requiresRewrite";
    private static final String WSRP_NAVIGATIONAL_VALUES = "wsrp-navigationalValues";
    private static final AbstractPortalContext PORTAL_CONTEXT = new AbstractPortalContext(Collections.singletonMap(
            "javax.portlet.markup.head.element.support", "true"));

    private static final String GTN_PREFIX = "gtn";

    /** . */
    private String storageId;

    /** . */
    private String storageName;

    /** . */
    private ModelAdapter<S, C> adapter;

    /** . */
    private org.gatein.pc.api.Portlet producedOfferedPortlet;

    /** . */
    private PortletContext producerOfferedPortletContext;

    /** A computed field that contains the runtime description of the portlet for edit mode. */
    private LocalizedString displayName;

    /** . */
    private PortletState<S> state;

    /** . */
    private String applicationId;

    private String theme_;

    private String portletStyle;

    private boolean showPortletMode = true;

    private PortletMode currentPortletMode_ = PortletMode.VIEW;

    private WindowState currentWindowState_ = WindowState.NORMAL;

    private List<String> supportModes_;

    private List<QName> supportedProcessingEvents_;

    private List<QName> supportedPublishingEvents_;

    private Map<QName, String> supportedPublicParams_;

    private boolean portletInPortal_ = true;

    private StateString navigationalState;

    /** A field storing localized value of javax.portlet.title * */
    private String configuredTitle;

    public UIPortlet() {
        // That value will be overriden when it is mapped onto a data storage
        storageName = UUID.randomUUID().toString();
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getWindowId() {
        return storageName;
    }

    /**
     * Retrieves the skin identifier associated with this portlet or <code>null</code> if there isn't one (for example, it
     * doesn't make any sense in the WSRP scenario).
     *
     * @return the skin identifier associated with this portlet or <code>null</code> if there isn't one
     */
    public String getSkinId() {
        ApplicationType<S> type = state.getApplicationType();
        if (type == ApplicationType.PORTLET) {
            return applicationId;
        } else if (type == ApplicationType.GADGET) {
            return "dashboard/GadgetPortlet";
        } else {
            return null;
        }
    }

    public String getId() {
        return new StringBuilder().append(GTN_PREFIX).append(storageName).toString();
    }

    public String getApplicationId() {
        return applicationId;
    }

    /**
     * portletStyle is 'Window' when it's in WebOS project - an GateIn extension,
     * portletStyle is null if is not in WebOS
     * @return a string represent current portlet style
     */
    public String getPortletStyle() {
        return portletStyle;
    }

    public void setPortletStyle(String s) {
        portletStyle = s;
    }

    /**
     * @return true if portlet is configured to show control icon that allow to change portlet mode
     */
    public boolean getShowPortletMode() {
        return showPortletMode;
    }

    /**
     * Used by portal to show the icon that allow to change portlet mode
     * @param b if show icon
     */
    public void setShowPortletMode(Boolean b) {
        showPortletMode = b;
    }

    /**
     *  Used internally by portal to change current state
     *  if portlet is in portal or in page
     */
    public void setPortletInPortal(boolean b) {
        portletInPortal_ = b;
    }

    /**
     * Check if portlet is in portal
     * @return true if portlet is in portal
     */
    public boolean isPortletInPortal() {
        return portletInPortal_;
    }

    /**
     * Theme is composed of map between theme name and skin name.
     * Theme format: {skinName}:{themeName}::{anotherSkin}:{anotherTheme}.
     * For example: the default them is 'Default:DefaultTheme::Vista:VistaTheme::Mac:MacTheme'.
     * Default theme means if portal skin is 'Default', this portlet's theme is 'DefaultTheme. If portal change skin to 'Vista',
     * portlet theme will be change to 'VistaTheme'.
     * @return current theme setting
     */
    public String getTheme() {
        if (theme_ == null || theme_.trim().length() < 1) {
            return DEFAULT_THEME;
        }
        return theme_;
    }

    /**
     * Used internally by Portal to change current portlet theme.
     * Theme format: {skinName}:{themeName}::{anotherSkin}:{anotherTheme}.
     * For example: 'Default:DefaultTheme::Vista:VistaTheme::Mac:MacTheme'
     */
    public void setTheme(String theme) {
        theme_ = theme;
    }

    /**
     * Get theme name according to portal skin.
     * If there is no coressponding theme. return 'DefaultTheme'
     * @param skin - portal skin
     * @return theme name
     */
    public String getSuitedTheme(String skin) {
        if (skin == null) {
            skin = Util.getUIPortalApplication().getSkin();
        }
        Map<String, String> themeMap = stringToThemeMap(getTheme());
        if (themeMap.containsKey(skin)) {
            return themeMap.get(skin);
        }
        return DEFAULT_THEME.split(":")[1];
    }

    /**
     * Add map between portlet theme and portal skin
     * @param skin - portal skin name
     * @param theme - portlet theme name
     */
    public void putSuitedTheme(String skin, String theme) {
        if (skin == null) {
            skin = Util.getUIPortalApplication().getSkin();
        }
        Map<String, String> themeMap = stringToThemeMap(getTheme());
        themeMap.put(skin, theme);
        setTheme(themeMapToString(themeMap));
    }

    private String themeMapToString(Map<String, String> themeMap) {
        StringBuffer builder = new StringBuffer();
        Iterator<Entry<String, String>> itr = themeMap.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, String> entry = itr.next();
            builder.append(entry.getKey()).append(":").append(entry.getValue());
            if (itr.hasNext()) {
                builder.append("::");
            }
        }
        return builder.toString();
    }

    private Map<String, String> stringToThemeMap(String themesString) {
        Map<String, String> themeMap = new HashMap<String, String>();
        String[] themeIds = themesString.split("::");
        for (String ele : themeIds) {
            String[] strs = ele.split(":");
            themeMap.put(strs[0], strs[1]);
        }
        return themeMap;
    }

    public PortletMode getCurrentPortletMode() {
        return currentPortletMode_;
    }

    public void setCurrentPortletMode(PortletMode mode) {
        currentPortletMode_ = mode;
    }

    public WindowState getCurrentWindowState() {
        return currentWindowState_;
    }

    public void setCurrentWindowState(WindowState state) {
        currentWindowState_ = state;
    }

    public List<QName> getSupportedProcessingEvents() {
        return supportedProcessingEvents_;
    }

    public void setSupportedProcessingEvents(List<QName> supportedProcessingEvents) {
        supportedProcessingEvents_ = supportedProcessingEvents;
    }

    public Map<QName, String> getSupportedPublicRenderParameters() {
        return supportedPublicParams_;
    }

    public void setSupportedPublicRenderParameters(Map<QName, String> supportedPublicRenderParameters) {
        supportedPublicParams_ = supportedPublicRenderParameters;
    }

    /**
     * Get localized displayName metadata configured in portlet.xml.<br/>
     * If can't find localized displayName, return portlet name.<br/>
     * If portlet doesn't exists anymore, return empty string.<br/>
     * This value is cached in session, that means it only query to portlet container one time
     * @return display name
     */
    public String getDisplayName() {
        if (displayName == null) {
            org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();
            if (portlet != null) {
                PortletInfo info = portlet.getInfo();
                MetaInfo meta = info.getMeta();
                displayName = meta.getMetaValue(MetaInfo.DISPLAY_NAME);
                String value = null;
                if (displayName != null) {
                    RequestContext i = PortalRequestContext.getCurrentInstance();
                    Locale locale = i.getLocale();
                    value = displayName.getString(locale, true);
                }
                if (value == null || value.length() == 0) {
                    value = info.getName();
                }
                return value;
            } else {
                return "";
            }
        } else {
            RequestContext i = PortalRequestContext.getCurrentInstance();
            Locale locale = i.getLocale();
            String value = displayName.getString(locale, true);

            if (ParameterValidation.isNullOrEmpty(value)) {
                org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();
                PortletInfo info = portlet.getInfo();
                value = info.getName();
            }

            return value;
        }
    }

    public org.gatein.pc.api.Portlet getProducedOfferedPortlet() {
        return producedOfferedPortlet;
    }

    public List<String> getSupportModes() {
        if (supportModes_ != null) {
            return supportModes_;
        }

        List<String> supportModes = new ArrayList<String>();

        org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

        // if we couldn't get the portlet that just return an empty modes list
        if (portlet == null) {
            return supportModes;
        }

        Set<ModeInfo> modes = portlet.getInfo().getCapabilities().getModes(MediaType.create("text/html"));
        for (ModeInfo mode : modes) {
            supportModes.add(mode.getModeName());
        }

        if (supportModes.size() > 0) {
            supportModes.remove("view");
        }
        setSupportModes(supportModes);

        return supportModes;
    }

    public void setSupportModes(List<String> supportModes) {
        supportModes_ = supportModes;
    }

    /**
     * Tells, according to the info located in portlet.xml, wether this portlet can handle a portlet event with the QName given
     * as the method argument
     */
    public boolean supportsProcessingEvent(QName name) {

        if (supportedProcessingEvents_ == null) {

            org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

            if (portlet == null) {
                if (producerOfferedPortletContext != null) {
                    log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
                } else {
                    log.info("Could not find portlet. The producerOfferedPortletContext is null");
                }
                return false;
            }

            Map<QName, EventInfo> consumedEvents = (Map<QName, EventInfo>) portlet.getInfo().getEventing().getConsumedEvents();

            if (consumedEvents == null) {
                return false;
            }

            supportedProcessingEvents_ = new ArrayList<QName>(consumedEvents.keySet());
        }

        for (QName eventName : supportedProcessingEvents_) {
            if (eventName.equals(name)) {
                if (log.isDebugEnabled()) {
                    log.debug("The Portlet " + producerOfferedPortletContext + " supports comsuming the event : " + name);
                }
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("The portlet " + producerOfferedPortletContext + " doesn't support consuming the event : " + name);
        }
        return false;
    }

    public boolean supportsPublishingEvent(QName name) {
        if (supportedPublishingEvents_ == null) {
            org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

            if (portlet == null) {
                log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
                return false;
            }

            Map<QName, EventInfo> producedEvents = (Map<QName, EventInfo>) portlet.getInfo().getEventing().getProducedEvents();

            if (producedEvents == null) {
                return false;
            }

            supportedPublishingEvents_ = new ArrayList<QName>(producedEvents.keySet());
        }

        for (QName eventName : supportedPublishingEvents_) {
            if (eventName.equals(name)) {
                if (log.isDebugEnabled()) {
                    log.debug("The Portlet " + producerOfferedPortletContext + " supports producing the event : " + name);
                }
                return true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("The portlet " + producerOfferedPortletContext + " doesn't support producing the event : " + name);
        }
        return false;
    }

    /**
     * Tells, according to the info located in portlet.xml, wether this portlet supports the public render parameter qname given
     * as method argument. If the qname is supported, the public render parameter id is returned otherwise false is returned.
     *
     * @param supportedPublicParam the supported public parameter qname
     * @return the supported public parameter id
     */
    public String supportsPublicParam(QName supportedPublicParam) {
        if (supportedPublicParams_ == null) {

            //
            if (producedOfferedPortlet == null) {
                log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
                return null;
            }

            //
            Collection<ParameterInfo> parameters = (Collection<ParameterInfo>) producedOfferedPortlet.getInfo().getNavigation()
                    .getPublicParameters();
            supportedPublicParams_ = new HashMap<QName, String>();
            for (ParameterInfo parameter : parameters) {
                supportedPublicParams_.put(parameter.getName(), parameter.getId());
            }
        }

        //
        String id = supportedPublicParams_.get(supportedPublicParam);
        if (id != null) {
            if (log.isDebugEnabled()) {
                log.debug("The Portlet " + producerOfferedPortletContext.getId() + " supports the public render parameter : "
                        + supportedPublicParam);
            }
            return id;
        }

        //
        return null;
    }

    /**
     * Tells, according to the info located in portlet.xml, wether this portlet supports the public render parameter given as a
     * method argument
     */
    public boolean supportsPublicParam(String supportedPublicParam) {
        if (supportedPublicParams_ == null) {

            //
            if (producedOfferedPortlet == null) {
                log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
                return false;
            }

            //
            Collection<ParameterInfo> parameters = (Collection<ParameterInfo>) producedOfferedPortlet.getInfo().getNavigation()
                    .getPublicParameters();
            supportedPublicParams_ = new HashMap<QName, String>();
            for (ParameterInfo parameter : parameters) {
                supportedPublicParams_.put(parameter.getName(), parameter.getId());
            }
        }

        //
        for (String publicParam : supportedPublicParams_.values()) {
            if (publicParam.equals(supportedPublicParam)) {
                if (log.isDebugEnabled()) {
                    log.debug("The Portlet " + producerOfferedPortletContext.getId()
                            + " supports the public render parameter : " + supportedPublicParam);
                }
                return true;
            }
        }

        //
        return false;
    }

    /**
     * This methods return the public render parameters names supported by the targeted portlet; in other words, it sorts the
     * full public render params list and only return the ones that the current portlet can handle
     */
    public List<String> getPublicRenderParamNames() {
        UIPortal uiPortal = Util.getUIPortal();
        Map<String, String[]> publicParams = uiPortal.getPublicParameters();

        List<String> publicParamsSupportedByPortlet = new ArrayList<String>();
        if (publicParams != null) {
            Set<String> keys = publicParams.keySet();
            for (String key : keys) {
                if (supportsPublicParam(key)) {
                    publicParamsSupportedByPortlet.add(key);
                }
            }
            return publicParamsSupportedByPortlet;
        }
        return new ArrayList<String>();
    }

    /*
     * Adding Map<String, String[]> parameter to support propagation of publicParameters from URL
     */
    public Map<String, String[]> getPublicParameters(Map<String, String[]> portletParameters) {
        Map<String, String[]> publicParamsMap = new HashMap<String, String[]>();
        UIPortal uiPortal = Util.getUIPortal();
        Map<String, String[]> publicParams = uiPortal.getPublicParameters();
        Set<String> allPublicParamsNames = publicParams.keySet();
        List<String> supportedPublicParamNames = getPublicRenderParamNames();

        for (String oneOfAllParams : allPublicParamsNames) {
            if (supportedPublicParamNames.contains(oneOfAllParams)) {
                publicParamsMap.put(oneOfAllParams, publicParams.get(oneOfAllParams));
                // Propagates public parameter from URL
                if (portletParameters != null && portletParameters.containsKey(oneOfAllParams)) {
                    publicParamsMap.put(oneOfAllParams, portletParameters.get(oneOfAllParams));
                    /* setRenderParam() in processAction() propagates public render params across pages.
                     * UIPortal params are updated to allow same behaviour using URL propagation.
                     */
                    publicParams.put(oneOfAllParams, portletParameters.get(oneOfAllParams));
                }
            }
        }

        // Case when portlet has not public parameters in UIPortal but there are supported public parameters in URL
        if (supportedPublicParams_ != null &&
            portletParameters != null &&
            portletParameters.size() > 0 &&
            allPublicParamsNames.size() == 0) {
            for (QName qName : supportedPublicParams_.keySet()) {
                String prpId = supportsPublicParam(qName);
                if (prpId != null && portletParameters.containsKey(prpId)) {
                    publicParamsMap.put(prpId, portletParameters.get(prpId));
                    publicParams.put(prpId, portletParameters.get(prpId));
                }
            }
        }

        // Handle exposed portal contextual properties
        ContextualPropertyManager propertyManager = this.getApplicationComponent(ContextualPropertyManager.class);
        Map<QName, String[]> exposedPortalState = propertyManager.getProperties(this);
        for (QName prpQName : exposedPortalState.keySet()) {
            String prpId = supportsPublicParam(prpQName);
            if (prpId != null) {
                publicParamsMap.put(prpId, exposedPortalState.get(prpQName));
            }
        }

        //
        return publicParamsMap;
    }

    public Map<String, String[]> getPublicParameters() {
        return getPublicParameters(null);
    }

    // This is code for integration with PC

    /**
     * Create the correct portlet invocation that will target the portlet represented by this UI component.
     *
     * @param type the invocation type
     * @param prc the portal request context
     * @param <I> the invocation type
     * @return the portlet invocation
     * @throws Exception any exception
     */
    public <I extends PortletInvocation> I create(Class<I> type, PortalRequestContext prc) throws Exception {
        ExoPortletInvocationContext pic = new ExoPortletInvocationContext(prc, this);

        //
        I invocation;
        HttpServletRequest servletRequest = prc.getRequest();
        HashMap<String, String[]> allParams = new HashMap<String, String[]>();
        allParams.putAll(prc.getPortletParameters());
        allParams.putAll(this.getPublicParameters());
        allParams.remove(ExoPortletInvocationContext.NAVIGATIONAL_STATE_PARAM_NAME);
        if (type.equals(ActionInvocation.class)) {
            ActionInvocation actionInvocation = new ActionInvocation(pic);
            actionInvocation.setRequestContext(new AbstractRequestContext(servletRequest));

            String interactionState = servletRequest.getParameter(ExoPortletInvocationContext.INTERACTION_STATE_PARAM_NAME);
            if (interactionState != null) {
                actionInvocation.setInteractionState(StateString.create(interactionState));
                // remove the interaction state from remaining params
                allParams.remove(ExoPortletInvocationContext.INTERACTION_STATE_PARAM_NAME);
            }

            actionInvocation.setForm(allParams);

            invocation = type.cast(actionInvocation);
        } else if (type.equals(ResourceInvocation.class)) {
            ResourceInvocation resourceInvocation = new ResourceInvocation(pic);
            resourceInvocation.setRequestContext(new AbstractRequestContext(servletRequest));

            String resourceId = servletRequest.getParameter(Constants.RESOURCE_ID_PARAMETER);
            if (!ParameterValidation.isNullOrEmpty(resourceId)) {
                resourceInvocation.setResourceId(resourceId);
            } else if (!ParameterValidation.isNullOrEmpty(prc.getRequestParameter(Constants.RESOURCE_ID_PARAMETER))) {
                resourceInvocation.setResourceId(prc.getRequestParameter(Constants.RESOURCE_ID_PARAMETER));
            }

            String cachability = servletRequest.getParameter(Constants.CACHELEVEL_PARAMETER);
            if (!ParameterValidation.isNullOrEmpty(cachability)) {
                // we need to convert the given value to upper case as it might come from WSRP in lower case
                resourceInvocation.setCacheLevel(CacheLevel.create(cachability.toUpperCase(Locale.ENGLISH)));
            }

            String resourceState = servletRequest.getParameter(ExoPortletInvocationContext.RESOURCE_STATE_PARAM_NAME);
            if (!ParameterValidation.isNullOrEmpty(resourceState)) {
                resourceInvocation.setResourceState(StateString.create(resourceState));
            }
            // remove the resource state from remaining params
            allParams.remove(ExoPortletInvocationContext.RESOURCE_STATE_PARAM_NAME);

            // deal with WSRP-specific parameters: add them to the invocation attributes if they exist and remove them from form
            // params
            String url = servletRequest.getParameter(WSRP_URL);
            if (!ParameterValidation.isNullOrEmpty(url)) {
                resourceInvocation.setAttribute(WSRP_URL, url);
            }
            allParams.remove(WSRP_URL);

            String preferOperation = servletRequest.getParameter(WSRP_PREFER_OPERATION);
            if (!ParameterValidation.isNullOrEmpty(preferOperation)) {
                resourceInvocation.setAttribute(WSRP_PREFER_OPERATION, preferOperation);
            }
            allParams.remove(WSRP_PREFER_OPERATION);

            String requiresRewrite = servletRequest.getParameter(WSRP_REQUIRES_REWRITE);
            if (!ParameterValidation.isNullOrEmpty(requiresRewrite)) {
                resourceInvocation.setAttribute(WSRP_REQUIRES_REWRITE, requiresRewrite);
            }
            allParams.remove(WSRP_REQUIRES_REWRITE);
            // End WSRP-specific parameters handling

            resourceInvocation.setForm(allParams);

            invocation = type.cast(resourceInvocation);
        } else if (type.equals(EventInvocation.class)) {
            invocation = type.cast(new EventInvocation(pic));
        } else if (type.equals(RenderInvocation.class)) {
            invocation = type.cast(new RenderInvocation(pic));
        } else {
            throw new AssertionError();
        }

        //
        invocation.setRequest(servletRequest);
        invocation.setResponse(prc.getResponse());

        // Navigational state
        invocation.setNavigationalState(navigationalState);

        /* Public navigational state.
         * Passing portletParameters for public render parameters propagation via URL.
         */
        invocation.setPublicNavigationalState(this.getPublicParameters(prc.getPortletParameters()));

        // WSRP-specific public navigational state handling needed when we have a URL coming from template
        String navigationalValues = servletRequest.getParameter(WSRP_NAVIGATIONAL_VALUES);
        if (!ParameterValidation.isNullOrEmpty(navigationalValues)) {
            // add to the invocation attributes so that it can be retrieved and used by the WSRP component
            invocation.setAttribute(WSRP_NAVIGATIONAL_VALUES, navigationalValues);
        }
        allParams.remove(WSRP_NAVIGATIONAL_VALUES);
        // End WSRP-specific public navigational state handling

        // Mode
        invocation.setMode(Mode.create(getCurrentPortletMode().toString()));

        // Window state
        invocation.setWindowState(org.gatein.pc.api.WindowState.create(getCurrentWindowState().toString()));

        StatefulPortletContext<C> preferencesPortletContext = getPortletContext();
        if (preferencesPortletContext == null) {
            return null;
        }

        // get the user profile cached in the prc during the start of the request
        UserProfile userProfile = (UserProfile) prc.getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);

        // client context
        AbstractClientContext clientContext;
        Cookie[] cookies = servletRequest.getCookies();
        if (cookies != null) {
            clientContext = new AbstractClientContext(servletRequest, Arrays.asList(cookies));
        } else {
            clientContext = new AbstractClientContext(servletRequest);
        }
        invocation.setClientContext(clientContext);

        // instance context
        ExoPortletInstanceContext instanceContext;
        // TODO: we should not be having these wsrp specific conditions through the code like
        // this, it should either work the same was as normal portlets or abstracted out to another class.
        if (ApplicationType.WSRP_PORTLET.equals(state.getApplicationType())) {
            WSRP wsrp = (WSRP) preferencesPortletContext.getState();
            AccessMode accessMode = AccessMode.CLONE_BEFORE_WRITE;

            if (wsrp.getState() != null) {
                StatefulPortletContext statefulPortletContext = StatefulPortletContext.create(
                        preferencesPortletContext.getId(), PortletStateType.OPAQUE, wsrp.getState());

                invocation.setTarget(statefulPortletContext);
            } else {
                PortletContext portletContext = PortletContext.createPortletContext(preferencesPortletContext.getId());
                invocation.setTarget(portletContext);
            }

            // if the portlet is a cloned one already, we can modify it directly instead of requesting a clone
            if (wsrp.isCloned()) {
                accessMode = AccessMode.READ_WRITE;
            }
            instanceContext = new ExoPortletInstanceContext(preferencesPortletContext.getId(), accessMode);
        } else {
            instanceContext = new ExoPortletInstanceContext(preferencesPortletContext.getId());
            invocation.setTarget(preferencesPortletContext);
        }
        invocation.setInstanceContext(instanceContext);
        invocation.setServerContext(new ExoServerContext(servletRequest, prc.getResponse()));
        invocation.setUserContext(new ExoUserContext(servletRequest, userProfile));
        invocation.setWindowContext(new AbstractWindowContext(storageName));
        invocation.setPortalContext(PORTAL_CONTEXT);
        invocation.setSecurityContext(new AbstractSecurityContext(servletRequest));

        //
        return invocation;
    }

    public void update(PropertyChange... changes) throws Exception {
        PortletContext portletContext = getPortletContext();

        //
        PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);

        // Get marshalled version
        StatefulPortletContext<C> updatedCtx = (StatefulPortletContext<C>) portletInvoker
                .setProperties(portletContext, changes);

        //
        C updateState = updatedCtx.getState();

        // Now save it
        update(updateState);
    }

    public PortletState<S> getState() {
        return state;
    }

    public void setState(PortletState<S> state) {
        if (state != null) {
            try {
                PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);
                DataStorage dataStorage = getApplicationComponent(DataStorage.class);
                String applicationId = dataStorage.getId(state.getApplicationState());
                ModelAdapter<S, C> adapter = ModelAdapter.getAdapter(state.getApplicationType());
                PortletContext producerOfferedPortletContext = adapter.getProducerOfferedPortletContext(applicationId);
                org.gatein.pc.api.Portlet producedOfferedPortlet;

                try {
                    producedOfferedPortlet = portletInvoker.getPortlet(producerOfferedPortletContext);
                } catch (Exception e) {
                    // Whenever couldn't invoke the portlet object, set the request portlet to null for the error tobe
                    // properly handled and displayed when the portlet is rendered
                    producedOfferedPortlet = null;
                    log.error(e.getMessage(), e);
                }

                this.adapter = adapter;
                this.producerOfferedPortletContext = producerOfferedPortletContext;
                this.producedOfferedPortlet = producedOfferedPortlet;
                this.applicationId = applicationId;
            } catch (NoSuchDataException e) {
                log.error(e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } else {
            this.adapter = null;
            this.producedOfferedPortlet = null;
            this.producerOfferedPortletContext = null;
            this.applicationId = null;
        }
        this.state = state;
    }

    /**
     * Returns the state of the portlet as a set of preferences.
     *
     * @return the preferences of the portlet
     * @throws Exception any exception
     */
    public Portlet getPreferences() throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        return adapter.getState(container, state.getApplicationState());
    }

    /**
     * Returns the portlet context of the portlet.
     *
     * @return the portlet context
     * @throws Exception any exception
     */
    public StatefulPortletContext<C> getPortletContext() throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        return adapter.getPortletContext(container, applicationId, state.getApplicationState());
    }

    /**
     * Update the state of the portlet.
     *
     * @param updateState the state update
     * @throws Exception any exception
     */
    public void update(C updateState) throws Exception {
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        ExoContainer container = context.getApplication().getApplicationServiceContainer();
        state.setApplicationState(adapter.update(container, updateState, state.getApplicationState()));
        setState(state);
    }

    /**
     * Return modifed portlet stated (after portlet action invovation)
     * @param modifiedContext
     * @throws Exception
     */
    public C getModifiedState(PortletContext modifiedContext) throws Exception {
        return adapter.getStateFromModifiedContext(this.getPortletContext(), modifiedContext);
    }

    /**
     * Return cloned portlet state (updated after portlet action invocation).
     * This method is used in case WSRP
     * @param clonedContext
     * @throws Exception
     */
    public C getClonedState(PortletContext clonedContext) throws Exception {
        return adapter.getstateFromClonedContext(this.getPortletContext(), clonedContext);
    }

    /** This is used by the dashboard portlet and should not be used else where. It will be removed some day. */
    private static final ThreadLocal<UIPortlet> currentPortlet = new ThreadLocal<UIPortlet>();

    public static UIPortlet getCurrentUIPortlet() {
        return currentPortlet.get();
    }

    /**
     * Performs an invocation on this portlet.
     *
     * @param invocation the portlet invocation
     * @return the portlet invocation response
     * @throws PortletInvokerException any invoker exception
     */
    public PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException {
        PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);
        currentPortlet.set(this);
        try {
            return portletInvoker.invoke(invocation);
        } finally {
            currentPortlet.set(null);
        }
    }

    /**
     * navigationalState - internal portlet container parameter (go with portlet url).
     * called when navigation state updated
     * @param navigationalState
     */
    void setNavigationalState(StateString navigationalState) {
        this.navigationalState = navigationalState;
    }

    /**
     * configuredTitle - the localized title configured in portlet.xml.
     * This value returned ans set after portlet container invocation.
     * @param _configuredTitle - portlet title responsed from portlet container
     */
    protected void setConfiguredTitle(String _configuredTitle) {
        this.configuredTitle = _configuredTitle;
    }

    /**
     * Returns the title showed on the InfoBar. The title is computed in following manner.
     * <p/>
     * 1. First, the method getTitle(), inherited from UIPortalComponent is called. The getTitle() returns what users set in the
     * PortletSetting tab, the current method returns call result if it is not null.
     * <p/>
     * 2. configuredTitle, which is the localized value of javax.portlet.title is returned if it is not null.
     * <p/>
     * 3. If the method does not terminate at neither (1) nor (2), the configured display name is returned.
     *
     * @return
     */
    public String getDisplayTitle() {
        String displayedTitle = getTitle();
        if (displayedTitle != null && displayedTitle.trim().length() > 0) {
            return displayedTitle;
        }

        if (configuredTitle != null) {
            return configuredTitle;
        }

        return getDisplayName();
    }

    /**
     * Parsing response from portlet container. The response contains:<br/>
     * html markup, portlet title, response properties:<br/>
     * - JS resource dependency (defined in gatein-resources.xml)<br/>
     * - html header<br/>
     * - cookie<br/>
     * - extra markup header<br/>
     * If errors occur during portlet lifecycle processing. PortletExceptionHandleService is called.
     * Add plugins to this service to customize portlet error handler
     * @param pir - response object from portlet container
     * @param context - request context
     * @return markup to render on browser
     * @see PortletExceptionHandleService
     */
    public Text generateRenderMarkup(PortletInvocationResponse pir, WebuiRequestContext context) {
        PortalRequestContext prcontext = (PortalRequestContext) context;

        Text markup = null;
        if (pir instanceof FragmentResponse) {
            JavascriptManager jsMan = context.getJavascriptManager();
            jsMan.loadScriptResource(ResourceScope.PORTLET, getApplicationId());

            FragmentResponse fragmentResponse = (FragmentResponse) pir;
            switch (fragmentResponse.getType()) {
                case FragmentResponse.TYPE_CHARS:
                    markup = Text.create(fragmentResponse.getContent());
                    break;
                case FragmentResponse.TYPE_BYTES:
                    markup = Text.create(fragmentResponse.getBytes(), Charset.forName("UTF-8"));
                    break;
                case FragmentResponse.TYPE_EMPTY:
                    markup = Text.create("");
                    break;
            }
            setConfiguredTitle(fragmentResponse.getTitle());

            // setup portlet properties
            if (fragmentResponse.getProperties() != null) {
                // setup transport headers
                if (fragmentResponse.getProperties().getTransportHeaders() != null) {
                    MultiValuedPropertyMap<String> transportHeaders = fragmentResponse.getProperties().getTransportHeaders();
                    for (String key : transportHeaders.keySet()) {
                        if (JAVASCRIPT_DEPENDENCY.equals(key)) {
                            for (String value : transportHeaders.getValues(key)) {
                                jsMan.require(value);
                            }
                        } else {
                            for (String value : transportHeaders.getValues(key)) {
                                prcontext.getResponse().setHeader(key, value);
                            }
                        }
                    }
                }

                // setup up portlet cookies
                if (fragmentResponse.getProperties().getCookies() != null) {
                    List<Cookie> cookies = fragmentResponse.getProperties().getCookies();
                    for (Cookie cookie : cookies) {
                        prcontext.getResponse().addCookie(cookie);
                    }
                }

                // setup markup headers
                if (fragmentResponse.getProperties().getMarkupHeaders() != null) {
                    MultiValuedPropertyMap<Element> markupHeaders = fragmentResponse.getProperties().getMarkupHeaders();

                    List<Element> markupElements = markupHeaders.getValues(MimeResponse.MARKUP_HEAD_ELEMENT);
                    if (markupElements != null) {
                        for (Element element : markupElements) {
                            if (!context.useAjax() && "title".equals(element.getNodeName().toLowerCase())
                                    && element.getFirstChild() != null) {
                                String title = element.getFirstChild().getNodeValue();
                                prcontext.getRequest().setAttribute(PortalRequestContext.REQUEST_TITLE, title);
                            } else {
                                prcontext.addExtraMarkupHeader(element, getId());
                            }
                        }
                    }
                }
            }

        } else {

            PortletContainerException pcException;

            if (pir instanceof ErrorResponse) {
                ErrorResponse errorResponse = (ErrorResponse) pir;
                pcException = new PortletContainerException(errorResponse.getMessage(), errorResponse.getCause());
            } else {
                pcException = new PortletContainerException("Unknown invocation response type [" + pir.getClass()
                        + "]. Expected a FragmentResponse or an ErrorResponse");
            }

            //
            PortletExceptionHandleService portletExceptionService = getApplicationComponent(PortletExceptionHandleService.class);
            if (portletExceptionService != null) {
                portletExceptionService.handle(pcException);
            }

            // Log the error
            log.error("Portlet render threw an exception", pcException);

            markup = Text.create(context.getApplicationResourceBundle().getString("UIPortlet.message.RuntimeError"));
        }

        return markup;
    }
}
