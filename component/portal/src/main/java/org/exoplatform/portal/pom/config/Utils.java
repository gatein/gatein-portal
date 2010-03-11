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

package org.exoplatform.portal.pom.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Utils
{
   public final static String USER_NAME_VALIDATOR_REGEX = "^[\\p{L}][\\p{L}._\\-\\d]+$";

   public final static String FIRST_CHARACTER_NAME_VALIDATOR_REGEX = "^[\\p{L}][\\p{L}._'\\- \\d]+$";


   /**
    * todo: move to common module
    *
    * @param separator
    * @param strings
    * @return
    */
   public static String join(String separator, String... strings)
   {
      if (strings == null)
      {
         return null;
      }
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < strings.length; i++)
      {
         Object o = strings[i];
         if (i > 0)
         {
            sb.append(separator);
         }
         sb.append(o);
      }
      return sb.toString();
   }

   public static String join(String separator, List<String> strings)
   {
      if (strings == null)
      {
         return null;
      }
      String[] array = strings.toArray(new String[strings.size()]);
      return join(separator, array);
   }

   /**
    * todo: move to common module
    *
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

   public static <E> List<E> safeImmutableList(E... list)
   {
      if (list == null || list.length == 0)
      {
         return Collections.emptyList();
      }
      else if (list.length == 1)
      {
         E e = list[0];
         return Collections.singletonList(e);
      }
      else
      {
         List<E> copy = Arrays.asList(list);
         return Collections.unmodifiableList(copy);
      }
   }

   public static <E> List<E> safeImmutableList(List<E> list)
   {
      if (list == null || list.size() == 0)
      {
         return Collections.emptyList();
      }
      else if (list.size() == 1)
      {
         E e = list.get(0);
         return Collections.singletonList(e);
      }
      else
      {
         ArrayList<E> copy = new ArrayList<E>(list);
         return Collections.unmodifiableList(copy);
      }
   }

   public static <K, V> Map<K, V> safeImmutableMap(Map<K, V> map)
   {
      if (map == null || map.size() == 0)
      {
         return Collections.emptyMap();
      }
      else if (map.size() == 1)
      {
         Map.Entry<K, V> entry = map.entrySet().iterator().next();
         return Collections.singletonMap(entry.getKey(), entry.getValue());
      }
      else
      {
         Map<K, V> copy = new HashMap<K, V>(map);
         return Collections.unmodifiableMap(copy);
      }
   }

   /**
    * Splits a string according to a string separator.
    * <p/>
    * The provided index defines the beginning of the splitted chunks in the returned array. The values from the
    * beginning up to the value index - 1 will be null values.
    *
    * @param separator the string separator
    * @param index     the index to which the chunks begin
    * @param s         the string to split
    * @return an array containing the splitted chunks plus extra leading pad
    */
   public static String[] split(String separator, int index, String s)
   {
      return split(s, 0, index, separator);
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

   /**
    * Escape the provided string for being usable as a query litteral.
    *
    * @param s the string to escpae
    * @return the escaped result
    */
   public static String queryEscape(String s)
   {
      return s.replaceAll("[\\\\%_'\"]", "\\\\$0");
   }
}
