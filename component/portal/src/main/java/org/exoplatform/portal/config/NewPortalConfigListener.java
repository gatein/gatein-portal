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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.application.PortletPreferences.PortletPreferencesSet;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen
 * tuan08@users.sourceforge.net May 22, 2006
 */

public class NewPortalConfigListener extends BaseComponentPlugin
{

   private ConfigurationManager cmanager_;

   private DataStorage dataStorage_;

   private volatile List<?> configs;

   private PageTemplateConfig pageTemplateConfig_;

   private String defaultPortal;

   private boolean isUseTryCatch;

   private Log log = ExoLogger.getLogger("Portal:UserPortalConfigService");

   public NewPortalConfigListener(DataStorage dataStorage, ConfigurationManager cmanager, InitParams params)
      throws Exception
   {
      cmanager_ = cmanager;
      dataStorage_ = dataStorage;

      ObjectParameter objectParam = params.getObjectParam("page.templates");
      if (objectParam != null)
         pageTemplateConfig_ = (PageTemplateConfig)objectParam.getObject();

      defaultPortal = "classic";
      ValueParam valueParam = params.getValueParam("default.portal");
      if (valueParam != null)
         defaultPortal = valueParam.getValue();
      if (defaultPortal == null || defaultPortal.trim().length() == 0)
         defaultPortal = "classic";
      configs = params.getObjectParamValues(NewPortalConfig.class);

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

   }

