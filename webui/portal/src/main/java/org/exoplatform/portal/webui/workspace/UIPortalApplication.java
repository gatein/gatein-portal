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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.resource.Skin;
import org.exoplatform.portal.resource.SkinConfig;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.resource.SkinURL;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * This extends the UIApplication and hence is a sibling of UIPortletApplication
 * (used by any eXo Portlets as the Parent class to build the portlet component
 * tree). The UIPortalApplication is responsible to build its subtree according
 * to some configuration parameters. If all components are displayed it is
 * composed of 2 UI components: -UIWorkingWorkSpace: the right part that can
 * display the normal or webos portal layouts - UIPopupWindow: a popup window
 * that display or not
 */
@ComponentConfig(lifecycle = UIPortalApplicationLifecycle.class, template = "system:/groovy/portal/webui/workspace/UIPortalApplication.gtmpl")
public class UIPortalApplication extends UIApplication
{
   public static final int NORMAL_MODE = 0;

   public static final int APP_BLOCK_EDIT_MODE = 1;

   public static final int APP_VIEW_EDIT_MODE = 2;

   public static final int CONTAINER_BLOCK_EDIT_MODE = 3;

   public static final int CONTAINER_VIEW_EDIT_MODE = 4;

   private int modeState = NORMAL_MODE;

   private String nodePath_;

   private Orientation orientation_ = Orientation.LT;

   final static public String UI_WORKING_WS_ID = "UIWorkingWorkspace";

   final static public String UI_VIEWING_WS_ID = "UIViewWS";

   final static public String UI_EDITTING_WS_ID = "UIEditInlineWS";

   final static public String UI_MASK_WS_ID = "UIMaskWorkspace";

   private String skin_ = "Default";

   private UserPortalConfig userPortalConfig_;

   private boolean isSessionOpen = false;
   
   private Map<UIPortalKey, UIPortal> all_UIPortals;
   
   private List<PageNavigation> all_Navigations;
   
   private UIPortal showedUIPortal;
   
   /**
    * The constructor of this class is used to build the tree of UI components
    * that will be aggregated in the portal page.<br/>
    * 1) The component is stored in the current PortalRequestContext ThreadLocal<br/>
    * 2) The configuration for the portal associated with the current user request 
    *    is extracted from the PortalRequestContext<br/>
    * 3) Then according to the context path, either a public or private portal is initiated.
    *    Usually a public portal does not contain the left column and only the private one has it.<br/>
    * 4) The skin to use is setup <br/>
    * 5) Finally, the current component is associated with the
    *    current portal owner
    * 
    * @throws Exception
    */
   public UIPortalApplication() throws Exception
   {
      log = ExoLogger.getLogger("portal:UIPortalApplication");
      PortalRequestContext context = PortalRequestContext.getCurrentInstance();
      
      userPortalConfig_ = (UserPortalConfig)context.getAttribute(UserPortalConfig.class);
      if (userPortalConfig_ == null)
         throw new Exception("Can't load user portal config");
      
      // Get portal skin
      this.reloadSkinPortal(context);
      
      // dang.tung - set portal language by user preference -> browser ->
      // default
      // ------------------------------------------------------------------------------
      LocaleConfigService localeConfigService = getApplicationComponent(LocaleConfigService.class);
      Locale locale = context.getLocale();
      if (locale == null)
      {
         if (log.isWarnEnabled())
            log.warn("No locale set on PortalRequestContext! Falling back to 'en'.");
         locale = Locale.ENGLISH;
      }

      String localeName = LocaleContextInfo.getLocaleAsString(locale);
      LocaleConfig localeConfig = localeConfigService.getLocaleConfig(localeName);
      if (localeConfig == null)
      {
         if (log.isWarnEnabled())
            log.warn("Unsupported locale set on PortalRequestContext: " + localeName + "! Falling back to 'en'.");
         localeConfig = localeConfigService.getLocaleConfig(Locale.ENGLISH.getLanguage());
      }
      setOrientation(localeConfig.getOrientation());

      // -------------------------------------------------------------------------------
      context.setUIApplication(this);

      this.all_UIPortals = new HashMap<UIPortalKey, UIPortal>(5);
      
      addWorkingWorkspace();
      
      setOwner(context.getPortalOwner());
      
      //Minh Hoang TO: Localizes navigations, need to put this code snippet below 'setLocale' block
      this.all_Navigations = userPortalConfig_.getNavigations();
      localizeNavigations();
   }

