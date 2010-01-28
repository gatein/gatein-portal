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

package org.exoplatform.commons.serialization.serial;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataContainer implements Externalizable
{

   /** . */
   private final LinkedList<Object> structure;

   public DataContainer()
   {
      this.structure = new LinkedList<Object>();
   }

   public void writeInt(int i)
   {
      structure.add(i);
   }

   public void writeObject(Object o)
   {
      structure.add(o);
   }

   public int readInt()
   {
      return (Integer)structure.removeFirst();
   }

   public Object readObject()
   {
      return structure.removeFirst();
   }

   public void writeExternal(ObjectOutput out) throws IOException
   {
      out.writeInt(structure.size());
      for (Object o : structure)
      {
         out.writeObject(o);
      }
   }

   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      int size = in.readInt();
      while (size-- > 0)
      {
         structure.addLast(in.readObject());
      }
   }

   @Override
   public String toString()
   {
      return "DataContainer[" + structure + "]";
   }
}
