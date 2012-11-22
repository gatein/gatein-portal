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
public interface LocaleConfig {

    String getDescription();

    void setDescription(String desc);

    String getOutputEncoding();

    void setOutputEncoding(String enc);

    String getInputEncoding();

    void setInputEncoding(String enc);

    Locale getLocale();

    void setLocale(Locale locale);

    void setLocale(String localeName);

    String getLanguage();

    String getLocaleName();

    String getTagIdentifier();

    ResourceBundle getResourceBundle(String name);

    ResourceBundle getMergeResourceBundle(String[] names);

    ResourceBundle getNavigationResourceBundle(String ownerType, String ownerId);

    void setInput(HttpServletRequest req) throws java.io.UnsupportedEncodingException;

    void setOutput(HttpServletResponse res);

    /**
     * Returns the orientation of the locale config.
     *
     * @return the orientation
     */
    Orientation getOrientation();

    /**
     * Updates the orientation of the locale config.
     *
     * @param orientation the new orientation
     */
    void setOrientation(Orientation orientation);

}
