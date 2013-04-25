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

package org.gatein.portal.page;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.SimpleMultiValuedPropertyMap;
import org.gatein.pc.api.spi.ClientContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class GateInClientContext implements ClientContext {

    /** . */
    private final SimpleMultiValuedPropertyMap<String> properties = new SimpleMultiValuedPropertyMap<String>();

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public MultiValuedPropertyMap<String> getProperties() {
        return properties;
    }

    @Override
    public List<Cookie> getCookies() {
        return Collections.emptyList();
    }
}
