/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.gatein.portal.controller.resource.script;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.web.ServletContextImpl;
import org.gatein.common.io.IOTools;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.Module;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.gatein.portal.controller.resource.script.ScriptGraph;
import org.gatein.portal.controller.resource.script.ScriptResource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestModule extends AbstractGateInTest
{

   /** . */
   private ServletContext servletContext;

   /** . */
   private ClassLoader classLoader;

   @Override
   protected void setUp() throws Exception
   {
      URL classesURL = TestModule.class.getResource("");
      assertNotNull(classesURL);
      File classes = new File(classesURL.toURI());
      assertTrue(classes.exists());
      assertTrue(classes.isDirectory());
      ClassLoader classLoader = new URLClassLoader(new URL[]{new File(classes, "WEB-INF/classes").toURI().toURL()}, ClassLoader.getSystemClassLoader());
      ResourceBundle bundle = ResourceBundle.getBundle("bundle", Locale.ENGLISH, classLoader, Module.CONTROL);
      assertNotNull(bundle);

      //
      ServletContextImpl servletContext = new ServletContextImpl(TestModule.class, "/webapp", "webapp");
      assertNotNull(servletContext.getResource("/simple.js"));

      //
      this.servletContext = servletContext;
      this.classLoader = classLoader;
   }

   public void testScriptServing() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource module = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local bar = module.addLocalModule("/webapp", "/simple.js", null, 0);
      Reader reader = bar.read(null, servletContext, classLoader);
      assertReader("pass", reader);
   }

   public void testScriptNotFound() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource module = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local bar = module.addLocalModule("/webapp", "/notfound.js", null, 0);
      assertNull(bar.read(null, servletContext, classLoader));
   }

   public void testResolveNotLocalized() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", null, 0);
      Reader reader = module.read(null, servletContext, classLoader);
      assertReader("${foo}", reader);
   }

   public void testNotResolved() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/missing.js", "bundle", 0);
      Reader reader = module.read(null, servletContext, classLoader);
      assertReader("", reader);
   }
   public void testEscapeDoubleQuote() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "double_quote_bundle", 0);
      Reader reader = module.read(Locale.ENGLISH, servletContext, classLoader);
      assertReader("\"", reader);
   }

   public void testEscapeSimpleQuote() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "simple_quote_bundle", 0);
      Reader reader = module.read(Locale.ENGLISH, servletContext, classLoader);
      assertReader("'", reader);
   }

   public void testEnglishAsDefaultLocale() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "bundle", 0);
      Reader reader = module.read(null, servletContext, classLoader);
      assertReader("foo_en", reader);
   }

   public void testEnglishAsFallbackLocale() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "bundle", 0);
      Reader reader = module.read(Locale.CANADA, servletContext, classLoader);
      assertReader("foo_en", reader);
   }

   public void testSpecificLanguage() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "bundle", 0);
      Reader reader = module.read(Locale.FRENCH, servletContext, classLoader);
      assertReader("foo_fr", reader);
   }

   public void testSpecificCountry() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Module.Local module = foo.addLocalModule("/webapp", "/localized.js", "bundle", 0);
      Reader reader = module.read(Locale.FRANCE, servletContext, classLoader);
      assertReader("foo_fr_FR", reader);
   }
   
   public void testAdapter() throws Exception
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource foo = graph.addResource(ResourceScope.SHARED.create("module"));
      Content[] contents = new Content[] {new Content("var a;", false), new Content("/localized.js"), new Content("var b;", false)};
      Module.Local module = foo.addLocalModule("/webapp", contents, "bundle", 0);
      Reader reader = module.read(Locale.ENGLISH, servletContext, classLoader);
      assertReader("foo_en", reader);
   }

   private void assertReader(Object expected, Reader reader)
   {
      try
      {
         assertNotNull(reader);
         StringWriter script = new StringWriter();
         IOTools.copy(reader, script);
         ScriptEngineManager mgr = new ScriptEngineManager();
         ScriptEngine engine = mgr.getEngineByName("JavaScript");
         engine.eval(script.toString());
         Object test = engine.get("test");
         assertEquals(expected, test);
      }
      catch (Exception e)
      {
         throw failure(e);
      }
   }
}
