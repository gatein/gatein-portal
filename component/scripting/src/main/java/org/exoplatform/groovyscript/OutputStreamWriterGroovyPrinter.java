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
package org.exoplatform.groovyscript;

import org.exoplatform.commons.utils.BinaryOutput;
import org.exoplatform.commons.utils.OutputStreamPrinter;
import org.exoplatform.commons.utils.Text;

import java.io.IOException;
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

   public void write(byte[] b, int off, int len) throws IOException
   {
      out.write(b, off, len);
   }

   public void write(byte b) throws IOException
   {
      out.write(b);
   }
}
