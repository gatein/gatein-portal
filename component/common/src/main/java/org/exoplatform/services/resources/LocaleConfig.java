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

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * May 3, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: LocaleConfig.java 5799 2006-05-28 17:55:42Z geaz $
 **/
public interface LocaleConfig
{

   public String getDescription();

   public void setDescription(String desc);

   public String getOutputEncoding();

   public void setOutputEncoding(String enc);

   public String getInputEncoding();

   public void setInputEncoding(String enc);

   public Locale getLocale();

   public void setLocale(Locale locale);

   public void setLocale(String localeName);

   public String getLanguage();

   public String getLocaleName();

   public ResourceBundle getResourceBundle(String name);

   public ResourceBundle getMergeResourceBundle(String[] names);

   public ResourceBundle getNavigationResourceBundle(String ownerType, String ownerId);

   public void setInput(HttpServletRequest req) throws java.io.UnsupportedEncodingException;

   public void setOutput(HttpServletResponse res);

   /**
    * Returns the orientation of the locale config.
    *
    * @return the orientation
    */
   public Orientation getOrientation();

   /**
    * Updates the orientation of the locale config.
    *
    * @param orientation the new orientation
    */
   public void setOrientation(Orientation orientation);

}
