/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.exoplatform.commons.xml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

/**
 * Parses XML 1.0 and XML 1.1 declarations. This class can be used in a
 * situation where the actual encoding of an XML document is known but the
 * encoding stated in the XML declaration of the given XML file needs to be
 * determined, e.g. if it is necessary to find out if the declared encoding is
 * the same as the actual encoding.
 * 
 * Usage Example: <code>new XMLDeclarationParser("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").parse().get(XMLDeclarationParser.ENCODING)</code>
 * returns <code>"UTF-8"</code>
 * 
 * @author ppalaga@redhat.com
 * 
 */
public class XMLDeclarationParser
{

   public static final char APOS = '\'';
   public static final char CR = '\r';
   public static final char EQ = '=';
   public static final char GT = '>';
   public static final char LF = '\n';
   public static final char LT = '<';
   public static final char QUESTION_MARK = '?';
   public static final char QUOT = '"';
   public static final char SPACE = ' ';
   public static final char TAB = '\t';

   public static final String ENCODING = "encoding";
   public static final String STANDALONE = "standalone";
   public static final String VERSION = "version";
   public static final String XML = "xml";

   private static final int INVALID = -1;

   private boolean atEndOfInput = false;

   private Map<String, String> attributes = new HashMap<String, String>(4);

   private StringBuilder charBuffer = new StringBuilder(16);
   private int currentChar = INVALID;
   private Reader in;
   private int pos = 0;

   public XMLDeclarationParser(Reader in)
   {
      super();
      this.in = in;
   }

   public XMLDeclarationParser(String xml)
   {
      this(new StringReader(xml));
   }

   private void consumeOptionalWhiteSpace() throws IOException
   {
      while (true && !atEndOfInput)
      {
         int ch = current();
         switch (ch)
         {
         case SPACE:
         case TAB:
         case CR:
         case LF:
            next();
            break;
         default:
            return;
         }
      }
   }

   private int current() throws IOException
   {
      if (currentChar < 0)
      {
         next();
      }
      return currentChar;
   }

   private void ensureNotEndOfInput() throws SAXException,
         IOException
   {
      if (current() < 0)
      {
         throw new SAXException("Unexpected end of input.");
      }
   }

   private String key() throws IOException
   {
      charBuffer.setLength(0);
      ENDOFKEY: while (true && !atEndOfInput)
      {
         int ch = current();
         switch (ch)
         {
         case EQ:
         case SPACE:
         case TAB:
         case CR:
         case LF:
            break ENDOFKEY;
         default:
            charBuffer.append((char) ch);
            next();
         }
      }
      return charBuffer.toString();
   }

   private void keyVal() throws IOException, SAXException
   {
      String key = key();
      consumeOptionalWhiteSpace();
      match(EQ);
      consumeOptionalWhiteSpace();
      String value = value();
      attributes.put(key, value);
      consumeOptionalWhiteSpace();
   }

   private void match(char toMatch) throws SAXException,
         IOException
   {
      ensureNotEndOfInput();
      int ch = current();
      if (ch != toMatch)
      {
         throw new SAXException("Unexpected character '"
               + (char) ch + "' at position " + pos + "; expected '" + toMatch
               + "'.");
      }
      next();
   }

   private void match(String toMatch) throws SAXException,
         IOException
   {
      for (int i = 0; i < toMatch.length(); i++)
      {
         match(toMatch.charAt(i));
      }
   }

   private void matchWhiteSpace() throws IOException,
         SAXException
   {
      ensureNotEndOfInput();
      int ch = current();
      switch (ch)
      {
      case SPACE:
      case TAB:
      case CR:
      case LF:
         next();
         break;
      default:
         throw new SAXException(
               "Whitespace expected at postion " + pos
                     + " of an XML declaration.");
      }
      consumeOptionalWhiteSpace();
   }

   private int next() throws IOException
   {
      if (!atEndOfInput)
      {
         currentChar = in.read();
         pos++;
         if (currentChar < 0)
         {
            atEndOfInput = true;
         }
      }
      return currentChar;
   }

   public Map<String, String> parse() throws SAXException,
         IOException
   {
      match(LT);
      match(QUESTION_MARK);
      match(XML);
      matchWhiteSpace();

      while (current() != QUESTION_MARK && !atEndOfInput)
      {
         keyVal();
      }

      match(QUESTION_MARK);
      match(GT);

      return attributes;
   }

   private String value() throws IOException, SAXException
   {
      ensureNotEndOfInput();
      int quote = current();
      switch (quote)
      {
      case QUOT:
      case APOS:
         next();
         break;
      default:
         throw new SAXException("Unexpected character '"
               + (char) quote + "' at position " + pos + "; expected '" + QUOT
               + "' or '" + APOS + "'.");
      }

      charBuffer.setLength(0);
      ENDOFLITERAL: while (true && !atEndOfInput)
      {
         int ch = current();
         if (ch == quote)
         {
            next();
            break ENDOFLITERAL;
         }
         else
         {
            charBuffer.append((char) ch);
            next();
         }
      }
      return charBuffer.toString();
   }

}
