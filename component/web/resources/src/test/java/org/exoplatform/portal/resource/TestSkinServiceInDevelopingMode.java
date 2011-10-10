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

import org.exoplatform.services.resources.Orientation;

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
   void touchSetUp()
   {
      isFirstStartup = false;
   }

   public void testCompositeSkin()
   {
      SkinConfig fSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      SkinConfig sSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
      assertNotNull(fSkin);
      assertNotNull(sSkin);

      Skin merged = skinService.merge(Arrays.asList(fSkin, sSkin));
      SkinURL url = merged.createURL();

      url.setOrientation(Orientation.LT);
      assertEquals("@import url(/mockwebapp/skin/FirstPortlet-lt.css);\n"
         + "@import url(/mockwebapp/skin/SecondPortlet-lt.css);", skinService.getCSS(url.toString()));

      url.setOrientation(Orientation.RT);
      assertEquals("@import url(/mockwebapp/skin/FirstPortlet-rt.css);\n"
         + "@import url(/mockwebapp/skin/SecondPortlet-rt.css);", skinService.getCSS(url.toString()));
   }

   public void testCache()
   {
      String path = "/path/to/test/caching.css";

      resResolver.addResource(path, "foo");
      assertEquals("foo", skinService.getCSS(path));

      resResolver.addResource(path, "bar");
      assertEquals("bar", skinService.getCSS(path));
   }
   
   public void testProcessImportCSS()
   {
      String parent = "/process/import/css.css";

      resResolver.addResource(parent, "@import url(Portlet/Stylesheet.css); aaa;");
      assertEquals("@import url(/process/import/Portlet/Stylesheet-lt.css); aaa;", skinService.getCSS(parent));
      
      resResolver.addResource(parent, "@import url('/Portlet/Stylesheet.css'); aaa;");
      assertEquals("@import url('/Portlet/Stylesheet-lt.css'); aaa;", skinService.getCSS(parent));

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
      assertEquals("@import url(/process/import/childCSS/child-lt.css);  background:url(/process/import/images/foo.gif);",
         skinService.getCSS(parent));
   }

   public void testLastModifiedSince()
   {
      String path = "/last/modify/since.css";
      resResolver.addResource(path, "foo");

      assertTrue(skinService.getCSS(path).length() > 0);
      assertEquals(Long.MAX_VALUE, skinService.getLastModified(path));
   }
}
