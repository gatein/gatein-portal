/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.exoplatform.portal.application.localization;

import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.LocalePolicy;
import org.picocontainer.Startable;

import java.util.List;
import java.util.Locale;

/**
 * This service represents a default policy for determining LocaleConfig to be used for user's session.
 * This service is registered through portal services configuration file: conf/portal/configuration.xml
 * Custom locale determination policy can be implemented by overriding or completely replacing this class,
 * and registering an alternative implementation.
 *
 * @see NoBrowserLocalePolicyService
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class DefaultLocalePolicyService implements LocalePolicy, Startable
{
   /**
    * @see LocalePolicy#determineLocale(LocaleContextInfo)
    */
   public Locale determineLocale(LocaleContextInfo context)
   {
      Locale locale = null;
      if (context.getRemoteUser() == null)
         locale = getLocaleConfigForAnonymous(context);
      else
         locale = getLocaleConfigForRegistered(context);

      if (locale == null)
         locale = context.getPortalLocale();

      return locale;
   }

   /**
    * Override this method to change the LocaleConfig determination for registered users.
    * Default is: use user's profile language, if not available fall back to LOCALE cookie,
    * and finally if that is not available either fall back to browser language preference.
    *
    * @param context locale context info available to implementations in order to determine appropriate Locale
    * @return Locale representing a language to use, or null
    */
   protected Locale getLocaleConfigForRegistered(LocaleContextInfo context)
   {
      Locale locale = context.getLocaleIfSupported(context.getUserProfileLocale());
      if (locale == null)
         locale = getLocaleConfigFromCookie(context);
      if (locale == null)
         locale = getLocaleConfigFromBrowser(context);

      return locale;
   }

   /**
    * Override this method to change the Locale determination based on browser language preferences.
    * If you want to disable the use of browser language preferences simply return null.
    *
    * @param context locale context info available to implementations in order to determine appropriate Locale
    * @return Locale representing a language to use, or null
    */
   protected Locale getLocaleConfigFromBrowser(LocaleContextInfo context)
   {
      List<Locale> locales = context.getBrowserLocales();
      for (Locale loc: locales)
         return context.getLocaleIfSupported(loc);

      return null;
   }

   /**
    * Override this method to change Locale determination for users that aren't logged in.
    * By default the request's LOCALE cookie is used, if that is not available the browser
    * language preferences are used.
    *
    * @param context locale context info available to implementations in order to determine appropriate Locale
    * @return Locale representing a language to use, or null
    */
   protected Locale getLocaleConfigForAnonymous(LocaleContextInfo context)
   {
      Locale locale = getLocaleConfigFromCookie(context);
      if (locale == null)
         locale = getLocaleConfigFromBrowser(context);

      return locale;
   }

   /**
    * Override this method to change the Locale determination based on browser cookie.
    * If you want to disable the use of browser cookies simply return null.
    *
    * @param context locale context info available to implementations in order to determine appropriate Locale
    * @return Locale representing a language to use, or null
    */
   protected Locale getLocaleConfigFromCookie(LocaleContextInfo context)
   {
      List<Locale> locales = context.getCookieLocales();
      for (Locale locale: locales)
         return context.getLocaleIfSupported(locale);

      return null;
   }

   /**
    * Starter interface method
    */
   public void start()
   {
   }

   /**
    * Starter interface method
    */
   public void stop()
   {
   }

}
