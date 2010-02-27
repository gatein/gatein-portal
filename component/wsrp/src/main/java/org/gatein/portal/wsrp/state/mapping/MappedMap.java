/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.mapping;

import org.exoplatform.commons.utils.Tools;
import org.gatein.common.util.AbstractTypedMap;
import org.gatein.common.util.ParameterValidation;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MappedMap<Key, Value>
{
   /** Need to ignore JCR properties for now until scoping mechanism exists on @Properties */
   private static final Set<String> jcrBlacklistedPropertyKeys = Tools.set("jcr:uuid", "jcr:primaryType");
   private Set<String> blacklistedPropertyKeys;
   private Converter<String, Key> keyConverter;
   private Converter<Object, Value> valueConverter;

   public static final Converter<String, String> IDENTITY_KEY_CONVERTER = new Converter<String, String>()
   {

      public String fromInternal(String s)
      {
         return s;
      }

      public String toInternal(String s)
      {
         return s;
      }
   };

   public MappedMap(Converter<String, Key> keyConverter, Converter<Object, Value> valueConverter, String... blacklistedPropertyNames)
   {
      this.keyConverter = keyConverter;
      this.valueConverter = valueConverter;

      int blacklistedNumber = blacklistedPropertyNames.length;
      if(blacklistedNumber > 0)
      {
         blacklistedPropertyKeys = new HashSet<String>(jcrBlacklistedPropertyKeys);
         blacklistedPropertyKeys.addAll(Arrays.asList(blacklistedPropertyNames));
      }
   }

   public Map<Key, Value> toExternalMap(Map<String, Object> internalMap)
   {
      if (!internalMap.isEmpty())
      {
         Map<Key, Value> externalMap = new HashMap<Key, Value>(internalMap.size());
         for (Map.Entry<String, Object> entry : internalMap.entrySet())
         {
            String key = entry.getKey();

            // ignore blacklisted properties
            if (!blacklistedPropertyKeys.contains(key))
            {
               externalMap.put(keyConverter.fromInternal(key), valueConverter.fromInternal(entry.getValue()));
            }
         }

         return externalMap;
      }
      else
      {
         return Collections.emptyMap();
      }
   }

   public void initFrom(Map<Key, Value> externalMap, Map<String, Object> internalMap)
   {
      if (ParameterValidation.existsAndIsNotEmpty(externalMap))
      {
         for (Map.Entry<Key, Value> entry : externalMap.entrySet())
         {
            internalMap.put(keyConverter.toInternal(entry.getKey()), valueConverter.toInternal(entry.getValue()));
         }
      }
   }

   public static interface Converter<Internal, External>
   {
      External fromInternal(Internal internal);
      Internal toInternal(External external);
   }
}
