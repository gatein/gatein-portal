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

import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObjectSupport;
import org.exoplatform.commons.utils.Text;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class GroovyPrinter extends GroovyObjectSupport implements GroovyInterceptable
{

   /**
    * Optimize the call to the various print methods.
    *
    * @param name the method name
    * @param args the method arguments
    * @return the return value
    */
   @Override
   public Object invokeMethod(String name, Object args)
   {
      // Optimize access to print methods
      if (args instanceof Object[])
      {
         Object[] array = (Object[])args;
         if (array.length == 1)
         {
            if ("print".equals(name))
            {
               print(array[0]);
               return null;
            }
            else if ("println".equals(name))
            {
               println(array[0]);
               return null;
            }
         }
      }

      // Back to Groovy method call
      return super.invokeMethod(name, args);
   }

   public final void println(Object o)
   {
      print(o);
      println();
   }

   public final void println()
   {
      try
      {
         write('\n');
      }
      catch (IOException ignore)
      {
      }
   }

   public final void print(Object o)
   {
      try
      {
         if (o == null)
         {
            write("null");
         }
         else if (o instanceof Text)
         {
            write((Text)o);
         }
         else
         {
            write(o.toString());
         }
      }
      catch (IOException ignore)
      {
      }
   }

   protected abstract void write(char c) throws IOException;

   protected abstract void write(String s) throws IOException;

   protected abstract void write(Text text) throws IOException;

   protected abstract void flush() throws IOException;

   protected abstract void close() throws IOException;

}
