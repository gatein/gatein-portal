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

package org.exoplatform.portal.mop.description;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.config.POMSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache
{

   /** . */
   private final ConcurrentHashMap<CacheKey, CacheValue> map;

   public SimpleDataCache()
   {
      this.map = new ConcurrentHashMap<CacheKey, CacheValue>();
   }

   @Override
   protected Described.State getState(POMSession session, CacheKey key)
   {
      CacheValue value = map.get(key);
      if (value == null)
      {
         value = getValue(session, key);
      }
      return value != null ? value.state : null;
   }

   @Override
   protected void removeState(CacheKey key)
   {
      map.remove(key);
   }

   @Override
   protected void putValue(CacheKey key, CacheValue value)
   {
      map.put(key, value);
   }
}
