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
import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * May 7, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ExoResourceBundle.java 9439 2006-10-12 03:28:53Z thuannd $
 **/
@SuppressWarnings("serial")
public class ExoResourceBundle extends ListResourceBundle implements Serializable
{

   /** . */
   private final ResourceBundleData data;

   public ExoResourceBundle(String data)
   {
      this.data = new ResourceBundleData(data);
   }

   public ExoResourceBundle(ResourceBundleData data)
   {
      if (data == null)
      {
         throw new NullPointerException();
      }

      //
      this.data = data;
   }

   public ExoResourceBundle(ResourceBundleData data, ResourceBundle parent)
   {
      this(data);
      setParent(parent);
   }

   public Object[][] getContents()
   {
      return data.contents;
   }

   public void putAll(Map<? super Object, ? super Object> map)
   {
      Enumeration<String> keys = getKeys();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         if (key != null)
         {
            map.put(key.trim(), getString(key));
         }
      }
   }

}