   public void run() throws Exception
   {
      if (isInitedDB(defaultPortal))
         return;

      if (isUseTryCatch)
      {

         for (Object ele : configs)
         {
            try
            {
               NewPortalConfig portalConfig = (NewPortalConfig)ele;
               initPortletPreferencesDB(portalConfig);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (Object ele : configs)
         {
            try
            {
               NewPortalConfig portalConfig = (NewPortalConfig)ele;
               initPortalConfigDB(portalConfig);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (Object ele : configs)
         {
            try
            {
               NewPortalConfig portalConfig = (NewPortalConfig)ele;
               initPageDB(portalConfig);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (Object ele : configs)
         {
            try
            {
               NewPortalConfig portalConfig = (NewPortalConfig)ele;
               initPageNavigationDB(portalConfig);
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }
         for (Object ele : configs)
         {
            try
            {
               NewPortalConfig portalConfig = (NewPortalConfig)ele;
               portalConfig.getPredefinedOwner().clear();
            }
            catch (Exception e)
            {
               log.error("NewPortalConfig error: " + e.getMessage(), e);
            }
         }

      }
      else
      {
         for (Object ele : configs)
         {
            NewPortalConfig portalConfig = (NewPortalConfig)ele;
            initPortletPreferencesDB(portalConfig);
         }
         for (Object ele : configs)
         {
            NewPortalConfig portalConfig = (NewPortalConfig)ele;
            initPortalConfigDB(portalConfig);
         }
         for (Object ele : configs)
         {
            NewPortalConfig portalConfig = (NewPortalConfig)ele;
            initPageDB(portalConfig);
         }
         for (Object ele : configs)
         {
            NewPortalConfig portalConfig = (NewPortalConfig)ele;
            initPageNavigationDB(portalConfig);
         }
         for (Object ele : configs)
         {
            NewPortalConfig portalConfig = (NewPortalConfig)ele;
            portalConfig.getPredefinedOwner().clear();
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
    * @return the specified new portal config
    */
   NewPortalConfig getPortalConfig(String ownerType)
   {
      for (Object object : configs)
      {
         NewPortalConfig portalConfig = (NewPortalConfig)object;
         if (portalConfig.getOwnerType().equals(ownerType))
         {
            // We are defensive, we make a deep copy
            return new NewPortalConfig(portalConfig);
         }
      }
      return null;
   }

   @SuppressWarnings("unchecked")
   synchronized void addPortalConfigs(NewPortalConfigListener listener)
   {
      if (configs == null)
      {
         this.configs = listener.configs;
      }
      else if (listener.configs != null && !listener.configs.isEmpty())
      {
         List result = new ArrayList(configs);
         result.addAll(listener.configs);
         this.configs = Collections.unmodifiableList(result);
      }
   }

   private boolean isInitedDB(String portalName) throws Exception
   {
      PortalConfig pconfig = dataStorage_.getPortalConfig(portalName);
      return pconfig != null;
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

   private void createPortalConfig(NewPortalConfig config, String owner) throws Exception
   {
      String type = config.getOwnerType();

      // get path of xml file, check if path in template folder and if path not in
      // template folder
      boolean notTemplate = (config.getTemplateOwner() == null || config.getTemplateOwner().trim().length() < 1);
      String path = getPathConfig(config, owner, type, notTemplate);

      // get xml content and parse xml content
      try
      {
         String xml = getDefaultConfigIfExists(config.getTemplateLocation(), path);

         if (xml == null)
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
         if (!notTemplate)
         {
            xml = StringUtils.replace(xml, "@owner@", owner);
         }

         PortalConfig pconfig = fromXML(config.getOwnerType(), owner, xml, PortalConfig.class);

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
      catch (JiBXException e)
      {
         log.error(e.getMessage() + " file: " + path, e);
         throw e;
      }
      catch (IOException e)
      {
         log.error(e.getMessage() + " file: " + path);
      }
   }

   private void createPage(NewPortalConfig config, String owner) throws Exception
   {

      // get path of xml file, check if path in template folder and if path not in
      // template folder
      boolean notTemplate = (config.getTemplateOwner() == null || config.getTemplateOwner().trim().length() < 1);
      String path = getPathConfig(config, owner, "pages", notTemplate);

      // get xml content and parse xml content
      try
      {
         String xml = getDefaultConfig(config.getTemplateLocation(), path);
         if (xml == null)
         {
            return;
         }

         if (!notTemplate)
         {
            xml = StringUtils.replace(xml, "@owner@", owner);
         }

         PageSet pageSet = fromXML(config.getOwnerType(), owner, xml, PageSet.class);
         ArrayList<Page> list = pageSet.getPages();
         for (Page page : list)
         {
            dataStorage_.create(page);
         }
      }
      catch (JiBXException e)
      {
         log.error(e.getMessage() + " file: " + path, e);
         throw e;
      }
   }

   private void createPageNavigation(NewPortalConfig config, String owner) throws Exception
   {
      // get path of xml file, check if path in template folder and if path not in
      // template folder
      boolean notTemplate = (config.getTemplateOwner() == null || config.getTemplateOwner().trim().length() < 1);
      String path = getPathConfig(config, owner, "navigation", notTemplate);

      // get xml content and parse xml content
      try
      {
         String xml = getDefaultConfig(config.getTemplateLocation(), path);
         if (xml == null)
         {
            return;
         }

         if (!notTemplate)
         {
            xml = StringUtils.replace(xml, "@owner@", owner);
         }
         PageNavigation navigation = fromXML(config.getOwnerType(), owner, xml, PageNavigation.class);
         PageNavigation currentNavigation = dataStorage_.getPageNavigation(navigation.getOwner());
         if (currentNavigation == null)
         {
            dataStorage_.create(navigation);
         }
         else
         {
            navigation.merge(currentNavigation);
            dataStorage_.save(navigation);
         }
      }
      catch (JiBXException e)
      {
         log.error(e.getMessage() + " file: " + path, e);
         throw e;
      }
   }

   private void createPortletPreferences(NewPortalConfig config, String owner) throws Exception
   {
      // get path of xml file, check if path in template folder and if path not in
      // template folder
      boolean notTemplate = (config.getTemplateOwner() == null || config.getTemplateOwner().trim().length() < 1);
      String path = getPathConfig(config, owner, "portlet-preferences", notTemplate);

      // get xml content and parse xml content
      try
      {
         String xml = getDefaultConfig(config.getTemplateLocation(), path);
         if (xml == null)
         {
            return;
         }

         if (!notTemplate)
         {
            xml = StringUtils.replace(xml, "@owner@", owner);
         }

         PortletPreferencesSet portletSet = fromXML(config.getOwnerType(), owner, xml, PortletPreferencesSet.class);
         ArrayList<PortletPreferences> list = portletSet.getPortlets();
         for (PortletPreferences portlet : list)
         {
            dataStorage_.save(portlet);
         }
      }
      catch (JiBXException e)
      {
         log.error(e.getMessage() + " file: " + path, e);
         throw e;
      }
   }

   private String getDefaultConfigIfExists(String location, String path) throws Exception
   {
      URL url = cmanager_.getURL(location + path);
      return url == null ? null : IOUtil.getStreamContentAsString(url.openStream());
   }

   private String getDefaultConfig(String location, String path) throws Exception
   {
      String s = location + path;
      try
      {
         return IOUtil.getStreamContentAsString(cmanager_.getInputStream(s));
      }
      catch (Exception ignore)
      {
         log.debug("Could not get file " + s + " will return null instead", ignore);
         return null;
      }
   }

   private String getPathConfig(NewPortalConfig portalConfig, String owner, String dataType, boolean notTemplate)
   {
      String path = "";
      if (!notTemplate)
      {
         String ownerType = portalConfig.getOwnerType();
         path = "/" + ownerType + "/template/" + portalConfig.getTemplateOwner() + "/" + dataType + ".xml";
      }
      else
      {
         String ownerType = portalConfig.getOwnerType();
         path = "/" + ownerType + "/" + owner + "/" + dataType + ".xml";
      }
      return path;
   }

   public Page createPageFromTemplate(String ownerType, String owner, String temp) throws Exception
   {
      return fromXML(ownerType, owner, getTemplateConfig(temp, "page"), Page.class);
   }

   private String getTemplateConfig(String name, String dataType) throws Exception
   {
      String path = pageTemplateConfig_.getLocation() + "/" + name + "/" + dataType + ".xml";
      InputStream is = cmanager_.getInputStream(path);
      return IOUtil.getStreamContentAsString(is);
   }

   // Deserializing code

   private <T> T fromXML(String ownerType, String owner, String xml, Class<T> clazz) throws Exception
   {
      ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(clazz);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      T o = clazz.cast(uctx.unmarshalDocument(is, "UTF-8"));
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
