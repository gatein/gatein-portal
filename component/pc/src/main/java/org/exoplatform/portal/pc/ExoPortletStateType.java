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

package org.exoplatform.portal.pc;

import org.gatein.pc.api.PortletStateType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoPortletStateType extends PortletStateType<ExoPortletState>
{

   /** . */
   private static final ExoPortletStateType instance = new ExoPortletStateType();

   public static ExoPortletStateType getInstance()
   {
      return instance;
   }

   public Class<ExoPortletState> getJavaType()
   {
      return ExoPortletState.class;
   }

   public boolean equals(ExoPortletState state1, ExoPortletState state2)
   {
      return state1.equals(state2);
   }

   public int hashCode(ExoPortletState state)
   {
      return state.hashCode();
   }

   public String toString(ExoPortletState state)
   {
      return state.toString();
   }
}
