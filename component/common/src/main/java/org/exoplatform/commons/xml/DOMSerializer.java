/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.commons.xml;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.text.EntityEncoder;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * An high performance and custom DOM serializer based on stax {@link XMLStreamWriter}.
 *
 * <p>The serializer takes care of correctly writing empty script elements with their non empty form, because we want to ouput
 * xhtml text that will still work on html browsers.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DOMSerializer
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(DOMSerializer.class);

   /** Thread safe. */
   private static final XMLOutputFactory outputFactory;

   /** . */
   private static final String DEFAULT_XML_OUTPUT_FACTORY = "com.sun.xml.internal.stream.XMLOutputFactoryImpl";

   static
   {
      XMLOutputFactory tmp;
      try
      {
         Class<XMLOutputFactory> cl = (Class<XMLOutputFactory>)Thread.currentThread().getContextClassLoader().loadClass(DEFAULT_XML_OUTPUT_FACTORY);
         tmp = cl.newInstance();
      }
      catch (Exception e)
      {
         tmp = XMLOutputFactory.newInstance();
         log.warn("Could not instantiate " + DEFAULT_XML_OUTPUT_FACTORY + " will use default provided by runtime instead " +
            tmp.getClass().getName());
      }

      //
      outputFactory = tmp;
   }

   public static void serialize(Element element, Writer writer) throws IOException, XMLStreamException
   {
      XMLStreamWriter xml = outputFactory.createXMLStreamWriter(writer);
      serialize(element, xml);
      xml.writeEndDocument();
      xml.flush();
   }

   private static void serialize(Element element, XMLStreamWriter writer) throws IOException, XMLStreamException
   {
      String tagName = element.getTagName();

      // Determine if empty
      // Note that we won't accumulate the elements that would be serialized for performance reason
      // we will just reiterate later before ending the element
      boolean empty;
      if (tagName.equalsIgnoreCase("script"))
      {
         empty = false;
      }
      else
      {
         empty = true;
         NodeList children = element.getChildNodes();
         int length = children.getLength();
         for (int i = 0;i < length && empty;i++)
         {
            Node child = children.item(i);
            if (child instanceof CharacterData)
            {
               empty = false;
            }
            else if (child instanceof Element)
            {
               empty = false;
            }
         }
      }

      //
      if (empty)
      {
         writer.writeEmptyElement(tagName);
      }
      else
      {
         writer.writeStartElement(tagName);
      }

      // Write attributes
      if (element.hasAttributes())
      {
         NamedNodeMap attrs = element.getAttributes();
         int length = attrs.getLength();
         for (int i = 0;i < length;i++)
         {
            Attr attr = (Attr)attrs.item(i);
            writer.writeAttribute(attr.getName(), attr.getValue());
         }
      }

      //
      if (!empty)
      {
         // Serialize children that are worth to be
         NodeList children = element.getChildNodes();
         int length = children.getLength();
         for (int i = 0;i < length;i++)
         {
            Node child = children.item(i);
            if(child instanceof CDATASection)
            {
               writer.writeCData(((CDATASection)child).getData());
            }
            else if (child instanceof CharacterData)
            {
               writeTextData(writer, ((CharacterData)child).getData());
            }
            else if (child instanceof Element)
            {
               serialize((Element)child, writer);
            }
         }

         // Close
         writer.writeEndElement();
      }
   }
   
   private static void writeTextData(XMLStreamWriter writer, String data) throws XMLStreamException
   {
      StringBuilder builder = new StringBuilder();
      
      for(int i = 0; i < data.length(); i++)
      {
         char c = data.charAt(i);
         String encodedValue = EntityEncoder.FULL.lookup(c);
         
         if(encodedValue == null)
         {
            builder.append(c);
         }
         else
         {
            builder.append(encodedValue);
         }
      }
      
      writer.writeCharacters(builder.toString());
   }
}
