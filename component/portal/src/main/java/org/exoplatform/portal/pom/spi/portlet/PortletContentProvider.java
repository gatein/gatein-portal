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

import org.exoplatform.portal.pom.spi.ContentProviderHelper;
import org.exoplatform.portal.pom.spi.HelpableContentProvider;
import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.GetState;
import org.gatein.mop.spi.content.StateContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletContentProvider implements ContentProvider<Preferences>,
   HelpableContentProvider<PortletPreferencesState, Preferences>
{

   public PortletContentProvider()
   {
   }

   public GetState<Preferences> getState(String contentId)
   {
      throw new UnsupportedOperationException();
   }

   public Preferences combine(List<Preferences> states)
   {
      Map<String, Preference> entries = new HashMap<String, Preference>();

      //
      for (Preferences preferences : states)
      {
         for (Preference preference : preferences)
         {
            Preference previous = entries.get(preference.getName());
            if (previous == null || !previous.isReadOnly())
            {
               entries.put(preference.getName(), preference);
            }
         }
      }

      //
      return new Preferences(entries);
   }

   public void setState(StateContainer container, Preferences state)
   {
      ContentProviderHelper.setState(container, state, this);
   }

   public Preferences getState(StateContainer container)
   {
      return ContentProviderHelper.getState(container, this);
   }

   public Class<Preferences> getStateType()
   {
      return Preferences.class;
   }

   public String getNodeName()
   {
      return "mop:portletpreferences";
   }

   public void setInternalState(PortletPreferencesState portletPreferencesState, Preferences preferences)
   {
      portletPreferencesState.setPayload(preferences);
   }

   public Preferences getState(PortletPreferencesState portletPreferencesState)
   {
      return (Preferences)portletPreferencesState.getPayload();
   }
}