   /**
    * Sets the specified portal to be showed in the normal mode currently
    * 
    * @param uiPortal
    */
   public void setShowedUIPortal(UIPortal uiPortal)
   {
      this.showedUIPortal = uiPortal;
      
      UISiteBody siteBody = this.findFirstComponentOfType(UISiteBody.class);
      if(siteBody != null)
      {
         //TODO: Check this part carefully
         siteBody.setUIComponent(uiPortal);
      }
   }
   
   /**
    * Returns current UIPortal which being showed in normal mode
    * 
    * @return
    */
   public UIPortal getShowedUIPortal()
   {
      return showedUIPortal;
   }
   
   /**
    * Returns a cached UIPortal matching to OwnerType and OwnerId if any
    * 
    * @param ownerType
    * @param ownerId
    * @return
    */
   public UIPortal getCachedUIPortal(String ownerType, String ownerId)
   {
      if(ownerType == null || ownerId == null)
      {
         return null;
      }
      return this.all_UIPortals.get(new UIPortalKey(ownerType, ownerId));
   }
   
   /**
    * Associates the specified UIPortal to a cache map with specified key which bases on OwnerType and OwnerId
    * 
    * @param uiPortal
    */
   public void putCachedUIPortal(UIPortal uiPortal)
   {
      String ownerType = uiPortal.getOwnerType();
      String ownerId = uiPortal.getOwner();
      
      if(ownerType != null && ownerId != null)
      {
         this.all_UIPortals.put(new UIPortalKey(ownerType, ownerId), uiPortal);
      }
   }
   
   /**
    * Remove the UIPortal from the cache map
    * 
    * @param ownerType
    * @param ownerId
    */
   public void removeCachedUIPortal(String ownerType, String ownerId)
   {
      if(ownerType == null || ownerId == null)
      {
         return;
      }
      this.all_UIPortals.remove(new UIPortalKey(ownerType, ownerId));
   }
   
   public boolean isSessionOpen()
   {
      return isSessionOpen;
   }

   public void setSessionOpen(boolean isSessionOpen)
   {
      this.isSessionOpen = isSessionOpen;
   }

   public Orientation getOrientation()
   {
      return orientation_;
   }

   public void setOrientation(Orientation orientation)
   {
      this.orientation_ = orientation;
   }

   public Locale getLocale()
   {
      return Util.getPortalRequestContext().getLocale();
   }

   public void setModeState(int mode)
   {
      this.modeState = mode;
   }

   public int getModeState()
   {
      return modeState;
   }

   /**
    * @deprecated use the Mode State instead
    * 
    * @return True if the Portal is not in the normal mode
    */
   public boolean isEditing()
   {
      return (modeState != NORMAL_MODE);
   }

   public Collection<String> getJavascriptURLs()
   {
      JavascriptConfigService service = getApplicationComponent(JavascriptConfigService.class);
      return service.getAvailableScriptsPaths();
   }

   public Collection<Skin> getPortalSkins()
   {
      SkinService skinService = getApplicationComponent(SkinService.class);

      //
      Collection<Skin> skins = new ArrayList<Skin>(skinService.getPortalSkins(skin_));

      //
      SkinConfig skinConfig = skinService.getSkin(Util.getUIPortal().getName(), skin_);
      if (skinConfig != null)
      {
         skins.add(skinConfig);
      }

      //
      Set<SkinConfig> portletConfigs = getPortalPortletSkins();
      // don't merge portlet if portlet not available
      if (!portletConfigs.isEmpty())
      {
         skins.add(skinService.merge(portletConfigs));
      }
      //
      return skins;
   }

