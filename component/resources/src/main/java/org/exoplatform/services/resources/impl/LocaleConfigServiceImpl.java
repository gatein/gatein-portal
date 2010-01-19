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

package org.exoplatform.services.resources.impl;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.IdentityResourceBundle;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Orientation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com This Service
 *         is used to manage the locales that the applications can handle
 */
public class LocaleConfigServiceImpl implements LocaleConfigService
{

   private static Log log = ExoLogger.getLogger(LocaleConfigServiceImpl.class);

   private LocaleConfig defaultConfig_;

   private Map<String, LocaleConfig> configs_;

   private static final Map<String, Orientation> orientations = new HashMap<String, Orientation>();

   static
   {
      orientations.put("lt", Orientation.LT);
      orientations.put("rt", Orientation.RT);
      orientations.put("tl", Orientation.TL);
      orientations.put("tr", Orientation.TR);
   }

   public LocaleConfigServiceImpl(InitParams params, ConfigurationManager cmanager) throws Exception
   {
      configs_ = new HashMap<String, LocaleConfig>(10);
      String confResource = params.getValueParam("locale.config.file").getValue();
      InputStream is = cmanager.getInputStream(confResource);
      parseConfiguration(is);
   }

   /**
    * @return Return the default LocaleConfig
    */
   public LocaleConfig getDefaultLocaleConfig()
   {
      return defaultConfig_;
   }

   /**
    * @param lang
    *           a locale language
    * @return The LocalConfig
    */
   public LocaleConfig getLocaleConfig(String lang)
   {
      LocaleConfig config = configs_.get(lang);
      if (config != null)
         return config;
      return defaultConfig_;
   }

   /**
    * @return All the LocalConfig that manage by the service
    */
   public Collection<LocaleConfig> getLocalConfigs()
   {
      return configs_.values();
   }

   private void parseConfiguration(InputStream is) throws Exception
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setIgnoringComments(true);
      factory.setCoalescing(true);
      factory.setNamespaceAware(false);
      factory.setValidating(false);
      DocumentBuilder parser = factory.newDocumentBuilder();
      Document document = parser.parse(is);
      NodeList nodes = document.getElementsByTagName("locale-config");
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         NodeList children = node.getChildNodes();
         LocaleConfig config = new LocaleConfigImpl();
         for (int j = 0; j < children.getLength(); j++)
         {
            Node element = children.item(j);
            if ("locale".equals(element.getNodeName()))
            {
               config.setLocale(element.getFirstChild().getNodeValue());
            }
            else if ("output-encoding".equals(element.getNodeName()))
            {
               config.setOutputEncoding(element.getFirstChild().getNodeValue());
            }
            else if ("input-encoding".equals(element.getNodeName()))
            {
               config.setInputEncoding(element.getFirstChild().getNodeValue());
            }
            else if ("description".equals(element.getNodeName()))
            {
               config.setDescription(element.getFirstChild().getNodeValue());
            }
            else if ("orientation".equals(element.getNodeName()))
            {
               String s = element.getFirstChild().getNodeValue();
               Orientation orientation = orientations.get(s);
               if (orientation == null)
               {
                  log.error("Wrong orientation value " + s);
               }
               else
               {
                  config.setOrientation(orientation);
               }
            }
         }

         //
         if (config.getOrientation() == null)
         {
            log.debug("No orientation found on the locale config, use the LT default");
            config.setOrientation(Orientation.LT);
         }

         //
         log.debug("Added locale config " + config + " to the set of locale configs");

         //
         String country = config.getLocale().getCountry();
         if (country != null && country.length() > 0)
         {
            configs_.put(config.getLanguage() + "_" + country, config);
         }
         else
         {
            configs_.put(config.getLanguage(), config);
         }
         if (i == 0)
            defaultConfig_ = config;
      }

      //
      if (PropertyManager.isDevelopping())
      {
         LocaleConfig magicConfig = new LocaleConfigImpl();
         magicConfig.setLocale(IdentityResourceBundle.MAGIC_LOCALE);
         magicConfig.setDescription("Magic locale");
         magicConfig.setInputEncoding("UTF-8");
         magicConfig.setOutputEncoding("UTF-8");
         magicConfig.setDescription("Default configuration for the debugging locale");
         magicConfig.setOrientation(Orientation.LT);
         configs_.put(magicConfig.getLanguage(), magicConfig);
         log.debug("Added magic locale for debugging bundle usage at runtime");
      }
   }
}
