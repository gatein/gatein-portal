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
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.page.UIPageActionListener.ChangePageNodeActionListener;
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
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.web.login.InitiateLoginServlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@ComponentConfig(lifecycle = UIPortalLifecycle.class, template = "system:/groovy/portal/webui/portal/UIPortal.gtmpl", events = {
   @EventConfig(listeners = ChangePageNodeActionListener.class),
   @EventConfig(listeners = ChangeApplicationListActionListener.class),
   @EventConfig(listeners = MoveChildActionListener.class),
   // @EventConfig(listeners =
   // RemoveJSApplicationToDesktopActionListener.class),
   @EventConfig(listeners = UIPortal.ChangeWindowStateActionListener.class),
   @EventConfig(listeners = UIPortal.LogoutActionListener.class),
   @EventConfig(listeners = ShowLoginFormActionListener.class),
   @EventConfig(listeners = ChangeLanguageActionListener.class),
   @EventConfig(listeners = EditPortalPropertiesActionListener.class),
   @EventConfig(listeners = ChangeSkinActionListener.class),
   @EventConfig(listeners = RecoveryPasswordAndUsernameActionListener.class),
   @EventConfig(listeners = UIPortal.AccountSettingsActionListener.class),
   @EventConfig(listeners = UIPortalActionListener.PingActionListener.class)})
public class UIPortal extends UIContainer
{

   /** Storage id. */
   private String storageId;

   private String owner;

   private String ownerType;

   private String locale;

   private String[] accessPermissions;

   private String editPermission;

   private String skin;

   private Properties properties;

   private List<PageNavigation> navigations;

   private List<PageNode> selectedPaths_;

   private PageNode selectedNode_;

   private PageNavigation selectedNavigation_;

   private Map<String, String[]> publicParameters_ = new HashMap<String, String[]>();

   private UIComponent maximizedUIComponent;

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public String getOwner()
   {
      return owner;
   }

