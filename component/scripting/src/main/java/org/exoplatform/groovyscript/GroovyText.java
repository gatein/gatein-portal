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
import org.exoplatform.commons.utils.Text;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyText extends Text 
{

   /** . */
   private final String s;

   /** . */
   private final byte[] bytes;

   /** . */
   private static final Charset UTF_8 = Charset.forName("UTF-8");

   public GroovyText(String s)
   {
      try
      {
         this.s = s;
         this.bytes = s.getBytes("UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new UndeclaredThrowableException(e);
      }
   }

   @Override
   public void writeTo(Writer writer) throws IOException
   {
      if (writer instanceof BinaryOutput)
      {
         BinaryOutput osw = (BinaryOutput)writer;
         if (UTF_8.equals(osw.getCharset()))
         {
            osw.write(bytes);
            return;
         }
      }
      writer.append(s);
   }
}
