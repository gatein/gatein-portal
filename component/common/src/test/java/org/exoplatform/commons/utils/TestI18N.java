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

import junit.framework.TestCase;

import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestI18N extends TestCase
{

   public void testToRFC1766()
   {
      assertEquals("en", I18N.toTagIdentifier(Locale.ENGLISH));
      assertEquals("en-GB", I18N.toTagIdentifier(Locale.UK));
      assertEquals("en-GB", I18N.toTagIdentifier(new Locale("en", "GB", "ab")));
      try
      {
         I18N.toTagIdentifier(null);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
   }

   public void testParseRFC1766()
   {
      assertEquals(Locale.ENGLISH, I18N.parseTagIdentifier("en"));
      assertEquals(Locale.UK, I18N.parseTagIdentifier("en-GB"));
      String[] incorrects = {"", " en", "en_GB"};
      for (String incorrect : incorrects)
      {
         try
         {
            I18N.parseTagIdentifier(incorrect);
            fail("Was expecting " + incorrect + " to not be parsed");
         }
         catch (IllegalArgumentException expected)
         {
         }
      }
      try
      {
         I18N.parseTagIdentifier(null);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
   }

   public void testParseJavaIdentifier()
   {
      assertJavaIdentifier(Locale.ENGLISH, "en");
      assertNotJavaIdentifier("");
      assertNotJavaIdentifier("e");
      assertNotJavaIdentifier("+e");
      assertNotJavaIdentifier("e+");
      assertNotJavaIdentifier("_");
      assertNotJavaIdentifier("en+");
      assertNotJavaIdentifier("en_");
      assertNotJavaIdentifier("en_G");
      assertNotJavaIdentifier("__");
      assertNotJavaIdentifier("en_+");
      assertNotJavaIdentifier("en_G+");
      assertJavaIdentifier(Locale.UK, "en_GB");
      assertNotJavaIdentifier("en__");
      assertNotJavaIdentifier("en_GB_");
      assertNotJavaIdentifier("en__+");
      assertNotJavaIdentifier("en_GB_+");
      assertNotJavaIdentifier("__f");
      assertJavaIdentifier(new Locale("en", "", "f"), "en__f");
      assertJavaIdentifier(new Locale("", "GB", "f"), "_GB_f");
   }

   public void testDefaultLocales()
   {
      for (Locale expected : Locale.getAvailableLocales())
      {
         if (isJava6Compatible(expected))
         {
            String s = expected.toString();
            Locale parsed = I18N.parseJavaIdentifier(s);
            assertEquals(expected, parsed);
         }
      }
   }

   /**
    * Tests if it is possible to construct the given locale using means
    * available in Java 6 only.
    * 
    * The given locale instance is considered Java 6 compatible if and only if
    * no script, calendar, etc. fields are set that are there in the
    * {@link Locale} class since Java 7 only. However, there are two hard-coded
    * exceptions to this rule, for which this method returns {@code true}: (i)
    * "ja_JP_JP_#u-ca-japanese" and (ii) "th_TH_TH_#u-nu-thai". These two
    * exceptions are there due to the fact that new Locale("ja", "JP",
    * "JP").toString() and new Locale("th", "TH", "TH").toString() return
    * "ja_JP_JP_#u-ca-japanese" and "th_TH_TH_#u-nu-thai" respectively in Java
    * 7; see <a
    * href="http://docs.oracle.com/javase/7/docs/api/java/util/Locale.html">Java
    * 7 Locale, Compatibility Section</a>.
    * 
    * The result is computed through comparing the expected Java 6 locale ID
    * (e.g. "en_GB") with the result of {@link Locale#toString()}. If the output
    * of of {@link Locale#toString()} matches the expected ID (i.e. something of
    * the form {language}[_[{country}][_{variant}]]) or if
    * {@link Locale#toString()} equals one of "ja_JP_JP_#u-ca-japanese" or
    * "th_TH_TH_#u-nu-thai" then {@code true} is returned. Otherwise this method
    * returns {@code false}.
    * 
    * @param locale
    * @return {@code true} or {@code false}
    */
   private static boolean isJava6Compatible(Locale locale)
   {
      StringBuilder localeIdBuilder = new StringBuilder(16);
      localeIdBuilder.append(locale.getLanguage());

      String country = locale.getCountry();
      boolean hasCountry = country != null && country.length() > 0;
      String variant = locale.getVariant();
      boolean hasVariant = variant != null && variant.length() > 0;
      if (hasCountry || hasVariant)
      {
         localeIdBuilder.append('_');
         if (hasCountry)
         {
            localeIdBuilder.append(country);
         }
         if (hasVariant)
         {
            localeIdBuilder.append('_').append(variant);
         }
      }
      String expectedJava6LocaleId = localeIdBuilder.toString();
      String foundLocaleId = locale.toString();
      if ("ja_JP_JP".equals(expectedJava6LocaleId)
            && "ja_JP_JP_#u-ca-japanese".equals(foundLocaleId))
      {
         return true;
      }
      else if ("th_TH_TH".equals(expectedJava6LocaleId)
            && "th_TH_TH_#u-nu-thai".equals(foundLocaleId))
      {
         return true;
      }
      else
      {
         return expectedJava6LocaleId.equals(foundLocaleId);
      }

   }

   private void assertNotJavaIdentifier(String s)
   {
      try
      {
         I18N.parseJavaIdentifier(s);
         fail("Was expecting " + s + " to fail");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   private void assertJavaIdentifier(Locale expected, String s)
   {
      assertEquals(expected, I18N.parseJavaIdentifier(s));
   }

   public void testGetParentLocale()
   {
      Locale l3 = new Locale("a", "b", "c");
      Locale l2 = new Locale("a", "b");
      Locale l1 = new Locale("a");
      assertEquals(l2, I18N.getParent(l3));
      assertEquals(l1, I18N.getParent(l2));
      assertEquals(null, I18N.getParent(l1));
      try
      {
         I18N.getParent(null);
         fail("Was expecting an NPE");
      }
      catch (NullPointerException ignore)
      {
      }
   }
}