   public void setOwner(String s)
   {
      owner = s;
   }

   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String s)
   {
      locale = s;
   }

   public String[] getAccessPermissions()
   {
      return accessPermissions;
   }

   public void setAccessPermissions(String[] accessGroups)
   {
      this.accessPermissions = accessGroups;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public void setEditPermission(String editPermission)
   {
      this.editPermission = editPermission;
   }

   public String getSkin()
   {
      return skin;
   }

   public void setSkin(String s)
   {
      skin = s;
   }

   public String getOwnerType()
   {
      return ownerType;
   }

   public void setOwnerType(String ownerType)
   {
      this.ownerType = ownerType;
   }

   public Map<String, String[]> getPublicParameters()
   {
      return publicParameters_;
   }

   public void setPublicParameters(Map<String, String[]> publicParams)
   {
      publicParameters_ = publicParams;
   }

   public List<PageNavigation> getNavigations() throws Exception
   {
      UserPortalConfigService serv = getApplicationComponent(UserPortalConfigService.class);
      for (int i = 0; i < navigations.size(); i++)
      {
         PageNavigation ele = navigations.get(i);
         if (serv.getPageNavigation(ele.getOwnerType(), ele.getOwnerId()) == null)
         {
            navigations.remove(i);
            --i;
         }
      }

      return navigations;
   }

   public void setNavigation(List<PageNavigation> navs) throws Exception
   {
      navigations = navs;
      selectedPaths_ = new ArrayList<PageNode>();
      if (navigations == null || navigations.size() < 1)
         return;
      // PageNavigation pNav = navigations.get(0);
      // if(pNav.getNodes() == null || pNav.getNodes().size() < 1) return;

      // TODO dang.tung: get suitable navigation
      // ----------------------------------------------------------
      PageNavigation pNav = null;
      for (PageNavigation nav : navs)
      {
         if (nav.getNodes() != null && nav.getNodes().size() > 0)
         {
            pNav = nav;
            break;
         }
      }
      if (pNav == null)
         return;
      // ----------------------------------------------------------
      selectedNode_ = pNav.getNodes().get(0);
      selectedPaths_.add(selectedNode_);
      UIPageBody uiPageBody = findFirstComponentOfType(UIPageBody.class);
      if (uiPageBody == null)
         return;
      uiPageBody.setPageBody(selectedNode_, this);
      UIPortalApplication uiApp = Util.getUIPortalApplication();
      refreshNavigation(uiApp.getLocale());
   }

   public void setSelectedNode(PageNode node)
   {
      selectedNode_ = node;
   }

   public PageNode getSelectedNode() throws Exception
   {
      if (selectedNode_ != null)
         return selectedNode_;
      if (getSelectedNavigation() == null || selectedNavigation_.getNodes() == null
         || selectedNavigation_.getNodes().size() < 1)
         return null;
      selectedNode_ = selectedNavigation_.getNodes().get(0);
      return selectedNode_;
   }

   public List<PageNode> getSelectedPaths()
   {
      return selectedPaths_;
   }

   public void setSelectedPaths(List<PageNode> nodes)
   {
      selectedPaths_ = nodes;
   }

   public PageNavigation getSelectedNavigation() throws Exception
   {
      if (selectedNavigation_ != null && selectedNavigation_.getNodes() != null
         && selectedNavigation_.getNodes().size() > 0)
      {
         return selectedNavigation_;
      }
      if (getNavigations().size() < 1)
         return null;
      // TODO dang.tung: get right selectedNavigation
      // -------------------------------------------
      List<PageNavigation> navs = getNavigations();
      PageNavigation pNav = navs.get(0);
      for (PageNavigation nav : navs)
      {
         if (nav.getNodes() != null && nav.getNodes().size() > 0)
         {
            pNav = nav;
            break;
         }
      }
      // -------------------------------------------
      setSelectedNavigation(pNav);
      return pNav;
   }

   public PageNavigation getPageNavigation(int id)
   {
      for (PageNavigation nav : navigations)
      {
         if (nav.getId() == id)
            return nav;
      }
      return null;
   }

   public void setSelectedNavigation(PageNavigation selectedNavigation)
   {
      selectedNavigation_ = selectedNavigation;
   }

   public UIComponent getMaximizedUIComponent()
   {
      return maximizedUIComponent;
   }

   public void setMaximizedUIComponent(UIComponent maximizedReferenceComponent)
   {
      this.maximizedUIComponent = maximizedReferenceComponent;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setProperties(Properties props)
   {
      properties = props;
   }

   public String getProperty(String name)
   {
      if (name == null)
         throw new NullPointerException();
      if (properties == null)
         return null;
      return properties.get(name);
   }

   public String getProperty(String name, String defaultValue)
   {
      String value = getProperty(name);
      if (value == null)
         value = defaultValue;
      return value;
   }

   public void setProperty(String name, String value)
   {
      if (name == null || properties == null)
         throw new NullPointerException();
      if (value == null)
         properties.remove(name);
      else
         properties.setProperty(name, value);
   }

   public void removeProperty(String name)
   {
      if (name == null || properties == null)
         throw new NullPointerException();
      properties.remove(name);
   }

   public String getSessionAlive()
   {
      return getProperty(PortalProperties.SESSION_ALIVE, PortalProperties.SESSION_ON_DEMAND);
   }

   public void setSessionAlive(String type)
   {
      setProperty(PortalProperties.SESSION_ALIVE, type);
   }

   @Deprecated
   public void refreshNavigation()
   {
      ResourceBundleManager mgr = getApplicationComponent(ResourceBundleManager.class);
      for (PageNavigation nav : navigations)
      {
         if (nav.getOwnerType().equals(PortalConfig.USER_TYPE))
            continue;
         ResourceBundle res = mgr.getNavigationResourceBundle(locale, nav.getOwnerType(), nav.getOwnerId());
         for (PageNode node : nav.getNodes())
         {
            resolveLabel(res, node);
         }
      }
   }

   public void refreshNavigation(Locale locale)
   {
      for (PageNavigation nav : navigations)
      {
         localizePageNavigation(nav);
      }
   }

   private void localizePageNavigation(PageNavigation nav)
   {
      ResourceBundleManager mgr = getApplicationComponent(ResourceBundleManager.class);
      if (nav.getOwnerType().equals(PortalConfig.USER_TYPE))
         return;
      ResourceBundle res = mgr.getNavigationResourceBundle(locale, nav.getOwnerType(), nav.getOwnerId());
      for (PageNode node : nav.getNodes())
      {
         resolveLabel(res, node);
      }
   }

   private void resolveLabel(ResourceBundle res, PageNode node)
   {
      node.setResolvedLabel(res);
      if (node.getChildren() == null)
         return;
      for (PageNode childNode : node.getChildren())
      {
         resolveLabel(res, childNode);
      }
   }

   static public class LogoutActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         HttpServletRequest req = prContext.getRequest();
         req.getSession().invalidate();
         Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
         cookie.setPath(req.getContextPath());
         cookie.setMaxAge(0);
         prContext.getResponse().addCookie(cookie);
         // String portalName = URLEncoder.encode(Util.getUIPortal().getName(),
         // "UTF-8") ;
         String portalName = URLEncoder.encode(prContext.getPortalOwner(), "UTF-8");
         String redirect = req.getContextPath() + "/public/" + portalName + "/";
         prContext.getResponse().sendRedirect(redirect);
         prContext.setResponseComplete(true);
      }
   }

   static public class ChangeWindowStateActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = event.getSource();
         String portletId = event.getRequestContext().getRequestParameter("portletId");
         UIPortlet uiPortlet = uiPortal.findComponentById(portletId);
         WebuiRequestContext context = event.getRequestContext();
         uiPortlet.createEvent("ChangeWindowState", event.getExecutionPhase(), context).broadcast();
      }
   }

   public static class AccountSettingsActionListener extends EventListener<UIPortal>
   {
      public void execute(Event<UIPortal> event) throws Exception
      {
         UIPortal uiPortal = Util.getUIPortal();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null);
         uiMaskWS.setUIComponent(uiAccountForm);
         uiMaskWS.setShow(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      }
   }

}