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

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class WriterGroovyPrinter extends GroovyPrinter
{

   /** . */
   private Writer writer;

   public WriterGroovyPrinter(Writer writer)
   {
      if (writer == null)
      {
         throw new NullPointerException();
      }
      this.writer = writer;
   }

   @Override
   public void write(char[] cbuf, int off, int len) throws IOException
   {
      writer.write(cbuf, off, len);
   }

   @Override
   public void close() throws IOException
   {
      writer.close();
   }

   @Override
   public void flush() throws IOException
   {
      writer.flush();
   }
}
