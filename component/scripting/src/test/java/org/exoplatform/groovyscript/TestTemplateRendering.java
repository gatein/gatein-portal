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

import org.exoplatform.commons.utils.CharsetTextEncoder;
import org.exoplatform.commons.utils.OutputStreamPrinter;
import org.exoplatform.component.test.AbstractGateInTest;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestTemplateRendering extends AbstractGateInTest
{

   private DateFormat dateFormatFR;
   private DateFormat dateFormatEN;
   
   @Override
   protected void setUp() throws Exception
   {
      // TODO Auto-generated method stub
      super.setUp();
      
      dateFormatFR = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.FRANCE);
      dateFormatEN = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
   }

   public void testOutputStreamWriter() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("a<%='b'%>c<%out.print('d');%>e");
      ByteArrayOutputStream  baos = new ByteArrayOutputStream();
      OutputStreamPrinter writer = new OutputStreamPrinter(CharsetTextEncoder.getUTF8(), baos);
      template.render(writer);
      writer.close();
      assertEquals("abcde", baos.toString("UTF-8"));
   }

   public void testDate1() throws Exception
   {
      Date dateToTest = new Date(0);
      GroovyTemplate template = new GroovyTemplate("<% print(new Date(0)); %>");
      assertEquals(dateFormatFR.format(dateToTest), template.render(Locale.FRENCH));
      assertEquals(dateFormatEN.format(dateToTest), template.render(Locale.ENGLISH));
      assertEquals(dateToTest.toString(), template.render());
   }

   public void testDate2() throws Exception
   {
      Date dateToTest = new Date(0);
      GroovyTemplate template = new GroovyTemplate("<% def date = new Date(0) %>$date");
      System.out.println("template.getGroovy() = " + template.getGroovy());
      assertEquals(dateFormatFR.format(dateToTest), template.render(Locale.FRENCH));
      assertEquals(dateFormatEN.format(dateToTest), template.render(Locale.ENGLISH));
      assertEquals(dateToTest.toString(), template.render());
   }

   public void testDate3() throws Exception
   {
      Date dateToTest = new Date(0);
      GroovyTemplate template = new GroovyTemplate("<%= new Date(0) %>");
      System.out.println("template.getGroovy() = " + template.getGroovy());
      assertEquals(dateFormatFR.format(dateToTest), template.render(Locale.FRENCH));
      assertEquals(dateFormatEN.format(dateToTest), template.render(Locale.ENGLISH));
      assertEquals(dateToTest.toString(), template.render());
   }

   public void testFoo() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("a");
      String render = template.render();
      assertEquals("a", render);
   }

   public void testBar() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<%='a'%>");
      String render = template.render();
      assertEquals("a", render);
   }

   public void testFooBar() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("a<%='b'%>c");
      String render = template.render();
      assertEquals("abc", render);
   }

   public void testJuu() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% out.print(\"a\"); %>");
      String render = template.render();
      assertEquals("a", render);
   }

   public void testLineBreak() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("\n");
      String render = template.render();
      assertEquals("\n", render);
   }

   public void testMultiLine() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate(
         "a\n" +
         "b\n" +
         "<%= 'c' %>\n" +
         "d"
      );
      String render = template.render();
      assertEquals("a\nb\nc\nd", render);
   }

   public void testIf() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate(
         "a\n" +
         "<% if (true) {\n %>" +
         "b\n" +
         "<% } %>");
      String s = template.render();
      assertEquals("a\nb\n", s);
   }

   public void testLineComment() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% // foo %>a\nb");
      String s = template.render();
      assertEquals("a\nb", s);
   }

   public void testContextResolution() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<%= foo %>");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("bar", s);
   }

   public void testGString() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("$foo");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("bar", s);
   }

   public void testQuoteAfterGString() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("$foo\"");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("bar\"", s);
   }

   public void testDollarInExpression() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<%= \"$foo\" %>");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("bar", s);
   }

   public void testEscapeDollarInExpression() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<%= \"\\$foo\" %>");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("$foo", s);
   }

   public void testEscapeDollarInText() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("\\$foo");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("$foo", s);
   }

   public void testDollarInScriplet() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% out.print(\"$foo\") %>");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("bar", s);
   }

   public void testEscapeDollarInScriplet() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% out.print(\"\\$foo\") %>");
      Map<String, String> context = new HashMap<String, String>();
      context.put("foo", "bar");
      String s = template.render(context);
      assertEquals("$foo", s);
   }

   public void testQuote() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("\"");
      String s = template.render();
      assertEquals("\"", s);
   }

   public void testFooFoo() throws Exception
   {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("UIPortalApplication.gtmpl");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[256];
      for (int l = in.read(buffer);l != -1;l = in.read(buffer))
      {
         baos.write(buffer, 0, l);
      }
      String gtmpl = baos.toString("UTF-8");
      GroovyTemplate template = new GroovyTemplate(gtmpl);
   }

   public void testException() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% throw new java.awt.AWTException(); %>");
      try
      {
         template.render();
         fail();
      }
      catch (TemplateRuntimeException e)
      {
         assertTrue(e.getCause() instanceof AWTException);
      }
   }

   public void testRuntimeException() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% throw new java.util.EmptyStackException(); %>");
      try
      {
         template.render();
         fail();
      }
      catch (TemplateRuntimeException e)
      {
         assertTrue(e.getCause() instanceof EmptyStackException);
      }
   }

   public void testIOException() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% throw new java.io.IOException(); %>");
      try
      {
         template.render();
         fail();
      }
      catch (IOException e)
      {
      }
   }

   public void testError() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% throw new java.awt.AWTError(); %>");
      try
      {
         template.render();
         fail();
      }
      catch (AWTError e)
      {
      }
   }

   public void testThrowable() throws Exception
   {
      GroovyTemplate template = new GroovyTemplate("<% throw new Throwable(); %>");
      try
      {
         template.render();
         fail();
      }
      catch (Throwable t)
      {
      }
   }

   public void testScriptLineNumber() throws Exception
   {
      testLineNumber("<%");
      assertLineNumber(2, "throw new Exception('e')", "<%\nthrow new Exception('e')%>");
   }

   public void testExpressionLineNumber() throws Exception
   {
      testLineNumber("<%=");
   }

   private void testLineNumber(String prolog) throws Exception
   {
      assertLineNumber(1, "throw new Exception('a')", prolog + "throw new Exception('a')%>");
      assertLineNumber(1, "throw new Exception('b')", "foo" + prolog + "throw new Exception('b')%>");
      assertLineNumber(2, "throw new Exception('c')", "foo\n" + prolog + "throw new Exception('c')%>");
      assertLineNumber(1, "throw new Exception('d')", "<%;%>foo" + prolog + "throw new Exception('d')%>");
   }

   public static Object out;

   public void testWriterAccess() throws Exception
   {
      out = null;
      Writer writer = new StringWriter();
      GroovyTemplate template = new GroovyTemplate("<% " + TestTemplateRendering.class.getName() + ".out = out; %>");
      template.render(writer);
      assertNotNull(out);
   }

   private void assertLineNumber(int expectedLineNumber, String expectedText, String script) throws TemplateCompilationException, IOException
   {
      GroovyTemplate template = new GroovyTemplate(script);
      try
      {
         template.render();
         fail();
      }
      catch (TemplateRuntimeException t)
      {
         assertEquals(expectedText, t.getText());
         assertEquals(expectedLineNumber, (Object)t.getLineNumber());
         StackTraceElement scriptElt = null;
         for (StackTraceElement elt : t.getCause().getStackTrace())
         {
            if (elt.getClassName().equals(template.getClassName()))
            {
               scriptElt = elt;
               break;
            }
         }
         assertEquals(expectedLineNumber, scriptElt.getLineNumber());
      }
   }

}
