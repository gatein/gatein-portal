/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.web.controller.router;

import junit.framework.TestCase;
import org.exoplatform.web.controller.QualifiedName;
import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestLegacyPortal extends TestCase
{

   /** . */
   private Router router;

   @Override
   protected void setUp() throws Exception
   {
      this.router = router().
         add(route("/").
            with(
               routeParam("gtn:handler").withValue("portal"),
               requestParam("gtn:componentid").named("portal:componentId"),
               requestParam("gtn:action").named("portal:action"),
               requestParam("gtn:objectid").named("portal:objectId")).
            sub(route("/public/{gtn:sitename}{gtn:path}").
               with(
                  routeParam("gtn:access").withValue("public"),
                  pathParam("gtn:path").matchedBy(".*").preservePath())).
            sub(route("/private/{gtn:sitename}{gtn:path}").
               with(
                  routeParam("gtn:access").withValue("private"),
                  pathParam("gtn:path").matchedBy(".*").preservePath()))).
         build();
   }

   public void testPrivateClassicComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "");
      expectedParameters.put(Names.GTN_COMPONENTID, "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic?portal:componentId=foo", router.render(expectedParameters));
   }

   public void testPrivateClassic() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "");

      //
      assertEquals(expectedParameters, router.route("/private/classic"));
      assertEquals("/private/classic", router.render(expectedParameters));
   }

   public void testPrivateClassicSlash() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "/");

      //
      assertEquals(expectedParameters, router.route("/private/classic/"));
      assertEquals("/private/classic/", router.render(expectedParameters));
   }

   public void testPrivateClassicSlashComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "/");
      expectedParameters.put(Names.GTN_COMPONENTID, "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic/", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic/?portal:componentId=foo", router.render(expectedParameters));
   }

   public void testPrivateClassicHome() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "/home");

      //
      assertEquals(expectedParameters, router.route("/private/classic/home"));
      assertEquals("/private/classic/home", router.render(expectedParameters));
   }

   public void testPrivateClassicHomeComponent() throws Exception
   {
      Map<QualifiedName, String> expectedParameters = new HashMap<QualifiedName, String>();
      expectedParameters.put(Names.GTN_HANDLER, "portal");
      expectedParameters.put(Names.GTN_SITENAME, "classic");
      expectedParameters.put(Names.GTN_ACCESS, "private");
      expectedParameters.put(Names.GTN_PATH, "/home");
      expectedParameters.put(Names.GTN_COMPONENTID, "foo");

      //
      assertEquals(expectedParameters, router.route("/private/classic/home", Collections.singletonMap("portal:componentId", new String[]{"foo"})));
      assertEquals("/private/classic/home?portal:componentId=foo", router.render(expectedParameters));
   }
}
