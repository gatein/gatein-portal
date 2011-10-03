/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.gatein.common.io.WriterCharWriter;
import org.gatein.common.text.CharWriter;
import org.gatein.common.text.EncodingException;
import org.gatein.common.text.EntityEncoder;
import org.gatein.common.util.ParameterValidation;

import java.io.StringWriter;
import java.io.Writer;

/**
 * This encoder provides a few methods to encode the String to its HTML entity representation.
 * 
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class HTMLEntityEncoder extends EntityEncoder
{
   private static volatile HTMLEntityEncoder singletonInstance;

   public static HTMLEntityEncoder getInstance()
   {
      if (singletonInstance == null)
      {
         synchronized (HTMLEntityEncoder.class)
         {
            if (singletonInstance == null)
            {
               singletonInstance = new HTMLEntityEncoder();
            }
         }
      }
      return singletonInstance;
   }
   
   /** . */
   private final String[] hexToEntity = buildHexEntityNumberArray();

   /**
    * Character set that are immune from encoding in HTML
    */
   private static final char[] IMMUNE_HTML = { ',', '.', '-', '_', ' ' };
   
   /**
    * Character set that are immune from encoding in HTML Attribute
    */
   private static final char[] IMMUNE_HTMLATTR = { ',', '.', '-', '_' };

   /**
    * Encode data for use in HTML
    * 
    * @param input the string to encode for HTML
    * @return input encoded for HTML
    */
   public String encodeHTML(String input)
   {
      return encode(input, IMMUNE_HTML);
   }

   /**
    * Encode data for use in HTML attributes.
    * 
    * @param input the string to encode for a HTML attribute
    * @return input encoded for use as value of a HTML attribute
    */
   public String encodeHTMLAttribute(String input)
   {
      return encode(input, IMMUNE_HTMLATTR);
   }

   @Override
   public void safeEncode(char[] chars, int off, int len, CharWriter writer) throws EncodingException
   {
      safeEncode(chars, off, len, writer, IMMUNE_HTML);
   }
   
   /**
    * @param chars the array to encode
    * @param off the offset in the chars array
    * @param len the length of chars to encode
    * @param writer the writer to use
    * @param immune the characters array are immune from encoding
    * @throws EncodingException
    */
   private void safeEncode(char[] chars, int off, int len, CharWriter writer, char[] immune) throws EncodingException
   {

      // The index of the last copied char
      int previous = off;

      //
      int to = off + len;

      // Perform lookup char by char
      for (int current = off; current < to; current++)
      {
         char c = chars[current];

         // Lookup
         if (isImmutable(immune, c))
         {
            continue;
         }

         String replacement;

         String hex;

         // Do we have a replacement
         if ((replacement = lookupEntityName(c)) != null)
         {
            // We lazy create the result

            // Append the previous chars if any
            writer.append(chars, previous, current - previous);

            // Append the replaced entity
            writer.append('&').append(replacement).append(';');

            // Update the previous pointer
            previous = current + 1;
         }
         else if ((hex = lookupHexEntityNumber(c)) != null)
         {
            // We lazy create the result

            // Append the previous chars if any
            writer.append(chars, previous, current - previous);

            // Append the replaced entity
            writer.append("&#x").append(hex).append(';');

            // Update the previous pointer
            previous = current + 1;
         }
      }

      //
      writer.append(chars, previous, chars.length - previous);
   }

   public final String lookupEntityName(char c)
   {
      return lookup(c);
   }

   public final String lookupHexEntityNumber(char c)
   {
      if (c < 0xFF)
      {
         return hexToEntity[c];
      }

      return Integer.toHexString(c);
   }

   private boolean isImmutable(char[] array, char c)
   {
      for (char ch : array)
      {
         if (c == ch)
         {
            return true;
         }
      }
      return false;
   }

   private String encode(String input, char[] immutable)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(input, "String");

      Writer sw = new StringWriter();
      CharWriter charWriter = new WriterCharWriter(sw);
      safeEncode(input.toCharArray(), 0, input.length(), charWriter, immutable);
      return sw.toString();
   }

   /**
    * Build an array to store the hex string for characters to be encoded.
    * If the character shouldn't be encoded, then store null.
    * 
    * @return An array containing characters in hex string that are to be encoded.
    */
   private String[] buildHexEntityNumberArray()
   {
      String[] array = new String[256];
      
      for (char c = 0; c < 0xFF; c++)
      {
         if (c >= 0x30 && c <= 0x39 || c >= 0x41 && c <= 0x5A || c >= 0x61 && c <= 0x7A)
         {
            array[c] = null;
         }
         else
         {
            array[c] = Integer.toHexString(c);
         }
      }
      
      return array;
   }
}
