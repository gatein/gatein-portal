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

package org.exoplatform.portal.resource;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceScope;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestParser extends AbstractGateInTest
{
   
   public void testShared() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<module>" +
         "<name>foo</name>" +
         "<script>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</script>" +
         "<depends>" +
         "<module>bar</module>" +
         "</depends>" +
         "<depends>" +
         "<module>juu</module>" +
         "</depends>" +
         "</module>" +

         "<scripts>" +
         "<name>foo_scripts</name>" +
         "<script>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</script>" +
         "<depends>" +
         "<scripts>bar</scripts>" +
         "</depends>" +
         "<depends>" +
         "<scripts>juu</scripts>" +
         "</depends>" +
         "</scripts>" +

         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(2, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.SHARED, "foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());

      desc = scripts.get(1);
      assertEquals(new ResourceId(ResourceScope.SHARED, "foo_scripts"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
   }

   public void testPortlet() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portlet>" +
         "<name>foo</name>" +
         "<module>" +
         "<script>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</script>" +
         "<depends>" +
         "<module>bar</module>" +
         "</depends>" +
         "<depends>" +
         "<module>juu</module>" +
         "</depends>" +
         "</module>" +
         "</portlet>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.PORTLET, "mypath/foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
   }

   public void testPortal() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portal>" +
         "<name>foo</name>" +
         "<module>" +
         "<script>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</script>" +
         "<depends>" +
         "<module>bar</module>" +
         "</depends>" +
         "<depends>" +
         "<module>juu</module>" +
         "</depends>" +
         "</module>" +
         "</portal>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.PORTAL, "foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar")), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"))), desc.getDependencies());
   }
   
   public void testModules() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portal>" +
         "<name>foo</name>" +
         "<module>" +
         "<script>" +
         "<name>local_module</name>" +
         "<path>/local_module.js</path>" +
         "</script>" +
         "</module>" +
         "</portal>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      
      List<Javascript> modules = desc.getModules();
      assertEquals(1, modules.size());
      
      Javascript local = modules.get(0); 
      assertTrue(local instanceof Javascript.Local);
      assertEquals("local_module", local.getModule());
   }
   
   public void testResourceBundle() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portal>" +
         "<name>foo</name>" +
         "<module>" +
         "<script>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "<resource-bundle>my_bundle</resource-bundle>" +
         "</script>" +
         "</module>" +
         "</portal>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.PORTAL, "foo"), desc.getId());
      assertEquals(1, desc.getModules().size());
      Javascript.Local js = (Javascript.Local) desc.getModules().get(0);
      assertEquals("my_bundle", js.getResourceBundle());
   }
   
   public void testSupportedLocales() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portal>" +
         "<name>foo</name>" +
         "<module>" +
         "<supported-locale>EN</supported-locale>" +
         "<supported-locale>FR-fr</supported-locale>" +
         "</module>" +
         "</portal>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      List<Locale> locales = desc.getSupportedLocales();
      assertEquals(Arrays.asList(Locale.ENGLISH, Locale.FRANCE), locales);
   }

   public void testRemoteResource() throws Exception
   {

      String validConfig = "" +
         "<gatein-resources xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.gatein.org/xml/ns/gatein_resources_1_2_1 http://www.gatein.org/xml/ns/gatein_resources_1_2_1\" xmlns=\"http://www.gatein.org/xml/ns/gatein_resources_1_2_1\">" +
         "<module><name>foo</name><url>http://jquery.com/jquery.js</url></module>" +
         "</gatein-resources>";

      JavascriptConfigParser parser = new JavascriptConfigParser("mypath");
      List<ScriptResourceDescriptor> descs = parser.parseConfig(new ByteArrayInputStream(validConfig.getBytes("UTF-8")));

      assertEquals(1, descs.size());
      ScriptResourceDescriptor desc = descs.get(0);
      List<Javascript> scripts = desc.getModules();
      assertEquals(1, scripts.size());
      assertTrue(scripts.get(0) instanceof Javascript.Remote);
   }
}
