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
package org.exoplatform.portal.resource;

import org.exoplatform.commons.utils.BinaryOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * A simple ResourceRenderer used in JUnit tests of SkinService
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/29/11
 */
public class MockResourceRenderer implements ResourceRenderer
{

   private final BinaryOutput output;

   private final static Charset UTF_8 = Charset.forName("UTF-8");

   public MockResourceRenderer(final OutputStream out)
   {
      this.output = new BinaryOutput()
      {
         public Charset getCharset()
         {
            return UTF_8;
         }

         public void write(byte b) throws IOException
         {
            out.write(b);
         }

         public void write(byte[] bytes) throws IOException
         {
            out.write(bytes);
         }

         public void write(byte[] bytes, int off, int len) throws IOException
         {
            out.write(bytes, off, len);
         }
      };
   }

   public BinaryOutput getOutput() throws IOException
   {
      return output;
   }

   public void setExpiration(long seconds)
   {
   }
}
