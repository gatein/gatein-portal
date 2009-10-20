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
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Represents text that can have several internal representations in order to minimize serialization when it is possible.
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
    * @return the text object
    * @throws IllegalArgumentException if the bytes is null
    */
   public static Text create(byte[] bytes) throws IllegalArgumentException
   {
      return new Bytes(bytes);
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
    * @todo provide an optimized subclass but it's not much used for now
    * @throws IllegalArgumentException if the string is null
    */
   public static Text create(String s) throws IllegalArgumentException
   {
      return new Chars(s.toCharArray());
   }

   public abstract byte[] getBytes();

   public abstract char[] getChars();

   public abstract void appendTo(Appendable appendable) throws IOException;

   public abstract void writeTo(Writer writer) throws IOException;

   private static class Bytes extends Text
   {

      private final byte[] bytes;

      private Bytes(byte[] bytes)
      {
         this.bytes = bytes;
      }

      public byte[] getBytes()
      {
         return bytes;
      }

      public char[] getChars()
      {
         try
         {
            return new String(bytes, "utf-8").toCharArray();
         }
         catch (java.io.UnsupportedEncodingException e)
         {
            return new String(bytes).toCharArray();
         }
      }

      public void appendTo(Appendable appendable) throws IOException
      {
         for (char c : getChars())
         {
            appendable.append(c);
         }
      }

      public void writeTo(Writer writer) throws IOException
      {
         for (char c : getChars())
         {
            writer.append(c);
         }
      }
   }

   private static class Chars extends Text implements CharSequence
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

      public byte[] getBytes()
      {
         String s = new String(chars, offset, count);
         try
         {
            return s.getBytes("UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            return s.getBytes();
         }
      }

      public char[] getChars()
      {
         // Recompute the internal state
         if (offset > 0 || count < chars.length)
         {
            char[] tmp = new char[count];
            System.arraycopy(chars, offset, tmp, 0, count);
            chars = tmp;
            offset = 0;
         }
         return chars;
      }

      public void writeTo(Writer writer) throws IOException
      {
         writer.write(chars, offset, count);
      }

      public void appendTo(Appendable appendable) throws IOException
      {
         appendable.append(this);
      }

      public int length()
      {
         return count;
      }

      public char charAt(int index)
      {
         return chars[index - offset];
      }

      public CharSequence subSequence(int start, int end)
      {
         if (start < 0)
         {
            throw new ArrayIndexOutOfBoundsException("Start index cannot be negative");
         }
         if (end < 0)
         {
            throw new ArrayIndexOutOfBoundsException("End index cannot be negative");
         }
         if (start > end)
         {
            throw new ArrayIndexOutOfBoundsException("Start index cannot be greater than the end index");
         }
         if (end > count)
         {
            throw new ArrayIndexOutOfBoundsException("End index cannot be greater than the sequence length");
         }
         return new Chars(chars, offset + start, end - start);
      }

      @Override
      public String toString()
      {
         return new String(chars, offset, count);
      }
   }
}
