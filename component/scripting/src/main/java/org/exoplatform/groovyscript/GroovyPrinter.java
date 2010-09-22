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

import groovy.lang.GString;
import groovy.lang.GroovyInterceptable;
import groovy.lang.GroovyObjectSupport;
import org.exoplatform.commons.utils.Text;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
abstract class GroovyPrinter extends GroovyObjectSupport implements GroovyInterceptable
{

   /** An optional locale. */
   private Locale locale;

   public Locale getLocale()
   {
      return locale;
   }

   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }

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

   /**
    * We handle in this method a conversion of an object to another one for formatting purposes.
    *
    * @param o the object to format
    * @return the formatted object
    */
   private Object format(Object o)
   {
      if (o instanceof Date)
      {
         if (locale != null)
         {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            o = dateFormat.format((Date)o);
         }
      }

      //
      return o;
   }

   private String toString(Object o)
   {
      Object f = format(o);
      if (f == null)
      {
         return "null";
      }
      else if (f instanceof String)
      {
         return (String)f;
      }
      else
      {
         return o.toString();
      }
   }

   public final void print(Object o)
   {
      try
      {
         if (o instanceof Text)
         {
            write((Text)o);
         }
         else if (o instanceof GString)
         {
            GString gs = (GString)o;
            Object[] values = gs.getValues();
            for (int i = 0;i < values.length;i++)
            {
               values[i] = format(values[i]);
            }
            write(o.toString());
         }
         else
         {
            write(toString(o));
         }
      }
      catch (IOException ignore)
      {
      }
   }

   protected abstract Writer getWriter();

   protected abstract void write(char c) throws IOException;

   protected abstract void write(String s) throws IOException;

   protected abstract void write(Text text) throws IOException;

   protected abstract void flush() throws IOException;

   protected abstract void close() throws IOException;

}
