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

import groovy.lang.Binding;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.exoplatform.commons.utils.OutputStreamPrinter;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
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
   private final String templateId;

   /** . */
   private final String groovyText;

   /** . */
   private final Class<?> scriptClass;

   /** . */
   private final Map<Integer, TextItem> lineTable;

   public GroovyScript(String templateId, String groovyText, Class<?> scriptClass, Map<Integer, TextItem> lineTable)
   {
      this.templateId = templateId;
      this.groovyText = groovyText;
      this.scriptClass = scriptClass;
      this.lineTable = lineTable;
   }

   public String getTemplateId()
   {
      return templateId;
   }

   public String getGroovyText()
   {
      return groovyText;
   }

   public Class<?> getScriptClass()
   {
      return scriptClass;
   }

   /**
    * Renders the script with the provided context and locale to the specified writer.
    *
    * @param context the context
    * @param writer the writer
    * @param locale the locale
    * @throws IOException
    * @throws TemplateRuntimeException
    */
   public void render(
      Map context,
      Writer writer,
      Locale locale) throws IOException, TemplateRuntimeException
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
      printer.setLocale(locale);

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
      return new TemplateRuntimeException(templateId, firstItem, t);
   }
}
