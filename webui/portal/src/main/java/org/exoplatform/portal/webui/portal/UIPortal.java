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
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.portal.webui.application.UIPortlet;
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
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.login.InitiateLoginServlet;
import org.exoplatform.web.login.LogoutControl;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.exoplatform.web.security.security.CookieTokenService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.WindowState;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@ComponentConfig(lifecycle = UIPortalLifecycle.class, template = "system:/groovy/portal/webui/portal/UIPortal.gtmpl", events = {
   @EventConfig(listeners = ChangeApplicationListActionListener.class),
   @EventConfig(listeners = MoveChildActionListener.class),
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

   public SiteKey getSiteKey()
   {
      return siteKey;
   }
   
   public void setSiteKey(SiteKey key)
   {
      siteKey = key;
   }
   
   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String s)
   {
      locale = s;
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

   /**
    * @deprecated Use {@link #getSiteType()} instead
    * 
    * @return
    */
   @Deprecated
   public String getOwnerType()
   {
      return siteKey.getTypeName();
   }
   
   public SiteType getSiteType()
   {
      return siteKey.getType();
   }

   public Map<String, String[]> getPublicParameters()
   {
      return publicParameters_;
   }

   public void setPublicParameters(Map<String, String[]> publicParams)
   {
      publicParameters_ = publicParams;
   }

   public UserNode getNavPath() throws Exception
   {
      if (navPath == null)
      {
         UIPortalApplication uiApp = Util.getUIPortalApplication();
         navPath = uiApp.getUserPortalConfig().getUserPortal().getDefaultPath(null);
      }
      return navPath;
   }
   
   public void setNavPath(UserNode nav)
   {
      this.navPath = nav;
   }
   
   /**
    * Return cached UIPage associated to the specified pageReference
    * 
    * @param pageReference key whose associated UIPage is to be returned
    * @return the UIPage associated to the specified pageReference or null if not any
    */
   public UIPage getUIPage(String pageReference)
   {
      if(all_UIPages == null)
      {
         this.all_UIPages = new HashMap<String, UIPage>(5);
         return null;
      }
      return this.all_UIPages.get(pageReference);
   }
   
   public void setUIPage(String pageReference, UIPage uiPage)
   {
      if(this.all_UIPages == null)
      {
         this.all_UIPages = new HashMap<String, UIPage>(5);
      }
      this.all_UIPages.put(pageReference, uiPage);
   }
   
   public void clearUIPage(String pageReference)
   {
      if (this.all_UIPages != null)
         this.all_UIPages.remove(pageReference);
   }
   
   public UserNavigation getUserNavigation() throws Exception
   {
      UIPortalApplication uiPortalApp = getAncestorOfType(UIPortalApplication.class);
      return uiPortalApp.getUserPortalConfig().getUserPortal().getNavigation(siteKey);
   }
   
   /**
    * Refresh the UIPage under UIPortal 
    * 
    * @throws Exception
    */
   public void refreshUIPage() throws Exception
   {
      UIPageBody uiPageBody = findFirstComponentOfType(UIPageBody.class);
      if(uiPageBody == null)
      {
         return;
      }
      
      if (uiPageBody.getMaximizedUIComponent() != null)
      {
         UIPortlet currentPortlet = (UIPortlet)uiPageBody.getMaximizedUIComponent();
         currentPortlet.setCurrentWindowState(WindowState.NORMAL);
         uiPageBody.setMaximizedUIComponent(null);
      }
      uiPageBody.setPageBody(getSelectedUserNode(), this);
   }
   
   public UserNode getSelectedUserNode() throws Exception
   {
      return getNavPath();
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
   
   public Boolean isShowInfobar()
   {
      String value = getProperty(PortalProperties.SHOW_PORTLET_INFO, "1");
      if (Integer.parseInt(value) == 1)
      {
         return true;
      }
      return false;
   }

   public void setShowInfobar(Boolean value)
   {
      if (value)
      {
         setProperty(PortalProperties.SHOW_PORTLET_INFO, "1");
      }
      else
      {
         setProperty(PortalProperties.SHOW_PORTLET_INFO, "0");
      }
   }
   
   public String getLabel()
   {
      return label;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   static public class LogoutActionListener extends EventListener<UIComponent>
   {
      public void execute(Event<UIComponent> event) throws Exception
      {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         HttpServletRequest req = prContext.getRequest();
         
         //Delete the token from JCR
         String token = getTokenCookie(req);
         if(token != null)
         {
            AbstractTokenService tokenService = AbstractTokenService.getInstance(CookieTokenService.class);
            tokenService.deleteToken(token);
         }

         String portalName = prContext.getPortalOwner();
         NodeURL createURL =
            prContext.createURL(NodeURL.TYPE);
         createURL.setResource(new NavigationResource(SiteType.PORTAL, portalName, null));
         
         LogoutControl.wantLogout();
         Cookie cookie = new Cookie(InitiateLoginServlet.COOKIE_NAME, "");
         cookie.setPath(req.getContextPath());
         cookie.setMaxAge(0);
         prContext.getResponse().addCookie(cookie);

         prContext.sendRedirect(createURL.toString());
      }
      
      private String getTokenCookie(HttpServletRequest req)
      {
         Cookie[] cookies = req.getCookies();
         if (cookies != null)
         {
            for (Cookie cookie : cookies)
            {
               if (InitiateLoginServlet.COOKIE_NAME.equals(cookie.getName()))
               {
                  return cookie.getValue();
               }
            }
         }
         return null;
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
         UIPortal uiPortal = event.getSource();
         UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);

         //Modified by nguyenanhkien2a@gmail.com
         //We should check account for existing
         String username = Util.getPortalRequestContext().getRemoteUser();
         OrganizationService service = uiPortal.getApplicationComponent(OrganizationService.class);
         User useraccount = service.getUserHandler().findUserByName(username);
         
         if(useraccount != null)
         {        
            UIAccountSetting uiAccountForm = uiMaskWS.createUIComponent(UIAccountSetting.class, null, null);
            uiMaskWS.setUIComponent(uiAccountForm);
            uiMaskWS.setShow(true);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
         }
         else 
         {
            //Show message detail to user and then logout if user press ok button
            JavascriptManager jsManager = Util.getPortalRequestContext().getJavascriptManager();
            jsManager.importJavascript("eXo");
            jsManager.addJavascript("if(confirm('" + 
               Util.getPortalRequestContext().getApplicationResourceBundle().getString("UIAccountProfiles.msg.NotExistingAccount") + 
               "')) {eXo.portal.logout();}");
         }
      }
   }
}
