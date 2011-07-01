/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.tree.diff;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class HierarchyContext<L, N, H> {

   /** . */
   final ListAdapter<L, H> listAdapter;

   /** . */
   final HierarchyAdapter<L, N, H> hierarchyAdapter;

   /** . */
   final N root;

   public HierarchyContext(ListAdapter<L, H> listAdapter, HierarchyAdapter<L, N, H> hierarchyAdapter, N root) throws NullPointerException {
      if (listAdapter == null) {
         throw new NullPointerException();
      }
      if (hierarchyAdapter == null) {
         throw new NullPointerException();
      }
      if (root == null) {
         throw new NullPointerException();
      }

      //
      this.listAdapter = listAdapter;
      this.hierarchyAdapter = hierarchyAdapter;
      this.root = root;
   }

   public HierarchyAdapter<L, N, H> getHierarchyAdapter() {
      return hierarchyAdapter;
   }

   public N getRoot() {
      return root;
   }

   public N findByHandle(H handle) {
      return hierarchyAdapter.getDescendant(root, handle);
   }
}
