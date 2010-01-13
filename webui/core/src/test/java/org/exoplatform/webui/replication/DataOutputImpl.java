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

package org.exoplatform.webui.replication;

import junit.framework.Assert;
import org.exoplatform.webui.application.replication.serial.DataOutput;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataOutputImpl implements DataOutput
{

   /** . */
   private LinkedList<Object> stream = new LinkedList<Object>();

   public void writeInt(int i)
   {
      stream.add(0);
      stream.addLast(i);
   }

   public void writeString(String s)
   {
      stream.add(1);
      stream.addLast(s);
   }

   public void writeSerializable(Serializable serializable)
   {
      stream.add(2);
      stream.addLast(serializable);
   }

   public void assertEmpty()
   {
      Assert.assertEquals(0, stream.size());
   }

   public void assertInt(int i)
   {
      assertElement(0, i);
   }

   public void assertString(String s)
   {
      assertElement(1, s);
   }

   public void assertSerializable(Serializable s)
   {
      assertElement(2, s);
   }

   private void assertElement(int type, Object o)
   {
      Assert.assertEquals(type, stream.removeFirst());
      Assert.assertEquals(o, stream.removeFirst());
   }
}
