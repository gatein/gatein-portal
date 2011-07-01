/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.user;

import org.exoplatform.portal.mop.SiteKey;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A simple implementation for unit tests.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class SimpleUserPortalContext implements UserPortalContext
{

   /** . */
   private final Map<SiteKey, ResourceBundle> bundles;

   /** . */
   private final Locale locale;

   public SimpleUserPortalContext(Locale locale)
   {
      this.locale = locale;
      this.bundles = new HashMap<SiteKey, ResourceBundle>();
   }

   void add(SiteKey key, ResourceBundle bundle)
   {
      bundles.put(key, bundle);
   }

   public ResourceBundle getBundle(UserNavigation navigation)
   {
      return bundles.get(navigation.getKey());
   }

   public Locale getUserLocale()
   {
      return locale;
   }
}