   private Set<SkinConfig> getPortalPortletSkins()
   {
      Set<SkinConfig> portletConfigs = new HashSet<SkinConfig>();
      for (UIComponent child : findFirstComponentOfType(UIPortal.class).getChildren())
      {
         if (child instanceof UIPortlet)
         {
            SkinConfig portletConfig = getPortletSkinConfig((UIPortlet)child);
            if (portletConfig != null)
            {
               portletConfigs.add(portletConfig);
            }
         }
      }
      return portletConfigs;
   }

   public String getSkin()
   {
      return skin_;
   }

   public void setSkin(String skin)
   {
      this.skin_ = skin;
   }

   private SkinConfig getSkin(String module, String skin)
   {
      SkinService skinService = getApplicationComponent(SkinService.class);
      return skinService.getSkin(module, skin);
   }

   /**
    * Returns a list of portlets skin that have to be added in the HTML head
    * tag. The skin can directly point to a real css file (this is the case of
    * all the porlet included in a page) or point to a servlet that agregates
    * different portlet CSS files into one to lower the number of HTTP calls
    * (this is the case in production as all the portlets included in a portal,
    * and hence there on everypage are merged into a single CSS file)
    * 
    * @return the portlet skins
    */
   public Set<Skin> getPortletSkins()
   {
      // Set to avoid repetition
      Set<Skin> skins = new HashSet<Skin>();

      // Determine portlets visible on the page
      List<UIPortlet> uiportlets = new ArrayList<UIPortlet>();
      UIWorkingWorkspace uiWorkingWS = getChildById(UI_WORKING_WS_ID);
      UIPortal uiPortal = uiWorkingWS.findFirstComponentOfType(UIPortal.class);
      uiPortal.findComponentOfType(uiportlets, UIPortlet.class);
      UIPortalToolPanel toolPanel = uiWorkingWS.findFirstComponentOfType(UIPortalToolPanel.class);
      if (toolPanel != null && toolPanel.isRendered())
      {
         toolPanel.findComponentOfType(uiportlets, UIPortlet.class);
      }

      // Get portal portlets to filter since they are already in the portal
      // skins
      Set<SkinConfig> portletConfigs = getPortalPortletSkins();

      //
      for (UIPortlet uiPortlet : uiportlets)
      {
         SkinConfig skinConfig = getPortletSkinConfig(uiPortlet);
         if (skinConfig == null)
         {
            skinConfig = getDefaultPortletSkinConfig(uiPortlet);
         }
         if (skinConfig != null && !portletConfigs.contains(skinConfig))
         {
            skins.add(skinConfig);
         }
      }

      //
      return skins;
   }

   private SkinConfig getDefaultPortletSkinConfig(UIPortlet portlet)
   {
      String portletId = portlet.getSkinId();
      if (portletId != null)
      {
         return getSkin(portletId, "Default");
      }
      else
      {
         return null;
      }
   }
 
   private SkinConfig getPortletSkinConfig(UIPortlet portlet)
   {
      String portletId = portlet.getSkinId();
      if (portletId != null)
      {
         return getSkin(portletId, skin_);
      }
      else
      {
         return null;
      }
   }
   
