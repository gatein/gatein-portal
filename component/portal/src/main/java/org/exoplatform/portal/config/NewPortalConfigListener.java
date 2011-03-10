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

package org.exoplatform.portal.config;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.PortletPreferences.PortletPreferencesSet;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.jibx.runtime.*;
import org.jibx.runtime.impl.UnmarshallingContext;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen
 * tuan08@users.sourceforge.net May 22, 2006
 */

public class NewPortalConfigListener extends BaseComponentPlugin
{

   /** . */
   private ConfigurationManager cmanager_;

   /** . */
   private DataStorage dataStorage_;

   /** . */
   private volatile List<NewPortalConfig> configs;

   /** . */
   private List<SiteConfigTemplates> templateConfigs;

   /** . */
   private String pageTemplatesLocation_;

   /** . */
   private String defaultPortal;
   
   /**
    * If true the default portal name has been explicitly set.
    * If false the name has not been set and we are using the default.
    */
   private boolean defaultPortalSpecified = false;

   /** . */
   private boolean isUseTryCatch;

   /**
    * If true the portal clear portal metadata from data storage and replace
    * it with new data created from .xml files
    */
   private boolean overrideExistingData;

   /** . */
   private Logger log = LoggerFactory.getLogger(getClass());

   public NewPortalConfigListener(DataStorage dataStorage, ConfigurationManager cmanager, InitParams params)
      throws Exception
   {
      cmanager_ = cmanager;
      dataStorage_ = dataStorage;

      ValueParam valueParam = params.getValueParam("page.templates.location");
      if (valueParam != null)
         pageTemplatesLocation_ = valueParam.getValue();

      valueParam = params.getValueParam("default.portal");
      if (valueParam != null)
      {
         defaultPortal = valueParam.getValue();
      }
      
      if (defaultPortal == null || defaultPortal.trim().length() == 0)
      {
         defaultPortal = "classic";
      }
      else
      {
         defaultPortalSpecified = true;
      }
      
      configs = params.getObjectParamValues(NewPortalConfig.class);

      templateConfigs = params.getObjectParamValues(SiteConfigTemplates.class);

      // get parameter
      valueParam = params.getValueParam("initializing.failure.ignore");
      // determine in the run function, is use try catch or not
      if (valueParam != null)
      {
         isUseTryCatch = (valueParam.getValue().toLowerCase().equals("true"));
      }
      else
      {
         isUseTryCatch = true;
      }

      valueParam = params.getValueParam("override");
      if(valueParam != null)
      {
         overrideExistingData = "true".equals(valueParam.getValue());
      }
      else
      {
         overrideExistingData = false;
      }

   }

