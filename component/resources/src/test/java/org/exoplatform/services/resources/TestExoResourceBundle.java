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

package org.exoplatform.services.resources;

public class TestExoResourceBundle extends AbstractResourceBundleTest
{

   public void testConstructor()
   {
      ExoResourceBundle bundle;
      bundle = new ExoResourceBundle("key1=value");
      assertEquals(1, bundle.getContents().length);
      assertEquals("value", bundle.getString("key1"));
      bundle = new ExoResourceBundle("key1=value\nkey2=value");
      assertEquals(2, bundle.getContents().length);
      assertEquals("value", bundle.getString("key1"));
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle("key1=value\r\nkey2=value");
      assertEquals(2, bundle.getContents().length);
      assertEquals("value", bundle.getString("key1"));
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle("#comment\r\nkey2=value");
      assertEquals(1, bundle.getContents().length);
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle("  #comment\r\nkey2=value");
      assertEquals(1, bundle.getContents().length);
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle("  bad entry\r\nkey2=value");
      assertEquals(1, bundle.getContents().length);
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle("#key1 =value\r\nkey2=value");
      assertEquals(1, bundle.getContents().length);
      assertEquals("value", bundle.getString("key2"));
      bundle = new ExoResourceBundle(" key1 =value\r\n key2 =value");
      assertEquals(2, bundle.getContents().length);
      assertEquals("value", bundle.getString(" key1 "));
      assertEquals("value", bundle.getString(" key2 "));
   }

   public void testUnicode2Char()
   {
      for (int i = 0; i < (1 << 16); i++)
      {
         String value = Integer.toHexString(i);
         while (value.length() < 4)
         {
            value = "0" + value;
         }
         assertEquals((char)i, ResourceBundleData.unicode2Char("\\u" + value));
      }
   }

   public void testConvert()
   {
      assertEquals("Normal Value", ResourceBundleData.convert("Normal Value"));
      assertEquals("\u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", ResourceBundleData
         .convert("\\u00E9\\u00E7\\u00E0\\u00F9\\u0194\\u0BF5"));
      assertEquals("before \u00E9\u00E7\u00E0\u00F9\u0194\u0BF5", ResourceBundleData
         .convert("before \\u00E9\\u00E7\\u00E0\\u00F9\\u0194\\u0BF5"));
      assertEquals("\u00E9\u00E7\u00E0\u00F9\u0194\u0BF5 after", ResourceBundleData
         .convert("\\u00E9\\u00E7\\u00E0\\u00F9\\u0194\\u0BF5 after"));
      assertEquals("before \u00E9\u00E7\u00E0 between \u00F9\u0194\u0BF5 after", ResourceBundleData
         .convert("before \\u00E9\\u00E7\\u00E0 between \\u00F9\\u0194\\u0BF5 after"));
   }
}
