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
package org.exoplatform.portal.mop.i18n;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.gatein.common.util.ConversionException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 15, 2011
 */

/**
 * The entry point for carrying the information
 * and can be attached to mop entities
 */
@MixinType(name = "gtn:i18nized")
public abstract class I18Nized
{

   private static Locale parent(Locale locale)
   {
      if (locale.getVariant() != null && !locale.getVariant().isEmpty())
      {
         return new Locale(locale.getLanguage(), locale.getCountry());
      }
      else if (locale.getCountry() != null && !locale.getCountry().isEmpty())
      {
         return new Locale(locale.getLanguage());
      }
      else
      {
         return null;
      }
   }

   @Create
   public abstract LanguageSpace createLanguageSpace();
   
   @OneToOne
   @Owner
   @MappedBy("gtn:languages")
   public abstract LanguageSpace getLanguageSpace();
   
   public abstract void setLanguageSpace(LanguageSpace languageSpace);

   public <M> Resolution<M> resolveMixin(Class<M> mixinType, Locale wantedLocale)
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (wantedLocale == null)
      {
         throw new NullPointerException("No null wanted locale accepted");
      }
      for (Locale current = wantedLocale;current != null;current = parent(current))
      {
         M mixin = getMixin(mixinType, current, false);
         if (mixin != null)
         {
            return new Resolution<M>(current, mixin);
         }
      }
      return null;
   }

   public <M> Map<Locale, M> getMixins(Class<M> mixinType)
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      Map<Locale, M> mixins = new HashMap<Locale, M>();
      LanguageSpace languageSpace = getLanguageSpace();
      if (languageSpace != null)
      {
         for (Map.Entry<String, Language> entry : languageSpace.getChildren().entrySet())
         {
            M mixin = entry.getValue().getMixin(mixinType, false);
            if (mixin != null)
            {
               try
               {
                  Locale locale = I18NAdapter.parseLocale(entry.getKey());
                  mixins.put(locale, mixin);
               }
               catch (ConversionException e)
               {
                  // Handle me gracefully
                  e.printStackTrace();
               }
            }
         }
      }
      return mixins;
   }
   
   public <M> M getMixin(Class<M> mixinType, Locale locale, boolean createMixin) throws NullPointerException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      LanguageSpace languageSpace = getLanguageSpace();
      if (languageSpace == null && createMixin)
      {
         languageSpace = createLanguageSpace();
         setLanguageSpace(languageSpace);
      }
      if (languageSpace != null)
      {
         return languageSpace.getLanguage(mixinType, I18NAdapter.toString(locale), createMixin);
      }
      else
      {
         return null;
      }
   }

   public <M> Collection<Locale> removeMixin(Class<M> mixinType)
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      Collection<Locale> locales = Collections.emptyList();
      LanguageSpace languageSpace = getLanguageSpace();
      if (languageSpace != null)
      {
         for (Language language : languageSpace.getChildren().values())
         {
            if (language.removeMixin(mixinType))
            {
               try
               {
                  String lang = language.getName();
                  Locale locale = I18NAdapter.parseLocale(lang);
                  if (locales.isEmpty())
                  {
                     locales = new ArrayList<Locale>();
                  }
                  locales.add(locale);
               }
               catch (ConversionException e)
               {
                  // Handle me gracefully
                  e.printStackTrace();
               }
            }
         }
      }
      return locales;
   }
}
