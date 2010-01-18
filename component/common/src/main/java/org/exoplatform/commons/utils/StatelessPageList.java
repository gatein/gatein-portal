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

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class StatelessPageList<E> extends NoArgConstructorPageList<E> implements Serializable
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

   /** . */
   private LazyList<E> lazyList;

   protected StatelessPageList(int pageSize)
   {
      super(pageSize);

      //
      if (pageSize < 0)
      {
         throw new IllegalArgumentException();
      }

      //
      this.lazyList = null;
   }

   private void ensureCorrectState()
   {
      if (lazyList == null)
      {
         ListAccess<E> listAccess;
         try
         {
            listAccess = connect();
         }
         catch (Exception e)
         {
            throw new UndeclaredThrowableException(e, "Cannot connect to list access ");
         }

         //
         lazyList = new LazyList<E>(listAccess, super.getPageSize());

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

   protected abstract ListAccess<E> connect() throws Exception;

   // Serialization

   private void writeObject(ObjectOutputStream out) throws IOException
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

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
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