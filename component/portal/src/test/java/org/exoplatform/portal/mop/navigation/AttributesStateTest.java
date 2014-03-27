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

package org.exoplatform.portal.mop.navigation;

import org.gatein.mop.api.Attributes;
import org.gatein.mop.core.util.SimpleAttributes;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class AttributesStateTest extends TestCase {

    public void testSyncToEmpty() {
        AttributesState src = new AttributesState.Builder()
            .attribute("k1", "v1")
            .attribute("k2", "v2")
            .build();
        assertEquals(2, src.size());
        String prefix = "my-prefix-";
        Attributes target = new SimpleAttributes();
        AttributesState.sync(src, prefix , target);

        assertEquals(2, target.getKeys().size());
        assertEquals("v1", target.getString("my-prefix-k1"));
        assertEquals("v2", target.getString("my-prefix-k2"));
    }

    public void testSyncReplaceAll() {
        AttributesState src = new AttributesState.Builder()
            .attribute("k1", "v1.1")
            .attribute("k2", "v2.1")
            .build();
        assertEquals(2, src.size());
        String prefix = "my-prefix-";
        Attributes target = new SimpleAttributes();
        target.setString("my-prefix-k1", "v1");
        target.setString("my-prefix-k2", "v2");


        AttributesState.sync(src, prefix , target);
        assertEquals(2, target.getKeys().size());
        assertEquals("v1.1", target.getString("my-prefix-k1"));
        assertEquals("v2.1", target.getString("my-prefix-k2"));
    }

    public void testSyncReplaceSome() {
        AttributesState src = new AttributesState.Builder()
            .attribute("k1", "v1.1")
            .build();
        assertEquals(1, src.size());
        String prefix = "my-prefix-";
        Attributes target = new SimpleAttributes();
        target.setString("my-prefix-k1", "v1");
        target.setString("my-prefix-k2", "v2");
        target.setString("k3", "v3");


        AttributesState.sync(src, prefix , target);
        assertEquals(2, target.getKeys().size());
        assertEquals("v1.1", target.getString("my-prefix-k1"));
        assertFalse(target.getKeys().contains("my-prefix-k2"));
        assertEquals("v3", target.getString("k3"));
    }

    public void testSyncFromEmpty() {
        AttributesState src = new AttributesState.Builder()
            .build();
        assertEquals(0, src.size());
        String prefix = "my-prefix-";
        Attributes target = new SimpleAttributes();
        target.setString("my-prefix-k1", "v1");
        target.setString("my-prefix-k2", "v2");
        target.setString("k3", "v3");

        AttributesState.sync(src, prefix , target);
        assertEquals(1, target.getKeys().size());
        assertFalse(target.getKeys().contains("my-prefix-k1"));
        assertFalse(target.getKeys().contains("my-prefix-k2"));
        assertEquals("v3", target.getString("k3"));
    }

    public void testSyncFromNull() {
        AttributesState src = null;
        String prefix = "my-prefix-";
        Attributes target = new SimpleAttributes();
        target.setString("my-prefix-k1", "v1");
        target.setString("my-prefix-k2", "v2");
        target.setString("k3", "v3");

        AttributesState.sync(src, prefix , target);
        assertEquals(1, target.getKeys().size());
        assertFalse(target.getKeys().contains("my-prefix-k1"));
        assertFalse(target.getKeys().contains("my-prefix-k2"));
        assertEquals("v3", target.getString("k3"));
    }

}
