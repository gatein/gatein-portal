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

package org.exoplatform.services.resources.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.resources.AbstractResourceBundleTest;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;

import java.util.Collection;
import java.util.Locale;

/**
 * Thu, May 15, 2004 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestLocaleConfig.java 5799 2006-05-28 17:55:42Z geaz $
 * @email: tuan08@yahoo.com
 */
public class TestLocaleConfig extends AbstractResourceBundleTest
{

   private LocaleConfigService service_;

   public TestLocaleConfig(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      service_ = (LocaleConfigService)PortalContainer.getInstance().getComponentInstanceOfType(LocaleConfigService.class);
   }

   public void tearDown() throws Exception
   {

   }

   public void testLocaleConfigManager() throws Exception
   {
      //-------------------default locale is English-------------
      LocaleConfig locale = service_.getDefaultLocaleConfig();
      assertTrue("expect defautl locale config is found", locale != null);
      assertTrue("expect default locale is English", locale.getLocale().equals(Locale.ENGLISH));
      // --------------get a locale------------------
      locale = service_.getLocaleConfig("fr");
      assertTrue("expect locale config is found", locale != null);
      assertTrue("expect France locale is found", locale.getLocale().equals(Locale.FRENCH));

      locale = service_.getLocaleConfig("vi");
      assertTrue("expect locale config is found", locale != null);
      assertEquals("expect Viet Nam locale is found", "vi", locale.getLocale().toString());
      /*-------------------get all locale config-------------------
       * 4 preconfig locales: English, France, Arabic, Vietnamese
      **/
      Collection<LocaleConfig> locales = service_.getLocalConfigs();
      assertTrue(locales.size() == 4);

      Locale vnlocale = service_.getLocaleConfig("vi").getLocale();
      assertContains(locales, vnlocale);
      assertContains(locales, Locale.ENGLISH);
      assertContains(locales, Locale.FRENCH);
   }

   private void assertContains(Collection<LocaleConfig> configs, Locale locale)
   {
      for (LocaleConfig config : configs)
      {
         if (config.getLocale().equals(locale))
         {
            return;
         }
      }
      fail("Was expecting locale " + locale + " to be present in " + configs);
   }
}