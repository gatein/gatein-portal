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

import org.exoplatform.commons.serialization.MarshalledObject;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.pom.config.POMSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache
{

   /** . */
   private final ConcurrentHashMap<MarshalledObject<CacheKey>, MarshalledObject<CacheValue>> map;

   public SimpleDataCache()
   {
      this.map = new ConcurrentHashMap<MarshalledObject<CacheKey>, MarshalledObject<CacheValue>>();
   }

   @Override
   protected Described.State getState(POMSession session, CacheKey key)
   {
      MarshalledObject<CacheKey> marshalledKey = MarshalledObject.marshall(key);
      MarshalledObject<CacheValue> marshalledValue = map.get(marshalledKey);
      if (marshalledValue == null)
      {
         CacheValue value = getValue(session, key);
         if (value != null)
         {
            map.put(marshalledKey, MarshalledObject.marshall(value));
            return value.state;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return marshalledValue.unmarshall().state;
      }
   }

   @Override
   protected void removeState(CacheKey key)
   {
      map.remove(MarshalledObject.marshall(key));
   }

   @Override
   protected void putValue(CacheKey key, CacheValue value)
   {
      map.put(MarshalledObject.marshall(key), MarshalledObject.marshall(value));
   }
}
