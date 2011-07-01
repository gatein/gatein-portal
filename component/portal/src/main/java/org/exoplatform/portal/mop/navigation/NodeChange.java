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

package org.exoplatform.portal.mop.navigation;

/**
 * Describe a change applied to a node.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class NodeChange<N>
{

   /** The target. */
   final N target;

   private NodeChange(N target)
   {
      if (target == null)
      {
         throw new NullPointerException("No null target accepted");
      }

      //
      this.target = target;
   }

   public final N getTarget()
   {
      return target;
   }

   /**
    * Dispatch the change to the proper listener method:
    * <ul>
    *    <li>{@link Created} dispatches to {@link NodeChangeListener#onCreate(Object, Object, Object, String)}</li>
    *    <li>{@link Added} dispatches to {@link NodeChangeListener#onAdd(Object, Object, Object)}</li>
    *    <li>{@link Destroyed} dispatches to {@link NodeChangeListener#onDestroy(Object, Object)}</li>
    *    <li>{@link Moved} dispatches to {@link NodeChangeListener#onMove(Object, Object, Object, Object)}</li>
    *    <li>{@link Removed} dispatches to {@link NodeChangeListener#onRemove(Object, Object)}</li>
    *    <li>{@link Renamed} dispatches to {@link NodeChangeListener#onRename(Object, Object, String)}</li>
    *    <li>{@link Updated} dispatches to {@link NodeChangeListener#onUpdate(Object, NodeState)}</li>
    * </ul>
    *
    * @param listener the listener
    */
   protected abstract void dispatch(NodeChangeListener<N> listener);

   public final static class Destroyed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      Destroyed(N parent, N node)
      {
         super(node);

         //
         if (parent == null)
         {
            throw new NullPointerException("No null parent accepted");
         }

         //
         this.parent = parent;
      }

      public N getParent()
      {
         return parent;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onDestroy(target, parent);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Destroyed[target" + target + ",parent=" +  parent + "]";
      }
   }

   public final static class Removed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      Removed(N parent, N node)
      {
         super(node);

         //
         if (parent == null)
         {
            throw new NullPointerException("No null parent accepted");
         }

         //
         this.parent = parent;
      }

      public N getParent()
      {
         return parent;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onRemove(target, parent);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Removed[target" + target + ",parent=" +  parent + "]";
      }
   }

   public final static class Created<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N previous;

      /** . */
      final String name;

      Created(N parent, N previous, N node, String name) throws NullPointerException
      {
         super(node);

         //
         if (parent == null)
         {
            throw new NullPointerException("No null parent accepted");
         }
         if (name == null)
         {
            throw new NullPointerException("No null name accepted");
         }

         //
         this.parent = parent;
         this.previous = previous;
         this.name = name;
      }

      public N getParent()
      {
         return parent;
      }

      public N getPrevious()
      {
         return previous;
      }

      public String getName()
      {
         return name;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onCreate(target, parent, previous, name);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Created[target" + target + ",previous" + previous + ",parent=" + parent + ",name=" + name + "]";
      }
   }

   public final static class Added<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final N previous;

      Added(N parent, N previous, N node)
      {
         super(node);

         //
         if (parent == null)
         {
            throw new NullPointerException("No null parent accepted");
         }

         //
         this.parent = parent;
         this.previous = previous;
      }

      public N getParent()
      {
         return parent;
      }

      public N getPrevious()
      {
         return previous != null ? previous : null;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onAdd(target, parent, previous);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Added[target" + target + ",previous" + previous + ",parent=" + parent + "]";
      }
   }

   public final static class Moved<N> extends NodeChange<N>
   {
      
      /** . */
      final N from;

      /** . */
      final N to;

      /** . */
      final N previous;

      Moved(N from, N to, N previous, N node)
      {
         super(node);

         //
         if (from == null)
         {
            throw new NullPointerException("No null from accepted");
         }
         //
         if (to == null)
         {
            throw new NullPointerException("No null to accepted");
         }

         //
         this.from = from;
         this.to = to;
         this.previous = previous;
      }

      public N getFrom()
      {
         return from;
      }

      public N getTo()
      {
         return to;
      }

      public N getPrevious()
      {
         return previous != null ? previous : null;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onMove(target, from, to, previous);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Moved[target" + target + ",from=" + from + ",to=" + to + ",previous=" + previous +  "]";
      }
   }

   public final static class Renamed<N> extends NodeChange<N>
   {

      /** . */
      final N parent;

      /** . */
      final String name;

      Renamed(N parent, N node, String name)
      {
         super(node);

         //
         if (parent == null)
         {
            throw new NullPointerException("No null parent accepted");
         }
         if (name == null)
         {
            throw new NullPointerException("No null name accepted");
         }

         //
         this.parent = parent;
         this.name = name;
      }

      public N getParent()
      {
         return parent;
      }

      public String getName()
      {
         return name;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onRename(target, parent, name);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Renamed[target" + target + ",name=" + name + "]";
      }
   }

   public final static class Updated<N> extends NodeChange<N>
   {

      /** . */
      final NodeState state;

      public Updated(N node, NodeState state)
      {
         super(node);

         //
         this.state = state;
      }

      public NodeState getState()
      {
         return state;
      }

      @Override
      protected void dispatch(NodeChangeListener<N> listener)
      {
         listener.onUpdate(target, state);
      }

      @Override
      public String toString()
      {
         return "NodeChange.Updated[target" + target + ",state=" + state + "]";
      }
   }
}
