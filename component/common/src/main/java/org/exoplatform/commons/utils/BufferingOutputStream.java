/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A stream that maintains a buffer and flush it on a delegate output stream when it is
 * filled. Unlike {@link java.io.BufferedOutputStream} this class is not synchronized. 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BufferingOutputStream extends OutputStream
{

   /** . */
   private final OutputStream out;

   /** . */
   private byte[] buffer;

   /** . */
   private boolean open;

   /** . */
   private int offset;

   /** . */
   private final int size;

   public BufferingOutputStream(OutputStream out, int bufferSize)
   {
      if (out == null)
      {
         throw new NullPointerException("No null output stream");
      }
      if (bufferSize < 1)
      {
         throw new IllegalArgumentException("No buffer size under 1");
      }
      this.out = out;
      this.buffer = new byte[bufferSize];
      this.size = bufferSize;
      this.open = true;
   }

   @Override
   public void write(int b) throws IOException
   {
      if (!open)
      {
         throw new IOException("closed");
      }
      if (offset >= size)
      {
         out.write(buffer);
         offset = 0;
      }
      buffer[offset++] = (byte)b;
   }

   @Override
   public void write(byte[] b, int off, int len) throws IOException
   {
      if (!open)
      {
         throw new IOException("closed");
      }

      //
      if (offset + len >= size)
      {
         // Clear the buffer
         out.write(buffer, 0, offset);
         offset = 0;

         // While the data length is greater than the the buffer size
         // write directly to the wire
         while (len >= size)
         {
            out.write(b, off, size);
            off += size;
            len -= size;
         }
      }

      //
      System.arraycopy(b, off, buffer, offset, len);
      offset += len;
   }

   @Override
   public void flush() throws IOException
   {
      if (!open)
      {
         throw new IOException("closed");
      }

      //
      if (offset > 0)
      {
         out.write(buffer, 0, offset);
         offset = 0;
      }

      //
      out.flush();
   }

   @Override
   public void close() throws IOException
   {
      if (!open)
      {
         throw new IOException("closed");
      }

      //
      if (offset > 0)
      {
         out.write(buffer, 0, offset);
         offset = 0;
      }

      //
      open = false;
      out.close();
   }
}
