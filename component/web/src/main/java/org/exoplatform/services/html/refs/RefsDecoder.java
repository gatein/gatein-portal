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

package org.exoplatform.services.html.refs;

import org.exoplatform.services.chars.CharsUtil;

import java.util.Comparator;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 8, 2006
 */
public class RefsDecoder
{

   private static final CharRefs charRefs = new CharRefs();

   static Comparator<CharRef> comparator = new Comparator<CharRef>()
   {
      public int compare(CharRef o1, CharRef o2)
      {
         return o1.getName().compareTo(o2.getName());
      }
   };

   public String decode(String text)
   {
      return new String(decodeChars(text.toCharArray()));
   }

   public char[] decode(char[] chars)
   {
      return decodeChars(chars);
   }

   private char[] decodeChars(char[] chars)
   {
      if (!charRefs.isSorted())
         charRefs.sort(comparator);
      int index = CharsUtil.indexOf(chars, '&', 0);
      if (index < 0 || CharsUtil.indexOf(chars, ';', index) < 0)
         return chars;
      CharsSequence decode = new CharsSequence(chars.length);
      int temp = 0;
      int end = 0;
      boolean ref = true;
      char[] ckey = null;
      while (index < chars.length)
      {
         if (temp < index)
            copy(chars, decode, temp, index);
         end = CharsUtil.indexOf(chars, ';', index);
         if (end < 0)
            break;
         if (chars[index + 1] == '#')
         {
            ckey = new char[end - index - 2];
            System.arraycopy(chars, index + 2, ckey, 0, ckey.length);
            ref = false;
         }
         else
         {
            ckey = new char[end - index - 1];
            System.arraycopy(chars, index + 1, ckey, 0, ckey.length);
            ref = true;
         }
         if (!ref)
         {
            ref = decode(ckey, decode);
         }
         else
         {
            if (Character.isLetter(ckey[0]))
            {
               CharRef item = charRefs.searchByName(new String(ckey), comparator);
               if (item != null)
               {
                  decode.append((char)item.getValue());
               }
               else
               {
                  copy(chars, decode, index, end + 1);
               }
            }
            else
            {
               copy(chars, decode, index, end);
            }
         }
         temp = end + 1;
         index = CharsUtil.indexOf(chars, '&', temp);
         if (index < 0)
            break;
      }
      copy(chars, decode, temp, chars.length);
      return decode.getValues();
   }

   private void copy(char[] chars, CharsSequence decode, int start, int end)
   {
      while (start < chars.length)
      {
         if (start == end)
            break;
         decode.append(chars[start]);
         start++;
      }
   }

   private boolean decode(char[] values, CharsSequence decode)
   {
      char character;
      int number = 0;
      int radix = 0;
      boolean done = false;
      int i = 0;
      while (i < values.length && !done)
      {
         character = values[i];
         if (Character.isDigit(character))
         {
            if (radix == 0)
               radix = 10;
            number = number * radix + (character - '0');
         }
         else
         {
            switch (character)
            {
               case 'A' :
               case 'B' :
               case 'C' :
               case 'D' :
               case 'E' :
               case 'F' :
                  if (radix == 16)
                     number = number * radix + (character - 'A' + 10);
                  else
                     done = true;
                  break;
               case 'a' :
               case 'b' :
               case 'c' :
               case 'd' :
               case 'e' :
               case 'f' :
                  if (radix == 16)
                     number = number * radix + (character - 'a' + 10);
                  else
                     done = true;
                  break;
               case 'x' :
               case 'X' :
                  if (radix == 0)
                     radix = 16;
                  else
                     done = true;
                  break;
               case ';' :
                  done = true;
                  i++;
                  break;
               default :
                  done = true;
                  break;
            }
         }
         if (!done)
            i++;
      }
      if (number == 0)
         return true;
      decode.append((char)number);
      return false;
   }
}
