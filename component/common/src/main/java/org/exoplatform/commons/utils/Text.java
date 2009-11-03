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

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Represents text that can have several internal representations in order to minimize serialization when it is possible.
 * The bytes returned by the byte oriented method must returned the data encoded with the UTF-8 encoding.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Text
{

   /**
    * Create a text object from the provided byte array.
    * 
    * @param bytes the markup as bytes
    * @param charset the charset
    * @return the text object
    * @throws IllegalArgumentException if the bytes is null
    */
   public static Text create(byte[] bytes, Charset charset) throws IllegalArgumentException
   {
      return new Bytes(bytes, charset);
   }

   /**
    * Create a text object from the provided char array.
    *
    * @param chars the markup as bytes
    * @return the text object
    * @throws IllegalArgumentException if the chars is null
    */
   public static Text create(char[] chars) throws IllegalArgumentException
   {
      return new Chars(chars);
   }

   /**
    * Create a text object from the provided char array.
    *
    * @param s the markup as bytes
    * @return the text object
    * @throws IllegalArgumentException if the string is null
    */
   public static Text create(String s) throws IllegalArgumentException
   {
      return new Chars(s.toCharArray());
   }

   public abstract void writeTo(Writer writer) throws IOException;

   private static class Bytes extends Text
   {

      /** . */
      private final byte[] bytes;

      /** . */
      private final Charset charset;

      /** . */
      private volatile String s;

      private Bytes(byte[] bytes, Charset charset)
      {
         this.bytes = bytes;
         this.charset = charset;
      }

      public void writeTo(Writer writer) throws IOException
      {
         if (writer instanceof BinaryOutput)
         {
            BinaryOutput osw = (BinaryOutput)writer;
            if (charset.equals(osw.getCharset()))
            {
               osw.write(bytes);
               return;
            }
         }
         if (s == null)
         {
            s = new String(bytes, charset.name());
         }
         writer.append(s);
      }
   }

   private static class Chars extends Text
   {

      /** Inclusive from index. */
      private int offset;

      /** Count. */
      private int count;

      /** The chars. */
      private char[] chars;

      private Chars(char[] chars)
      {
         this.chars = chars;
         this.offset = 0;
         this.count = chars.length;
      }

      private Chars(char[] chars, int offset, int count)
      {
         this.chars = chars;
         this.offset = offset;
         this.count = count;
      }

      public void writeTo(Writer writer) throws IOException
      {
         writer.write(chars, offset, count);
      }

      @Override
      public String toString()
      {
         return new String(chars, offset, count);
      }
   }
}
