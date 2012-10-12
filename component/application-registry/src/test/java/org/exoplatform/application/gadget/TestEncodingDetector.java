/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.application.gadget;

import junit.framework.TestCase;
import org.junit.Test;
import org.xml.sax.SAXParseException;
import java.io.ByteArrayInputStream;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 10/12/12
 */
public class TestEncodingDetector extends TestCase
{

   @Test
   public void testXML() throws Exception
   {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
         "<web-app></web-app>";
      String encode = EncodingDetector.detect(new ByteArrayInputStream(xml.getBytes()));
      assertEquals("UTF-8", encode);

      xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +
         "<web-app></web-app>";
      encode = EncodingDetector.detect(new ByteArrayInputStream(xml.getBytes()));
      //Not a bug, look at http://stackoverflow.com/questions/3482494/howto-let-the-sax-parser-determine-the-encoding-from-the-xml-declaration
      assertEquals("UTF-8", encode);
   }

   public void testXMLWithCorrectLocationDTD() throws Exception
   {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
         "<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\" >" +
         "<web-app></web-app>";
      String encode = EncodingDetector.detect(new ByteArrayInputStream(xml.getBytes()));
      assertEquals("UTF-8", encode);
   }

   @Test
   public void testXMLWithWrongLocationDTD() throws Exception
   {
      String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
         "<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtdaaa/web-app_2_3.dtd\" >" +
         "<web-app></web-app>";
      try
      {
         String encode = EncodingDetector.detect(new ByteArrayInputStream(xml.getBytes()));
         assertEquals("UTF-8", encode);
      }
      catch (SAXParseException parseEx)
      {
         fail("Must not encounter exception here as we have disabled 'loading DTD' feature in SAXParser");
      }
   }
}
