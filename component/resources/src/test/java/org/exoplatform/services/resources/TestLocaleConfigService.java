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

package org.exoplatform.services.resources;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.resources.impl.LocaleConfigImpl;
import org.exoplatform.services.resources.impl.LocaleConfigServiceImpl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestLocaleConfigService extends AbstractResourceBundleTest
{

   public TestLocaleConfigService()
   {
   }

   public TestLocaleConfigService(String s)
   {
      super(s);
   }

   public void testFoo() throws Exception
   {
      PropertyManager.setProperty(PropertyManager.DEVELOPING, "false");
      LocaleConfigService service = createService();
      Map<String, LocaleConfig> map = createMap(service);
      assertEquals(4, map.size());
      assertCommonConfigs(service);
   }

   public void testBar() throws Exception
   {
      PropertyManager.setProperty(PropertyManager.DEVELOPING, "true");
      LocaleConfigService service = createService();
      Map<String, LocaleConfig> map = createMap(service);
      assertEquals(5, map.size());
      assertCommonConfigs(service);
      LocaleConfig ma = service.getLocaleConfig("ma");
      assertLocaleConfig(ma, "ma", "Default configuration for the debugging locale", "UTF-8", "UTF-8", Orientation.LT,
         IdentityResourceBundle.MAGIC_LOCALE);
   }

   public void testLocaleConfig() throws Exception
   {
      LocaleConfig ma = new LocaleConfigImpl();
      ma.setLocale("ma");
      assertEquals("ma", ma.getLocaleName());

      LocaleConfig ma1 = new LocaleConfigImpl();
      ma1.setLocale(new Locale("ma"));
      assertEquals("ma", ma1.getLocaleName());
   }

   private Map<String, LocaleConfig> createMap(LocaleConfigService service)
   {
      Map<String, LocaleConfig> map = new HashMap<String, LocaleConfig>();
      for (LocaleConfig config : service.getLocalConfigs())
      {
         map.put(config.getLanguage(), config);
      }
      return map;
   }

   private LocaleConfigService createService() throws Exception
   {
      ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
      InitParams params = new InitParams();
      ValueParam param = new ValueParam();
      param.setName("locale.config.file");
      param.setValue("classpath:/resources/locales-config.xml");
      params.addParameter(param);

      //
      LocaleConfigService service = new LocaleConfigServiceImpl(params, cm);
      return service;
   }

   private void assertCommonConfigs(LocaleConfigService service)
   {
      LocaleConfig en = service.getLocaleConfig("en");
      LocaleConfig fr = service.getLocaleConfig("fr");
      LocaleConfig ar = service.getLocaleConfig("ar");
      LocaleConfig vi = service.getLocaleConfig("vi");
      assertLocaleConfig(en, "en", "Default configuration for english locale", "UTF-8", "UTF-8", Orientation.LT,
         Locale.ENGLISH);
      assertLocaleConfig(fr, "fr", "Default configuration for the french locale", "UTF-8", "UTF-8", Orientation.LT,
         Locale.FRENCH);
      assertLocaleConfig(ar, "ar", "Default configuration for the arabic locale", "UTF-8", "UTF-8", Orientation.RT,
         new Locale("ar"));
      assertLocaleConfig(vi, "vi", "Default configuration for the vietnam locale", "UTF-8", "UTF-8", Orientation.LT,
         new Locale("vi"));
   }

   private void assertLocaleConfig(LocaleConfig config, String language, String description, String inputEncoding,
      String outputEncoding, Orientation orientation, Locale locale)
   {
      assertNotNull(config);
      assertEquals(language, config.getLanguage());
      assertEquals(description, config.getDescription());
      assertEquals(inputEncoding, config.getInputEncoding());
      assertEquals(outputEncoding, config.getOutputEncoding());
      assertEquals(orientation, config.getOrientation());
      assertEquals(locale, config.getLocale());
   }
}
