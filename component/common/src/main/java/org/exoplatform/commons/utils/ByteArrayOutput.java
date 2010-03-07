/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ByteArrayOutput implements BinaryOutput {

   /** . */
   private static final Charset UTF_8 = Charset.forName("UTF-8");

   /** . */
   private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

   public Charset getCharset()
   {
      return UTF_8;
   }

   public void write(byte b) throws IOException
   {
      baos.write(b);
   }

   public void write(byte[] bytes) throws IOException
   {
      baos.write(bytes);
   }

   public void write(byte[] bytes, int off, int len) throws IOException
   {
      baos.write(bytes, off, len);
   }

   public byte[] getBytes()
   {
      return baos.toByteArray();
   }

   public String getString() throws UnsupportedEncodingException
   {
      return baos.toString(UTF_8.name());
   }
}
