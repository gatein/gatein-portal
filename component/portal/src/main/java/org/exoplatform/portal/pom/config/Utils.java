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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Utils
{

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

   public static String[] split(String separator, String s)
   {
      if (s == null)
      {
         return null;
      }
      return split(s, 0, 0, separator);
   }

   /**
    * Splits a string according to a string separator.
    *
    * The provided index defines the beginning of the splitted chunks in the returned array. The values
    * from the beginning up to the value index - 1 will be null values.
    *
    * @param separator the string separator
    * @param index the index to which the chunks begin
    * @param s the string to split
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
}
