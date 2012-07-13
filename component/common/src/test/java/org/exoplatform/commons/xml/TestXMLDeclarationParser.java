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
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.SAXException;

import junit.framework.Assert;
import junit.framework.TestCase;

public class TestXMLDeclarationParser extends TestCase
{
   
   private static Map<String, String> map(String... keyVal) {
      Map<String, String> result = new HashMap<String, String>(4);
      int i = 0;
      while (i < keyVal.length) {
         result.put(keyVal[i++], keyVal[i++]);
      }
      return result;
   }
   
   public void testUnexpectedEndOfInput() throws SAXException, IOException {
      assertFail("<?");
      assertFail("<?xml");
      assertFail("<?xml ");
      assertFail("<?xml version");
      assertFail("<?xml version=");
      assertFail("<?xml version= ");
      assertFail("<?xml version='");
      assertFail("<?xml version='1.0'");
      assertFail("<?xml version='1.0' ");
      assertFail("<?xml version='1.0' encoding='UTF-8'?");
   }
   
   private static void assertFail(String xml) throws IOException {
      XMLDeclarationParser parser = new XMLDeclarationParser(xml);
      try
      {
         parser.parse();
         fail("XMLDeclarationParseException expected.");
      }
      catch (SAXException e)
      {
      }
   }
   
   public void testParse() throws SAXException, IOException {
      /* with apos */
      XMLDeclarationParser parser = new XMLDeclarationParser("<?xml version='1.0' encoding='UTF-8'?>");
      Assert.assertEquals(map("version", "1.0", "encoding", "UTF-8"), parser.parse());
      
      /* with quot */
      parser = new XMLDeclarationParser("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      Assert.assertEquals(map("version", "1.0", "encoding", "UTF-8"), parser.parse());

      /* with some white space */
      parser = new XMLDeclarationParser("<?xml    version =\n\"1.0\" \t\t encoding=\r\"UTF-8\" ?>");
      Assert.assertEquals(map("version", "1.0", "encoding", "UTF-8"), parser.parse());
      
   }
}
