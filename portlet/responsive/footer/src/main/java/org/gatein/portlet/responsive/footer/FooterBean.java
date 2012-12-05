/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.footer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class FooterBean
{
   LocaleConfigService localeService;
   ResourceBundleService resourceBundleService;
   
   
   public FooterBean()
   {
      localeService = (LocaleConfigService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LocaleConfigService.class);
      resourceBundleService = (ResourceBundleService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ResourceBundleService.class);
   }
   
   public Locale getCurrentLocale()
   {
      return Util.getPortalRequestContext().getLocale();
   }
   
   public List<Locale> getLanguages()
   {
      Locale currentLocale = Util.getPortalRequestContext().getLocale();
      
      Collection<LocaleConfig> localeConfigs = localeService.getLocalConfigs();
      
      List<Locale> locales = new ArrayList<Locale>();
      for (LocaleConfig localeConfig: localeConfigs)
      {
         locales.add(localeConfig.getLocale());
      }
      
      Collections.sort(locales, new LocaleComparator());
      return locales;
   }

   private class LocaleComparator implements Comparator<Locale>
   {

      @Override
      public int compare(Locale firstLocale, Locale secondLocale)
      {
         return (firstLocale.getDisplayName(getCurrentLocale()).compareTo(secondLocale.getDisplayName(getCurrentLocale())));
      }
      
   }
}

