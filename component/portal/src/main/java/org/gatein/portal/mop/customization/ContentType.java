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

package org.gatein.portal.mop.customization;

import java.io.Serializable;
import java.util.HashMap;

import org.exoplatform.portal.pom.spi.portlet.Portlet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ContentType<S extends Serializable> {

    /** . */
    private static final HashMap<String, ContentType<?>> REGISTRY = new HashMap<String, ContentType<?>>();

    /** . */
    public static final ContentType<Portlet> PORTLET = new ContentType<Portlet>() {
        @Override
        public String getValue() {
            return "application/portlet";
        }
    };

/*
    static {
        // Force load class for registry
        Class<?> clazz = PortletState.class;
        try {
            clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
*/

    protected ContentType() {
        REGISTRY.put(getValue(), this);
    }

    public static <S extends Serializable> ContentType<S> forValue(String mimeType) {
        return (ContentType<S>) REGISTRY.get(mimeType);
    }

    public abstract String getValue();

}
