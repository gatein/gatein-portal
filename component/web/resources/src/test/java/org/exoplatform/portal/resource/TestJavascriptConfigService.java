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
import org.exoplatform.portal.controller.resource.Resource;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.exoplatform.web.application.javascript.Javascript;
import org.exoplatform.web.application.javascript.JavascriptConfigParser;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.gatein.common.io.IOTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 */
public class TestJavascriptConfigService extends AbstractWebResourceTest
{
   private JavascriptConfigService jsService;

   private static MockResourceResolver resResolver;

   private static ServletContext mockServletContext;
   
   private static boolean isFirstStartup = true;
   
   @Override
   protected void setUp() throws Exception
   {
      final PortalContainer portalContainer = getContainer();
      jsService = (JavascriptConfigService)portalContainer.getComponentInstanceOfType(JavascriptConfigService.class);
      
      if (isFirstStartup)
      {
         Map<String, String> resources = new HashMap<String, String>(4);
         resources.put("/js/test1.js", "aaa; // inline comment");
         resources.put("/js/test2.js", "bbb;");
         resources.put("/js/test3.js", "ccc;");
         resources.put("/js/test4.js", "ddd;");
         mockServletContext = new MockJSServletContext("mockwebapp", resources);
         jsService.registerContext(mockServletContext);

         resResolver = new MockResourceResolver();
         jsService.addResourceResolver(resResolver);
         URL url = portalContainer.getPortalClassLoader().getResource("mockwebapp/gatein-resources.xml");
         JavascriptConfigParser.processConfigResource(url.openStream(), jsService, mockServletContext);

         isFirstStartup = false;
      }
   }

   public void testResourceResolver()
   {
      String jScript = jsService.getJScript("/path/to/MockResourceResolver");
      assertEquals(MockResourceResolver.class.getName(), jScript);
      
      jScript = jsService.getJScript("/path/to/non-existing.js");
      assertNull(jScript);
      
      resResolver.addResource("/path/to/non-existing.js", "foo");
      jScript = jsService.getJScript("/path/to/non-existing.js");
      assertNotNull("foo", jScript);
   }

   public void testCommonJScripts()
   {
      assertEquals(5, jsService.getCommonJScripts().size());
      assertTrue(jsService.isModuleLoaded("js.test1"));
      assertTrue(jsService.isModuleLoaded("js.test2"));
      assertTrue(jsService.isModuleLoaded("js.test3"));
      assertTrue(jsService.isModuleLoaded("js.test4"));
      assertTrue(jsService.isModuleLoaded("js.test7"));
      
      assertFalse(jsService.isModuleLoaded("js.test5"));
      
      //
      Javascript script = jsService.getScript(new Resource(ResourceScope.MODULE, "js.test1"));
      assertNotNull(script);
      assertTrue(script instanceof Javascript.Internal);
   }

   public void testPriority()
   {
      Iterator<String> availPaths = jsService.getAvailableScriptsPaths().iterator();      
      assertEquals(mockServletContext.getContextPath() + "/js/test2.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test4.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test1.js", availPaths.next());
      assertEquals(mockServletContext.getContextPath() + "/js/test3.js", availPaths.next());
      assertEquals("http://example.com/test7.js", availPaths.next());
      assertFalse(availPaths.hasNext());
   }
   
   public void testMergingCommonJScripts() throws IOException
   {
      String mergedJS = new String(jsService.getMergedJavascript());
      assertEquals("\nbbb;ddd;aaa;ccc;", mergedJS);
      assertEquals("\nbbb;ddd;aaa;ccc;", jsService.getMergedCommonJScripts().getText());
      assertTrue(jsService.getLastModified() < System.currentTimeMillis());

      //
      Map<Resource, Javascript> map = new HashMap<Resource, Javascript>();
      for (Javascript script :jsService.getScripts(true))
      {
         map.put(script.getResource(), script);
      }
      Set<Resource> expectedSet = new HashSet<Resource>();
      expectedSet.add(new Resource(ResourceScope.GLOBAL, "merged"));
      expectedSet.add(new Resource(ResourceScope.MODULE, "js.test7"));
      assertEquals(expectedSet, map.keySet());

      //
      Javascript.Internal merged = (Javascript.Internal)map.get(new Resource(ResourceScope.GLOBAL, "merged"));
      mergedJS = read(jsService.open(merged));
      assertEquals("bbb;ddd;aaa; // inline commentccc;", mergedJS);
   }

   public void testCaching()
   {
      String path = "/path/to/caching";
      resResolver.addResource(path, "foo");
      String jScript = jsService.getJScript(path);
      assertEquals("foo", jScript);
      
      resResolver.addResource(path, "bar");
      jScript = jsService.getJScript(path);
      assertEquals("foo", jScript);

      // invalidate cache
      jsService.invalidateCachedJScript(path);
      jScript = jsService.getJScript(path);
      assertEquals("bar", jScript);
   }

   public void testPortalJScript() throws IOException
   {
      Collection<Javascript> site = jsService.getPortalJScripts("site1");
      assertEquals(1, site.size());
      Iterator<Javascript> iterator = site.iterator();
      assertEquals(mockServletContext.getContextPath() + "/js/test5.js", iterator.next().getPath());

      //
      Javascript script = jsService.getScript(new Resource(ResourceScope.PORTAL, "site1"));
      assertNotNull(script);
      assertTrue(script instanceof Javascript.Internal);
      assertEquals("", read(jsService.open((Javascript.Internal)script)));

      site = jsService.getPortalJScripts("site2");
      assertEquals(2, site.size());
      iterator = site.iterator();
      assertEquals(mockServletContext.getContextPath() + "/js/test6.js", iterator.next().getPath());
      assertEquals(mockServletContext.getContextPath() + "/js/test5.js", iterator.next().getPath());

      //
      script = jsService.getScript(new Resource(ResourceScope.PORTAL, "site2"));
      assertNotNull(script);
      assertTrue(script instanceof Javascript.Internal);
      assertEquals("", read(jsService.open((Javascript.Internal)script)));

      //
      assertNull(jsService.getPortalJScripts("classic"));
      assertNull(jsService.getScript(new Resource(ResourceScope.PORTAL, "classic")));

      //
      jsService.removePortalJScripts("site1");
      assertNull(jsService.getPortalJScripts("site1"));
      assertNull(jsService.getScript(new Resource(ResourceScope.PORTAL, "site1")));

      //
      Javascript portalJScript = Javascript.create(new Resource(ResourceScope.PORTAL, "portal1"), "/portal", "/mockwebapp", Integer.MAX_VALUE);
      jsService.addPortalJScript(portalJScript);
      String jScript = jsService.getJScript(portalJScript.getPath());
      assertNull(jScript);
      assertEquals("", read(jsService.open((Javascript.Internal) script)));
      resResolver.addResource(portalJScript.getPath(), "bar1");
      jScript = jsService.getJScript(portalJScript.getPath());
      assertEquals("bar1", jScript);
   }
   
   private static class MockJSServletContext extends MockServletContext
   {
      private Map<String, String> resources;
      
      public MockJSServletContext(String contextName, Map<String, String> resources)
      {
         super(contextName);
         this.resources = resources;
      }
      
      public String getContextPath()
      {
         return "/" + getServletContextName();
      }
      
      @Override
      public InputStream getResourceAsStream(String s)
      {
         String input = resources.get(s);
         if (input != null)
         {
            return new ByteArrayInputStream(input.getBytes());
         }
         else
         {
            return null;
         }
      }
   }
   
   private static String read(InputStream in) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      IOTools.copy(in, baos);
      return baos.toString();
   }
}
