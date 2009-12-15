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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.UserProfileLifecycle;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
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
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event.Phase;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
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
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.portlet.impl.spi.AbstractClientContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortalContext;
import org.gatein.pc.portlet.impl.spi.AbstractRequestContext;
import org.gatein.pc.portlet.impl.spi.AbstractSecurityContext;
import org.gatein.pc.portlet.impl.spi.AbstractServerContext;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.Serializable;
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

/** May 19, 2006 */
@ComponentConfig(lifecycle = UIPortletLifecycle.class, template = "system:/groovy/portal/webui/application/UIPortlet.gtmpl", events = {
   @EventConfig(listeners = RenderActionListener.class),
   @EventConfig(listeners = ChangePortletModeActionListener.class),
   @EventConfig(listeners = ChangeWindowStateActionListener.class),
   @EventConfig(listeners = DeleteComponentActionListener.class, confirm = "UIPortlet.deletePortlet"),
   @EventConfig(listeners = EditPortletActionListener.class),
   @EventConfig(phase = Phase.PROCESS, listeners = ProcessActionActionListener.class),
   @EventConfig(phase = Phase.PROCESS, listeners = ServeResourceActionListener.class),
   @EventConfig(phase = Phase.PROCESS, listeners = ProcessEventsActionListener.class)})
public class UIPortlet<S, C extends Serializable> extends UIApplication
{

   protected static final Log log = ExoLogger.getLogger("portal:UIPortlet");

   static final public String DEFAULT_THEME = "Default:DefaultTheme::Vista:VistaTheme::Mac:MacTheme";

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

   private List<String> supportedPublicParams_;

   private boolean portletInPortal_ = true;

   private StateString navigationalState;

   public UIPortlet()
   {
      // That value will be overriden when it is mapped onto a data storage
      storageName = UUID.randomUUID().toString();
   }

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public String getStorageName()
   {
      return storageName;
   }

   public void setStorageName(String storageName)
   {
      this.storageName = storageName;
   }

   public String getWindowId()
   {
      return storageName;
   }

   /**
    * Retrieves the skin identifier associated with this portlet or <code>null</code> if there isn't one (for example,
    * it doesn't make any sense in the WSRP scenario).
    *
    * @return the skin identifier associated with this portlet or <code>null</code> if there isn't one
    */
   public String getSkinId()
   {
      ApplicationType<S> type = state.getApplicationType();
      if (type == ApplicationType.PORTLET)
      {
         return applicationId;
      }
      else if (type == ApplicationType.GADGET)
      {
         return "dashboard/GadgetPortlet";
      }
      else
      {
         return null;
      }
   }

   public String getId()
   {
      return storageName;
   }

   public String getApplicationId()
   {
      return applicationId;
   }

   public String getPortletStyle()
   {
      return portletStyle;
   }

   public void setPortletStyle(String s)
   {
      portletStyle = s;
   }

   public boolean getShowPortletMode()
   {
      return showPortletMode;
   }

   public void setShowPortletMode(Boolean b)
   {
      showPortletMode = b;
   }

   public void setPortletInPortal(boolean b)
   {
      portletInPortal_ = b;
   }

   public boolean isPortletInPortal()
   {
      return portletInPortal_;
   }

   public String getTheme()
   {
      if (theme_ == null || theme_.trim().length() < 1)
      {
         return DEFAULT_THEME;
      }
      return theme_;
   }

   public void setTheme(String theme)
   {
      theme_ = theme;
   }

   public String getSuitedTheme(String skin)
   {
      if (skin == null)
      {
         skin = getAncestorOfType(UIPortalApplication.class).getSkin();
      }
      Map<String, String> themeMap = stringToThemeMap(getTheme());
      if (themeMap.containsKey(skin))
      {
         return themeMap.get(skin);
      }
      return DEFAULT_THEME.split(":")[1];
   }

