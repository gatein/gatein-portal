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

import sun.io.ByteToCharConverter;

import java.io.CharConversionException;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Sep 16, 2006
 */
@SuppressWarnings("deprecation")
class ConverterSD extends StringDecoder
{

   private ByteToCharConverter btc;

   ConverterSD(ByteToCharConverter btc, String rcn)
   {
      super(rcn);
      this.btc = btc;
   }

   String charsetName()
   {
      return btc.getCharacterEncoding();
   }

   char[] decode(byte[] ba, int off, int len)
   {
      int en = scale(len, btc.getMaxCharsPerByte());
      char[] ca = new char[en];
      if (len == 0)
         return ca;
      btc.reset();
      int n = 0;
      try
      {
         n = btc.convert(ba, off, off + len, ca, 0, en);
         n += btc.flush(ca, btc.nextCharIndex(), en);
      }
      catch (CharConversionException x)
      {
         n = btc.nextCharIndex();
      }
      return trim(ca, n);
   }
}