   /**
    * The central area is called the WorkingWorkspace. It is composed of: 1) A
    * UIPortal child which is filled with portal data using the PortalDataMapper
    * helper tool 2) A UIPortalToolPanel which is not rendered by default A
    * UIMaskWorkspace is also added to provide powerfull focus only popups
    * 
    * @throws Exception
    */
   private void addWorkingWorkspace() throws Exception
   {
      UIWorkingWorkspace uiWorkingWorkspace =
         addChild(UIWorkingWorkspace.class, UIPortalApplication.UI_WORKING_WS_ID, null);
      UIComponentDecorator uiViewWS = uiWorkingWorkspace.addChild(UIComponentDecorator.class, null, UI_VIEWING_WS_ID);

      DataStorage dataStorage = getApplicationComponent(DataStorage.class);
      Container container = dataStorage.getSharedLayout();
      UIPortal uiPortal = createUIComponent(UIPortal.class, null, null);
      PortalDataMapper.toUIPortal(uiPortal, userPortalConfig_);
      
      this.putCachedUIPortal(uiPortal);
      this.showedUIPortal = uiPortal;
      
      uiWorkingWorkspace.addChild(UIEditInlineWorkspace.class, null, UI_EDITTING_WS_ID).setRendered(false);
      if (container != null)
      {
         org.exoplatform.portal.webui.container.UIContainer uiContainer =
            createUIComponent(org.exoplatform.portal.webui.container.UIContainer.class, null, null);
         uiContainer.setStorageId(container.getStorageId());
         PortalDataMapper.toUIContainer(uiContainer, container);
         UISiteBody uiSiteBody = uiContainer.findFirstComponentOfType(UISiteBody.class);
         //uiSiteBody.setUIComponent(uiPortal);
         uiSiteBody.setUIComponent(this.showedUIPortal);
         uiContainer.setRendered(true);
         uiViewWS.setUIComponent(uiContainer);
      }
      else
      {
         //uiViewWS.setUIComponent(uiPortal);
         uiViewWS.setUIComponent(this.showedUIPortal);
      }
      // uiWorkingWorkspace.addChild(UIPortalToolPanel.class, null,
      // null).setRendered(false);
      // editInlineWS.addChild(UIPortalToolPanel.class, null,
      // null).setRendered(false);
      addChild(UIMaskWorkspace.class, UIPortalApplication.UI_MASK_WS_ID, null);
   }

   /**
    * The processDecode() method is doing 3 actions: 
    * 1) if the nodePath is null (case of the first request) a call to 
    * super.processDecode(context) is made and we end the method here 
    * 2) if the nodePath exist but is equals to the current one 
    * then we also call super and stops here 
    * 3) if the requested nodePath is not equals to the current one or current 
    * page no longer exists, then an event of type PageNodeEvent.CHANGE_PAGE_NODE 
    * is sent to the associated EventListener; a call to super is then done
    */
   public void processDecode(WebuiRequestContext context) throws Exception
   {
      PortalRequestContext pcontext = (PortalRequestContext)context;
      String nodePath = pcontext.getNodePath().trim();
      
      if (!nodePath.equals(nodePath_) || !isPageExist())
      {
         nodePath_ = nodePath;
         PageNodeEvent<UIPortal> pnevent = new PageNodeEvent<UIPortal>(showedUIPortal, PageNodeEvent.CHANGE_PAGE_NODE, nodePath_);
         showedUIPortal.broadcast(pnevent, Event.Phase.PROCESS);
      }
      super.processDecode(context);
   }

