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
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestParser extends AbstractGateInTest
{
   
   public void testShared() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<scripts>" +
         "<name>foo</name>" +
         "<module>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</module>" +
         "<depends>" +
         "<scripts>bar</scripts>" +
         "</depends>" +
         "<depends>" +
         "<scripts>juu</scripts>" +
         "<mode>on-load</mode>" +
         "</depends>" +
         "</scripts>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.SHARED, "foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar"), false), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"), true)), desc.getDependencies());
   }

   public void testPortlet() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portlet>" +
         "<name>foo</name>" +
         "<scripts>" +
         "<module>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</module>" +
         "<depends>" +
         "<scripts>bar</scripts>" +
         "</depends>" +
         "<depends>" +
         "<scripts>juu</scripts>" +
         "<mode>on-load</mode>" +
         "</depends>" +
         "</scripts>" +
         "</portlet>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.PORTLET, "mypath/foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar"), false), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"), true)), desc.getDependencies());
   }

   public void testPortal() throws Exception
   {
      String config = "" +
         "<gatein-resources>" +
         "<portal>" +
         "<name>foo</name>" +
         "<scripts>" +
         "<module>" +
         "<name>foo_module</name>" +
         "<path>/foo_module.js</path>" +
         "</module>" +
         "<depends>" +
         "<scripts>bar</scripts>" +
         "</depends>" +
         "<depends>" +
         "<scripts>juu</scripts>" +
         "<mode>on-load</mode>" +
         "</depends>" +
         "</scripts>" +
         "</portal>" +
         "</gatein-resources>";

      //
      JavascriptConfigParser parser = new JavascriptConfigParser("/mypath");
      List<ScriptResourceDescriptor> scripts = parser.parseConfig(new ByteArrayInputStream(config.getBytes("UTF-8")));
      assertEquals(1, scripts.size());
      ScriptResourceDescriptor desc = scripts.get(0);
      assertEquals(new ResourceId(ResourceScope.PORTAL, "foo"), desc.getId());
      assertEquals(Arrays.asList(new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "bar"), false), new DependencyDescriptor(new ResourceId(ResourceScope.SHARED, "juu"), true)), desc.getDependencies());
   }
}
