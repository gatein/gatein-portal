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

package org.gatein.portal.impl.mop.ram;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestContext extends TestCase {


    public void testAddChildToPersistent() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");
        curr.assertNotChildOf(root, child);
        next.assertChildOf(root, "a", child);

        //
        next.merge();
        curr.assertChildOf(root, "a", child);
        next.assertChildOf(root, "a", child);
    }

    public void testAddChildToTransient() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");
        String grandChild = next.addChild(child, "b", "");
        curr.assertNotChildOf(root, child);
        curr.assertNotChildOf(child, grandChild);
        next.assertChildOf(root, "a", child);
        next.assertChildOf(child, "b", grandChild);

        //
        next.merge();
        curr.assertChildOf(root, child);
        curr.assertChildOf(child, grandChild);
        next.assertChildOf(root, child);
        next.assertChildOf(child, grandChild);
    }

    public void testRemovePersistentChild() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");
        next.merge();

        //
        next.remove(child);
        curr.assertChildOf(root, child);
        next.assertNotChildOf(root, child);

        //
        next.merge();
        curr.assertNotChildOf(root, child);
        next.assertNotChildOf(root, child);
    }

    public void testRemoveTransientChild() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");

        //
        next.remove(child);
        curr.assertNotChildOf(root, child);
        next.assertNotChildOf(root, child);

        //
        next.merge();
        curr.assertNotChildOf(root, child);
        next.assertNotChildOf(root, child);
    }

    public void testRemovePersistentHierarchy() {

        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");
        String grandChild = next.addChild(child, "b", "");
        next.merge();

        //
        next.remove(child);
        curr.assertChildOf(root, "a", child);
        curr.assertChildOf(child, "b", grandChild);
        next.assertNotChildOf(root, child);
        next.assertNotChildOf(child, grandChild);

        //
        next.merge();
        curr.assertNotChildOf(root, child);
        curr.assertNotChildOf(child, grandChild);
        next.assertNotChildOf(root, child);
        next.assertNotChildOf(child, grandChild);
    }

    public void testRemoveTransientHierarchy() {

        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String child = next.addChild(root, "a", "");
        String grandChild = next.addChild(child, "b", "");

        //
        next.remove(child);
        curr.assertNotChildOf(root, child);
        curr.assertNotChildOf(child, grandChild);
        next.assertNotChildOf(root, child);
        next.assertNotChildOf(child, grandChild);

        //
        next.merge();
        curr.assertNotChildOf(root, child);
        curr.assertNotChildOf(child, grandChild);
        next.assertNotChildOf(root, child);
        next.assertNotChildOf(child, grandChild);
    }

    public void testDuplicatePersistentChild() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        next.addChild(root, "a", "");
        next.merge();

        //
        try {
            next.addChild(root, "a", "");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testDuplicateTransientChild() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        next.addChild(root, "a", "");

        //
        try {
            next.addChild(root, "a", "");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testAddSibling() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String c = next.addChild(root, "c", "c");
        String b = next.addSibling(a, "b", "b");
        String d = next.addSibling(c, "d", "d");
        List<String> children = next.getChildren(root);
        assertEquals(Arrays.asList(a, b, c, d), children);
    }

    public void testRename() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String b = next.addSibling(a, "b", "b");
        String c = next.addSibling(b, "c", "c");
        next.merge();

        //
        next = curr.open();
        try {
            next.rename(b, "c");
        } catch (IllegalArgumentException ignore) {
        }
        assertEquals("b", curr.getNode(b).getName());
        assertEquals("b", next.getNode(b).getName());
        next.rename(b, "d");
        assertEquals("b", curr.getNode(b).getName());
        assertEquals("d", next.getNode(b).getName());
        next.merge();

        //
        assertEquals("d", curr.getNode(b).getName());
        assertEquals("d", next.getNode(b).getName());
    }

    public void testMoveIAE() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String b = next.addSibling(a, "b", "b");
        String c = next.addChild(a, "c", "c");
        String d = next.addChild(root, "d", "d");
        next.merge();

        //
        next = curr.open();
        try {
            next.move(c, c, null);
        } catch (IllegalArgumentException expected) {
        }
        try {
            next.move(c, b, a);
        } catch (IllegalArgumentException expected) {
        }
        try {
            next.move(c, b, d);
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testMoveFirst() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String b = next.addSibling(a, "b", "b");
        String c = next.addChild(a, "c", "c");
        String d = next.addChild(b, "d", "d");
        next.merge();

        //
        next = curr.open();
        next.move(c, b, null);
        next.assertChildOf(b, "c", c);
        next.assertNotChildOf(a, c);
        Assert.assertEquals(Arrays.asList(c, d), next.getChildren(b));
        next.merge();
        curr.assertChildOf(b, "c", c);
        curr.assertNotChildOf(a, c);
        Assert.assertEquals(Arrays.asList(c, d), curr.getChildren(b));
    }

    public void testMoveAfter() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String b = next.addSibling(a, "b", "b");
        String c = next.addChild(a, "c", "c");
        String d = next.addChild(b, "d", "d");
        next.merge();

        //
        next = curr.open();
        next.move(c, b, d);
        next.assertChildOf(b, "c", c);
        next.assertNotChildOf(a, c);
        Assert.assertEquals(Arrays.asList(d, c), next.getChildren(b));
        next.merge();
        curr.assertChildOf(b, "c", c);
        curr.assertNotChildOf(a, c);
        Assert.assertEquals(Arrays.asList(d, c), curr.getChildren(b));
    }

    public void testMoveReorder() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a = next.addChild(root, "a", "a");
        String b = next.addSibling(a, "b", "b");
        String c = next.addSibling(b, "c", "c");
        next.merge();

        //
        next = curr.open();
        next.move(a, root, b);
        next.assertChildOf(root, "a", a);
        Assert.assertEquals(Arrays.asList(b, a, c), next.getChildren(root));
        next.merge();
        curr.assertChildOf(root, "a", a);
        Assert.assertEquals(Arrays.asList(b, a, c), curr.getChildren(root));
    }

    public void testClone() {
        TestedContext curr = new TestedContext();
        String root = curr.getRoot();

        //
        TestedContext next = curr.open();
        String a1 = next.addChild(root, "a1", "a1");
        String b1 = next.addChild(a1, "b", "b");
        String c1 = next.addChild(a1, "c", "c");
        String d1 = next.addChild(b1, "d", "d");
        next.merge();

        //
        String a2 = next.clone(a1, root, "a2");
        while (true) {
            next.assertChildOf(root, "a2", a2);
            List<String> a2Children = next.getChildren(a2);
            assertEquals(2, a2Children.size());
            String b2 = a2Children.get(0);
            Node b2Node = next.getNode(b2);
            List<String> b2Children = next.getChildren(b2);
            assertEquals("b", b2Node.getName());
            assertEquals("b", b2Node.getState());
            assertEquals(1, b2Children.size());
            String d2 = b2Children.get(0);
            Node d2Node = next.getNode(d2);
            assertEquals("d", d2Node.getName());
            assertEquals("d", d2Node.getState());
            assertEquals(Collections.<String>emptyList(), next.getChildren(d2));
            String c2 = a2Children.get(1);
            Node c2Node = next.getNode(c2);
            List<String> c2Children = next.getChildren(c2);
            assertEquals(0, c2Children.size());
            assertEquals("c", c2Node.getName());
            assertEquals("c", c2Node.getState());
            if (next != curr) {
                next.merge();
                next = curr;
            } else {
                break;
            }
        }
    }
}