   /**
    * The processrender() method handles the creation of the returned HTML
    * either for a full page render or in the case of an AJAX call The first
    * request, Ajax is not enabled (means no ajaxRequest parameter in the
    * request) and hence the super.processRender() method is called. This will
    * hence call the processrender() of the Lifecycle object as this method is
    * not overidden in UIPortalApplicationLifecycle. There we simply render the
    * bounded template (groovy usually). Note that bounded template are also
    * defined in component annotations, so for the current class it is
    * UIPortalApplication.gtmpl On second calls, request have the "ajaxRequest"
    * parameter set to true in the URL. In that case the algorithm is a bit more
    * complex: a) The list of components that should be updated is extracted
    * using the context.getUIComponentToUpdateByAjax() method. That list was
    * setup during the process action phase b) Portlets and other UI components
    * to update are split in 2 different lists c) Portlets full content are
    * returned and set with the tag <div class="PortalResponse"> d) Block to
    * updates (which are UI components) are set within the <div
    * class="PortalResponseData"> tag e) Then the scripts and the skins to
    * reload are set in the <div class="PortalResponseScript">
    */
   public void processRender(WebuiRequestContext context) throws Exception
   {
      Writer w = context.getWriter();

      //
      if (!context.useAjax())
      {
         super.processRender(context);
      }
      else
      {
         PortalRequestContext pcontext = (PortalRequestContext)context;

         UIMaskWorkspace uiMaskWS = getChildById(UIPortalApplication.UI_MASK_WS_ID);
         if (uiMaskWS.isUpdated())
            pcontext.addUIComponentToUpdateByAjax(uiMaskWS);
         if (getUIPopupMessages().hasMessage())
         {
            pcontext.addUIComponentToUpdateByAjax(getUIPopupMessages());
         }

         Set<UIComponent> list = context.getUIComponentToUpdateByAjax();
         List<UIPortlet> uiPortlets = new ArrayList<UIPortlet>(3);
         List<UIComponent> uiDataComponents = new ArrayList<UIComponent>(5);

         if (list != null)
         {
            for (UIComponent uicomponent : list)
            {
               if (uicomponent instanceof UIPortlet)
                  uiPortlets.add((UIPortlet)uicomponent);
               else
                  uiDataComponents.add(uicomponent);
            }
         }
         w.write("<div class=\"PortalResponse\">");
         w.write("<div class=\"PortalResponseData\">");
         for (UIComponent uicomponent : uiDataComponents)
         {
            if (log.isDebugEnabled())
               log.debug("AJAX call: Need to refresh the UI component " + uicomponent.getName());
            renderBlockToUpdate(uicomponent, context, w);
         }
         w.write("</div>");

         if (!context.getFullRender())
         {
            for (UIPortlet uiPortlet : uiPortlets)
            {
               if (log.isDebugEnabled())
                  log.debug("AJAX call: Need to refresh the Portlet " + uiPortlet.getId());

               w.write("<div class=\"PortletResponse\" style=\"display: none\">");
               w.append("<div class=\"PortletResponsePortletId\">" + uiPortlet.getId() + "</div>");
               w.append("<div class=\"PortletResponseData\">");

               /*
                * If the portlet is using our UI framework or supports it then
                * it will return a set of block to updates. If there is not
                * block to update the javascript client will see that as a full
                * refresh of the content part
                */
               uiPortlet.processRender(context);

               w.append("</div>");
               w.append("<div class=\"PortletResponseScript\"></div>");
               w.write("</div>");
            }
         }
         w.write("<div class=\"MarkupHeadElements\">");
         List<String> headElems = ((PortalRequestContext)context).getExtraMarkupHeadersAsStrings();
         for (String elem : headElems) 
         {
            w.write(elem);
         }
         w.write("</div>");
         w.write("<div class=\"PortalResponseScript\">");
         pcontext.getJavascriptManager().writeJavascript(w);
         w.write("eXo.core.Browser.onLoad();\n");
         pcontext.getJavascriptManager().writeCustomizedOnLoadScript(w);
         String skin = getAddSkinScript(list);
         if (skin != null)
         {
            w.write(skin);
         }
         w.write("</div>");
         w.write("</div>");
      }
   }

   private String getAddSkinScript(Set<UIComponent> updateComponents)
   {
      if (updateComponents == null)
         return null;
      List<UIPortlet> uiportlets = new ArrayList<UIPortlet>();
      for (UIComponent uicomponent : updateComponents)
      {
         if (uicomponent instanceof UIContainer)
         {
            UIContainer uiContainer = (UIContainer)uicomponent;
            uiContainer.findComponentOfType(uiportlets, UIPortlet.class);
         }
         if (uicomponent instanceof UIComponentDecorator)
         {
            UIComponentDecorator uiDecorator = (UIComponentDecorator)uicomponent;
            if (uiDecorator.getUIComponent() instanceof UIContainer)
            {
               UIContainer uiContainer = (UIContainer)uiDecorator.getUIComponent();
               uiContainer.findComponentOfType(uiportlets, UIPortlet.class);
            }
         }
      }
      List<SkinConfig> skins = new ArrayList<SkinConfig>();
      SkinService skinService = getApplicationComponent(SkinService.class);
      for (UIPortlet uiPortlet : uiportlets)
      {
         String skinId = uiPortlet.getSkinId();
         if (skinId != null)
         {
            SkinConfig skinConfig = skinService.getSkin(skinId, skin_);
            if (skinConfig != null)
               skins.add(skinConfig);
         }
      }
      StringBuilder b = new StringBuilder(1000);
      for (SkinConfig ele : skins)
      {
         SkinURL url = ele.createURL();
         url.setOrientation(orientation_);
         b.append("eXo.core.Skin.addSkin('").append(ele.getId()).append("','").append(url).append("');\n");
      }
      return b.toString();
   }