   public void run() throws Exception
   {
      //DANGEROUS! If the user delete the defaultPortal (ie: classic), the next time he restarts
      //the server. Data of predefined owners would be overriden
      if (dataStorage_.getPortalConfig(defaultPortal) != null && !overrideExistingData)
         return;

      if (isUseTryCatch)
      {

         for (NewPortalConfig ele : configs)
         {
            try
            {
               initPortletPreferencesDB(ele);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (NewPortalConfig ele : configs)
         {
            try
            {
               initPortalConfigDB(ele);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (NewPortalConfig ele : configs)
         {
            try
            {
               initPageDB(ele);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (NewPortalConfig ele : configs)
         {
            try
            {
               initPageNavigationDB(ele);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (NewPortalConfig ele : configs)
         {
            try
            {
               ele.getPredefinedOwner().clear();
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }

      }
      else
      {
         for (NewPortalConfig ele : configs)
         {
            initPortletPreferencesDB(ele);
         }
         for (NewPortalConfig ele : configs)
         {
            initPortalConfigDB(ele);
         }
         for (NewPortalConfig ele : configs)
         {
            initPageDB(ele);
         }
         for (NewPortalConfig ele : configs)
         {
            initPageNavigationDB(ele);
         }
         for (NewPortalConfig ele : configs)
         {
            ele.getPredefinedOwner().clear();
         }
      }
   }

   String getDefaultPortal()
   {
      return defaultPortal;
   }

   /**
    * Returns a specified new portal config. The returned object can be safely modified by as it is
    * a copy of the original object.
    *
    * @param ownerType the owner type
    * @param template 
    * @return the specified new portal config
    */
   NewPortalConfig getPortalConfig(String ownerType, String template)
   {
      for (NewPortalConfig portalConfig : configs)
      {
         if (portalConfig.getOwnerType().equals(ownerType))
         {
            // We are defensive, we make a deep copy
            return new NewPortalConfig(portalConfig);
         }
      }
      return null;
   }

   /**
    * This is used to merge an other NewPortalConfigListener to this one
    * 
    * @param other
    */
   public void mergePlugin(NewPortalConfigListener other)
   {
      //if other didn't actually set anything for the default portal name
      //then we should continue to use the current value. This way if an extension
      //doesn't set it, it wont override the parent's set value.
      if (other.defaultPortalSpecified)
      {
         this.defaultPortal = other.defaultPortal;
      }
      
      if (configs == null)
      {
         this.configs = other.configs;
      }
      else if (other.configs != null && !other.configs.isEmpty())
      {
         List<NewPortalConfig> result = new ArrayList<NewPortalConfig>(configs);
         result.addAll(other.configs);
         this.configs = Collections.unmodifiableList(result);
      }

      if (templateConfigs == null)
      {
         this.templateConfigs = other.templateConfigs;
      }
      else if (other.templateConfigs != null && !other.templateConfigs.isEmpty())
      {
         List<SiteConfigTemplates> result = new ArrayList<SiteConfigTemplates>(templateConfigs);
         result.addAll(other.templateConfigs);
         this.templateConfigs = Collections.unmodifiableList(result);
      }

      //The override is true if and only if one of the plugin NewPortalConfigListener configures its
      //override param as true
      overrideExistingData = overrideExistingData || other.overrideExistingData;
   }

   public void initPortalConfigDB(NewPortalConfig config) throws Exception
   {
      for (String owner : config.getPredefinedOwner())
      {
         createPortalConfig(config, owner);
      }
   }

   public void initPageDB(NewPortalConfig config) throws Exception
   {
      for (String owner : config.getPredefinedOwner())
      {
         createPage(config, owner);
      }
   }

   public void initPageNavigationDB(NewPortalConfig config) throws Exception
   {
      for (String owner : config.getPredefinedOwner())
      {
         createPageNavigation(config, owner);
      }
   }

   public void initPortletPreferencesDB(NewPortalConfig config) throws Exception
   {
      for (String owner : config.getPredefinedOwner())
      {
         if (!config.getOwnerType().equals(PortalConfig.USER_TYPE))
         {
            createPortletPreferences(config, owner);
         }
      }
   }

   public void createPortalConfig(NewPortalConfig config, String owner) throws Exception
   {
      try
      {
         String type = config.getOwnerType();
         PortalConfig pconfig = getConfig(config, owner, type, PortalConfig.class);

         if (pconfig == null)
         {
            // Ensure that the PortalConfig has been defined
            // The PortalConfig could be empty if the related PortalConfigListener
            // has been launched after starting this service
            PortalConfig cfg = dataStorage_.getPortalConfig(type, owner);
            if (cfg == null)
            {
               cfg = new PortalConfig(type);
               cfg.setPortalLayout(new Container());
               cfg.setName(owner);
               dataStorage_.create(cfg);
            }
            return;
         }

         // We use that owner value because it may have been fixed for group names
         owner = pconfig.getName();

         //
         PortalConfig currentPortalConfig = dataStorage_.getPortalConfig(type, owner);
         if (currentPortalConfig == null)
         {
            dataStorage_.create(pconfig);
         }
         else
         {
            dataStorage_.save(pconfig);
         }
      }
      catch (IOException e)
      {
         log.error("Could not load portal configuration", e);
      }
   }

   public void createPage(NewPortalConfig config, String owner) throws Exception
   {
      PageSet pageSet = getConfig(config, owner, "pages", PageSet.class);
      if (pageSet == null)
      {
         return;
      }
      ArrayList<Page> list = pageSet.getPages();
      for (Page page : list)
      {
         dataStorage_.create(page);
      }
   }

   public void createPageNavigation(NewPortalConfig config, String owner) throws Exception
   {
      PageNavigation navigation = getConfig(config, owner, "navigation", PageNavigation.class);
      if (navigation == null)
      {
         return;
      }
      PageNavigation currentNavigation = dataStorage_.getPageNavigation(navigation.getOwner());
      if (currentNavigation == null)
      {
         dataStorage_.create(navigation);
      }
      else
      {
         if(overrideExistingData)
         {
            dataStorage_.remove(currentNavigation);
            dataStorage_.create(navigation);
         }
         else
         {
            navigation.merge(currentNavigation);
            dataStorage_.save(navigation);
         }
      }
   }

   public void createPortletPreferences(NewPortalConfig config, String owner) throws Exception
   {
      PortletPreferencesSet portletSet = getConfig(config, owner, "portlet-preferences", PortletPreferencesSet.class);
      if (portletSet == null)
      {
         return;
      }
      ArrayList<PortletPreferences> list = portletSet.getPortlets();
      for (PortletPreferences portlet : list)
      {
         dataStorage_.save(portlet);
      }
   }

   private final Pattern OWNER_PATTERN = Pattern.compile("@owner@");

   /**
    * Best effort to load and unmarshall a configuration.
    *
    * @param config the config object
    * @param owner the owner
    * @param fileName the file name
    * @param type the type to unmarshall to
    * @return the xml of the config or null
    * @throws Exception any exception
    * @param <T> the generic type to unmarshall to
    */
   private <T> T getConfig(NewPortalConfig config, String owner, String fileName, Class<T> type) throws Exception
   {
      log.debug("About to load config=" + config + " owner=" + owner + " fileName=" + fileName);

      //
      String ownerType = config.getOwnerType();

      // Get XML
      String path = "/" + ownerType + "/" + owner + "/" + fileName + ".xml";
      String xml = getDefaultConfig(config.getTemplateLocation(), path);

      //
      if (xml == null)
      {
         boolean isTemplate = (config.getTemplateName() != null && config.getTemplateName().trim().length() > 0);
         if (isTemplate)
         {
            path = "/" + ownerType + "/template/" + config.getTemplateName() + "/" + fileName + ".xml";
            xml = getDefaultConfig(config.getTemplateLocation(), path);
            if (xml != null)
            {
               xml = OWNER_PATTERN.matcher(xml).replaceAll(owner);
            }
         }
      }

      //
      if (xml != null)
      {
         boolean ok = false;
         try
         {
            final T t = fromXML(config.getOwnerType(), owner, xml, type);
            ok = true;
            return t;
         }
         catch (JiBXException e)
         {
            log.error(e.getMessage() + " file: " + path, e);
            throw e;
         }
         finally
         {
            if (!ok)
            {
               log.error("Could not load file: " + path);
            }
         }
      }

      //
      return null;
   }

   private String getDefaultConfig(String location, String path) throws Exception
   {
      String s = location + path;
      String content = null;
      try
      {
         log.debug("Attempt to load file " + s);
         content = IOUtil.getStreamContentAsString(cmanager_.getInputStream(s));
         log.debug("Loaded file from path " + s + " with content " + content);
      }
      catch (Exception ignore)
      {
         log.debug("Could not get file " + s + " will return null instead");
      }
      return content;
   }

   public Page createPageFromTemplate(String ownerType, String owner, String temp) throws Exception
   {
      String path = pageTemplatesLocation_ + "/" + temp + "/page.xml";
      InputStream is = cmanager_.getInputStream(path);
      String xml = IOUtil.getStreamContentAsString(is);
      return fromXML(ownerType, owner, xml, Page.class);
   }

   public String getTemplateConfig(String type, String name)
   {
      for (SiteConfigTemplates tempConfig : templateConfigs)
      {
         Set<String> templates = tempConfig.getTemplates(type);
         if (templates != null && templates.contains(name))
            return tempConfig.getLocation();
      }
      return null;
   }

   // Deserializing code

   private <T> T fromXML(String ownerType, String owner, String xml, Class<T> clazz) throws Exception
   {
      ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(clazz);
      UnmarshallingContext uctx = (UnmarshallingContext)bfact.createUnmarshallingContext();
      uctx.setDocument(is, null, "UTF-8", false);
      T o = clazz.cast(uctx.unmarshalElement());
      if (o instanceof PageNavigation)
      {
         PageNavigation nav = (PageNavigation)o;
         nav.setOwnerType(ownerType);
         nav.setOwnerId(owner);
         fixOwnerName((PageNavigation)o);
      }
      else if (o instanceof PortalConfig)
      {
         PortalConfig portalConfig = (PortalConfig)o;
         portalConfig.setType(ownerType);
         portalConfig.setName(owner);
         fixOwnerName(portalConfig);
      }
      else if (o instanceof PortletPreferencesSet)
      {
         for (PortletPreferences portlet : ((PortletPreferencesSet)o).getPortlets())
         {
            fixOwnerName(portlet);
         }
      }
      else if (o instanceof PageSet)
      {
         for (Page page : ((PageSet)o).getPages())
         {
            page.setOwnerType(ownerType);
            page.setOwnerId(owner);
            fixOwnerName(page);
            // The page will be created in the calling method
            //        pdcService_.create(page);
         }
      }
      return o;
   }

   private static String fixOwnerName(String type, String owner)
   {
      if (type.equals(PortalConfig.GROUP_TYPE) && !owner.startsWith("/"))
      {
         return "/" + owner;
      }
      else
      {
         return owner;
      }
   }

   public static String fixInstanceIdOwnerName(String persistenceId)
   {
      int pos1 = persistenceId.indexOf("#");
      if (pos1 != -1)
      {
         String type = persistenceId.substring(0, pos1);
         int pos2 = persistenceId.indexOf(":", pos1 + 1);
         if (pos2 != -1)
         {
            String owner = persistenceId.substring(pos1 + 1, pos2);
            String windowId = persistenceId.substring(pos2 + 1);
            owner = fixOwnerName(type, owner);
            return type + "#" + owner + ":" + windowId;
         }
      }
      return persistenceId;
   }

   private static void fixOwnerName(PortalConfig config)
   {
      config.setName(fixOwnerName(config.getType(), config.getName()));
      fixOwnerName(config.getPortalLayout());
   }

   private static void fixOwnerName(Container container)
   {
      for (Object o : container.getChildren())
      {
         if (o instanceof Container)
         {
            fixOwnerName((Container)o);
         }
      }
   }

   private static void fixOwnerName(PageNavigation pageNav)
   {
      pageNav.setOwnerId(fixOwnerName(pageNav.getOwnerType(), pageNav.getOwnerId()));
      for (PageNode pageNode : pageNav.getNodes())
      {
         fixOwnerName(pageNode);
      }
   }

   private static void fixOwnerName(PageNode pageNode)
   {
      if (pageNode.getPageReference() != null)
      {
         String pageRef = pageNode.getPageReference();
         int pos1 = pageRef.indexOf("::");
         int pos2 = pageRef.indexOf("::", pos1 + 2);
         String type = pageRef.substring(0, pos1);
         String owner = pageRef.substring(pos1 + 2, pos2);
         String name = pageRef.substring(pos2 + 2);
         owner = fixOwnerName(type, owner);
         pageRef = type + "::" + owner + "::" + name;
         pageNode.setPageReference(pageRef);
      }
      if (pageNode.getChildren() != null)
      {
         for (PageNode childPageNode : pageNode.getChildren())
         {
            fixOwnerName(childPageNode);
         }
      }
   }

   private static void fixOwnerName(PortletPreferences prefs)
   {
      prefs.setWindowId(fixInstanceIdOwnerName(prefs.getWindowId()));
   }

   private static void fixOwnerName(Page page)
   {
      page.setOwnerId(fixOwnerName(page.getOwnerType(), page.getOwnerId()));
      fixOwnerName((Container)page);
   }
}
