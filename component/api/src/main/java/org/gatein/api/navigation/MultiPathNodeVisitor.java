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

package org.gatein.api.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class MultiPathNodeVisitor implements NodeVisitor {
    private List<NodePath> paths = new ArrayList<NodePath>();

    public void add(NodePath path) {
        if (paths.isEmpty()) {
            paths.add(path);
            return;
        }

        // Add only distinct paths, replace any parent paths.
        boolean add = false;
        for (ListIterator<NodePath> itr = paths.listIterator(); itr.hasNext();) {
            NodePath np = itr.next();
            if (np.isParent(path)) {
                itr.set(path);
                return;
            } else if (path.isParent(np)) {
                add = false;
                break;
            } else {
                add = true;
            }
        }

        if (add)
            paths.add(path);
    }

    @Override
    public boolean visit(int depth, String name, NodeDetails details) {
        for (NodePath path : paths) {
            if (depth == 0)
                return true;

            if (depth < path.size()) {
                if (path.getSegment(depth - 1).equals(name)) {
                    return true;
                }
            } else if (depth == path.size()) {
                if (path.getSegment(depth - 1).equals(name)) {
                    return true;
                }
            }
        }

        return false;
    }
}
