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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.exoplatform.commons.utils.SecurityHelper;
import org.gatein.common.classloader.DelegatingClassLoader;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GroovyScriptBuilder {

    /** . */
    private final String templateId;

    /** . */
    private final String templateName;

    /** . */
    private final String templateText;

    /** . */
    private SectionType currentType = null;

    /** . */
    private StringBuilder accumulatedText = new StringBuilder();

    /** . */
    private Script script = new Script();

    public GroovyScriptBuilder(String templateId, String templateName, String templateText) {
        this.templateId = templateId;
        this.templateName = templateName;
        this.templateText = templateText;
    }

    private void begin(SectionType sectionType, Position pos) {
        if (sectionType == null) {
            throw new NullPointerException();
        }
        if (pos == null) {
            throw new NullPointerException();
        }
        if (currentType != null) {
            throw new IllegalStateException();
        }
        this.currentType = sectionType;

        //
        switch (currentType) {
            case STRING:
                break;
            case SCRIPTLET:
                break;
            case EXPR:
                script.appendGroovy(";out.print(\"${");
                break;
        }
    }

    private void append(SectionItem item) {
        if (item instanceof TextItem) {
            TextItem textItem = (TextItem) item;
            String text = textItem.getData();
            switch (currentType) {
                case STRING:
                    accumulatedText.append(text);
                    break;
                case SCRIPTLET:
                    script.appendGroovy(text);
                    script.positionTable.put(script.lineNumber, textItem);
                    break;
                case EXPR:
                    script.appendGroovy(text);
                    script.positionTable.put(script.lineNumber, textItem);
                    break;
            }
        } else if (item instanceof LineBreakItem) {
            switch (currentType) {
                case STRING:
                    accumulatedText.append("\n");
                    break;
                case SCRIPTLET:
                case EXPR:
                    script.appendGroovy("\n");
                    script.lineNumber++;
                    break;
            }
        } else {
            throw new AssertionError();
        }
    }

    private void end() {
        if (currentType == null) {
            throw new IllegalStateException();
        }

        //
        switch (currentType) {
            case STRING:
                if (accumulatedText.length() > 0) {
                    script.appendText(accumulatedText.toString());
                    accumulatedText.setLength(0);
                }
                break;
            case SCRIPTLET:
                // We append a line break because we want that any line comment does not affect the template
                script.appendGroovy("\n");
                script.lineNumber++;
                break;
            case EXPR:
                script.appendGroovy("}\");\n");
                script.lineNumber++;
                break;
        }

        //
        this.currentType = null;
    }

    public GroovyScript build() throws TemplateCompilationException {
        List<TemplateSection> sections = new TemplateParser().parse(templateText);

        //
        for (TemplateSection section : sections) {
            begin(section.getType(), section.getItems().get(0).getPosition());
            for (SectionItem item : section.getItems()) {
                append(item);
            }
            end();
        }

        //
        String groovyText = script.toString();

        //
        CompilerConfiguration config = new CompilerConfiguration();

        //
        byte[] bytes;
        try {
            config.setScriptBaseClass(BaseScript.class.getName());
            bytes = groovyText.getBytes(config.getSourceEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new TemplateCompilationException(e, groovyText);
        }

        //
        InputStream in = new ByteArrayInputStream(bytes);
        GroovyCodeSource gcs = new GroovyCodeSource(in, templateName, "/groovy/shell");
        GroovyClassLoader loader = new GroovyClassLoader(prepareClassLoader(), config);
        Class<?> scriptClass;
        try {
            scriptClass = loader.parseClass(gcs, false);
        } catch (CompilationFailedException e) {
            throw new GroovyCompilationException(e, templateText, groovyText);
        } catch (ClassFormatError e) {
            throw new GroovyCompilationException(e, templateText, groovyText);
        }

        return new GroovyScript(templateId, script.toString(), scriptClass,
                Collections.unmodifiableMap(new HashMap<Integer, TextItem>(script.positionTable)));
    }

    private ClassLoader prepareClassLoader() {
        final ClassLoader tccl = SecurityHelper.doPrivilegedAction(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        return new DelegatingClassLoader(tccl,
            GroovyClassLoader.class.getClassLoader(),
            javax.portlet.PortletConfig.class.getClassLoader());
    }

    /**
     * Internal representation of a script
     */
    private static class Script {

        /** . */
        private StringBuilder out = new StringBuilder();

        /** . */
        private List<TextContant> textMethods = new ArrayList<TextContant>();

        /** . */
        private int methodCount = 0;

        /** The line number table. */
        private Map<Integer, TextItem> positionTable = new HashMap<Integer, TextItem>();

        /** The current line number. */
        private int lineNumber = 1;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(out.toString());
            builder.append("\n");
            builder.append("public class Constants\n");
            builder.append("{\n");
            for (TextContant method : textMethods) {
                builder.append(method.getDeclaration()).append("\n");
            }
            builder.append("}\n");
            return builder.toString();
        }

        public void appendText(String text) {
            TextContant m = new TextContant("s" + methodCount++, text);
            out.append("out.print(Constants.").append(m.name).append(");\n");
            textMethods.add(m);
            lineNumber++;
        }

        public void appendGroovy(String s) {
            out.append(s);
        }
    }

    /**
     * This object encapsulate the generation of a method that outputs the specified text.
     */
    private static class TextContant {

        /** . */
        private final String name;

        /** . */
        private final String text;

        private TextContant(String name, String text) {
            this.name = name;
            this.text = text;
        }

        public String getDeclaration() {
            StringBuilder builder = new StringBuilder("");
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    builder.append("\\n");
                } else if (c == '\"') {
                    builder.append("\\\"");
                } else {
                    builder.append(c);
                }
            }
            return "public static final " + GroovyText.class.getName() + " " + name + " = new " + GroovyText.class.getName()
                    + "(\"" + builder + "\");";
        }
    }
}
