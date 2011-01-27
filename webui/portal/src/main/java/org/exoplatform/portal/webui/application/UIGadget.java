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

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;
import java.util.UUID;

/**
 * Created by The eXo Platform SAS Author : dang.tung tungcnw@gmail.com May 06,
 * 2008
 */
@ComponentConfig(template = "system:/groovy/portal/webui/application/UIGadget.gtmpl", events = {
   @EventConfig(listeners = UIGadget.SaveUserPrefActionListener.class),
   @EventConfig(listeners = UIGadget.SetNoCacheActionListener.class),
   @EventConfig(listeners = UIGadget.SetDebugActionListener.class)})
/**
 * This class represents user interface gadgets, it using UIGadget.gtmpl for
 * rendering UI in eXo. It mapped to Application model in page or container.
 */
public class UIGadget extends UIComponent
{

   /** The storage id. */
   private String storageId;

   /** The storage name. */
   private String storageName;

   /** . */
   private ApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget> state;

   /** . */
   private String gadgetId;

   private Properties properties_;

   private JSONObject metadata_;

   private String url_;

   private GadgetRegistryService gadgetRegistryService = null;

   public static final String PREF_KEY = "_pref_gadget_";

   public static final String PREF_NO_CACHE = "_pref_no_cache_";

   public static final String PREF_DEBUG = "_pref_debug_";

   public static final String HOME_VIEW = "home";

   public static final String CANVAS_VIEW = "canvas"; 
   
   public static final String METADATA_GADGETS = "gadgets";
   
   public static final String METADATA_USERPREFS = "userPrefs";
   
   public static final String METADATA_USERPREFS_TYPE = "type";
   
   public static final String METADATA_USERPREFS_TYPE_HIDDEN = "hidden";
   
   public static final String METADATA_USERPREFS_TYPE_LIST = "list";

   public String view = HOME_VIEW;

   /**
    * Initializes a newly created <code>UIGadget</code> object
    * 
    * @throws Exception if can't initialize object
    */
   public UIGadget()
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

   public String getId()
   {
      return storageName;
   }

   public ApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget> getState()
   {
      return state;
   }