   public void putSuitedTheme(String skin, String theme)
   {
      if (skin == null)
      {
         skin = getAncestorOfType(UIPortalApplication.class).getSkin();
      }
      Map<String, String> themeMap = stringToThemeMap(getTheme());
      themeMap.put(skin, theme);
      setTheme(themeMapToString(themeMap));
   }

   private String themeMapToString(Map<String, String> themeMap)
   {
      StringBuffer builder = new StringBuffer();
      Iterator<Entry<String, String>> itr = themeMap.entrySet().iterator();
      while (itr.hasNext())
      {
         Entry<String, String> entry = itr.next();
         builder.append(entry.getKey()).append(":").append(entry.getValue());
         if (itr.hasNext())
         {
            builder.append("::");
         }
      }
      return builder.toString();
   }

   private Map<String, String> stringToThemeMap(String themesString)
   {
      Map<String, String> themeMap = new HashMap<String, String>();
      String[] themeIds = themesString.split("::");
      for (String ele : themeIds)
      {
         String[] strs = ele.split(":");
         themeMap.put(strs[0], strs[1]);
      }
      return themeMap;
   }

   public PortletMode getCurrentPortletMode()
   {
      return currentPortletMode_;
   }

   public void setCurrentPortletMode(PortletMode mode)
   {
      currentPortletMode_ = mode;
   }

   public WindowState getCurrentWindowState()
   {
      return currentWindowState_;
   }

   public void setCurrentWindowState(WindowState state)
   {
      currentWindowState_ = state;
   }

   public List<QName> getSupportedProcessingEvents()
   {
      return supportedProcessingEvents_;
   }

   public void setSupportedProcessingEvents(List<QName> supportedProcessingEvents)
   {
      supportedProcessingEvents_ = supportedProcessingEvents;
   }

   public List<String> getSupportedPublicRenderParameters()
   {
      return supportedPublicParams_;
   }

   public void setSupportedPublicRenderParameters(List<String> supportedPublicRenderParameters)
   {
      supportedPublicParams_ = supportedPublicRenderParameters;
   }

   public String getDisplayTitle()
   {
      String title = getTitle();
      if (title == null)
      {
         title = getDisplayName();
      }
      return title;
   }

   public String getDisplayName()
   {
      if (displayName == null)
      {
         org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();
         if (portlet != null)
         {
            PortletInfo info = portlet.getInfo();
            MetaInfo meta = info.getMeta();
            displayName = meta.getMetaValue(MetaInfo.DISPLAY_NAME);
            String value = null;
            if (displayName != null)
            {
               RequestContext i = PortalRequestContext.getCurrentInstance();
               Locale locale = i.getLocale();
               value = displayName.getString(locale, true);
            }
            if (value == null || value.length() == 0)
            {
               value = info.getName();
            }
            return value;
         }
         else
         {
            return "";
         }
      }
      else
      {
         String value = null;
         if (displayName != null)
         {
            RequestContext i = PortalRequestContext.getCurrentInstance();
            Locale locale = i.getLocale();
            value = displayName.getString(locale, true);
         }
         if (value == null || value.length() == 0)
         {
            org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();
            PortletInfo info = portlet.getInfo();
            value = info.getName();
         }
         return value;
      }
   }

   public org.gatein.pc.api.Portlet getProducedOfferedPortlet()
   {
      return producedOfferedPortlet;
   }

   public List<String> getSupportModes()
   {
      if (supportModes_ != null)
      {
         return supportModes_;
      }

      List<String> supportModes = new ArrayList<String>();

      org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

      // if we couldn't get the portlet that just return an empty modes list
      if (portlet == null)
      {
         return supportModes;
      }

      Set<ModeInfo> modes = portlet.getInfo().getCapabilities().getModes(MediaType.create("text/html"));
      Iterator<ModeInfo> modeIter = modes.iterator();
      while (modeIter.hasNext())
      {
         ModeInfo info = modeIter.next();
         supportModes.add(info.getModeName());
      }

      if (supportModes.size() > 0)
      {
         supportModes.remove("view");
      }
      setSupportModes(supportModes);

      return supportModes;
   }

