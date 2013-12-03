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
package org.gatein.portal.web.page;

import java.util.Collections;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.gatein.portal.web.page.Encoder;
import org.junit.Test;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class EncoderTestCase {

    @Test
    public void testPair() {
        Assert.assertEquals("foo:foo_value", new Encoder(Collections.singletonMap("foo", new String[]{"foo_value"})).encode());
        Assert.assertEquals("foo:foo_value1,foo:foo_value2", new Encoder(Collections.singletonMap("foo", new String[]{"foo_value1","foo_value2"})).encode());
    }

    @Test
    public void testPairs() {
        LinkedHashMap<String, String[]> map = new LinkedHashMap<String, String[]>();
        map.put("foo", new String[]{"foo_value"});
        map.put("bar", new String[]{"bar_value"});
        Encoder encoder = new Encoder(map);
        Assert.assertEquals("foo:foo_value,bar:bar_value", encoder.encode());
    }

    @Test
    public void testEscapeKey() {
        Encoder encoder = new Encoder(Collections.singletonMap(" ", new String[]{"value"}));
        Assert.assertEquals("%20:value", encoder.encode());
    }

    @Test
    public void testEscapeValue() {
        Encoder encoder = new Encoder(Collections.singletonMap("key", new String[]{" "}));
        Assert.assertEquals("key:%20", encoder.encode());
    }
}
