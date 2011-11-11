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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-configuration.xml")})
public abstract class AbstractSkinServiceTest extends AbstractKernelTest
{
   protected SkinService skinService;

   private static ServletContext mockServletContext;

   protected static MockResourceResolver resResolver;

   abstract boolean isDevelopingMode();

   abstract boolean setUpTestEnvironment();

   abstract void touchSetUp();

   @Override
   protected void setUp() throws Exception
   {
      //Set running mode at starting up
      PropertyManager.setProperty(PropertyManager.DEVELOPING, String.valueOf(isDevelopingMode()));

      PortalContainer portalContainer = getContainer();

      skinService = (SkinService)portalContainer.getComponentInstanceOfType(SkinService.class);

      if (setUpTestEnvironment())
      {
         mockServletContext = new MockServletContext("mockwebapp", portalContainer.getPortalClassLoader());
         skinService.registerContext(mockServletContext);

         resResolver = new MockResourceResolver();
         skinService.addResourceResolver(resResolver);

         URL url = mockServletContext.getResource("/gatein-resources.xml");
         SkinConfigParser.processConfigResource(DocumentSource.create(url), skinService, mockServletContext);

         touchSetUp();
      }
   }

   public void testInitializing()
   {
      assertEquals(1, skinService.getAvailableSkinNames().size());
      assertTrue(skinService.getAvailableSkinNames().contains("TestSkin"));

      String css = skinService.getCSS("/path/to/MockResourceResolver");
      assertEquals(MockResourceResolver.class.getName(), css);
   }

   public void testPortalSkinAndPriority()
   {
      Collection<SkinConfig> portalSkinConfigs = skinService.getPortalSkins("TestSkin");
      assertNotNull(portalSkinConfigs);
      assertEquals(4, portalSkinConfigs.size());

      SkinConfig[] array = new SkinConfig[4];
      portalSkinConfigs.toArray(array);

      SkinConfig portalSkin = array[0];
      assertNotNull(portalSkin);
      assertEquals("CoreSkin", portalSkin.getModule());
      assertEquals(mockServletContext.getContextPath() + "/skin/core/Stylesheet.css", portalSkin.getCSSPath());

      portalSkin = array[1];
      assertNotNull(portalSkin);
      assertEquals("Module2", portalSkin.getModule());
      assertEquals(mockServletContext.getContextPath() + "/skin/module2/Stylesheet.css", portalSkin.getCSSPath());

      portalSkin = array[2];
      assertNotNull(portalSkin);
      assertEquals("Module3", portalSkin.getModule());
      assertEquals(mockServletContext.getContextPath() + "/skin/module3/Stylesheet.css", portalSkin.getCSSPath());

      portalSkin = array[3];
      assertNotNull(portalSkin);
      assertEquals("Module1", portalSkin.getModule());
      assertEquals(mockServletContext.getContextPath() + "/skin/module1/Stylesheet.css", portalSkin.getCSSPath());
   }

   public void testPortletSkin()
   {
      SkinConfig portletSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      assertNotNull(portletSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/FirstPortlet.css",
         portletSkin.getCSSPath());
      
      portletSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
      assertNotNull(portletSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/SecondPortlet.css",
         portletSkin.getCSSPath());
   }

   public void testThemes()
   {
      Map<String, Set<String>> themeStyles = skinService.getPortletThemes();
      Set<String> themes = themeStyles.get("Simple");
      assertNotNull(themes);
      assertTrue(themes.contains("SimpleBlue"));
      assertTrue(themes.contains("SimpleViolet"));

      assertNotNull(themeStyles.get("VistaStyle"));
   }

   public void testExistingCSS() throws Exception
   {
      String css = skinService.getCSS("/mockwebapp/skin/Stylesheet-lt.css");
      assertTrue(css.length() > 0);

      css = skinService.getCSS("/mockwebapp/skin/Stylesheet-rt.css");
      assertTrue(css.length() > 0);
   }

   public void testNonExistingCSS()
   {
      String css = skinService.getCSS("/non/existing/file.css");
      assertNull(css);
      
      css = skinService.getCSS("/non/existing/file-lt.css");
      assertNull(css);
   }

   public void testProcessComment()
   {
      String path = "/process/comment/file.css";
      String css =
         "foo; /*background:url(bar.gif); Inline comment*/" +
         "/* Block comment\n" +
         "   background:url(bar.gif);\n" +
         "   End of block comment */";
      
      resResolver.addResource(path, css);
      
      //TODO: It should only ignore the comment instead of removing it
//      assertEquals(
//         "foo; /*background:url(bar.gif); Inline comment*/" +
//         "/* Block comment\n" +
//         "   background:url(bar.gif);\n" +
//         "   End of block comment */",
//         skinService.getCSS(path));
      assertEquals("foo;", skinService.getCSS(path));
   }

   public void testOrientation()
   {
      String path = "/orientation/file";
      String css =
         "aaa;/*orientation=lt*/bbb;/*orientation=rt*/\n" +
         " aaa; /* orientation=lt */ bbb; /* orientation=rt */ \n" +
         "{aaa;bbb;/*orientation=lt*/ccc;ddd;/*orientation=rt*/}\n" +
         "{aaa;/*orientation=lt*/bbb;}{ccc;/*orientation=rt*/ddd;}";
      
      resResolver.addResource(path + ".css", css);

      assertEquals(
         "aaa;\n" +
         "aaa;\n" +
         "{aaa;bbb;/*orientation=lt*/ccc;}\n" +
         "{aaa;/*orientation=lt*/bbb;}{ddd;}",
         skinService.getCSS(path + "-lt.css"));
      
      assertEquals(
         "bbb;/*orientation=rt*/\n" +
         " bbb; /* orientation=rt */\n" +
         "{aaa;ccc;ddd;/*orientation=rt*/}\n" +
         "{bbb;}{ccc;/*orientation=rt*/ddd;}",
         skinService.getCSS(path + "-rt.css"));
   }

   public void testBackgroundURL()
   {
      String path = "/background/url/file.css";
      String css =
         "background:url(images/foo.gif);\n" +
         "background:url('/images/foo.gif');\n" +
         "aaa; background: #fff url('images/foo.gif') no-repeat center -614px; ccc;";
      
      resResolver.addResource(path, css);
      assertEquals(
         "background:url(/background/url/images/foo.gif);\n" + 
         "background:url('/images/foo.gif');\n" +
         "aaa; background: #fff url('/background/url/images/foo.gif') no-repeat center -614px; ccc;",
         skinService.getCSS(path));
   }
}
