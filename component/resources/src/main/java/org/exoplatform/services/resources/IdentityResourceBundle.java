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

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A resource bundle that returns the queried key. It returns an empty enumeration when the keys are queried.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class IdentityResourceBundle extends ResourceBundle
{

   public static final String MAGIC_LANGUAGE = "ma".intern();

   public static final Locale MAGIC_LOCALE = new Locale(MAGIC_LANGUAGE);

   private static final Vector<String> EMPTY_KEYS = new Vector<String>();

   private static final IdentityResourceBundle instance = new IdentityResourceBundle();

   public static ResourceBundle getInstance()
   {
      return instance;
   }

   protected Object handleGetObject(String key)
   {
      return key;
   }

   public Enumeration<String> getKeys()
   {
      return EMPTY_KEYS.elements();
   }

   @Override
   public Locale getLocale()
   {
      return MAGIC_LOCALE;
   }
}
