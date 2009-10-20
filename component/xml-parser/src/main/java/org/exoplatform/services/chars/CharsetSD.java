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

import sun.nio.cs.HistoricallyNamedCharset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Sep 15, 2006
 */
class CharsetSD extends StringDecoder
{

   private final Charset cs;

   private final CharsetDecoder cd;

   CharsetSD(Charset cs, String rcn)
   {
      super(rcn);
      this.cs = cs;
      this.cd =
         cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
   }

   String charsetName()
   {
      if (cs instanceof HistoricallyNamedCharset)
         return ((HistoricallyNamedCharset)cs).historicalName();
      return cs.name();
   }

   char[] decode(byte[] ba, int off, int len)
   {
      int en = scale(len, cd.maxCharsPerByte());
      char[] ca = new char[en];
      if (len == 0)
         return ca;
      cd.reset();
      ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
      CharBuffer cb = CharBuffer.wrap(ca);
      try
      {
         CoderResult cr = cd.decode(bb, cb, true);
         if (!cr.isUnderflow())
            cr.throwException();
         cr = cd.flush(cb);
         if (!cr.isUnderflow())
            cr.throwException();
      }
      catch (CharacterCodingException x)
      {
         throw new Error(x);
      }
      return trim(ca, cb.position());
   }

}
