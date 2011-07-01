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

import java.util.Comparator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class HierarchyDiff<L1, N1, L2, N2, H> {

   public static <L1, N1, L2, N2, H> HierarchyDiff<L1, N1, L2, N2, H> create(
      ListAdapter<L1, H> listAdapter1,
      HierarchyAdapter<L1, N1, H> hierarchyAdapter1,
      ListAdapter<L2, H> listAdapter2,
      HierarchyAdapter<L2, N2, H> hierarchyAdapter2,
      Comparator<H> comparator)
   {
      return new HierarchyDiff<L1, N1, L2, N2, H>(listAdapter1, hierarchyAdapter1, listAdapter2, hierarchyAdapter2, comparator);
   }

   /** . */
   final ListAdapter<L1, H> listAdapter1;

   /** . */
   final HierarchyAdapter<L1, N1, H> hierarchyAdapter1;

   /** . */
   final ListAdapter<L2, H> listAdapter2;

   /** . */
   final HierarchyAdapter<L2, N2, H> hierarchyAdapter2;

   /** . */
   final Comparator<H> comparator;

   public HierarchyDiff(ListAdapter<L1, H> listAdapter1, HierarchyAdapter<L1, N1, H> hierarchyAdapter1, ListAdapter<L2, H> listAdapter2, HierarchyAdapter<L2, N2, H> hierarchyAdapter2, Comparator<H> comparator) {
      this.listAdapter1 = listAdapter1;
      this.hierarchyAdapter1 = hierarchyAdapter1;
      this.listAdapter2 = listAdapter2;
      this.hierarchyAdapter2 = hierarchyAdapter2;
      this.comparator = comparator;
   }

   public HierarchyChangeIterator<L1, N1, L2, N2, H> iterator(N1 node1, N2 node2) {
      return new HierarchyChangeIterator<L1, N1, L2, N2, H>(this, new HierarchyContext<L1, N1, H>(listAdapter1, hierarchyAdapter1, node1), new HierarchyContext<L2, N2, H>(listAdapter2, hierarchyAdapter2, node2));
   }
}