   public UserPortalConfig getUserPortalConfig()
   {
      return userPortalConfig_;
   }

   public void setUserPortalConfig(UserPortalConfig userPortalConfig)
   {
      this.userPortalConfig_ = userPortalConfig;
   }
   
   private boolean isPageExist() throws Exception 
   {
      WebuiRequestContext context = Util.getPortalRequestContext();
      ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
      UserPortalConfigService userPortalConfigService =
         (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
      Page page = null;
      PageNode pageNode = Util.getUIPortal().getSelectedNode();
      if (pageNode != null)
      {
         try
         {
            if (pageNode.getPageReference() != null)
            {
               page = userPortalConfigService.getPage(pageNode.getPageReference(), context.getRemoteUser());
            }
         }
         catch (NoSuchDataException nsde)
         {
            return false;
         }         
      }
      return (page != null);
   }

   public void localizeNavigations()
   {
      ResourceBundleManager i18nManager = getApplicationComponent(ResourceBundleManager.class);
      Locale locale = getLocale();
      
      for(PageNavigation nav : this.getNavigations())
      {
         PageNavigationUtils.localizePageNavigation(nav, locale, i18nManager);
      }
   }
   
   /**
    * Get portal skin from {@link UserProfile} or from {@link UserPortalConfig}
    * 
    * @param context PortalRequestContext
    * @throws Exception 
    * 
    */
   public void reloadSkinPortal(PortalRequestContext context) throws Exception
   {
      String user = context.getRemoteUser();
      String portalSkin = null;
      OrganizationService orgService = getApplicationComponent(OrganizationService.class);
      
      if (user != null)
      {
         UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(user);
         if (userProfile != null)
         {
            portalSkin = userProfile.getUserInfoMap().get(Constants.USER_SKIN);
         }
         else
         {
            if (log.isWarnEnabled())
               log.warn("Could not load user profile for " + user + ". Using default portal locale.");
         }
      }
      
      // use the skin from the user profile if available, otherwise use from the portal config
      if (portalSkin != null && portalSkin.trim().length() > 0)
      {
         skin_ = portalSkin;
      }
      else
      {
         UserPortalConfig userPortalConfig = (UserPortalConfig)context.getAttribute(UserPortalConfig.class);
         String userPortalConfigSkin = userPortalConfig .getPortalConfig().getSkin();
         if (userPortalConfigSkin != null && userPortalConfigSkin.trim().length() > 0)
            skin_ = userPortalConfigSkin;
      }
   }
   
   public void setNavigations(List<PageNavigation> navs)
   {
      this.all_Navigations = navs;
   }
   
   public List<PageNavigation> getNavigations()
   {
      return this.all_Navigations;
   }
   
   private class UIPortalKey
   {

      /** . */
      private final String ownerType;

      /** . */
      private final String ownerId;

      UIPortalKey(String _ownerType, String _ownerId)
      {
         if (_ownerType == null)
         {
            throw new NullPointerException();
         }
         if (_ownerId == null)
         {
            throw new NullPointerException();
         }
         this.ownerType = _ownerType;
         this.ownerId = _ownerId;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == null || obj == null)
         {
            return this == null && obj == null;
         }
         if (!(obj instanceof UIPortalKey))
         {
            return false;
         }
         return this.ownerType.equals(((UIPortalKey)obj).ownerType) && this.ownerId.equals(((UIPortalKey)obj).ownerId);
      }
      
      @Override
      public int hashCode()
      {
         return this.ownerType.hashCode() * 2 + this.ownerId.hashCode();
      }
      
      @Override
      public String toString()
      {
        return "OWNERTYPE: " + ownerType + " OWNERID: " + ownerId;  
      }
   }

}
