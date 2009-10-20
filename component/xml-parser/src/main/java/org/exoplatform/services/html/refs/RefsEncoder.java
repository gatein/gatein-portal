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

/**
 * Created by  eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 8, 2006
 */
public class RefsEncoder
{

   private boolean hexadecimal = false;

   public RefsEncoder(boolean hexadecimalV)
   {
      hexadecimal = hexadecimalV;
   }

   public char[] encode(char[] chars)
   {
      CharRefs charRefs = EncodeService.ENCODE_CHARS_REF.getRef();
      if (!charRefs.isSorted())
         charRefs.sort(EncodeService.comparator);
      CharsSequence refValue = new CharsSequence(chars.length * 6);
      char c;
      CharRef ref;
      int i = 0;
      while (i < chars.length)
      {
         c = chars[i];
         ref = charRefs.searchByValue(c, EncodeService.comparator);
         if (ref != null)
         {
            refValue.append('&');
            refValue.append(ref.getName());
            refValue.append(';');
         }
         else if (!(c < 0x007F))
         {
            refValue.append("&#");
            if (hexadecimal)
            {
               refValue.append('x');
               refValue.append(Integer.toHexString(c));
            }
            else
               refValue.append(String.valueOf((int)c));
            refValue.append(';');
         }
         else
            refValue.append(String.valueOf((int)c));
         i++;
      }
      return refValue.getValues();
   }

}
