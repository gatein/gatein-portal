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

import org.exoplatform.component.test.BaseGateInTest;
import org.exoplatform.web.controller.regexp.RERenderer;
import org.exoplatform.web.controller.regexp.RENode;
import org.exoplatform.web.controller.regexp.REParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRouteEscaper extends BaseGateInTest
{

   private void match(String pattern, String test, String expectedValue) throws Exception
   {
      REParser parser = new REParser(pattern);
      RouteEscaper escaper = new RouteEscaper('/', '_');
      RENode.Disjunction re = parser.parseDisjunction();
      escaper.visit(re);
      Pattern p = Pattern.compile(new RERenderer().render(re, new StringBuilder()).toString());
      Matcher matcher = p.matcher(test);
      assertTrue(matcher.find());
      assertEquals(expectedValue, matcher.group());
   }

   public void testMatch() throws Exception
   {
      match(".*", "_", "_");
      match(".*", "_/", "_");
      match(".*", "_/_", "_");
      match("/", "_/", "_");
      match("/*", "_/_", "_");
      match("[/a]*", "_a_/_", "_a_");
      match("[,-1&&[^/]]*", "_/_", "");
   }

   public void testGroup() throws Exception
   {
      match("(/)", "_", "_");
      match("(?:/)", "_", "_");
      match(".(?=/)", "a_", "a");
      match("a(?!/)", "ab", "a");
      match(".(?<=/)a", "ba_a", "_a");
      match(".(?<!/)a", "_aba", "ba");
   }
}
