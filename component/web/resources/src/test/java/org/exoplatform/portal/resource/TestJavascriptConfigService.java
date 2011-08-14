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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 *
 */
public class TestJavascriptConfigService extends AbstractWebResourceTest
{
   private JavascriptConfigService jsService;

   private ServletContext mockServletContext;
   
   private static final String exModule = "extended.js.test";
   
   private static final String exPath = "/extended/js/test.js";
   
   private static final ServletContext exContext = new MockJSServletContext("extendedWebApp", null);

   @Override
   protected void setUp() throws Exception
   {
      final PortalContainer portalContainer = getContainer();
      jsService = (JavascriptConfigService)portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
            
      Map<String, String> resources = new HashMap<String, String>(4);
      resources.put("/js/test1.js", "aaa;");
      resources.put("/js/test2.js", "bbb;");
      resources.put("/js/test3.js", "ccc;");
      resources.put("/js/test4.js", "ddd;");      
      mockServletContext = new MockJSServletContext("mockwebapp", resources);
      
      URL url = portalContainer.getPortalClassLoader().getResource("mockwebapp/gatein-resources.xml");
      JavascriptConfigParser.processConfigResource(url.openStream(), jsService, mockServletContext);
   }

   public void testAvailableScripts()
   {
      assertEquals(4, jsService.getAvailableScripts().size());
      assertTrue(jsService.isModuleLoaded("js.test1"));
      assertTrue(jsService.isModuleLoaded("js.test2"));
      assertTrue(jsService.isModuleLoaded("js.test3"));
      assertTrue(jsService.isModuleLoaded("js.test4"));
      assertFalse(jsService.isModuleLoaded("js.test5"));            
   }

   public void testPriority()
   {
      Iterator<String> availPaths = jsService.getAvailableScriptsPaths().iterator();      
      assertEquals(mockServletContext.getContextPath() + "/js/test2.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test4.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test1.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test3.js", availPaths.next());
      assertFalse(availPaths.hasNext());
   }
   
   public void testExtendedJS() throws Exception
   {
      jsService.addExtendedJavascript(exModule, exPath, exContext, "extendedJS;");
      assertTrue(jsService.isModuleLoaded(exModule));
      assertTrue(jsService.getAvailableScriptsPaths().contains(exContext.getContextPath() + exPath));
      assertEquals("\nbbb;ddd;aaa;ccc;extendedJS;", new String(jsService.getMergedJavascript()));
  
      long first = jsService.getLastModified();
      assertTrue(first < System.currentTimeMillis());            
            
      jsService.removeExtendedJavascript(exModule, exPath, exContext);
      
      assertFalse(jsService.isModuleLoaded(exModule));
      assertFalse(jsService.getAvailableScriptsPaths().contains(exContext.getContextPath() + exPath));
      Thread.sleep(1000); //Wait to make sure we can compare lastModified properly
      assertEquals("\nbbb;ddd;aaa;ccc;", new String(jsService.getMergedJavascript()));
      
      long second = jsService.getLastModified();
      assertTrue(first < second);
      assertTrue(second < System.currentTimeMillis());
   }
   
   public void testMergedJS()
   {      
      String mergedJS = new String(jsService.getMergedJavascript());
      assertEquals("\nbbb;ddd;aaa;ccc;", mergedJS);            
      assertTrue(jsService.getLastModified() < System.currentTimeMillis());
   }   
   
   public void testRemoveJS()
   {
      assertEquals(4, jsService.getAvailableScripts().size());
      assertEquals(4, jsService.getAvailableScriptsPaths().size());            
      jsService.remove(mockServletContext);
      assertEquals(0, jsService.getAvailableScripts().size());
      assertEquals(0, jsService.getAvailableScriptsPaths().size());      
      
      assertTrue(jsService.getMergedJavascript().length > 0);
      jsService.refreshMergedJavascript();
      assertTrue(jsService.getMergedJavascript().length == 0);
   }

   @Override
   protected void tearDown() throws Exception
   {
      jsService.remove(mockServletContext);
      jsService.removeExtendedJavascript(exModule, exPath, exContext);
   }
   
   private static class MockJSServletContext extends MockServletContext
   {
      private Map<String, String> resources;
      
      public MockJSServletContext(String contextName, Map<String, String> resources)
      {
         super(contextName);
         this.resources = resources;
      }
      
      @Override
      public String getContextPath()
      {
         return "/" + getServletContextName();
      }
      
      @Override
      public InputStream getResourceAsStream(String s)
      {
         return new ByteArrayInputStream(resources.get(s).getBytes());
      }
   }
}
