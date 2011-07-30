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


import org.gatein.common.xml.stax.writer.formatting.NoOpFormatter;
import org.gatein.common.xml.stax.writer.formatting.XmlStreamingFormatter;
import org.staxnav.Naming;
import org.staxnav.StaxNavException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxWriterImpl<N> implements StaxWriter<N>, XMLStreamConstants
{
   private Naming<N> naming;
   private XMLStreamWriter writer;
   private XmlStreamingFormatter formatter;

   private Deque<Element> elementStack = new ArrayDeque<Element>();

   public StaxWriterImpl(Naming<N> naming, XMLStreamWriter writer)
   {
      this(naming, writer, null);
   }

   public StaxWriterImpl(Naming<N> naming, XMLStreamWriter writer, XmlStreamingFormatter formatter)
   {
      this(naming, writer, formatter, null, null);
   }

   public StaxWriterImpl(Naming<N> naming, XMLStreamWriter writer, XmlStreamingFormatter formatter, String encoding, String version)
   {
      this.naming = naming;
      this.writer = writer;
      if (formatter == null) formatter = new NoOpFormatter();

      this.formatter = formatter;
      elementStack.push(new RootElement(writer, formatter, encoding, version));
   }

   public StaxWriter<N> writeAttribute(String name, String value)
   {
      if (name == null) throw new IllegalArgumentException("name cannot be null");
      if (value == null) throw new IllegalArgumentException("value cannot be null");

      Element current = elementStack.peek();
      current.writeAttribute(name, value);
      return this;
   }

   public StaxWriter<N> writeAttribute(QName name, String value)
   {
      if (name == null) throw new IllegalArgumentException("name cannot be null");

      Element current = elementStack.peek();
      current.writeAttribute(name, value);
      return this;
   }

   public StaxWriter<N> writeStartElement(N element) throws StaxNavException
   {
      Element current = elementStack.peek();
      if (current instanceof RootElement)
      {
         if (((RootElement) current).started)
         {
            current = new StreamElement(writer, formatter);
            elementStack.push(current);
         }
      }
      else
      {
         current = new StreamElement(writer, formatter);
         elementStack.push(current);
      }

      current.writeStartElement(naming, element);
      return this;
   }

   public StaxWriter<N> writeEndElement() throws StaxNavException
   {
      if (elementStack.isEmpty()) throw new StaxNavException(null, "No matching startElement was found for this endElement");

      elementStack.pop().end();

      return this;
   }

   public StaxWriter<N> writeContent(String content) throws StaxNavException
   {
      return writeContent(WritableValueTypes.STRING, content);
   }

   public <V> StaxWriter<N> writeContent(WritableValueType<V> valueType, V content) throws StaxNavException
   {
      if (valueType == null) throw new IllegalArgumentException("valueType cannot be null.");
      if (content == null) throw new IllegalArgumentException("content cannot be null.");

      Element current = elementStack.peek();
      current.writeContent(valueType.format(content));

      return this;
   }

   public <V> StaxWriter<N> writeElement(N element, String content) throws StaxNavException
   {
      return writeElement(element, WritableValueTypes.STRING, content);
   }

   public <V> StaxWriter<N> writeElement(N element, WritableValueType<V> valueType, V content) throws StaxNavException
   {
      writeStartElement(element).writeContent(valueType, content).writeEndElement();
      return this;
   }

   public StaxWriter<N> writeNamespace(String prefix, String uri) throws StaxNavException
   {
      Element current = elementStack.peek();
      current.writeNamespace(prefix, uri);
      return this;
   }

   public StaxWriter<N> writeDefaultNamespace(String uri) throws StaxNavException
   {
      Element current = elementStack.peek();
      current.writeDefaultNamespace(uri);
      return this;
   }

   public StaxWriter<N> writeComment(final String comment) throws StaxNavException
   {
      Element current = elementStack.peek();
      current.writeComment(comment);
      return this;
   }

   public StaxWriter<N> writeCData(String cdata) throws StaxNavException
   {
      Element current = elementStack.peek();
      current.writeCData(cdata);
      return this;
   }

   public void finish() throws StaxNavException
   {
      while (!elementStack.isEmpty())
      {
         elementStack.pop().end();
      }
   }

   private static abstract class Element
   {
      abstract void writeAttribute(String name, String value) throws StaxNavException;

      abstract <N> void writeAttribute(QName name, String value) throws StaxNavException;

      abstract <N> void writeStartElement(Naming<N> naming, N name) throws StaxNavException;

      abstract void writeContent(String content) throws StaxNavException;

      abstract void writeNamespace(String prefix, String uri) throws StaxNavException;

      abstract void writeDefaultNamespace(String uri) throws StaxNavException;

      abstract void writeComment(String comment) throws StaxNavException;

      abstract void writeCData(String cdata) throws StaxNavException;

      abstract void end();

      XMLStreamWriter writer;
      XmlStreamingFormatter formatter;
      private List<StreamClosure> closures;

      Element(XMLStreamWriter writer, XmlStreamingFormatter formatter)
      {
         this(writer, formatter, null);
      }

      Element(XMLStreamWriter writer, XmlStreamingFormatter formatter, List<StreamClosure> closures)
      {
         this.writer = writer;
         this.formatter = formatter;
         this.closures = closures;
      }

      void apply(int event, StreamClosure closure) throws StaxNavException
      {
         try
         {
            formatter.before(writer, event);
            closure.execute(writer);
            formatter.after(writer, event);
         }
         catch (XMLStreamException e)
         {
            throw new StaxNavException(e);
         }
      }
   }

   private static class RootElement extends Element
   {
      private String encoding;
      private String version;
      private Element element;
      private boolean started;

      RootElement(XMLStreamWriter writer, XmlStreamingFormatter formatter, String encoding, String version)
      {
         super(writer, formatter);
         this.encoding = encoding;
         this.version = version;
      }

      public void writeAttribute(String name, String value) throws StaxNavException
      {
         get().writeAttribute(name, value);
      }

      public <N> void writeAttribute(QName name, String value) throws StaxNavException
      {
         get().writeAttribute(name, value);
      }

      public <N> void writeStartElement(Naming<N> naming, N name) throws StaxNavException
      {
         get().writeStartElement(naming, name);
         started = true;
      }

      public void writeContent(String content) throws StaxNavException
      {
         get().writeContent(content);
      }

      public void writeNamespace(String prefix, String uri) throws StaxNavException
      {
         get().writeNamespace(prefix, uri);
      }

      public void writeDefaultNamespace(String uri) throws StaxNavException
      {
         get().writeDefaultNamespace(uri);
      }

      public void writeComment(String comment) throws StaxNavException
      {
         get().writeComment(comment);
      }

      public void writeCData(String cdata) throws StaxNavException
      {
         get().writeCData(cdata);
      }

      public void end()
      {
         if (started)
         {
            get().end();
         }
         endDocument();
      }

      private Element get()
      {
         if (element == null)
         {
            startDocument();
            element = new StreamElement(writer, formatter);
         }
         return element;
      }

      private void startDocument() throws StaxNavException
      {
         apply(START_DOCUMENT, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               if (encoding == null && version == null)
               {
                  writer.writeStartDocument();
               }
               else if (encoding == null)
               {
                  writer.writeStartDocument(version);
               }
               else
               {
                  writer.writeStartDocument(encoding, version);
               }
            }
         });
      }

      private void endDocument() throws StaxNavException
      {
         try
         {
            apply(END_DOCUMENT, new StreamClosure()
            {
               public void execute(XMLStreamWriter writer) throws XMLStreamException
               {
                  writer.writeEndDocument();
               }
            });

            writer.flush();
         }
         catch (XMLStreamException e)
         {
            throw new StaxNavException(e);
         }
         finally
         {
            try
            {
               writer.close();
            }
            catch (XMLStreamException e)
            {
            }
         }
      }
   }

   private static class StreamElement extends Element
   {
      StreamElement(XMLStreamWriter writer, XmlStreamingFormatter formatter)
      {
         super(writer, formatter);
      }

      public void writeAttribute(final String name, final String value) throws StaxNavException
      {
         apply(ATTRIBUTE, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeAttribute(name, value);
            }
         });
      }

      public <N> void writeAttribute(QName name, final String value) throws StaxNavException
      {
         final String prefix = name.getPrefix();
         final String uri = name.getNamespaceURI();
         final String localPart = name.getLocalPart();

         apply(ATTRIBUTE, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               if (uri == null || XMLConstants.NULL_NS_URI.equals(uri))
               {
                  writer.writeAttribute(localPart, value);
               }
               else if (prefix == null || XMLConstants.DEFAULT_NS_PREFIX.equals(prefix))
               {
                  writer.writeAttribute(uri, localPart, value);
               }
               else
               {
                  writer.writeAttribute(prefix, uri, localPart, value);
               }
            }
         });
      }

      public <N> void writeStartElement(Naming<N> naming, N name) throws StaxNavException
      {
         final String prefix = naming.getPrefix(name);
         final String uri = naming.getURI(name);
         final String localPart = naming.getLocalPart(name);

         apply(START_ELEMENT, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               if (uri == null || XMLConstants.NULL_NS_URI.equals(uri))
               {
                  writer.writeStartElement(localPart);
               }
               else if (prefix == null || XMLConstants.DEFAULT_NS_PREFIX.equals(prefix))
               {
                  writer.writeStartElement(uri, localPart);
               }
               else
               {
                  writer.writeStartElement(prefix, localPart, uri);
               }
            }
         });
      }

      public void writeContent(final String content) throws StaxNavException
      {
         apply(CHARACTERS, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeCharacters(content);
            }
         });
      }

      public void writeNamespace(final String prefix, final String uri) throws StaxNavException
      {
         apply(NAMESPACE, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeNamespace(prefix, uri);
            }
         });
      }

      public void writeDefaultNamespace(final String uri) throws StaxNavException
      {
         apply(NAMESPACE, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeDefaultNamespace(uri);
            }
         });
      }

      public void writeComment(final String comment) throws StaxNavException
      {
         apply(COMMENT, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeComment(comment);
            }
         });
      }

      public void writeCData(final String cdata) throws StaxNavException
      {
         apply(CDATA, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeCData(cdata);
            }
         });
      }

      public void end()
      {
         apply(END_ELEMENT, new StreamClosure()
         {
            public void execute(XMLStreamWriter writer) throws XMLStreamException
            {
               writer.writeEndElement();
            }
         });
      }
   }

   private static interface StreamClosure
   {
      void execute(XMLStreamWriter writer) throws XMLStreamException;
   }
}
