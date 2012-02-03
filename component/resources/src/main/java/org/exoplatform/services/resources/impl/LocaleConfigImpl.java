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

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.services.resources.ResourceBundleService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class LocaleConfigImpl implements LocaleConfig
{

   static private Map<String, Locale> predefinedLocaleMap_ = null;

   static
   {
      predefinedLocaleMap_ = new HashMap<String, Locale>(10);
      predefinedLocaleMap_.put("us", Locale.US);
      predefinedLocaleMap_.put("en", Locale.ENGLISH);
      predefinedLocaleMap_.put("fr", Locale.FRENCH);
      predefinedLocaleMap_.put("zh", Locale.SIMPLIFIED_CHINESE);
   }

   private Locale locale_;

   private String outputEncoding_;

   private String inputEncoding_;

   private String description_;

   private String localeName_;

   private String tagIdentifier_;

   private Orientation orientation;

   public LocaleConfigImpl()
   {
   }

   public String getDescription()
   {
      return description_;
   }

   public void setDescription(String desc)
   {
      description_ = desc;
   }

   public String getOutputEncoding()
   {
      return outputEncoding_;
   }

   public void setOutputEncoding(String enc)
   {
      outputEncoding_ = enc;
   }

   public String getInputEncoding()
   {
      return inputEncoding_;
   }

   public void setInputEncoding(String enc)
   {
      inputEncoding_ = enc;
   }

   public Locale getLocale()
   {
      return locale_;
   }

   public void setLocale(Locale locale)
   {
      locale_ = locale;
      if (localeName_ == null)
         localeName_ = locale.getLanguage();
   }

   public void setLocale(String localeName)
   {
      localeName_ = localeName;
      locale_ = predefinedLocaleMap_.get(localeName);
      if (locale_ == null)
      {
         String[] localeParams = localeName.split("_");
         if (localeParams.length > 1)
         {
            locale_ = new Locale(localeParams[0], localeParams[1]);
         }
         else
         {
            locale_ = new Locale(localeName);
         }
         tagIdentifier_ = I18N.toTagIdentifier(locale_);
      }
   }

   public String getTagIdentifier()
   {
      return tagIdentifier_;
   }

   public String getLanguage()
   {
      return locale_.getLanguage();
   }

   public String getLocaleName()
   {
      return localeName_;
   }

   public void setLocaleName(String localeName)
   {
      localeName_ = localeName;
   }

   public ResourceBundle getResourceBundle(String name)
   {
      ResourceBundleService service =
         (ResourceBundleService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            ResourceBundleService.class);
      ResourceBundle res = service.getResourceBundle(name, locale_);
      return res;
   }

   public ResourceBundle getMergeResourceBundle(String[] names)
   {
      ResourceBundleService service =
         (ResourceBundleService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            ResourceBundleService.class);
      ResourceBundle res = service.getResourceBundle(names, locale_);
      return res;
   }

   public ResourceBundle getNavigationResourceBundle(String ownerType, String ownerId)
   {
      return getResourceBundle("locale.navigation." + ownerType + "." + ownerId.replaceAll("/", "."));
   }

   public void setInput(HttpServletRequest req) throws java.io.UnsupportedEncodingException
   {
      req.setCharacterEncoding(inputEncoding_);
   }

   public void setOutput(HttpServletResponse res)
   {
      res.setContentType("text/html; charset=" + outputEncoding_);
      res.setLocale(locale_);
   }

   public Orientation getOrientation()
   {
      return orientation;
   }

   public void setOrientation(Orientation orientation)
   {
      this.orientation = orientation;
   }

   @Override
   public String toString()
   {
      return "LocaleConfig[" + "localeName=" + localeName_ + ",locale=" + locale_ + ",description=" + description_
         + ",inputEncoding=" + inputEncoding_ + ",outputEncoding=" + outputEncoding_ + "]";
   }
}
