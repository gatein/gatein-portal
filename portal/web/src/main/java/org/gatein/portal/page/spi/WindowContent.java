/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.page.spi;

import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * The relationship between a content and a window.
 *
 * @author Julien Viet
 */
public interface WindowContent {

    String getName();

    /**
     * Resolve the title for the specified locale.
     *
     * @param locale the locale
     * @return the title or null
     */
    String resolveTitle(Locale locale);

    String getParameters();

    void setParameters(String s);

    String getWindowState();

    void setWindowState(String ws);

    String getMode();

    void setMode(String m);

    Map<String, String[]> computePublicParameters(Map<QName, String[]> parameters);

    Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes);

    WindowContent copy();

}
