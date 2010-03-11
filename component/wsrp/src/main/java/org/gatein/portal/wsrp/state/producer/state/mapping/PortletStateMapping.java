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

package org.gatein.portal.wsrp.state.producer.state.mapping;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;
import org.chromattic.api.annotations.Property;
import org.exoplatform.commons.utils.Tools;
import org.gatein.common.util.AbstractTypedMap;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.state.AbstractPropertyMap;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.portal.wsrp.state.mapping.MappedMap;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = PortletStateMapping.NODE_NAME)
public abstract class PortletStateMapping
{
   public static final String NODE_NAME = "pc:state";

   private static final String PORTLET_ID = "pc:portletid";
   private static final String TERMINATION_TIME = "pc:terminationtime";

   private static final ObjectToStringListConverter VALUE_CONVERTER = new ObjectToStringListConverter();
   private static final MappedMap<String, List<String>> mappedMap =
      new MappedMap<String, List<String>>(MappedMap.IDENTITY_KEY_CONVERTER, VALUE_CONVERTER, PORTLET_ID, TERMINATION_TIME);

   @Property(name = PORTLET_ID)
   public abstract String getPortletID();

   public abstract void setPortletID(String portletId);

   @Properties
   public abstract Map<String, Object> getProperties();

   @Property(name = TERMINATION_TIME)
   public abstract Date getTerminationTime();

   public abstract void setTerminationTime(Date terminationTime);

   public PropertyMap getPropertiesAsPropertyMap()
   {
      Map<String, Object> map = getProperties();

      if (!map.isEmpty())
      {
         return new SimplePropertyMap(mappedMap.toExternalMap(map));
      }
      else
      {
         return new SimplePropertyMap();
      }
   }

   public void setProperties(PropertyMap props)
   {
      mappedMap.initFrom(props, getProperties());
   }

   /**
    * todo: copied from  org.exoplatform.portal.pom.config.Utils class that should really be moved to common module...
    * @param separator
    * @param strings
    * @return
    */
   public static String join(String separator, List<String> strings)
   {
      if (strings == null)
      {
         return null;
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strings.size(); i++)
      {
         Object o = strings.get(i);
         if (i > 0)
         {
            sb.append(separator);
         }
         sb.append(o);
      }
      return sb.toString();
   }

   /**
    * todo: move to common module
    * @param separator
    * @param s
    * @return
    */
   public static String[] split(String separator, String s)
   {
      if (s == null)
      {
         return null;
      }
      return split(s, 0, 0, separator);
   }

   private static String[] split(String s, int fromIndex, int index, String separator)
   {
      int toIndex = s.indexOf(separator, fromIndex);
      String[] chunks;
      if (toIndex == -1)
      {
         chunks = new String[index + 1];
         toIndex = s.length();
      }
      else
      {
         chunks = split(s, toIndex + separator.length(), index + 1, separator);
      }
      chunks[index] = s.substring(fromIndex, toIndex);
      return chunks;
   }

   private static class ObjectToStringListConverter implements MappedMap.Converter<Object, List<String>>
   {

      public List<String> fromInternal(Object o)
      {
         return Arrays.asList(split(",", (String)o));
      }

      public String toInternal(List<String> strings)
      {
         return join(",", strings);
      }
   }
}
