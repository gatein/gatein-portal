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

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageListAccess<E, S> extends AbstractSerializablePageList<E> implements Serializable
{

   /** The state that recreates the list. */
   private S state;

   protected PageListAccess(S state, int pageSize)
   {
      super(pageSize);

      //
      if (state == null)
      {
         throw new NullPointerException();
      }
      if (pageSize < 0)
      {
         throw new IllegalArgumentException();
      }

      //
      this.state = state;
   }

   @Override
   protected final ListAccess<E> connect() throws Exception
   {
      return create(state);
   }

   protected abstract ListAccess<E> create(S state) throws Exception;
   
   // Serialization

   private void writeObject(ObjectOutputStream out) throws IOException
   {
      super.writeState(out);
      out.writeObject(state);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      super.readState(in);
      state = (S)in.readObject();
   }
}