   public void setSupportModes(List<String> supportModes)
   {
      supportModes_ = supportModes;
   }

   /**
    * Tells, according to the info located in portlet.xml, wether this portlet can handle a portlet event with the QName
    * given as the method argument
    */
   public boolean supportsProcessingEvent(QName name)
   {

      if (supportedProcessingEvents_ == null)
      {

         org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

         if (portlet == null)
         {
            log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
            return false;
         }

         Map<QName, EventInfo> consumedEvents =
            (Map<QName, EventInfo>)portlet.getInfo().getEventing().getConsumedEvents();

         if (consumedEvents == null)
         {
            return false;
         }

         supportedProcessingEvents_ = new ArrayList<QName>(consumedEvents.keySet());
      }

      for (Iterator<QName> iter = supportedProcessingEvents_.iterator(); iter.hasNext();)
      {
         QName eventName = iter.next();
         if (eventName.equals(name))
         {
            log.info("The Portlet " + producerOfferedPortletContext + " supports comsuming the event : " + name);
            return true;
         }
      }
      log.info("The portlet " + producerOfferedPortletContext + " doesn't support consuming the event : " + name);
      return false;
   }

   public boolean supportsPublishingEvent(QName name)
   {
      if (supportedPublishingEvents_ == null)
      {
         org.gatein.pc.api.Portlet portlet = getProducedOfferedPortlet();

         if (portlet == null)
         {
            log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
            return false;
         }

         Map<QName, EventInfo> producedEvents =
            (Map<QName, EventInfo>)portlet.getInfo().getEventing().getProducedEvents();

         if (producedEvents == null)
         {
            return false;
         }

         supportedPublishingEvents_ = new ArrayList<QName>(producedEvents.keySet());
      }

      for (Iterator<QName> iter = supportedPublishingEvents_.iterator(); iter.hasNext();)
      {
         QName eventName = iter.next();
         if (eventName.equals(name))
         {
            log.info("The Portlet " + producerOfferedPortletContext + " supports producing the event : " + name);
            return true;
         }
      }
      log.info("The portlet " + producerOfferedPortletContext + " doesn't support producing the event : " + name);
      return false;
   }

