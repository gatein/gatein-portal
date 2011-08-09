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

package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.commons.serialization.api.annotations.Converted;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.info.PreferencesInfo;
import org.gatein.pc.federation.FederatingPortletInvoker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 * thanhtungty@gmail.com
 * Jun 24, 2008
 */

@ComponentConfig(template = "app:/groovy/applicationregistry/webui/component/UIPortletManagement.gtmpl", events = {
   @EventConfig(listeners = UIPortletManagement.SelectPortletActionListener.class),
   @EventConfig(listeners = UIPortletManagement.SelectPortletType.class)})
@Serialized
public class UIPortletManagement extends UIContainer
{

   /** Should match WSRPPortletInfo.PRODUCER_NAME_META_INFO_KEY */
   private static final String PRODUCER_NAME_META_INFO_KEY = "producer-name";

   static final public String LOCAL = "local";

   static final public String REMOTE = "remote";

   private List<WebApp> webApps;

   private PortletExtra selectedPorlet;

   private String[] portletTypes = new String[]{LOCAL, REMOTE};

   private String selectedType;

   private static final Comparator<WebApp> WEB_APP_COMPARATOR = new Comparator<WebApp>()
   {
      public int compare(WebApp app1, WebApp app2)
      {
         return app1.getName().compareToIgnoreCase(app2.getName());
      }
   };

   private static final Comparator<PortletExtra> PORTLET_EXTRA_COMPARATOR = new Comparator<PortletExtra>()
   {
      public int compare(PortletExtra portlet1, PortletExtra portlet2)
      {
         return portlet1.getName().compareToIgnoreCase(portlet2.getName());
      }
   };

   public UIPortletManagement() throws Exception
   {
      setSelectedType(LOCAL);
   }

   private void initWebApps(String type) throws Exception
   {
      webApps = new LinkedList<WebApp>(); // LinkedList is more appropriate here since we add lots of elements and sort
      // julien : who said that LinkedList is appropriate for sorting ?
      ExoContainer manager = ExoContainerContext.getCurrentContainer();

      FederatingPortletInvoker portletInvoker =
         (FederatingPortletInvoker)manager.getComponentInstance(FederatingPortletInvoker.class);
      boolean remote = REMOTE.equals(type);

      Set<Portlet> portlets;
      if (!remote)
      {
         portlets = portletInvoker.getLocalPortlets();
      }
      else
      {
         portlets = portletInvoker.getRemotePortlets();
      }

      if (portlets == null || portlets.isEmpty())
      {
         return;
      }

      for (Portlet portlet : portlets)
      {
         PortletInfo info = portlet.getInfo();

         // in the remote case, the "application name" will be the name of the remote invoker
         String appName;
         if (remote)
         {
            LocalizedString producerNameLS = info.getMeta().getMetaValue(PRODUCER_NAME_META_INFO_KEY);
            if (producerNameLS != null)
            {
               appName = producerNameLS.getDefaultString();
            }
            else
            {
               throw new IllegalStateException("Missing PortletInvoker id in remote portlet metadata");
            }
         }
         else
         {
            appName = info.getApplicationName();
         }

         WebApp webApp = getWebApp(appName);
         if (webApp == null)
         {
            webApp = new WebApp(appName);
            webApps.add(webApp);
         }
         webApp.addPortlet(new PortletExtra(portlet));
      }

      Collections.sort(webApps, WEB_APP_COMPARATOR);
      for (WebApp ele : webApps)
      {
         Collections.sort(ele.getPortlets(), PORTLET_EXTRA_COMPARATOR);
      }
   }

   public WebApp getWebApp(String name)
   {
      for (WebApp ele : webApps)
      {
         if (ele.getName().equals(name))
         {
            return ele;
         }
      }
      return null;
   }

   public List<WebApp> getWebApps()
   {
      return webApps;
   }

   public String getSelectedType()
   {
      return selectedType;
   }

   public void setSelectedType(String type) throws Exception
   {
      selectedType = type;
      initWebApps(type);
      if (webApps != null && !webApps.isEmpty())
      {
         List<PortletExtra> list = webApps.get(0).getPortlets();
         if (!list.isEmpty())
         {
            setSelectedPortlet(list.get(0));
         }
      }
      else
      {
         setSelectedPortlet((PortletExtra)null);
      }
   }

   public String[] getPortletTypes()
   {
      return portletTypes;
   }

   public PortletExtra getSelectedPortlet()
   {
      return selectedPorlet;
   }

