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

package org.exoplatform.web.controller.router;

import org.exoplatform.web.controller.QualifiedName;

import java.util.Collections;

import static org.exoplatform.web.controller.metadata.DescriptorBuilder.*;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestRoutePriority extends AbstractTestController
{

   public void testExactMatchingAfterWildcard() throws Exception
   {

      Router router = router().
         add(route("/{foo}")).
         add(route("/foo").with(routeParam("foo").withValue("b"))).
         build();

      assertEquals(Collections.singletonMap(Names.FOO, "foo"), router.route("/foo"));
      assertEquals("/foo", router.render(Collections.singletonMap(Names.FOO, "foo")));
      assertEquals("/b", router.render(Collections.singletonMap(Names.FOO, "b")));
   }

   public void testExactMatchingBeforeWildcard() throws Exception
   {

      Router router = router().
         add(route("/foo").with(routeParam("foo").withValue("b"))).
         add(route("/{foo}")).
         build();

      assertEquals(Collections.singletonMap(Names.FOO, "b"), router.route("/foo"));
      assertEquals("/foo", router.render(Collections.singletonMap(Names.FOO, "b")));
      assertEquals("/foo", router.render(Collections.singletonMap(Names.FOO, "foo")));
   }
}
