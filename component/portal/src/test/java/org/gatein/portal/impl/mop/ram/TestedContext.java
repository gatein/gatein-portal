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

import java.util.List;

import junit.framework.Assert;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestedContext extends Store {

    public TestedContext() {
    }

    public TestedContext(Store previous) {
        super(previous);
    }

    @Override
    protected Store create() {
        return new TestedContext(this);
    }

    public void assertChildOf(String parentId, String expectedName, String childId) {
        String name = assertChildOf(parentId, childId);
        Assert.assertEquals(expectedName, name);
    }

    public String assertChildOf(String parentId, String childId) {
        List<String> children = getChildren(parentId);
        Assert.assertTrue("Child " + childId + " is not in the children list " + children, children.contains(childId));
        Assert.assertEquals(parentId, getParent(childId));
        Node entry = getNode(childId);
        return entry.getName();
    }

    public void assertNotChildOf(String parentId, String childId) {
        if (contains(parentId)) {
            List<String> children = getChildren(parentId);
            Assert.assertFalse(children.contains(childId));
        }
        if (contains(childId)) {
            Assert.assertNotSame(parentId, getParent(childId));
        }
    }
}
