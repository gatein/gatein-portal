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

import static org.exoplatform.web.controller.metadata.DescriptorBuilder.pathParam;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.route;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.routeParam;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.router;

import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.RouterConfigException;

import java.util.Arrays;



/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */

public class TestSkinServiceInDevelopingMode extends AbstractSkinServiceTest
{
   private static boolean isFirstStartup = true;
   
   boolean isDevelopingMode()
   {
      return true;
   }

   @Override
   boolean setUpTestEnvironment()
   {
      return isFirstStartup;
   }

   @Override
   Router getRouter()
   {
      Router router;
      try
      {
         router = router().add(
            route("/skins/{gtn:version}/{gtn:resource}{gtn:compress}{gtn:orientation}.css")
               .with(routeParam("gtn:handler").withValue("skin"))
               .with(pathParam("gtn:version").matchedBy("[^/]*").preservePath())
               .with(pathParam("gtn:orientation").matchedBy("-(lt)|-(rt)|").captureGroup(true))
               .with(pathParam("gtn:compress").matchedBy("-(min)|").captureGroup(true))
               .with(pathParam("gtn:resource").matchedBy(".+?").preservePath())).build();
         return router;
      }
      catch (RouterConfigException e)
      {
         return null;
      }
   }

   @Override
   void touchSetUp()
   {
      isFirstStartup = false;
   }

   public void testCompositeSkin() throws Exception
   {
      SkinConfig fSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      SkinConfig sSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
      assertNotNull(fSkin);
      assertNotNull(sSkin);

      Skin merged = skinService.merge(Arrays.asList(fSkin, sSkin));
      SkinURL url = merged.createURL(controllerCtx);
      
      url.setOrientation(Orientation.LT);
      assertEquals(
         "@import url(/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/FirstPortlet-lt.css);\n" +
         "@import url(/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/SecondPortlet-lt.css);", 
         skinService.getCSS(newControllerContext(getRouter(), url.toString()), false));

      url.setOrientation(Orientation.RT);
      assertEquals(
         "@import url(/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/FirstPortlet-rt.css);\n" +
         "@import url(/portal/skins/" + ASSETS_VERSION + "/mockwebapp/skin/SecondPortlet-rt.css);", 
         skinService.getCSS(newControllerContext(getRouter(), url.toString()), false));
   }

   public void testCache() throws Exception
   {
      String resource = "/path/to/test/caching.css";
      String url = newSimpleSkin(resource).createURL(controllerCtx).toString();
      resResolver.addResource(resource, "foo");
      assertEquals("foo", skinService.getCSS(newControllerContext(getRouter(), url), false));

      resResolver.addResource(resource, "bar");
      assertEquals("bar", skinService.getCSS(newControllerContext(getRouter(), url), false));
   }
   
   public void testProcessImportCSS() throws Exception
   {
      String parent = "/process/import/css.css";
      String parentURL = newSimpleSkin(parent).createURL(controllerCtx).toString();
      resResolver.addResource(parent, "@import url(Portlet/Stylesheet.css); aaa;");
      assertEquals(
         "@import url(/portal/skins/" + ASSETS_VERSION + "/process/import/Portlet/Stylesheet-lt.css); aaa;", 
         skinService.getCSS(newControllerContext(getRouter(), parentURL), false));
      
      resResolver.addResource(parent, "@import url('/Portlet/Stylesheet.css'); aaa;");
      assertEquals(
         "@import url('/portal/skins/" + ASSETS_VERSION + "/Portlet/Stylesheet-lt.css'); aaa;", 
         skinService.getCSS(newControllerContext(getRouter(), parentURL), false));

      //parent file import child css file
      resResolver.addResource(parent, "@import url(childCSS/child.css);  background:url(images/foo.gif);");
      String child = "/process/import/childCSS/child.css";
      resResolver.addResource(child, "background:url(bar.gif);");

      /*
       * Now test merge and process recursively (run in non-dev mode)
       * We have folder /process/import/css.css
       *                                        /images/foo.gif
       *                                        /childCSS/child.css
       *                                                        /bar.gif
       */
      assertEquals("@import url(/portal/skins/" + ASSETS_VERSION + "/process/import/childCSS/child-lt.css);  background:url(/process/import/images/foo.gif);",
         skinService.getCSS(newControllerContext(getRouter(), parentURL), false));
   }

   public void testLastModifiedSince() throws Exception
   {
      String resource = "/last/modify/since.css";
      SkinURL skinURL = newSimpleSkin(resource).createURL(controllerCtx);
      resResolver.addResource(resource, "foo");

      assertTrue(skinService.getCSS(newControllerContext(getRouter(), skinURL.toString()), false).length() > 0);
      assertEquals(Long.MAX_VALUE, skinService.getLastModified(newControllerContext(getRouter(), skinURL.toString())));
      skinURL.setOrientation(Orientation.RT);
      assertEquals(Long.MAX_VALUE, skinService.getLastModified(newControllerContext(getRouter(), skinURL.toString())));
   }
}
