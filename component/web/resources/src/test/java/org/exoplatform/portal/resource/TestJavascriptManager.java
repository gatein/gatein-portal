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

import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.web.application.JavascriptManager;

import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */

public class TestJavascriptManager extends AbstractWebResourceTest
{
   private JavascriptManager jsManager;

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      jsManager = new JavascriptManager();
   }
   
   public void testAddingScriptResources() throws IOException
   {
      Set<ResourceId> scriptResources = jsManager.getScriptResources();
      assertEquals(0, scriptResources.size());
      
      jsManager.loadScriptResource(ResourceScope.PORTAL, "foo");
      scriptResources = jsManager.getScriptResources();
      assertEquals(1, scriptResources.size());

      // Re-adding the same resource
      jsManager.loadScriptResource(ResourceScope.PORTAL, "foo");
      scriptResources = jsManager.getScriptResources();
      assertEquals(1, scriptResources.size());
      assertTrue(scriptResources.contains(new ResourceId(ResourceScope.PORTAL, "foo")));
   }
   
   public void testAddingJavascripts()
   {
      jsManager.addJavascript("foo  ");
      jsManager.addCustomizedOnLoadScript("bar");
      String expected = 
               "foo;\n" +
      		   "eXo.core.Browser.onLoad();\n" +
      		   "bar;\n";
      assertEquals(expected, jsManager.getJavaScripts());

      jsManager.importJavascript("eXo.webui.script");
      jsManager.importJavascript("eXo.webui.script-ext", "/webapp/jscript/");
      Set<String> importedJavaScripts = jsManager.getImportedJavaScripts();
      assertEquals(2, importedJavaScripts.size());
      assertTrue(importedJavaScripts.contains("/eXoResources/javascript/eXo/webui/script.js"));
      assertTrue(importedJavaScripts.contains("/webapp/jscript/eXo/webui/script-ext.js"));
   }
}