   public void setSelectedPortlet(PortletExtra portlet) throws Exception
   {
      selectedPorlet = portlet;
      if (selectedPorlet == null)
      {
         getChildren().clear();
         UIMessageBoard uiMessage = addChild(UIMessageBoard.class, null, null);
         uiMessage.setMessage(new ApplicationMessage("UIPortletManagement.msg.noPortlet", null));
         return;
      }
      UIPortletInfo uiPortletInfo = getChild(UIPortletInfo.class);
      if (uiPortletInfo == null)
      {
         getChildren().clear();
         uiPortletInfo = addChild(UIPortletInfo.class, null, null);
      }
      uiPortletInfo.setPortlet(selectedPorlet);
      uiPortletInfo.getChild(UICategorySelector.class).setRendered(false);
   }

   public void setSelectedPortlet(String id) throws Exception
   {
      String webAppName;
      String portletName;
      if (LOCAL.equals(selectedType))
      {
         String[] fragments = id.split("/");
         webAppName = fragments[0];
         portletName = fragments[1];
      }
      else
      {
         // extract PortletInvoker id to use as WebApp name
         final int separatorIndex = id.indexOf('.');
         webAppName = id.substring(0, separatorIndex);
         portletName = id.substring(separatorIndex + 1);
      }

      WebApp webApp = getWebApp(webAppName);
      for (PortletExtra ele : webApp.getPortlets())
      {
         if (ele.getName().equals(portletName))
         {
            setSelectedPortlet(ele);
            break;
         }
      }
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      super.processRender(context);
   }

   static public class SelectPortletType extends EventListener<UIPortletManagement>
   {

      public void execute(Event<UIPortletManagement> event) throws Exception
      {
         UIPortletManagement uiManagement = event.getSource();
         String type = event.getRequestContext().getRequestParameter(OBJECTID);
         uiManagement.setSelectedType(type);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
      }

   }

   static public class SelectPortletActionListener extends EventListener<UIPortletManagement>
   {

      public void execute(Event<UIPortletManagement> event) throws Exception
      {
         UIPortletManagement uiManagement = event.getSource();
         String portletId = event.getRequestContext().getRequestParameter(OBJECTID);
         uiManagement.setSelectedPortlet(portletId);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
      }

   }

   @Converted(PortletExtraSerializer.class)
   static public class PortletExtra
   {

      private String id_;

      private String name_;

      private String group_;

      private String type_;

      private PortletInfo portletInfo_;

      final PortletContext context;

      public PortletExtra(Portlet portlet)
      {
         PortletInfo info = portlet.getInfo();

         context = portlet.getContext();

         String appName = info.getApplicationName();
         boolean remote = portlet.isRemote();

         String portletId;
         if (remote)
         {
            portletId = context.getId();
         }
         else
         {
            portletId = info.getApplicationName() + "/" + info.getName();
         }

         String type = remote ? REMOTE : LOCAL;

         //
         id_ = portletId;
         group_ = appName;
         name_ = info.getName();
         type_ = type;
         portletInfo_ = info;

      }

      public String getId()
      {
         return id_;
      }

      public String getName()
      {
         return name_;
      }

      public String getPortletGroup()
      {
         return group_;
      }

      public String getType()
      {
         return type_;
      }

      public String getDisplayName()
      {
         try
         {
            final String displayName = getMetaValue(MetaInfo.DISPLAY_NAME, name_);
            return isRemote() ? displayName + " (remote)" : displayName;
         }
         catch (Exception ex)
         {
            return "COULD NOT GET DISPLAY NAME OF THE PORTLET";
         }
      }

      public String getDescription()
      {
         try
         {
            return getMetaValue(MetaInfo.DESCRIPTION, name_);
         }
         catch (Exception ex)
         {
            return "COULD NOT GET DESCRIPTION OF THE PORTLET";
         }
      }

      public PreferencesInfo getPortletPreferences()
      {
         try
         {
            return portletInfo_.getPreferences();
         }
         catch (Exception ex)
         {
            return null;
         }
      }

      private String getMetaValue(String metaKey, String defaultValue)
      {
         LocalizedString metaValue = portletInfo_.getMeta().getMetaValue(metaKey);
         if (metaValue == null || metaValue.getDefaultString() == null)
         {
            return defaultValue;
         }
         return metaValue.getDefaultString();
      }

      public boolean isRemote()
      {
         return REMOTE.equals(type_);
      }
   }

   @Serialized
   static public class WebApp
   {

      String name_;

      List<PortletExtra> portlets_;

      public WebApp()
      {
      }

      public WebApp(String name)
      {
         name_ = name;
      }

      public WebApp(String name, List<PortletExtra> portlets)
      {
         name_ = name;
         portlets_ = portlets;
      }

      public String getName()
      {
         return name_;
      }

      public void setName(String name)
      {
         name_ = name;
      }

      public List<PortletExtra> getPortlets()
      {
         return portlets_;
      }

      public void setPortlets(List<PortletExtra> portlets)
      {
         portlets_ = portlets;
      }

      public void addPortlet(PortletExtra portlet)
      {
         if (portlets_ == null)
         {
            portlets_ = new ArrayList<PortletExtra>();
         }
         portlets_.add(portlet);
      }

   }
}
