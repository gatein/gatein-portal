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

import org.exoplatform.services.jcr.impl.Constants;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;


/**
 * A Groovy template.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyTemplate implements Externalizable {

    private static final long serialVersionUID = -8220112880199970451L;

    private static final String DEFAULT_ENCODING = "UTF-8";

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
    private String templateText;

    private String templateId;
    private String templateName;

    /** The groovy script. */
    private volatile GroovyScript script;

    public GroovyTemplate() {}

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
            synchronized (this) {
                if(this.script == null) {
                    try {
                        GroovyScriptBuilder compiler = new GroovyScriptBuilder(templateId, templateName, templateText);
                        this.script = compiler.build();
                    } catch (TemplateCompilationException ex) {
                        Logger log = LoggerFactory.getLogger(GroovyTemplate.class);
                        log.error(ex.getMessage(), ex);
                    }
                }
            }
        }

        return this.script;
    }

    /**
     * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] bytes = templateId.getBytes(DEFAULT_ENCODING);
        out.writeInt(bytes.length);
        out.write(bytes);
        bytes = templateName.getBytes(DEFAULT_ENCODING);
        out.writeInt(bytes.length);
        out.write(bytes);
        bytes = templateText.getBytes(DEFAULT_ENCODING);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    /**
     * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException  {
        byte[] bytes = new byte[in.readInt()];
        in.readFully(bytes);
        templateId = new String(bytes, Constants.DEFAULT_ENCODING);
        bytes = new byte[in.readInt()];
        in.readFully(bytes);
        templateName = new String(bytes, Constants.DEFAULT_ENCODING);
        bytes = new byte[in.readInt()];
        in.readFully(bytes);
        templateText = new String(bytes, Constants.DEFAULT_ENCODING);
    }

   @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((templateId == null) ? 0 : templateId.hashCode());
        result = prime * result + ((templateName == null) ? 0 : templateName.hashCode());
        result = prime * result + ((templateText == null) ? 0 : templateText.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroovyTemplate other = (GroovyTemplate)obj;
        if (templateId == null) {
            if (other.templateId != null)
                return false;
        } else if (!templateId.equals(other.templateId))
            return false;
        if (templateName == null) {
            if (other.templateName != null)
                return false;
        } else if (!templateName.equals(other.templateName))
            return false;
        if (templateText == null) {
            if (other.templateText != null)
                return false;
        } else if (!templateText.equals(other.templateText))
            return false;
        return true;
    }
}
