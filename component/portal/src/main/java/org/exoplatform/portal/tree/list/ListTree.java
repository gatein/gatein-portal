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

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * <p>A tree structure where the children in which the children are organized as a linked list. The children of a
 * tree is a linked list and can be iterated with an iterator or thanks to the {@link #getFirst()}, {@link #getLast()},
 * {@link #getNext()} and {@link #getPrevious()} methods.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <T> the self bounding tree type
 */
public class ListTree<T extends ListTree<T>>
{

   /** . */
   private T parent;

   /** . */
   private T next;

   /** . */
   private T previous;

   /** . */
   private T head;

   /** . */
   private T tail;

   /** . */
   private int size;

   public ListTree()
   {
      this.next = null;
      this.previous = null;
      this.head = null;
      this.tail = null;
      this.size = 0;
   }

   /**
    * Returns the tree.
    *
    * @return the tree
    */
   public final T getNext()
   {
      return next;
   }

   /**
    * Returns the previous.
    *
    * @return the previous
    */
   public final T getPrevious()
   {
      return previous;
   }

   /**
    * Returns the parent.
    *
    * @return the parent
    */
   public final T getParent()
   {
      return parent;
   }

   /**
    * Returns the size.
    *
    * @return the size
    */
   public final int getSize()
   {
      return size;
   }

   /**
    * Returns the first tree.
    *
    * @return the first tree
    */
   public final T getFirst()
   {
      return head;
   }

   /**
    * Returns the last tree.
    *
    * @return the last tree
    */
   public final T getLast()
   {
      return tail;
   }

   /**
    * Returns a tree specified by its index.
    *
    * @param index the index
    * @return the corresponding tree
    * @throws IndexOutOfBoundsException if the index is incorrect
    */
   public final T get(int index) throws IndexOutOfBoundsException
   {
      if (index < 0)
      {
         throw new IndexOutOfBoundsException("No negative index allowed");
      }

      //
      T current = head;
      while (true)
      {
         if (current == null)
         {
            throw new IndexOutOfBoundsException("index " + index + " is greater than the children size");
         }
         if (index == 0)
         {
            break;
         }
         else
         {
            current = current.next;
            index--;
         }
      }
      return current;
   }

   /**
    * Insert the specified tree.
    *
    * @param index the index
    * @param tree the tree
    * @throws NullPointerException if the context is null
    * @throws IllegalArgumentException if an existing child with the same name already exist
    * @throws IndexOutOfBoundsException if the index is negative or is greater than the children size
    */
   public final void insertAt(Integer index, T tree) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException
   {
      if (tree == null)
      {
         throw new NullPointerException("No null tree accepted");
      }
      if (index != null && index < 0)
      {
         throw new IndexOutOfBoundsException("No negative index permitted");
      }

      //
      if (index != null)
      {
         T a = head;
         if (index == 0)
         {
            insertFirst(tree);
         }
         else
         {
            while (index > 0)
            {
               if (a == null)
               {
                  throw new IndexOutOfBoundsException();
               }
               index--;
               a = a.next;
            }

            //
            if (a == null)
            {
               insertLast(tree);
            }
            else if (a != tree)
            {
               a.insertBefore(tree);
            }
         }
      }
      else
      {
         T a = tail;
         if (a == null)
         {
            insertFirst(tree);
         }
         else if (a != tree)
         {
            a.insertAfter(tree);
         }
      }
   }

   /**
    * Insert the specified context at the last position among the children of this context.
    *
    * @param tree the content to insert
    * @throws NullPointerException if the tree argument is null
    */
   public final void insertLast(T tree)
   {
      if (tail == null)
      {
         insertFirst(tree);
      }
      else
      {
         tail.insertAfter(tree);
      }
   }

   /**
    * Insert the specified context at the first position among the children of this context.
    *
    * @param tree the content to insert
    * @throws NullPointerException if the tree argument is null
    */
   public void insertFirst(T tree) throws NullPointerException
   {
      if (tree == null)
      {
         throw new NullPointerException();
      }
      if (head == null)
      {
         beforeInsert(tree);
         if (tree.parent != null)
         {
            tree.remove();
         }
         head = tail = tree;
         tree.parent = (T)this;
         size++;
         afterInsert(tree);
      }
      else
      {
         head.insertBefore(tree);
      }
   }

   /**
    * Insert the specified tree after this tree
    *
    * @param tree the tree to insert after
    * @throws NullPointerException if the specified tree argument is null
    * @throws IllegalStateException if this tree does not have a parent
    */
   public final void insertAfter(T tree)
   {
      if (tree == null)
      {
         throw new NullPointerException("No null tree argument accepted");
      }
      if (parent == null)
      {
         throw new IllegalStateException();
      }
      if (this != tree)
      {
         parent.beforeInsert(tree);
         if (tree.parent != null)
         {
            tree.remove();
         }
         tree.previous = (T)this;
         tree.next = next;
         if (next == null)
         {
            parent.tail = tree;
         }
         else
         {
            next.previous = tree;
         }
         next = tree;
         tree.parent = parent;
         parent.size++;
         parent.afterInsert(tree);
      }
   }

   /**
    * Insert the specified tree before this tree
    *
    * @param tree the tree to insert before
    * @throws NullPointerException if the specified tree argument is null
    * @throws IllegalStateException if this tree does not have a parent
    */
   public final void insertBefore(T tree) throws NullPointerException, IllegalStateException
   {
      if (tree == null)
      {
         throw new NullPointerException("No null tree argument accepted");
      }
      if (parent == null)
      {
         throw new IllegalStateException();
      }
      if (this != tree)
      {
         parent.beforeInsert(tree);
         if (tree.parent != null)
         {
            tree.remove();
         }
         tree.previous = previous;
         tree.next = (T)this;
         if (previous == null)
         {
            parent.head = tree;
         }
         else
         {
            previous.next = tree;
         }
         previous = tree;
         tree.parent = parent;
         parent.size++;
         parent.afterInsert(tree);
      }
   }

   /**
    * Removes this tree from its parent
    *
    * @throws IllegalStateException if this tree does not have a parent
    */
   public final void remove() throws IllegalStateException
   {
      if (parent == null)
      {
         throw new IllegalStateException();
      }
      parent.beforeRemove((T)this);
      if (previous == null)
      {
         parent.head = next;
      }
      else
      {
         previous.next = next;
      }
      if (next == null)
      {
         parent.tail = previous;
      }
      else
      {
         next.previous = previous;
      }
      T _parent = parent;
      parent = null;
      previous = null;
      next = null;
      _parent.size--;
      _parent.afterRemove((T)this);
   }

   public final ListIterator<T> listIterator()
   {
/*
      if (map == null)
      {
         return null;
      }
*/
      return new ListIterator<T>()
      {
         T next = head;
         T current = null;
         T previous = null;
         int index = 0;

         public boolean hasNext()
         {
            return next != null;
         }

         public T next()
         {
            if (next != null)
            {
               current = next;

               //
               previous = next;
               next = next.next;
               index++;
               return current;
            }
            else
            {
               throw new NoSuchElementException();
            }
         }

         public boolean hasPrevious()
         {
            return previous != null;
         }

         public T previous()
         {
            if (previous != null)
            {
               current = previous;

               //
               next = previous;
               previous = previous.previous;
               index--;
               return current;
            }
            else
            {
               throw new NoSuchElementException();
            }
         }

         public int nextIndex()
         {
            return index;
         }

         public int previousIndex()
         {
            return index - 1;
         }

         public void remove()
         {
            if (current == null)
            {
               throw new IllegalStateException("no element to remove");
            }
            if (current == previous)
            {
               index--;
            }
            next = current.next;
            previous = current.previous;
            current.remove();
            current = null;
         }

         public void set(T tree)
         {
            throw new UnsupportedOperationException();
         }

         public void add(T tree)
         {
            if (previous == null)
            {
               insertFirst(tree);
            }
            else
            {
               previous.insertAfter(tree);
            }
            index++;
            previous = tree;
            next = tree.next;
         }
      };
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void beforeInsert(T tree)
   {
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void afterInsert(T tree)
   {
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void beforeRemove(T tree)
   {
   }

   /**
    * Callback to signal insertion occured.
    *
    * @param tree the child inserted
    */
   protected void afterRemove(T tree)
   {
   }
}

