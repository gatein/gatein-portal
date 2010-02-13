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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by The eXo Platform SAS . Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: May 14, 2004 Time: 1:12:22 PM
 */
@SuppressWarnings("serial")
public class ResourceBundleData extends ResourceBundleDescription implements Serializable
{

   /** . */
   private static Pattern LINE_SEPARATOR = Pattern.compile("[\\r]?\\n");

   /** . */
   private static Pattern UNICODE_CHARACTER = Pattern.compile("\\\\u[\\p{XDigit}]{4}+");

   /** . */
   private String data;

   /** . */
   Object[][] contents;

   public ResourceBundleData(String data)
   {
      setData(data);
      setResourceType("-");
      setLanguage(DEFAULT_LANGUAGE);
   }

   public ResourceBundleData()
   {
      this(null);
   }

   public String getData()
   {
      return data;
   }

   public void setData(String data)
   {
      this.data = data;

      if (data != null)
      {
         String[] tokens = LINE_SEPARATOR.split(data);
         List<String[]> properties = new ArrayList<String[]>();
         for (String token : tokens)
         {
            int idx = token.indexOf('=');
            if (idx < 0 || idx >= token.length() - 1)
            {
               continue;
            }
            String key = token.substring(0, idx);
            if (key.trim().startsWith("#"))
            {
               continue;
            }
            String value = convert(token.substring(idx + 1, token.length()));
            properties.add(new String[]{key, value});
         }
         String[][] aProperties = new String[properties.size()][2];
         contents = properties.toArray(aProperties);
      }
      else
      {
         contents = new Object[0][];
      }

   }

   static String convert(String content)
   {
      Matcher matcher = UNICODE_CHARACTER.matcher(content);
      StringBuilder buffer = new StringBuilder(content.length());
      int start = 0;
      while (matcher.find(start))
      {
         buffer.append(content.substring(start, matcher.start()));
         buffer.append(unicode2Char(matcher.group()));
         start = matcher.end();
      }
      if (start >= 0 && start < content.length())
      {
         buffer.append(content.substring(start));
      }
      return buffer.toString();
   }

   static char unicode2Char(String unicodeChar)
   {
      int value = 0;
      char aChar;
      for (int i = 0; i < 4; i++)
      {
         aChar = unicodeChar.charAt(i + 2);
         switch (aChar)
         {
            case '0' :
            case '1' :
            case '2' :
            case '3' :
            case '4' :
            case '5' :
            case '6' :
            case '7' :
            case '8' :
            case '9' : {
               value = (value << 4) + aChar - '0';
               break;
            }
            case 'a' :
            case 'b' :
            case 'c' :
            case 'd' :
            case 'e' :
            case 'f' : {
               value = (value << 4) + 10 + aChar - 'a';
               break;
            }
            case 'A' :
            case 'B' :
            case 'C' :
            case 'D' :
            case 'E' :
            case 'F' : {
               value = (value << 4) + 10 + aChar - 'A';
               break;
            }
            default : {
               throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
            }
         }
      }
      return (char)value;
   }
}
