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

import java.util.Arrays;

import org.exoplatform.services.resources.Orientation;



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

   public void testCompositeSkin() throws Exception
   {
      SkinConfig fSkin = skinService.getSkin("mockwebapp/FirstPortlet", "TestSkin");
      SkinConfig sSkin = skinService.getSkin("mockwebapp/SecondPortlet", "TestSkin");
      assertNotNull(fSkin);
      assertNotNull(sSkin);

      Skin merged = skinService.merge(Arrays.asList(fSkin, sSkin));
      SkinURL url = merged.createURL(new MockControllerContext());
      
      url.setOrientation(Orientation.LT);
      assertEquals(
         "@import url(/portal/skins/mockwebapp/skin/FirstPortlet-lt.css);\n" +
         "@import url(/portal/skins/mockwebapp/skin/SecondPortlet-lt.css);", 
         skinService.getCSS(new MockControllerContext(), url.toString().substring("/portal/skins".length()), false));

      url.setOrientation(Orientation.RT);
      assertEquals(
         "@import url(/portal/skins/mockwebapp/skin/FirstPortlet-rt.css);\n" +
         "@import url(/portal/skins/mockwebapp/skin/SecondPortlet-rt.css);", 
         skinService.getCSS(new MockControllerContext(), url.toString().substring("/portal/skins".length()), false));
   }

   public void testCache() throws Exception
   {
      String path = "/path/to/test/caching.css";

      resResolver.addResource(path, "foo");
      assertEquals("foo", skinService.getCSS(new MockControllerContext(), path, false));

      resResolver.addResource(path, "bar");
      assertEquals("bar", skinService.getCSS(new MockControllerContext(), path, false));
   }
   
   public void testProcessImportCSS() throws Exception
   {
      String parent = "/process/import/css.css";

      resResolver.addResource(parent, "@import url(Portlet/Stylesheet.css); aaa;");
      assertEquals(
         "@import url(/portal/skins/process/import/Portlet/Stylesheet-lt.css); aaa;", 
         skinService.getCSS(new MockControllerContext(), parent, false));
      
      resResolver.addResource(parent, "@import url('/Portlet/Stylesheet.css'); aaa;");
      assertEquals(
         "@import url('/portal/skins/Portlet/Stylesheet-lt.css'); aaa;", 
         skinService.getCSS(new MockControllerContext(), parent, false));

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
      assertEquals("@import url(/portal/skins/process/import/childCSS/child-lt.css);  background:url(/process/import/images/foo.gif);",
         skinService.getCSS(new MockControllerContext(), parent, false));
   }

   public void testLastModifiedSince() throws Exception
   {
      String path = "/last/modify/since.css";
      resResolver.addResource(path, "foo");

      assertTrue(skinService.getCSS(new MockControllerContext(), path, false).length() > 0);
      assertEquals(Long.MAX_VALUE, skinService.getLastModified(new MockControllerContext(), path));
   }
}
