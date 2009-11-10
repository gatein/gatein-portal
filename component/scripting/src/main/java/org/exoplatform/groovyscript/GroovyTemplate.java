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
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * A Groovy template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyTemplate
{

   private static String read(Reader reader) throws IOException
   {
      StringBuilder builder = new StringBuilder();
      char[] chars = new char[256];
      for (int s = reader.read(chars);s != -1; s = reader.read(chars))
      {
         builder.append(chars, 0, s);
      }
      return builder.toString();
   }

   /** The text of the template. */
   private final String templateText;

   /** The groovy script. */
   private final GroovyScript script;

   public GroovyTemplate(String id, String name, Reader scriptReader) throws IOException, TemplateCompilationException
   {
      this(id, name, read(scriptReader));
   }

   public GroovyTemplate(Reader scriptReader) throws IOException, TemplateCompilationException
   {
      this(read(scriptReader));
   }

   public GroovyTemplate(String templateText) throws TemplateCompilationException
   {
      this(null, null, templateText);
   }

   public GroovyTemplate(String templateId, String templateName, String templateText) throws TemplateCompilationException
   {
      if (templateName == null)
      {
         templateName = "fic";
      }

      //
      GroovyScriptBuilder compiler = new GroovyScriptBuilder(templateId, templateName, templateText);

      //
      this.script = compiler.build();
      this.templateText = templateText;
   }

   public String getId()
   {
      return script.getTemplateId();
   }

   public String getClassName()
   {
      return script.getScriptClass().getName();
   }

   public String getText()
   {
      return templateText;
   }

   public String getGroovy()
   {
      return script.getGroovyText();
   }

   public void render(Writer writer) throws IOException, TemplateRuntimeException
   {
      render(writer, null);
   }

   public void render(Writer writer, Map binding) throws IOException, TemplateRuntimeException
   {
      script.render(binding, writer);
   }

   public String render() throws IOException, TemplateRuntimeException
   {
      return render((Map)null);
   }

   public String render(Map binding) throws IOException, TemplateRuntimeException
   {
      StringWriter buffer = new StringWriter();
      render(buffer, binding);
      buffer.close();
      return buffer.toString();
   }
}
