/**
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
package org.exoplatform.groovyscript;

import org.exoplatform.commons.utils.BinaryOutput;
import org.exoplatform.commons.utils.OutputStreamPrinter;
import org.exoplatform.commons.utils.Text;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OutputStreamWriterGroovyPrinter extends GroovyPrinter implements BinaryOutput
{

   /** . */
   private final OutputStreamPrinter out;

   public OutputStreamWriterGroovyPrinter(OutputStreamPrinter out)
   {
      if (out == null)
      {
         throw new NullPointerException();
      }
      this.out = out;
   }

   @Override
   protected Writer getWriter()
   {
      return out;
   }

   @Override
   protected void write(char c) throws IOException
   {
      out.write(c);
   }

   @Override
   protected void write(String s) throws IOException
   {
      out.write(s);
   }

   @Override
   protected void write(Text text) throws IOException
   {
      text.writeTo(out);
   }

   @Override
   public void flush() throws IOException
   {
      out.flush();
   }

   @Override
   public void close() throws IOException
   {
      out.close();
   }

   public Charset getCharset()
   {
      return out.getCharset();
   }

   public void write(byte[] bytes) throws IOException
   {
      out.write(bytes);
   }

   public void write(byte[] bytes, int off, int len) throws IOException
   {
      out.write(bytes, off, len);
   }

   public void write(byte b) throws IOException
   {
      out.write(b);
   }
}
