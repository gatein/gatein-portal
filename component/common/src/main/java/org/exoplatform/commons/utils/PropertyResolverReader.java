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

package org.exoplatform.commons.utils;

import java.io.IOException;
import java.io.Reader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PropertyResolverReader extends Reader
{

   /** . */
   private static final int STATUS_READING = 0;

   /** . */
   private static final int STATUS_READ_DOLLAR = 1;

   /** . */
   private static final int STATUS_READING_PROPERTY = 2;

   /** . */
   private static final int STATUS_WRITING = 3;

   /** . */
   private static final int STATUS_TERMINATED = 4;

   /** . */
   private Reader delegate;

   /** . */
   private int status = STATUS_READING;

   /** . */
   private char[] buffer;

   /** . */
   private int bufferLen;

   /** . */
   private int bufferOff;

   /** . */
   private int mark;

   public PropertyResolverReader(Reader delegate)
   {
      this(delegate, 64);
   }

   public PropertyResolverReader(Reader delegate, int bufferSize)
   {
      if (delegate == null)
      {
         throw new NullPointerException();
      }
      if (bufferSize < 0)
      {
         throw new IllegalArgumentException();
      }

      //
      this.delegate = delegate;
      this.buffer = new char[bufferSize];
      this.bufferLen = 0;
      this.bufferOff = 0;
   }

   @Override
   public int read(char[] cbuf, int off, int len) throws IOException
   {
      final int olen = len;
      while (true)
      {
         switch (status)
         {
            case STATUS_READING:
            {
               if (len > 0)
               {
                  if (bufferOff == bufferLen)
                  {
                     resetBuffer();
                     fillBuffer(Math.min(len, buffer.length));
                  }
                  else
                  {
                     char c = buffer[bufferOff++];
                     if (c == '$')
                     {
                        mark = bufferOff - 1;
                        status = STATUS_READ_DOLLAR;
                     }
                     else
                     {
                        cbuf[off++] = c;
                        len--;
                     }
                  }
               }
               else
               {
                  return olen - len;
               }
               break;
            }
            case STATUS_TERMINATED:
            {
               if (mark != -1 && mark < bufferOff)
               {
                  if (len > 0)
                  {
                     cbuf[off++] = buffer[mark++];
                     len--;
                  }
                  else
                  {
                     return olen - len;
                  }
               }
               else
               {
                  if (olen > len)
                  {
                     return olen - len;
                  }
                  else
                  {
                     return -1;
                  }
               }
               break;
            }
            case STATUS_READ_DOLLAR:
            {
               if (bufferOff == bufferLen)
               {
                  // For now let's read 1 ?
                  fillBuffer(1);
               }
               else
               {
                  char c = buffer[bufferOff++];
                  if (c == '{')
                  {
                     status = STATUS_READING_PROPERTY;
                  }
                  else
                  {
                     status = STATUS_WRITING;
                  }
               }
               break;
            }
            case STATUS_WRITING:
            {
               if (mark < bufferOff)
               {
                  if (len > 0)
                  {
                     cbuf[off++] = buffer[mark++];
                     len--;
                  }
                  else
                  {
                     return olen - len;
                  }
               }
               else
               {
                  mark = -1;
                  status = STATUS_READING;
               }
               break;
            }
            case STATUS_READING_PROPERTY:
            {
               if (bufferOff == bufferLen)
               {
                  // For now let's read 1 ?
                  fillBuffer(1);
               }
               else
               {
                  char c = buffer[bufferOff++];
                  if (c == '}')
                  {
                     String name = new String(buffer, mark + 2, bufferOff - mark - 2 - 1);
                     String value = resolve(name);
                     if (value == null)
                     {
                        status = STATUS_WRITING;
                     }
                     else
                     {
                        mark = bufferOff - value.length();
                        if (mark < 0)
                        {
                           int nextBufferLen = bufferLen - mark;
                           if (nextBufferLen > buffer.length)
                           {
                              char[] tmp = new char[nextBufferLen];
                              System.arraycopy(buffer, bufferOff, tmp, bufferOff - mark, bufferLen - bufferOff);
                              buffer = tmp;
                           }
                           else
                           {
                              System.arraycopy(buffer, bufferOff, buffer, bufferOff - mark, bufferLen - bufferOff);
                           }
                           bufferOff -= mark;
                           bufferLen = nextBufferLen;
                           mark = 0;
                        }
                        value.getChars(0, value.length(), buffer, mark);
                        status = STATUS_WRITING;
                     }
                  }
                  else
                  {
                     // We do nothing until we get end of stream of }
                  }
               }
               break;
            }
            default:
               throw new UnsupportedOperationException();
         }
      }
   }

   /**
    * Resolves a property value, this method is called during the stream analysis. When the returned value is null,
    * the property declaration will be read by the client (i.e ${a} will be read as ${a}).
    *
    * @param name the property name
    * @return the property value
    * @throws IOException any IOException
    */
   protected String resolve(String name)  throws IOException
   {
      return name;
   }
   
   private void resetBuffer()
   {
      if (bufferLen != bufferOff)
      {
         throw new AssertionError();
      }
      bufferOff = 0;
      bufferLen = 0;
   }
   
   private void fillBuffer(int amount) throws IOException
   {
      if (bufferLen > bufferOff)
      {
         throw new AssertionError();
      }
      if (amount < 0)
      {
         throw new IllegalArgumentException();
      }
      int space = bufferLen + amount - buffer.length;
      if (space > 0)
      {
         // We allocate more space
         char[] tmp = new char[buffer.length + space];
         System.arraycopy(buffer, 0, tmp, 0, buffer.length);
         buffer = tmp;
      }
      int ret = delegate.read(buffer, bufferLen, amount);
      if (ret != -1)
      {
         bufferLen += ret;
      }
      else
      {
         status = STATUS_TERMINATED;
      }
   }

   @Override
   public void close() throws IOException
   {
      delegate.close();
   }
}
