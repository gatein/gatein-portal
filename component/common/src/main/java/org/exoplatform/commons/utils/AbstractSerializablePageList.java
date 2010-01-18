/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.commons.utils;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

/**
 * <p>This class defines the common functionalities for the serializable subclasses of {@link PageList}. Note
 * that itself it does not implement the {@link java.io.Serializable} interface for the reason that it needs to
 * define a no arg constructor in a non serializble class (serialization constraint).</p>
 *
 * <p>The method defines an abstract {@link #connect()} method that is used to connect the the page list to the
 * underlying data.</p>
 *
 * <p>The methods {@link #readState(java.io.ObjectInputStream)} and {@link #writeState(java.io.ObjectOutputStream)}
 * are defined for the subclasses and should be called by <code>void readObject(ObjectInputStream in)</code> and
 * <code>void writeObject(ObjectOutputStream out)</code> custom serialization protocol.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractSerializablePageList<E> extends PageList<E>
{

   /** . */
   private static final Field pageSizeField;

   static
   {
      try
      {
         pageSizeField = PageList.class.getDeclaredField("pageSize_");
         pageSizeField.setAccessible(true);
      }
      catch (NoSuchFieldException e)
      {
         throw new Error(e);
      }
   }

   /**
    * Builds a page list.
    *
    * @param pageSize the page size
    */
   protected AbstractSerializablePageList(int pageSize)
   {
      super(pageSize);
   }

   /**
    * This constructor should not be used by subclasses, it is only for serialization needs.
    */
   protected AbstractSerializablePageList()
   {
      super(10);
   }

   /** . */
   private LazyList<E> lazyList;

   protected abstract ListAccess<E> connect() throws Exception;

   private void ensureCorrectState()
   {
      if (lazyList == null)
      {
         try
         {
            lazyList = new LazyList<E>(connect(), super.getPageSize());
         }
         catch (Exception e)
         {
            throw new UndeclaredThrowableException(e);
         }

         // Save temporarily the current page
         int currentPage = currentPage_;

         // Refresh state
         super.setAvailablePage(lazyList.size());

         // Put back current page that was written by previous method call
         if (currentPage != -1)
         {
            currentPage_ = currentPage;
         }
      }
   }

   @Override
   protected final void populateCurrentPage(int page) throws Exception
   {
      // Make sure we have correct state
      ensureCorrectState();

      //
      int from = getFrom();
      int to = getTo();
      currentListPage_ = lazyList.subList(from, to);
   }

   @Override
   public final List<E> getAll()
   {
      ensureCorrectState();

      //
      return lazyList;
   }

   // Serialization : should be used by subclasses

   protected final void writeState(ObjectOutputStream out) throws IOException
   {
      int pageSize;
      try
      {
         pageSize = pageSizeField.getInt(this);
      }
      catch (IllegalAccessException e)
      {
         InvalidObjectException ioe = new InvalidObjectException("Cannot set page size");
         ioe.initCause(e);
         throw ioe;
      }

      //
      out.writeInt(pageSize);
      out.writeInt(currentPage_);
   }

   protected final void readState(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      try
      {
         pageSizeField.setInt(this, in.readInt());
      }
      catch (IllegalAccessException e)
      {
         InvalidObjectException ioe = new InvalidObjectException("Cannot set page size");
         ioe.initCause(e);
         throw ioe;
      }
      currentPage_ = in.readInt();
   }

   // Intercept all method calls

   @Override
   public final int getAvailablePage()
   {
      ensureCorrectState();
      return super.getAvailablePage();
   }

   @Override
   public final int getTo()
   {
      ensureCorrectState();
      return super.getTo();
   }

   @Override
   public final int getFrom()
   {
      ensureCorrectState();
      return super.getFrom();
   }

   @Override
   protected final void setAvailablePage(int available)
   {
      ensureCorrectState();
      super.setAvailablePage(available);
   }

   @Override
   protected final void checkAndSetPage(int page) throws Exception
   {
      ensureCorrectState();
      super.checkAndSetPage(page);
   }

   @Override
   public final List<E> getPage(int page) throws Exception
   {
      ensureCorrectState();
      return super.getPage(page);
   }

   @Override
   public final List<E> currentPage() throws Exception
   {
      ensureCorrectState();
      return super.currentPage();
   }

   @Override
   public final int getAvailable()
   {
      ensureCorrectState();
      return super.getAvailable();
   }

   @Override
   public final int getCurrentPage()
   {
      ensureCorrectState();
      return super.getCurrentPage();
   }

   @Override
   public final void setPageSize(int pageSize)
   {
      ensureCorrectState();
      super.setPageSize(pageSize);
   }

   @Override
   public final int getPageSize()
   {
      ensureCorrectState();
      return super.getPageSize();
   }
}
