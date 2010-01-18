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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class EmptySerializablePageList<E> extends NoArgConstructorPageList<E> implements Serializable
{

   /** . */
   private static final EmptySerializablePageList instance = new EmptySerializablePageList();

   public EmptySerializablePageList()
   {
      super(10);
   }

   @Override
   protected void populateCurrentPage(int page) throws Exception
   {
      currentListPage_ = Collections.emptyList();
   }

   @Override
   public List<E> getAll() throws Exception
   {
      return Collections.emptyList();
   }

   public static <E> PageList<E> get()
   {
      // Cast OK
      return (PageList<E>)instance;
   }
}
