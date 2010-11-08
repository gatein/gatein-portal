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

import junit.framework.TestCase;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestDOMSerializer extends TestCase
{

   @Override
   protected void setUp() throws Exception
   {
   }

   public void testScriptNoAttributes() throws Exception
   {
      assertSerialization("<script></script>", "<script/>");
   }

   public void testScriptWithAttribute() throws Exception
   {
      assertSerialization("<script type=\"text/javascript\"></script>", "<script type='text/javascript'/>");
   }

   public void testMetaNoAttributes() throws Exception
   {
      assertSerialization("<meta/>", "<meta/>");
   }

   public void testMetaWithAttribute() throws Exception
   {
      assertSerialization("<meta http-equiv=\"Content-Type\"/>", "<meta http-equiv='Content-Type'></meta>");
   }
   
   public void testOrdinaryTextElement() throws Exception
   {
      assertSerialization("<div>Blah Blah</div>", "<div>Blah Blah</div>");
   }
   
   public void testCDATaElement() throws Exception
   {
      assertSerialization("<div><![CDATA[Test Content]]></div>", "<div><![CDATA[Test Content]]></div>");
   }

   private void assertSerialization(String expectedMarkup, String markup) throws Exception
   {
      Element elt = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(markup))).getDocumentElement();
      StringWriter writer = new StringWriter();
      DOMSerializer.serialize(elt, writer);
      assertEquals(expectedMarkup, writer.toString());
   }
}
