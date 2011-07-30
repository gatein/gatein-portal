/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.common.xml.stax.writer;

import org.gatein.common.xml.stax.writer.builder.StaxFormatterBuilder;
import org.gatein.common.xml.stax.writer.builder.StaxFormatterBuilderImpl;
import org.gatein.common.xml.stax.writer.builder.StaxWriterBuilder;
import org.gatein.common.xml.stax.writer.builder.StaxWriterBuilderImpl;
import org.gatein.common.xml.stax.writer.formatting.XmlStreamingFormatter;
import org.staxnav.EnumElement;
import org.staxnav.Naming;
import org.staxnav.StaxNavException;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxWriterUtils
{
   public static <N> StaxWriter<N> createWriter(Naming<N> naming, OutputStream outputStream) throws StaxNavException
   {
      return buildDefaultWriter(outputStream).build(naming);
   }

   public static StaxWriter<String> createWriter(OutputStream outputStream) throws StaxNavException
   {
      return createWriter(new Naming.Local(), outputStream);
   }

   public static StaxWriter<QName> createQualifiedWriter(OutputStream outputStream) throws StaxNavException
   {
      return createWriter(new Naming.Qualified(), outputStream);
   }

   public static <E extends Enum<E> & EnumElement<E>> StaxWriter<E> createWriter(Class<E> enumeratedClass,
                                                                                 OutputStream outputStream) throws StaxNavException
   {
      Naming<E> naming;
      if (EnumElement.class.isAssignableFrom(enumeratedClass))
      {
         naming = new Naming.Enumerated.Mapped<E>(enumeratedClass, null);
      }
      else
      {
         naming = new Naming.Enumerated.Simple<E>(enumeratedClass, null);
      }

      return createWriter(naming, outputStream);
   }

   public static <N> StaxWriter<N> createWriter(Naming<N> naming, Writer writer) throws StaxNavException
   {
      return buildDefaultWriter().withWriter(writer).build(naming);
   }

   public static StaxWriter<String> createWriter(Writer writer) throws StaxNavException
   {
      return createWriter(new Naming.Local(), writer);
   }

   public static StaxWriter<QName> createQualifiedWriter(Writer writer) throws StaxNavException
   {
      return createWriter(new Naming.Qualified(), writer);
   }

   public static <E extends Enum<E> & EnumElement<E>> StaxWriter<E> createWriter(Class<E> enumeratedClass,
                                                                                 Writer writer) throws StaxNavException
   {
      Naming<E> naming;
      if (EnumElement.class.isAssignableFrom(enumeratedClass))
      {
         naming = new Naming.Enumerated.Mapped<E>(enumeratedClass, null);
      }
      else
      {
         naming = new Naming.Enumerated.Simple<E>(enumeratedClass, null);
      }

      return createWriter(naming, writer);
   }

   public static StaxWriterBuilder buildWriter()
   {
      return new StaxWriterBuilderImpl();
   }

   public static XmlStreamingFormatter createFormatter()
   {
      return buildFormatter().withNewline(DEFAULT_NEWLINE)
         .ofIndentSize(DEFAULT_INDENT_SIZE).withIndentCharacter(DEFAULT_INDENT_CHAR)
         .build();
   }

   public static XmlStreamingFormatter createFormatter(int indentSize)
   {
      return buildFormatter().withNewline(DEFAULT_NEWLINE)
         .ofIndentSize(indentSize).withIndentCharacter(DEFAULT_INDENT_CHAR)
         .build();
   }

   public static StaxFormatterBuilder buildFormatter()
   {
      return new StaxFormatterBuilderImpl();
   }

   public static <N> void writeOptionalElement(StaxWriter<N> writer, N element, String content)
   {
      if (content == null) return;

      writer.writeElement(element, content);
   }

   public static <N, V> void writeOptionalElement(StaxWriter<N> writer, N element, WritableValueType<V> valueType, V value)
   {
      if (value == null) return;

      writer.writeElement(element, valueType, value);
   }

   public static <N> void writeOptionalContent(StaxWriter<N> writer, N element, String content)
   {
      writer.writeStartElement(element);
      if (content != null)
      {
         writer.writeContent(content);
      }
      writer.writeEndElement();
   }

   public static <N, V> void writeOptionalContent(StaxWriter<N> writer, N element, WritableValueType<V> valueType, V value)
   {
      writer.writeStartElement(element);
      if (value != null)
      {
         writer.writeContent(valueType, value);
      }
      writer.writeEndElement();
   }

   private static StaxWriterBuilder buildDefaultWriter()
   {
      return buildWriter().withEncoding("UTF-8").withVersion("1.0")
         .withPropertyIfSupported("com.ctc.wstx.outputEscapeCr", Boolean.FALSE)
         .withFormatting(createFormatter());
   }

   private static StaxWriterBuilder buildDefaultWriter(OutputStream outputStream)
   {
      return buildWriter().withEncoding("UTF-8").withVersion("1.0")
         .withPropertyIfSupported("com.ctc.wstx.outputEscapeCr", Boolean.FALSE)
         .withFormatting(createFormatter())
         .withOutputStream(outputStream, "UTF-8");
   }

   private static final int DEFAULT_INDENT_SIZE = 3;
   private static final char DEFAULT_INDENT_CHAR = ' ';
   private static final String DEFAULT_NEWLINE;

   static
   {
      String newline = null;
      try
      {
         newline = System.getProperty("line.separator");
      }
      catch (Throwable ignored)
      {
      }
      if (newline == null) newline = "\n";

      DEFAULT_NEWLINE = newline;
   }

   private StaxWriterUtils(){}
}
