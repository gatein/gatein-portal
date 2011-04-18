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

import org.chromattic.api.annotations.*;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;
import org.gatein.mop.core.api.workspace.content.AbstractCustomizationState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "mop:portletpreferences")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("mop")
public abstract class PortletState extends AbstractCustomizationState
{

   /** . */
   private Portlet payload;

   @OneToMany
   public abstract Map<String, PreferenceState> getChildren();

   @Create
   public abstract PreferenceState create();

   public void setPayload(Portlet payload)
   {
      Map<String, PreferenceState> entries = getChildren();
      entries.clear();

      for (Preference pref : payload)
      {
         PreferenceState prefState = create();

         //
         entries.put(pref.getName(), prefState);

         // Compute values
         List<String> toCopyValues = pref.getValues();
         ArrayList<String> copiedValues = new ArrayList<String>(toCopyValues.size());
         for (int i = 0;i < toCopyValues.size();i++)
         {
            String value = toCopyValues.get(i);
            if (value == null)
            {
               value = "";
            }
            copiedValues.add(value);
         }

         //
         prefState.setValue(copiedValues);
         prefState.setReadOnly(pref.isReadOnly());
      }

      // Invalidate payload that will be reloaded next time
      payload = null;
   }

   public Portlet getPayload()
   {
      if (payload == null)
      {
         PortletBuilder builder = new PortletBuilder();
         for (PreferenceState entry : getChildren().values())
         {
            builder.add(entry.getName(), entry.getValues(), entry.getReadOnly());
         }
         payload = builder.build();
      }
      return payload;
   }
}
