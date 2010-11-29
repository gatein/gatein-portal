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

package org.exoplatform.portal.resource;

import org.exoplatform.commons.utils.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CachedStylesheet
{

   /** The optimized encoder. */
   private static final TextEncoder encoder = new CharsetTextEncoder(new TableCharEncoder(CharsetCharEncoder.getUTF8()));

   /** . */
   private final String text;

   /** . */
   private final byte[] bytes;
   
   private long lastModified;

   public CachedStylesheet(String text)
   {
      // Compute encoded bytes
      byte[] bytes;
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream(text.length() * 2);
         encoder.encode(text, 0, text.length(), baos);
         baos.flush();
         bytes = baos.toByteArray();
      } catch (IOException e)
      {
         throw new UndeclaredThrowableException(e, "That should not happen");
      }

      //
      this.text = text;
      this.bytes = bytes;
//  Remove miliseconds because string of date retrieve from Http header doesn't have miliseconds 
      lastModified = (new Date().getTime() / 1000) * 1000;
   }

   public String getText()
   {
      return text;
   }   

   public long getLastModified()
   {
      return lastModified;
   }

   public void writeTo(BinaryOutput output) throws IOException
   {
      output.write(bytes);
   }
}
