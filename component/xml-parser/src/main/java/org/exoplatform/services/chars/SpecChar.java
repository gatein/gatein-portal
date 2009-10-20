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

package org.exoplatform.services.chars;

/**
 *
 * @author nhuthuan
 * Email: nhudinhthuan@yahoo.com
 */
public class SpecChar
{

   public static char s = ' ';

   public static char t = '\t';

   public static char n = '\n';

   public static char b = '\b';

   public static char f = '\f';

   public static char r = '\r';

   public static char END_TAG = '/', OPEN_TAG = '<', CLOSE_TAG = '>';

   public static char HYPHEN = '-', QUESTION_MASK = '?', PUNCTUATION_MASK = '!';

   public static int findWhiteSpace(String value, int start)
   {
      for (int i = start; i < value.length(); i++)
      {
         if (Character.isWhitespace(value.charAt(i)))
            return i;
      }
      return value.length();
   }
}
