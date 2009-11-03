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

import groovy.lang.Binding;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.exoplatform.commons.utils.OutputStreamPrinter;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A wrapper for a Groovy script and its meta data.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyScript
{

   /** . */
   private final String groovyText;

   /** . */
   private final Class<?> scriptClass;

   /** . */
   private final Map<Integer, TextItem> lineTable;

   public GroovyScript(String groovyText, Class<?> scriptClass, Map<Integer, TextItem> lineTable)
   {
      this.groovyText = groovyText;
      this.scriptClass = scriptClass;
      this.lineTable = lineTable;
   }

   public String getGroovyText()
   {
      return groovyText;
   }

   public Class<?> getScriptClass()
   {
      return scriptClass;
   }

   public void render(Map context, Writer writer) throws IOException, TemplateRuntimeException
   {
      Binding binding = context != null ? new Binding(context) : new Binding();

      //
      GroovyPrinter printer;
      if (writer instanceof OutputStreamPrinter)
      {
         printer = new OutputStreamWriterGroovyPrinter((OutputStreamPrinter)writer);
      }
      else
      {
         printer = new WriterGroovyPrinter(writer);
      }

      //
      BaseScript script = (BaseScript)InvokerHelper.createScript(scriptClass, binding);
      script.printer = printer;

      //
      try
      {
         script.run();
      }
      catch (Exception e)
      {
         if (e instanceof IOException)
         {
            throw (IOException)e;
         }
         else
         {
            throw buildRuntimeException(e);
         }
      }
      catch (Throwable e)
      {
         if (e instanceof Error)
         {
            throw ((Error)e);
         }
         throw buildRuntimeException(e);
      }

      //
      script.flush();
   }

   private TemplateRuntimeException buildRuntimeException(Throwable t)
   {
      StackTraceElement[] trace = t.getStackTrace();

      //
      TextItem firstItem = null;

      // Try to find the groovy script lines
      for (int i = 0;i < trace.length;i++)
      {
         StackTraceElement element = trace[i];
         if (element.getClassName().equals(scriptClass.getName()))
         {
            int lineNumber = element.getLineNumber();
            TextItem item = lineTable.get(lineNumber);
            int templateLineNumber;
            if (item != null)
            {
               templateLineNumber = item.getPosition().getLine();
               if (firstItem == null)
               {
                  firstItem = item;
               }
            }
            else
            {
               templateLineNumber = -1;
            }
            element = new StackTraceElement(
               element.getClassName(),
               element.getMethodName(),
               element.getFileName(),
               templateLineNumber);
            trace[i] = element;
         }
      }

      //
      t.setStackTrace(trace);

      //
      return new TemplateRuntimeException(firstItem, t);
   }
}
