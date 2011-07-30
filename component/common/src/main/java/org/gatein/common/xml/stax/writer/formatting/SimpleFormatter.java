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

package org.gatein.common.xml.stax.writer.formatting;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SimpleFormatter implements XmlStreamingFormatter
{

   public static final int DEFAULT_INDENT_SIZE = 3;
   public static final char DEFAULT_INDENT_CHAR = ' ';
   public static final String DEFAULT_NEWLINE;

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

   private String indentSequence;
   private String newline;
   private int depth = 0;

   private int previousEvent;

   public SimpleFormatter()
   {
      this(DEFAULT_INDENT_CHAR, DEFAULT_INDENT_SIZE, DEFAULT_NEWLINE);
   }

   public SimpleFormatter(char indentCharacter, int indentSize, String newline)
   {
      if (newline == null) throw new IllegalArgumentException("newline cannot be null");
      this.newline = newline;

      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < indentSize; i++)
      {
         sb.append(indentCharacter);
      }
      indentSequence = sb.toString();
   }

   public void before(XMLStreamWriter writer, int event) throws XMLStreamException
   {
      switch (event)
      {
         case START_ELEMENT:
            depth++;
            nl(writer);
            indent(writer);
            break;
         case END_ELEMENT:
            if (previousEvent == END_ELEMENT)
            {
               nl(writer);
               indent(writer);
            }
            depth--;
      }
   }

   public void after(XMLStreamWriter writer, int event) throws XMLStreamException
   {
      previousEvent = event;
   }

   private void nl(XMLStreamWriter writer) throws XMLStreamException
   {
      writer.writeCharacters(newline);
   }

   private void indent(XMLStreamWriter writer) throws XMLStreamException
   {
      for (int i = 0; i < depth - 1; i++)
      {
         writer.writeCharacters(indentSequence);
      }
   }
}
