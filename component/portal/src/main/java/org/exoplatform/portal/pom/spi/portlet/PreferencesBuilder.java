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

package org.exoplatform.portal.pom.spi.portlet;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PreferencesBuilder
{

   /** The state being configured. */
   private Preferences prefs;

   public PreferencesBuilder()
   {
      this.prefs = new Preferences();
   }

   /**
    * Initialize the builder with the provided preferences.
    *
    * @param that the preferences to clone
    */
   public PreferencesBuilder(Preferences that)
   {
      Preferences prefs = new Preferences();
      prefs.state.putAll(that.state);

      //
      this.prefs = prefs;
   }

   public PreferencesBuilder add(Preference preference)
   {
      if (preference == null)
      {
         throw new NullPointerException();
      }
      prefs.state.put(preference.getName(), preference);
      return this;
   }

   public PreferencesBuilder add(String name, List<String> values, boolean readOnly)
   {
      return add(new Preference(name, values, readOnly));
   }

   public PreferencesBuilder add(String name, List<String> values)
   {
      return add(new Preference(name, values, false));
   }

   public PreferencesBuilder add(String name, String value, boolean readOnly)
   {
      return add(new Preference(name, value, readOnly));
   }

   public PreferencesBuilder add(String name, String value)
   {
      return add(name, value, false);
   }

   public PreferencesBuilder remove(String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      prefs.state.remove(name);
      return this;
   }

   public PreferencesBuilder clear()
   {
      prefs.state.clear();
      return this;
   }

   public Preferences build()
   {
      Preferences tmp = prefs;
      prefs = new Preferences();
      return tmp;
   }
}
