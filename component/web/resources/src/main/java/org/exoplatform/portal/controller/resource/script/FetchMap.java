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

package org.exoplatform.portal.controller.resource.script;

import java.util.HashMap;
import java.util.Map;

/**
 * <p></p>Extends an {@link HashMap} to add convenient method for safely adding a fetch mode to a map.
 * The method {@link #add(Object, FetchMode)} will add the mode only if the new mode implies the previous
 * mode in the map.</p>
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class FetchMap<E> extends HashMap<E, FetchMode>
{

   public FetchMap()
   {
   }

   public FetchMap(Map<? extends E, ? extends FetchMode> m)
   {
      super(m);
   }

   public boolean add(E element, FetchMode mode) throws NullPointerException
   {
      if (element == null)
      {
         throw new NullPointerException("No null element accepted");
      }
      
      //
      FetchMode prev = get(element);
      if (prev == null)
      {
         put(element, mode);
         return true;
      }
      else if (mode != null && mode.implies(prev))
      {
         put(element, mode);
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean add(E element) throws NullPointerException
   {
      return add(element, null);
   }
      
   public void addAll(Map<E, FetchMode> m) 
   {
      for (E elem : m.keySet())
      {
         add(elem, m.get(elem));
      }
   }
}
