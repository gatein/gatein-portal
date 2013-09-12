/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.config.serialize;

import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class JibxArraySerializeTest extends TestCase {
    public void testSerializeStringArray() {
        assertEquals("", JibxArraySerialize.serializeStringArray(null));
        assertEquals("", JibxArraySerialize.serializeStringArray(new String[0]));
        assertEquals("", JibxArraySerialize.serializeStringArray(new String[] {""}));
        assertEquals("", JibxArraySerialize.serializeStringArray(new String[] {"", ""}));
        assertEquals("", JibxArraySerialize.serializeStringArray(new String[] {null}));
        assertEquals("", JibxArraySerialize.serializeStringArray(new String[] {null, null}));
        assertEquals("one;two;three", JibxArraySerialize.serializeStringArray(new String[] {"one", "two", "three"}));
        assertEquals("one;two;three", JibxArraySerialize.serializeStringArray(new String[] {" one", "two ", "three"}));
    }
    public void testDeserializeStringArray() {
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(null));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(""));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(" "));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(";"));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(" ; "));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializeStringArray(" ; ; "));
        assertArrayEquals(new String[] {"one", "two", "three"}, JibxArraySerialize.deserializeStringArray("one;two;three"));
        assertArrayEquals(new String[] {"one", "two", "three"}, JibxArraySerialize.deserializeStringArray(" one;two ; three"));
        assertArrayEquals(new String[] {"one two", "three"}, JibxArraySerialize.deserializeStringArray("one two;three"));
    }
    public void testSerializePermissions() {
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(null));
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(new String[0]));
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(new String[] {""}));
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(new String[] {"", ""}));
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(new String[] {null}));
        assertEquals("Nobody", JibxArraySerialize.serializePermissions(new String[] {null, null}));
        assertEquals("one;two;three", JibxArraySerialize.serializePermissions(new String[] {"one", "two", "three"}));
        assertEquals("one;two;three", JibxArraySerialize.serializePermissions(new String[] {" one", "two ", "three"}));
    }
    public void testDeserializePermissions() {
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions("Nobody"));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(" Nobody "));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(null));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(""));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(" "));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(";"));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(" ; "));
        assertArrayEquals(new String[0], JibxArraySerialize.deserializePermissions(" ; ; "));
        assertArrayEquals(new String[] {"one", "two", "three"}, JibxArraySerialize.deserializePermissions("one;two;three"));
        assertArrayEquals(new String[] {"one", "two", "three"}, JibxArraySerialize.deserializePermissions(" one;two ; three"));
        assertArrayEquals(new String[] {"one two", "three"}, JibxArraySerialize.deserializePermissions("one two;three"));
    }

}
