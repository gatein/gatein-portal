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

package org.exoplatform.commons.utils;

import java.nio.charset.Charset;

/**
 * A char encoder that use a table to cache the result produced by a delegate char encoder. This encoder
 * is stateless and should only be composed with stateless char encoder otherwise an unexpected result
 * may happen.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TableCharEncoder implements CharEncoder
{

   private static final char MAX = (char)0x10FFFD;

   private byte[][] table;

   private final CharEncoder charEncoder;

   /**
    * Creates a new table based char encoder.
    *
    * @param charEncoder the delegate char encoder
    */
   public TableCharEncoder(CharEncoder charEncoder)
   {
      this.charEncoder = charEncoder;
      this.table = new byte[MAX + 1][];
   }

   public Charset getCharset()
   {
      return charEncoder.getCharset();
   }

   public byte[] encode(char c)
   {
      byte[] bytes = table[c];
      if (bytes == null)
      {
         bytes = charEncoder.encode(c);
         table[c] = bytes;
      }
      return bytes;
   }
}
