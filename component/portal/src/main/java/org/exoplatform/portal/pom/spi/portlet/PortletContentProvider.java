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

import org.gatein.mop.spi.content.ContentProvider;
import org.gatein.mop.spi.content.StateContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortletContentProvider implements ContentProvider<Portlet, PortletState>
{

   public PortletContentProvider()
   {
   }

   public Portlet combine(List<Portlet> states)
   {
      Map<String, Preference> entries = new HashMap<String, Preference>();

      //
      for (Portlet preferences : states)
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
      return new Portlet(entries);
   }

   public void setState(StateContainer<PortletState> container, Portlet state)
   {
      PortletState prefs = container.getState();

      //
      if (prefs != null)
      {
         if (state == null)
         {
            container.setState(null);
         }
         else
         {
            prefs.setPayload(state);
         }
      }
      else
      {
         if (state != null)
         {
            prefs = container.create();
            prefs.setPayload(state);
         }
      }
   }

   public Portlet getState(StateContainer<PortletState> container)
   {
      PortletState prefs = container.getState();
      if (prefs != null)
      {
         return prefs.getPayload();
      }
      else
      {
         return null;
      }
   }

   public Class<Portlet> getExternalType()
   {
      return Portlet.class;
   }

   public Class<PortletState> getInternalType()
   {
      return PortletState.class;
   }
}
