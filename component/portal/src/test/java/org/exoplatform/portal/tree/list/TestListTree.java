/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.tree.list;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestListTree extends TestCase
{

   public static class IntegerTree extends ListTree<IntegerTree>
   {

      /** . */
      private final int value;

      public IntegerTree(int value)
      {
         this.value = value;
      }
   }

   private static IntegerTree tree(String name, int value, IntegerTree... trees)
   {
      IntegerTree tree = new IntegerTree(value);
      if (trees != null)
      {
         for (IntegerTree child : trees)
         {
            tree.insertAt(null, child);
         }
      }
      return tree;
   }

   private void assertChildren(IntegerTree tree, Integer... expected)
   {
      List<Integer> children = new ArrayList<Integer>();
      for (Iterator<IntegerTree> iterator = tree.listIterator();iterator.hasNext();)
      {
         IntegerTree next = iterator.next();
         children.add(next.value);
      }
      assertEquals(Arrays.asList(expected), children);
   }

   private void assertAllChildren(IntegerTree tree, Integer... expected)
   {
      List<Integer> children = new ArrayList<Integer>();
      for (IntegerTree current = tree.getFirst();current != null;current = current.getNext())
      {
         children.add(current.value);
      }
      assertEquals(Arrays.asList(expected), children);
   }

   private void assertAllChildren(IntegerTree tree)
   {
      assertAllChildren(tree, new Integer[0]);
   }

   public void testInsert1()
   {
      IntegerTree root = tree("", 0);
      assertChildren(root);
      assertAllChildren(root);

      //
      root = tree("", 0);
      root.insertAt(0, tree("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);

      //
      root = tree("", 0);
      root.insertAt(null, tree("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);
   }

/*
   public void testInsertDuplicate()
   {
      IntegerTree root = tree("", 0, tree("a", 1));
      assertChildren(root, 1);
      assertAllChildren(root, 1);

      //
      try
      {
         root.insertAt(0, tree("a", 2));
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
         assertAllChildren(root, 1);
      }
   }
*/

   public void testInsertMove1()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree b = tree("b", 2);
      IntegerTree root1 = tree("", 0, a, b);

      //
      root1.insertAt(0, b);
      assertAllChildren(root1, 2, 1);
   }

   public void testInsertMove2()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree root1 = tree("", 0, a);

      //
      root1.insertAt(null, a);
      assertAllChildren(root1, 1);

      //
      root1.insertAt(0, a);
      assertAllChildren(root1, 1);
   }

   public void testInsertMove3()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree root1 = tree("", 0, a);
      IntegerTree root2 = tree("", 0);

      //
      root2.insertAt(0, a);
      assertAllChildren(root1);
      assertAllChildren(root2, 1);
      assertSame(root2, a.getParent());
   }

   public void testInsertReorder1()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree root1 = tree("", 0, a);

      //
      root1.insertAt(0, a);
      assertAllChildren(root1, 1);
      assertSame(root1, a.getParent());
   }

   public void testInsertReorder2()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree root1 = tree("", 0, a, tree("b", 2));

      //
      root1.insertAt(2, a);
      assertAllChildren(root1, 2, 1);
      assertSame(root1, a.getParent());

      //
      root1.insertAt(0, a);
      assertAllChildren(root1, 1, 2);
      assertSame(root1, a.getParent());
   }

   public void testRemove()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2), tree("c", 3));
      assertAllChildren(root, 1, 2, 3);

      //
      IntegerTree b = root.get(1);
      b.remove();
      assertNull(b.getParent());
      assertNull(b.getPrevious());
      assertNull(b.getNext());
      assertEquals(2, b.value);
      assertAllChildren(root, 1, 3);
   }

   public void testRemoveLast()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2));
      assertAllChildren(root, 1, 2);

      //
      IntegerTree b = root.get(1);
      assertEquals(2, b.value);
      b.remove();
      assertAllChildren(root, 1);
      assertEquals(1, root.getLast().value);
   }