   /**
    * Tells, according to the info located in portlet.xml, wether this portlet supports the public render parameter
    * given as a method argument
    */
   public boolean supportsPublicParam(String supportedPublicParam)
   {
      if (supportedPublicParams_ == null)
      {

         //
         if (producedOfferedPortlet == null)
         {
            log.info("Could not find portlet with ID : " + producerOfferedPortletContext.getId());
            return false;
         }

         //
         Collection<ParameterInfo> parameters =
            (Collection<ParameterInfo>)producedOfferedPortlet.getInfo().getNavigation().getPublicParameters();
         supportedPublicParams_ = new ArrayList<String>();
         for (ParameterInfo parameter : parameters)
         {
            supportedPublicParams_.add(parameter.getId());
         }
      }

      //
      for (String publicParam : supportedPublicParams_)
      {
         if (publicParam.equals(supportedPublicParam))
         {
            if (log.isDebugEnabled())
            {
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
    * This methods return the public render parameters names supported by the targeted portlet; in other words, it sorts
    * the full public render params list and only return the ones that the current portlet can handle
    */
   public List<String> getPublicRenderParamNames()
   {
      UIPortal uiPortal = Util.getUIPortal();
      Map<String, String[]> publicParams = uiPortal.getPublicParameters();

      List<String> publicParamsSupportedByPortlet = new ArrayList<String>();
      if (publicParams != null)
      {
         Set<String> keys = publicParams.keySet();
         for (String key : keys)
         {
            if (supportsPublicParam(key))
            {
               publicParamsSupportedByPortlet.add(key);
            }
         }
         return publicParamsSupportedByPortlet;
      }
      return new ArrayList<String>();
   }

   public Map<String, String[]> getPublicParameters()
   {
      Map<String, String[]> publicParamsMap = new HashMap<String, String[]>();
      UIPortal uiPortal = Util.getUIPortal();
      Map<String, String[]> publicParams = uiPortal.getPublicParameters();
      Set<String> allPublicParamsNames = publicParams.keySet();
      List<String> supportedPublicParamNames = getPublicRenderParamNames();
      for (String oneOfAllParams : allPublicParamsNames)
      {
         if (supportedPublicParamNames.contains(oneOfAllParams))
         {
            publicParamsMap.put(oneOfAllParams, publicParams.get(oneOfAllParams));
         }
      }
      return publicParamsMap;
   }

   // This is code for integration with PC

   /**
    * Create the correct portlet invocation that will target the portlet represented by this UI component.
    *
    * @param type the invocation type
    * @param prc  the portal request context
    * @param <I>  the invocation type
    * @return the portlet invocation
    * @throws Exception any exception
    */
   public <I extends PortletInvocation> I create(Class<I> type, PortalRequestContext prc) throws Exception
   {
      ExoPortletInvocationContext pic = new ExoPortletInvocationContext(prc, this);

      //
      I invocation;
      HttpServletRequest servletRequest = prc.getRequest();
      HashMap<String, String[]> allParams = new HashMap<String, String[]>();
      allParams.putAll(servletRequest.getParameterMap());
      allParams.putAll(this.getPublicParameters());
      if (type.equals(ActionInvocation.class))
      {
         ActionInvocation actionInvocation = new ActionInvocation(pic);
         actionInvocation.setForm(allParams);
         actionInvocation.setRequestContext(new AbstractRequestContext(servletRequest));

         String interactionState =
            servletRequest.getParameter(ExoPortletInvocationContext.INTERACTION_STATE_PARAM_NAME);
         if (interactionState != null)
         {
            actionInvocation.setInteractionState(StateString.create(interactionState));
         }

         invocation = type.cast(actionInvocation);
      }
      else if (type.equals(ResourceInvocation.class))
      {
         ResourceInvocation resourceInvocation = new ResourceInvocation(pic);
         resourceInvocation.setRequestContext(new AbstractRequestContext(servletRequest));

         String resourceId = servletRequest.getParameter(Constants.RESOURCE_ID_PARAMETER);
         if (resourceId != null)
         {
            resourceInvocation.setResourceId(resourceId);
         }

         String cachability = servletRequest.getParameter(Constants.CACHELEVEL_PARAMETER);
         if (cachability != null)
         {
            resourceInvocation.setCacheLevel(CacheLevel.valueOf(cachability));
         }

         String resourceState = servletRequest.getParameter(ExoPortletInvocationContext.RESOURCE_STATE_PARAM_NAME);
         if (resourceState != null)
         {
            resourceInvocation.setResourceState(StateString.create(resourceState));
         }

         resourceInvocation.setForm(allParams);

         invocation = type.cast(resourceInvocation);
      }
      else if (type.equals(EventInvocation.class))
      {
         invocation = type.cast(new EventInvocation(pic));
      }
      else if (type.equals(RenderInvocation.class))
      {
         invocation = type.cast(new RenderInvocation(pic));
      }
      else
      {
         throw new AssertionError();
      }

      // Navigational state
      invocation.setNavigationalState(navigationalState);

      // Public navigational state      
      invocation.setPublicNavigationalState(this.getPublicParameters());

      // Mode
      invocation.setMode(Mode.create(getCurrentPortletMode().toString()));

      // Window state
      invocation.setWindowState(org.gatein.pc.api.WindowState.create(getCurrentWindowState().toString()));

      StatefulPortletContext<C> preferencesPortletContext = getPortletContext();

      // get the user profile cached in the prc during the start of the request
      UserProfile userProfile = (UserProfile)prc.getAttribute(UserProfileLifecycle.USER_PROFILE_ATTRIBUTE_NAME);

      // client context
      AbstractClientContext clientContext;
      Cookie[] cookies = servletRequest.getCookies();
      if (cookies != null)
      {
         clientContext = new AbstractClientContext(servletRequest, Arrays.asList(cookies));
      }
      else
      {
         clientContext = new AbstractClientContext(servletRequest);
      }
      invocation.setClientContext(clientContext);

      // instance context
      ExoPortletInstanceContext instanceContext;
      if (ApplicationType.WSRP_PORTLET.equals(state.getApplicationType()))
      {
         WSRP wsrp = (WSRP)preferencesPortletContext.getState();
         AccessMode accessMode = AccessMode.CLONE_BEFORE_WRITE;
         if (wsrp.isCloned())
         {
            accessMode = AccessMode.READ_WRITE;
         }
         instanceContext = new ExoPortletInstanceContext(preferencesPortletContext.getId(), accessMode);
      }
      else
      {
         instanceContext = new ExoPortletInstanceContext(preferencesPortletContext.getId());
      }
      invocation.setInstanceContext(instanceContext);

      invocation.setServerContext(new AbstractServerContext(servletRequest, prc.getResponse()));
      //TODO: ExoUserContext impl not tested
      invocation.setUserContext(new ExoUserContext(servletRequest, userProfile));
      invocation.setWindowContext(new ExoWindowContext(storageName));
      invocation.setPortalContext(new AbstractPortalContext(Collections.singletonMap(
         "javax.portlet.markup.head.element.support", "false")));
      invocation.setSecurityContext(new AbstractSecurityContext(servletRequest));

      //
      invocation.setTarget(preferencesPortletContext);

      //
      return invocation;
   }

   public void update(PropertyChange... changes) throws Exception
   {
      PortletContext portletContext = getPortletContext();

      //
      PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);

      // Get marshalled version
      StatefulPortletContext<C> updatedCtx =
         (StatefulPortletContext<C>)portletInvoker.setProperties(portletContext, changes);

      //
      C updateState = updatedCtx.getState();

      // Now save it
      update(updateState);
   }

   public PortletState<S> getState()
   {
      return state;
   }

   public void setState(PortletState<S> state)
   {
      if (state != null)
      {
         try
         {
            PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);
            DataStorage dataStorage = getApplicationComponent(DataStorage.class);
            String applicationId = dataStorage.getId(state.getApplicationState());
            ModelAdapter<S, C> adapter = ModelAdapter.getAdapter(state.getApplicationType());
            PortletContext producerOfferedPortletContext = adapter.getProducerOfferedPortletContext(applicationId);
            org.gatein.pc.api.Portlet producedOfferedPortlet = portletInvoker.getPortlet(producerOfferedPortletContext);

            this.adapter = adapter;
            this.producerOfferedPortletContext = producerOfferedPortletContext;
            this.producedOfferedPortlet = producedOfferedPortlet;
            this.applicationId = applicationId;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      else
      {
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
   public Portlet getPreferences() throws Exception
   {
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
   public StatefulPortletContext<C> getPortletContext() throws Exception
   {
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
   public void update(C updateState) throws Exception
   {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      ExoContainer container = context.getApplication().getApplicationServiceContainer();
      state.setApplicationState(adapter.update(container, updateState, state.getApplicationState()));
      setState(state);
   }

   /** This is used by the dashboard portlet and should not be used else where. It will be removed some day. */
   private static final ThreadLocal<UIPortlet> currentPortlet = new ThreadLocal<UIPortlet>();

   public static UIPortlet getCurrentUIPortlet()
   {
      return currentPortlet.get();
   }

   /**
    * Performs an invocation on this portlet.
    *
    * @param invocation the portlet invocation
    * @return the portlet invocation response
    * @throws PortletInvokerException any invoker exception
    */
   public PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException
   {
      PortletInvoker portletInvoker = getApplicationComponent(PortletInvoker.class);
      currentPortlet.set(this);
      try
      {
         return portletInvoker.invoke(invocation);
      }
      finally
      {
         currentPortlet.set(null);
      }
   }

   void setNavigationalState(StateString navigationalState)
   {
      this.navigationalState = navigationalState;
   }
}