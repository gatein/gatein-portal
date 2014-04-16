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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * A Groovy template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyTemplate implements Serializable {

    private static final long serialVersionUID = -8220112880199970451L;

    // todo : move that to {@link org.gatein.common.io.IOTools}
    private static String read(Reader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        char[] chars = new char[256];
        for (int s = reader.read(chars); s != -1; s = reader.read(chars)) {
            builder.append(chars, 0, s);
        }
        return builder.toString();
    }

    /** The text of the template. */
    private final String templateText;

    private final String templateId;
    private final String templateName;

    /** The groovy script. */
    private transient GroovyScript script;

    public GroovyTemplate(String id, String name, Reader scriptReader) throws IOException, TemplateCompilationException {
        this(id, name, read(scriptReader));
    }

    public GroovyTemplate(Reader scriptReader) throws IOException, TemplateCompilationException {
        this(read(scriptReader));
    }

    public GroovyTemplate(String templateText) throws TemplateCompilationException {
        this(null, null, templateText);
    }

    public GroovyTemplate(String templateId, String templateName, String templateText) throws TemplateCompilationException {
        if (templateName == null) {
            templateName = "fic";
        } else {
            templateName = templateName.replaceAll("-", "_");
        }

        //
        GroovyScriptBuilder compiler = new GroovyScriptBuilder(templateId, templateName, templateText);

        //
        this.script = compiler.build();
        this.templateText = templateText;
        this.templateId = templateId;
        this.templateName = templateName;
    }

    public String getId() {
        return this.templateId;
    }

    public String getClassName() {
        return getScript().getScriptClass().getName();
    }

    public String getText() {
        return templateText;
    }

    public String getGroovy() {
        return getScript().getGroovyText();
    }

    public void render(Writer writer) throws IOException, TemplateRuntimeException {
        render(writer, (Map) null);
    }

    public void render(Writer writer, Locale locale) throws IOException, TemplateRuntimeException {
        render(writer, null, locale);
    }

    public void render(Writer writer, Map binding, Locale locale) throws IOException, TemplateRuntimeException {
        getScript().render(binding, writer, locale);
    }

    public void render(Writer writer, Map binding) throws IOException, TemplateRuntimeException {
        getScript().render(binding, writer, null);
    }

    public String render() throws IOException, TemplateRuntimeException {
        return render((Map) null);
    }

    public String render(Locale locale) throws IOException, TemplateRuntimeException {
        return render((Map) null, locale);
    }

    public String render(Map binding) throws IOException, TemplateRuntimeException {
        return render(binding, null);
    }

    public String render(Map binding, Locale locale) throws IOException, TemplateRuntimeException {
        StringWriter buffer = new StringWriter();
        render(buffer, binding, locale);
        buffer.close();
        return buffer.toString();
    }

    private GroovyScript getScript() {
        if(this.script == null) {
            try {
                GroovyScriptBuilder compiler = new GroovyScriptBuilder(templateId, templateName, templateText);
                this.script = compiler.build();
            } catch (TemplateCompilationException ex) {
                Logger log = LoggerFactory.getLogger(GroovyTemplate.class);
                log.error(ex.getMessage(), ex);
            }
        }
        return this.script;
    }
}
