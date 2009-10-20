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

package org.exoplatform.services.html.parser;

import org.exoplatform.services.chars.CharsDecoder;
import org.exoplatform.services.chars.chardet.Detector;
import org.exoplatform.services.chars.chardet.ICharsetDetectionObserver;
import org.exoplatform.services.chars.chardet.PSMDetector;
import org.exoplatform.services.common.DataReader;
import org.exoplatform.services.common.ServicesContainer;
import org.exoplatform.services.common.ServiceConfig.ServiceType;
import org.exoplatform.services.html.HTMLDocument;
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.token.TypeToken;

import java.io.File;
import java.io.InputStream;

/**
 * @author nhuthuan
 * Email: nhudinhthuan@yahoo.com
 */
public final class HTMLParser
{

   private static String charset_ = null;

   private final static String READER_ID = "HTMLParserReader";

   public static synchronized HTMLNode clone(HTMLNode node)
   {
      NodeImpl nodeImpl = (NodeImpl)node;
      HTMLNode newNode = null;
      if (nodeImpl.getType() == TypeToken.CONTENT || nodeImpl.getType() == TypeToken.COMMENT)
      {
         newNode = new NodeImpl(nodeImpl.getValue(), nodeImpl.getName());
      }
      else
      {
         newNode = new NodeImpl(nodeImpl.getValue(), nodeImpl.getName(), nodeImpl.getType());
      }
      return newNode;
   }

   public static synchronized HTMLDocument createDocument(char[] data) throws Exception
   {
      HTMLDocument document = new HTMLDocument();
      CharsToken tokens = new CharsToken();
      tokens.setDocument(document);
      ParserService.getTokenParser().createBeans(tokens, data);
      ParserService.parse(tokens, document);
      return document;
   }

   public static synchronized HTMLDocument createDocument(String text) throws Exception
   {
      return createDocument(text.toCharArray());
   }

   public static synchronized HTMLDocument createDocument(byte[] data, String charset) throws Exception
   {
      if (charset == null)
         charset = detect(data);
      char[] chars = CharsDecoder.decode(charset, data, 0, data.length);
      return createDocument(chars);
   }

   public static synchronized HTMLDocument createDocument(InputStream input, String charset) throws Exception
   {
      DataReader reader = ServicesContainer.get(ServiceType.SOFT_REFERENCE, READER_ID, DataReader.class);
      return createDocument(reader.loadInputStream(input).toByteArray(), charset);
   }

   public static synchronized HTMLDocument createDocument(File file, String charset) throws Exception
   {
      DataReader reader = ServicesContainer.get(ServiceType.SOFT_REFERENCE, READER_ID, DataReader.class);
      return createDocument(reader.load(file), charset);
   }

   public static String detect(byte[] buf)
   {
      Detector det = new Detector(PSMDetector.ALL);
      charset_ = null;
      det.init(new ICharsetDetectionObserver()
      {
         public void notify(String charset)
         {
            charset_ = charset;
         }
      });

      boolean isAscii = true;
      int len = buf.length;

      isAscii = det.isAscii(buf, len);
      if (!isAscii)
         det.doIt(buf, len, false);
      det.dataEnd();

      if (isAscii)
         charset_ = "ASCII";
      return charset_;
   }

   public static String getCharset()
   {
      return charset_;
   }
}