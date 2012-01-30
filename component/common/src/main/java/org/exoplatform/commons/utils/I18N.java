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

import java.util.Locale;

/**
 * Various I18N utility methods.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class I18N
{

   /**
    * Provide a string representation of the locale argument to the {@link java.util.Locale#toString()} method.
    *
    * @param locale the locale
    * @return the java representation
    * @throws NullPointerException if the locale argument is null
    */
   public static String toJavaIdentifier(Locale locale) throws NullPointerException
   {
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      return locale.toString();
   }

   private static boolean isLetter(char c)
   {
      return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
   }

   /**
    * Parse the java string representation and returns a locale.
    * See {@link #toJavaIdentifier(java.util.Locale)} method for more details.
    *
    * @param s the java representation to parse
    * @return the corresponding locale
    * @throws NullPointerException if the locale argument is null
    * @throws IllegalArgumentException if the string cannot be parsed to a locale
    */
   public static Locale parseJavaIdentifier(String s) throws NullPointerException, IllegalArgumentException
   {
      if (s.length() == 0)
      {
         throw new IllegalArgumentException("Empty locale");
      }

      //
      char c0 = s.charAt(0);
      if (c0 == '_')
      {
         return parseCountry("", s, 0);
      }
      else if (!isLetter(c0) || s.length() < 2 || !isLetter(s.charAt(1)))
      {
         throw new IllegalArgumentException("Invalid Java Locale identifier: '" + s + "'");
      }
      else
      {
         return parseCountry(s.substring(0, 2), s, 2);
      }
   }

   private static Locale parseCountry(String lang, String s, int index) throws IllegalArgumentException
   {
      if (s.length() == index)
      {
         return new Locale(lang);
      }
      else if (s.charAt(index) != '_' || s.length() < index + 3)
      {
         throw new IllegalArgumentException();
      }
      else
      {
         char c0 = s.charAt(index + 1);
         if (c0 == '_')
         {
            if (lang.length() == 0)
            {
               throw new IllegalArgumentException();
            }
            else
            {
               return parseVariant(lang, "", s, index + 1);
            }
         }
         else if (!isLetter(c0) || !isLetter(s.charAt(index + 2)))
         {
            throw new IllegalArgumentException();
         }
         else
         {
            return parseVariant(lang, s.substring(index + 1, index + 3), s, index + 3);
         }
      }
   }

   private static Locale parseVariant(String lang, String country, String s, int index) throws IllegalArgumentException
   {
      if (s.length() == index)
      {
         return new Locale(lang, country);
      }
      else if (s.charAt(index) != '_' || s.length() < index + 2)
      {
         throw new IllegalArgumentException();
      }
      else
      {
         for (int i = index + 1;i < s.length();i++)
         {
            if (!isLetter(s.charAt(i)))
            {
               throw new IllegalArgumentException("Invalid Java Locale identifier: '" + s + "'");
            }
         }
         String variant = s.substring(index + 1);
         return new Locale(lang, country, variant);
      }
   }

   /**
    * Provide a string representation of the locale argument according to the <a href="RFC 1766">http://www.ietf.org/rfc/rfc1766.txt</a>:
    * <ul>
    *    <li>locale with a language only will return the language string</li>
    *    <li>otherwise it returns the language and country separated by an hyphen '-'</li>
    * </ul>
    *
    * @param locale the locale
    * @return the RFC1766 representation
    * @throws NullPointerException if the locale argument is null
    */
   public static String toTagIdentifier(Locale locale) throws NullPointerException
   {
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      String country = locale.getCountry();
      String lang = locale.getLanguage();
      if (country != null && country.length() > 0)
      {
         return lang + "-" + country;
      }
      else
      {
         return lang;
      }
   }

   /**
    * Parse the  <a href="RFC 1766">http://www.ietf.org/rfc/rfc1766.txt</a> string representation and returns a locale.
    * See {@link #toTagIdentifier(java.util.Locale)} method for more details.
    *
    * @param s the RFC1766 representation to parse
    * @return the corresponding locale
    * @throws NullPointerException if the locale argument is null
    * @throws IllegalArgumentException if the string cannot be parsed to a locale
    */
   public static Locale parseTagIdentifier(String s) throws NullPointerException, IllegalArgumentException
   {
      if (s == null)
      {
         throw new NullPointerException("No null string accepted");
      }
      if (s.length() == 2)
      {
         return new Locale(s.substring(0, 2));
      }
      else if (s.length() == 5 && s.charAt(2) == '-')
      {
         String lang = s.substring(0, 2);
         String country = s.substring(3, 5);
         return new Locale(lang, country);
      }
      else
      {
         throw new IllegalArgumentException("Locale " + s + " cannot be parsed");
      }
   }
}