   public void setState(ApplicationState<org.exoplatform.portal.pom.spi.gadget.Gadget> state)
   {
      if (state != null)
      {
         try
         {
            DataStorage ds = getApplicationComponent(DataStorage.class);
            String gadgetId = ds.getId(state);

            //
            this.gadgetId = gadgetId;
            this.state = state;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         this.gadgetId = null;
         this.state = null;
      }

   }

   /**
    * Gets name of gadget application
    * 
    * @return the string represents name of gadget application
    */
   public String getApplicationName()
   {
      return gadgetId;
   }

   /**
    * Gets Properties of gadget application such as locationX, locationY in
    * desktop page
    * 
    * @return all properties of gadget application
    * @see org.exoplatform.portal.config.model.Application
    * @see org.exoplatform.portal.config.model.Properties
    */
   public Properties getProperties()
   {
      if (properties_ == null)
         properties_ = new Properties();
      return properties_;
   }

   /**
    * Sets Properties of gadget application such as locationX, locationY in
    * desktop page
    * 
    * @param properties Properties that is the properties of gadget application
    * @see org.exoplatform.portal.config.model.Properties
    * @see org.exoplatform.portal.config.model.Application
    */
   public void setProperties(Properties properties)
   {
      this.properties_ = properties;
   }

   public String getMetadata()
   {
      try
      {
         if (metadata_ == null)
         {
            String strMetadata = GadgetUtil.fetchGagdetMetadata(getUrl());
            metadata_ = new JSONObject(strMetadata);
         }
         JSONObject obj = metadata_.getJSONArray(METADATA_GADGETS).getJSONObject(0);
         String token = GadgetUtil.createToken(this.getUrl(), new Random().nextLong());
         obj.put("secureToken", token);
         return metadata_.toString();
      }
      catch (JSONException e)
      {
         return null;
      }
   }
   /**
    * Check if content of gadget has <UserPref>? (Content is parsed from gadget specification in .xml file)
    * @return boolean
    */
   public boolean isSettingUserPref()
   {
      try
      {
         if(metadata_ != null)
         {
            JSONObject obj = metadata_.getJSONArray(METADATA_GADGETS).getJSONObject(0);
            JSONObject userPrefs = obj.getJSONObject(METADATA_USERPREFS);
            JSONArray names = userPrefs.names();
            int count = names.length();
            if(count > 0)
            {
               for(int i = 0; i < count; i++)
               {
                  JSONObject o = (JSONObject) userPrefs.get(names.get(i).toString());
                  if(!(o.get(METADATA_USERPREFS_TYPE).equals(METADATA_USERPREFS_TYPE_HIDDEN) || 
                        o.get(METADATA_USERPREFS_TYPE).equals(METADATA_USERPREFS_TYPE_LIST)))
                     return true;
               }
               return false;
            }
         }
         return false;
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public boolean isLossData()
   {
      try
      {
         DataStorage service = getApplicationComponent(DataStorage.class);
         service.load(state, ApplicationType.GADGET);
         if (getApplication() == null)
         {
            throw new Exception();
         }
      }
      catch (Exception e)
      {
         return true;
      }
      return false;
   }

   /**
    * Gets GadgetApplication by GadgedRegistryService
    * 
    * @return Gadget Application
    * @throws Exception
    */
   private Gadget getApplication()
   {
      try
      {
         GadgetRegistryService gadgetService = getApplicationComponent(GadgetRegistryService.class);
         return gadgetService.getGadget(gadgetId);
      }
      catch (Exception ex)
      {
         return null;
      }
   }

   /**
    * Gets Url of gadget application, it saved before by GadgetRegistryService
    * 
    * @return url of gadget application, such as
    *         "http://www.google.com/ig/modules/horoscope.xml"
    */
   public String getUrl()
   {
      if (url_ == null)
      {
         Gadget gadget = getApplication();
         url_ = GadgetUtil.reproduceUrl(gadget.getUrl(), gadget.isLocal());
      }
      return url_;
   }

   private GadgetRegistryService getGadgetRegistryService()
   {
      if (gadgetRegistryService == null)
         gadgetRegistryService =
            (GadgetRegistryService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
               GadgetRegistryService.class);
      return gadgetRegistryService;
   }

   public boolean isNoCache()
   {
      if(PropertyManager.isDevelopping())
         return true;
      return false;
   }

   public void setNoCache(boolean value)
   {
   }

   public boolean isDebug()
   {
      if(PropertyManager.isDevelopping())
         return true;
      return false;
   }

   public void setDebug(boolean value)
   {
   }

   public boolean isGadgetDeveloper()
   {
      return getGadgetRegistryService().isGadgetDeveloper(Util.getPortalRequestContext().getRemoteUser());
   }

   public String getView()
   {
      if (view != null)
         return view;
      return HOME_VIEW;
   }

   public void setView(String view)
   {
      this.view = view;
   }

   /**
    * Gets user preference of gadget application
    * 
    * @return the string represents user preference of gadget application
    * @throws Exception when can't convert object to string
    */
   public String getUserPref() throws Exception
   {
      DataStorage service = getApplicationComponent(DataStorage.class);
      org.exoplatform.portal.pom.spi.gadget.Gadget pp = service.load(state, ApplicationType.GADGET);
      return pp != null ? pp.getUserPref() : null;
   }

   public void addUserPref(String addedUserPref) throws Exception
   {
      DataStorage service = getApplicationComponent(DataStorage.class);
      org.exoplatform.portal.pom.spi.gadget.Gadget gadget = new org.exoplatform.portal.pom.spi.gadget.Gadget();

      //
      gadget.addUserPref(addedUserPref);

      //
      state = service.save(state, gadget);

      // WARNING :
      // This is used to force a state save and it should not be copied else where to make things
      // convenient as this could lead to a severe performance degradation
      ModelDataStorage mds = getApplicationComponent(ModelDataStorage.class);
      mds.save();
   }

   /**
    * Initializes a newly created <code>SaveUserPrefActionListener</code>
    * object
    */
   static public class SaveUserPrefActionListener extends EventListener<UIGadget>
   {
      public void execute(Event<UIGadget> event) throws Exception
      {
         UIGadget uiGadget = event.getSource();
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         
         //
         try
         {
            uiGadget.addUserPref(event.getRequestContext().getRequestParameter("userPref"));
            Util.getPortalRequestContext().setResponseComplete(true);
         } 
         catch(Exception e)
         {                        
            UIPortletApplication uiPortlet = uiGadget.getAncestorOfType(UIPortletApplication.class);
            context.addUIComponentToUpdateByAjax(uiPortlet);
            throw new MessageException(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null, ApplicationMessage.ERROR));
         }

         //
         if (uiGadget.isLossData())
         {
            /*
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            uiApp.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
            PortalRequestContext pcontext = Util.getPortalRequestContext();
            pcontext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            */
            return;
         }

         //event.getRequestContext().setResponseComplete(true);
      }
   }

   static public class SetNoCacheActionListener extends EventListener<UIGadget>
   {
      public void execute(Event<UIGadget> event) throws Exception
      {
         /*
          * String noCache =
          * event.getRequestContext().getRequestParameter("nocache") ; UIGadget
          * uiGadget = event.getSource() ;
          * uiGadget.setNoCache(noCache.equals("1"));
          */
         event.getRequestContext().setResponseComplete(true);
      }
   }

   static public class SetDebugActionListener extends EventListener<UIGadget>
   {
      public void execute(Event<UIGadget> event) throws Exception
      {
         /*
          * String debug = event.getRequestContext().getRequestParameter("debug") ;
          * UIGadget uiGadget = event.getSource() ;
          * uiGadget.setDebug(debug.equals("1"));
          */
         event.getRequestContext().setResponseComplete(true);
      }
   }
}
