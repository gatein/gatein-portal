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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import juzu.impl.common.Builder;
import org.gatein.portal.web.page.Decoder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DecoderTestCase {

    private void assertEquals(Map<String, String[]> expected, String test) {
        Map<String, String[]> actual = new Decoder(test).decode().getParameters();
        Assert.assertEquals(expected.keySet(), actual.keySet());
        for (Map.Entry<String, String[]> entry : expected.entrySet()) {
            String[] val = actual.get(entry.getKey());
            Assert.assertTrue("Was expecting " + Arrays.toString(val) + " to be equals to " + Arrays.toString(entry.getValue()),
                    Arrays.equals(entry.getValue(), val));
        }
    }

    @Test
    public void testEmpty() {
        Assert.assertEquals(null, new Decoder("").decode().getError());
        Assert.assertEquals(null, new Decoder("()").decode().getError());
    }

    @Test
    public void testKey() {
        assertEquals(Collections.singletonMap("a", new String[]{""}), "a");
        assertEquals(Collections.singletonMap("a", new String[]{""}), "a:");
    }

    @Test
    public void testPair() {
        assertEquals(Collections.singletonMap("a", new String[]{"b"}), "a:b");
        assertEquals(Collections.singletonMap("a", new String[]{"b","c"}), "a:b,a:c");
    }

    @Test
    public void testKeys() {
        assertEquals(Builder.map("a", new String[]{""}).map("b", new String[]{""}).build(), "a,b");
        assertEquals(Builder.map("a", new String[]{""}).map("b", new String[]{""}).build(), "a:,b");
    }

    @Test
    public void testPairs() {
        assertEquals(Builder.map("a", new String[]{"b"}).map("c", new String[]{""}).build(), "a:b,c");
    }

    @Test
    public void testEmptyPair() {
        assertEquals(Collections.<String, String[]>emptyMap(), ",");
        assertEquals(Builder.map("a", new String[]{""}).build(), ",a");
        assertEquals(Builder.map("a", new String[]{""}).build(), "a,");
        assertEquals(Builder.map("a", new String[]{""}).map("b", new String[]{""}).build(), "a,,b");
    }

    @Test
    public void testError() {
        Assert.assertEquals(Decoder.MISSING_KEY_CODE, new Decoder(":").decode().getError());
        Assert.assertEquals(Decoder.MISSING_KEY_CODE, new Decoder("a,:b").decode().getError());
    }

}