/*
   public void testRename()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2), tree("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root.rename("a", "a");
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      root.rename("a", "d");
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "d", "b", "c");
   }

   public void testRenameWithNoChildren()
   {
      IntegerTree root = tree("", 0, (IntegerTree[]) null);
      assertFalse(root.hasTrees());

      //
      try
      {
         root.rename("a", "b");
         fail();
      }
      catch (IllegalStateException e)
      {
         assertFalse(root.hasTrees());
      }
   }

   public void testRenameWithNonExisting()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2), tree("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      try
      {
         root.rename("d", "e");
         fail();
      }
      catch (IllegalArgumentException e)
      {
         assertAllChildren(root, 1, 2, 3);
         assertAllChildren(root, "a", "b", "c");
      }
   }

   public void testRenameWithExisting()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2), tree("c", 3));
      assertAllChildren(root, 1, 2, 3);
      assertAllChildren(root, "a", "b", "c");

      //
      try
      {
         root.rename("a", "c");
         fail();
      }
      catch (IllegalArgumentException e)
      {
         assertAllChildren(root, 1, 2, 3);
         assertAllChildren(root, "a", "b", "c");
      }
   }
*/

/*
   public void testGetByIndexWithNoChildren()
   {
      IntegerTree root = tree("", 0, (IntegerTree[]) null);

      //
      try
      {
         root.get(0);
         fail();
      }
      catch (IllegalStateException e)
      {
      }
   }
*/

   public void testIteratorRemove()
   {
      IntegerTree root = tree("", 0, tree("a", 1));
      Iterator<IntegerTree> it = root.listIterator();

      //
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
      }

      //
      IntegerTree a = it.next();
      it.remove();
      assertNull(a.getParent());
      assertFalse(it.hasNext());
      assertAllChildren(root);
   }

   public void testListIterator1()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree root = tree("", 0, a);

      //
      ListIterator<IntegerTree> i = root.listIterator();
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());

      //
      assertSame(a, i.next());
      assertFalse(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());

      //
      assertSame(a, i.previous());
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());
   }

   public void testListIterator2()
   {
      IntegerTree a = tree("a", 1);
      IntegerTree b = tree("b", 2);
      IntegerTree root = tree("", 0, a, b);

      //
      ListIterator<IntegerTree> i = root.listIterator();
      assertTrue(i.hasNext());
      assertEquals(0, i.nextIndex());
      assertFalse(i.hasPrevious());
      assertEquals(-1, i.previousIndex());
      assertSame(a, i.next());
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(b, i.next());
      assertFalse(i.hasNext());
      assertEquals(2, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(1, i.previousIndex());
      i.remove();
      assertFalse(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
   }

   public void testListIterator3()
   {
      // Remove middle
      IntegerTree a = tree("a", 1);
      IntegerTree b = tree("b", 2);
      IntegerTree c = tree("c", 3);
      IntegerTree root = tree("", 0, a, b, c);
      ListIterator<IntegerTree> i = root.listIterator();
      i.next();
      i.next();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(c, i.next());

      // Remove middle
      root = tree("", 0, a = tree("a", 1), b = tree("b", 2), c = tree("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.next();
      i.previous();
      i.previous();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(c, i.next());

      // Remove middle
      root = tree("", 0, a = tree("a", 1), b = tree("b", 2), c = tree("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(a, i.previous());

      // Remove middle
      root = tree("", 0, a = tree("a", 1), b = tree("b", 2), c = tree("c", 3));
      i = root.listIterator();
      i.next();
      i.next();
      i.next();
      i.previous();
      i.previous();
      i.remove();
      assertTrue(i.hasNext());
      assertEquals(1, i.nextIndex());
      assertTrue(i.hasPrevious());
      assertEquals(0, i.previousIndex());
      assertSame(a, i.previous());
   }

   @SuppressWarnings("unchecked")
   public void testListIteratorNavigation()
   {
      IntegerTree root = tree("", 0, tree("1", 1), tree("2", 2), tree("3", 3), tree("4", 4), tree("5", 5));
      ListIterator<IntegerTree> it = root.listIterator();
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
      assertEquals(1, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(1, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
      assertEquals(1, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(2, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(2, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(2, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(3, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertEquals(4, it.next().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(3, it.previousIndex());
      assertEquals(4, it.nextIndex());
      assertEquals(5, it.next().value);
      assertTrue(!it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(4, it.previousIndex());
      assertEquals(5, it.nextIndex());
      assertEquals(5, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(3, it.previousIndex());
      assertEquals(4, it.nextIndex());
      assertEquals(4, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertEquals(3, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(2, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(it.hasPrevious());
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertEquals(1, it.previous().value);
      assertTrue(it.hasNext());
      assertTrue(!it.hasPrevious());
      assertEquals(-1, it.previousIndex());
      assertEquals(0, it.nextIndex());
   }

   /*
      @Override
      @SuppressWarnings("unchecked")
      public void testListIteratorSet() {
          list.add((E) "1");
          list.add((E) "2");
          list.add((E) "3");
          list.add((E) "4");
          list.add((E) "5");

          ListIterator<E> it = list.listIterator();
          assertEquals("1", it.next());
          it.set((E) "a");
          assertEquals("a", it.previous());
          it.set((E) "A");
          assertEquals("A", it.next());
          assertEquals("2", it.next());
          it.set((E) "B");
          assertEquals("3", it.next());
          assertEquals("4", it.next());
          it.set((E) "D");
          assertEquals("5", it.next());
          it.set((E) "E");
          assertEquals("[A, B, 3, D, E]", list.toString());
      }
   */

   public void testListIteratorRemove()
   {
      IntegerTree root = tree("", 0, tree("1", 1), tree("2", 2), tree("3", 3), tree("4", 4), tree("5", 5));
      ListIterator<IntegerTree> it = root.listIterator();
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
         // expected
      }
      assertEquals(1, it.next().value);
      assertEquals(2, it.next().value);
      assertAllChildren(root, 1, 2, 3, 4, 5);
      it.remove();
      assertAllChildren(root, 1, 3, 4, 5);
      assertEquals(3, it.next().value);
      assertEquals(3, it.previous().value);
      assertEquals(1, it.previous().value);
      it.remove();
      assertAllChildren(root, 3, 4, 5);
      assertTrue(!it.hasPrevious());
      assertEquals(3, it.next().value);
      it.remove();
      assertAllChildren(root, 4, 5);
      try
      {
         it.remove();
         fail();
      }
      catch (IllegalStateException e)
      {
         // expected
      }
      assertEquals(4, it.next().value);
      assertEquals(5, it.next().value);
      it.remove();
      assertAllChildren(root, 4);
      assertEquals(4, it.previous().value);
      it.remove();
      assertAllChildren(root);
   }

   public void testListIteratorAdd()
   {
      IntegerTree root = tree("", 0);
      ListIterator<IntegerTree> it = root.listIterator();
      it.add(tree("a", 1));
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      assertAllChildren(root, 1);
      it.add(tree("c", 3));
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertAllChildren(root, 1, 3);
      it.add(tree("e", 5));
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertAllChildren(root, 1, 3, 5);
      assertEquals(5, it.previous().value);
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      it.add(tree("d", 4));
      assertEquals(2, it.previousIndex());
      assertEquals(3, it.nextIndex());
      assertAllChildren(root, 1, 3, 4, 5);
      assertEquals(4, it.previous().value);
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertEquals(3, it.previous().value);
      assertEquals(0, it.previousIndex());
      assertEquals(1, it.nextIndex());
      it.add(tree("b", 2));
      assertEquals(1, it.previousIndex());
      assertEquals(2, it.nextIndex());
      assertAllChildren(root, 1, 2, 3, 4, 5);
   }

   public void testListIteratorMove()
   {
      IntegerTree root = tree("", 0, tree("a", 1), tree("b", 2), tree("c", 3));
      ListIterator<IntegerTree> it = root.listIterator();
      it.add(root.get(2));
      assertAllChildren(root, 3, 1, 2);
   }

   public void testInsertFirstThrowsNPE()
   {
      IntegerTree a = tree("a", 0);
      try
      {
         a.insertFirst(null);
         fail();
      }
      catch (NullPointerException ignore)
      {
      }
   }

   public void testInsertLastThrowsNPE()
   {
      IntegerTree a = tree("a", 0);
      try
      {
         a.insertLast(null);
         fail();
      }
      catch (NullPointerException ignore)
      {
      }
   }

   public void testInsertBeforeThrowsNPE()
   {
      IntegerTree a = tree("a", 0);
      try
      {
         a.insertBefore(null);
         fail();
      }
      catch (NullPointerException ignore)
      {
      }
   }

   public void testInsertBeforeThrowsISE()
   {
      IntegerTree a = tree("a", 0);
      IntegerTree b = tree("b", 1);
      try
      {
         a.insertBefore(b);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   public void testInsertAfterThrowsNPE()
   {
      IntegerTree a = tree("a", 0);
      try
      {
         a.insertAfter(null);
         fail();
      }
      catch (NullPointerException ignore)
      {
      }
   }

   public void testInsertAfterThrowsISE()
   {
      IntegerTree a = tree("a", 0);
      IntegerTree b = tree("b", 1);
      try
      {
         a.insertAfter(b);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   public void testRemoveThrowsISE()
   {
      IntegerTree a = tree("a", 0);
      try
      {
         a.remove();
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }
}