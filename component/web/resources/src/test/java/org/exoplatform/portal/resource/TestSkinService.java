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

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/29/11
 */
public class TestSkinService extends AbstractWebResourceTest
{
   private PortalContainer portalContainer;

   private SkinService skinService;

   private ServletContext mockServletContext;

   private volatile boolean initSkinService = true;

   /** A cache of Method in SkinService, served for reflect invocation **/
   private Map<String, Method> methodsOfSkinService = new HashMap<String, Method>();

   @Override
   protected void setUp() throws Exception
   {
      if(initSkinService)
      {
         initSkinService = false;

         portalContainer = getContainer();
         skinService = (SkinService)portalContainer.getComponentInstanceOfType(SkinService.class);
         mockServletContext = new MockServletContext("mockwebapp", portalContainer.getPortalClassLoader());
         skinService.registerContext(mockServletContext);

         processSkinConfiguration("/gatein-resources.xml");
      }
   }

   private void processSkinConfiguration(String configResource) throws Exception
   {
      URL url = mockServletContext.getResource(configResource);
      SkinConfigParser.processConfigResource(DocumentSource.create(url), skinService, mockServletContext);
   }

   public void testInitService()
   {
      assertNotNull(portalContainer);
      assertEquals("portal", portalContainer.getName());
      assertNotNull(skinService);
   }

   public void testInitSkinModules()
   {
      assertNotNull(skinService.getAvailableSkinNames());
      assertTrue(skinService.getAvailableSkinNames().contains("TestSkin"));
   }

   public void testInitThemes()
   {

   }

   public void testDeployedSkinModules()
   {
      assertNotNull(skinService.getAvailableSkinNames());
      assertTrue(skinService.getAvailableSkinNames().contains("TestSkin"));

      Collection<SkinConfig> skinConfigs = skinService.getPortalSkins("TestSkin");
      assertNotNull(skinConfigs);

      SkinConfig portalSkin = null;
      for(SkinConfig config : skinConfigs)
      {
         if("TestSkin".equals(config.getName()))
         {
            portalSkin = config;
            break;
         }
      }
      assertNotNull(portalSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/Stylesheet.css", portalSkin.getCSSPath());

      SkinConfig firstPortletSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      assertNotNull(firstPortletSkin);
      assertEquals(mockServletContext.getContextPath() + "/skin/portlet/FirstPortlet/Stylesheet.css", firstPortletSkin.getCSSPath());
   }

   public void testDeployedThemes()
   {

   }

   public void testRenderLT_CSS() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ResourceRenderer renderer = new MockResourceRenderer(out);

      skinService.renderCSS(renderer, "/mockwebapp/skin/Stylesheet-lt.css");

      //TODO: Check the array of bytes in out
   }

   public void testRenderRT_CSS() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ResourceRenderer renderer = new MockResourceRenderer(out);

      skinService.renderCSS(renderer, "/mockwebapp/skin/Stylesheet-rt.css");

      //TODO: Check the array of bytes in out
   }

   public void testBackground() throws Exception
   {
   }

   public void testServiceCache() throws Exception
   {
   }

   public void testSkinPriority() throws Exception
   {
   }

   public void testUndeploySkinConfig() throws Exception
   {
      //TODO: Fork the work of GateInSkinConfigRemoval here
   }

   /**
    * Designed to invoke reflectively private methods of SkinService. That facilitate
    * writting JUnit tests
    *
    * @param methodName
    * @param arguments
    */
   private void invokeMethodOfSkinService(String methodName, Object... arguments)
   {
      StringBuilder methodSignature = new StringBuilder(methodName);
      Class<?>[] paramTypes = new Class<?>[arguments.length];
      for(int i =0; i < arguments.length; i++)
      {
         paramTypes[i] = arguments[i].getClass();
         methodSignature.append("_");
         methodSignature.append(paramTypes[i].getName());
      }
      //First we look at the cache
      Method method = methodsOfSkinService.get(methodSignature.toString());

      //Find method by reflection
      if(method == null)
      {
         try
         {
            method = skinService.getClass().getDeclaredMethod(methodName, paramTypes);
            if(method != null)
            {
               method.setAccessible(true);
               methodsOfSkinService.put(methodSignature.toString(), method);
            }
            else
            {
               return;
            }
         }
         catch (NoSuchMethodException ex)
         {
         }
      }

      try
      {
         if(Modifier.isStatic(method.getModifiers()))
         {
            method.invoke(null, arguments);
         }
         else
         {
            method.invoke(skinService, arguments);
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
   }
}
