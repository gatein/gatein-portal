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

package org.exoplatform.component.test.web;

import junit.framework.TestCase;
import org.gatein.common.io.IOTools;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ServletContextTestCase extends TestCase
{

   public void testFileGetResource() throws Exception
   {
      URL url = ServletContextTestCase.class.getClassLoader().getResource("org/exoplatform/component/test/web/");
      assertNotNull(url);
      File root = new File(url.toURI());
      assertTrue(root.exists());
      assertTrue(root.isDirectory());

      //
      ServletContextImpl servletContext = new ServletContextImpl(root, "/webapp", "webapp");

      //
      URL fooURL = servletContext.getResource("/foo.txt");
      assertNotNull(fooURL);
      assertEquals("foo", new String(IOTools.getBytes(fooURL.openStream())));

      //
      URL barURL = servletContext.getResource("/folder/bar.txt");
      assertNotNull(barURL);
      assertEquals("bar", new String(IOTools.getBytes(barURL.openStream())));

      //
      assertEquals(null, servletContext.getResource("/bar.txt"));
   }

   public void testClassGetResource() throws Exception
   {
      ServletContextImpl servletContext = new ServletContextImpl(getClass(), "/webapp", "webapp");

      //
      URL fooURL = servletContext.getResource("/foo.txt");
      assertNotNull(fooURL);
      assertEquals("foo", new String(IOTools.getBytes(fooURL.openStream())));

      //
      URL barURL = servletContext.getResource("/folder/bar.txt");
      assertNotNull(barURL);
      assertEquals("bar", new String(IOTools.getBytes(barURL.openStream())));

      //
      assertEquals(null, servletContext.getResource("/bar.txt"));
   }

   public void testContextPath() throws Exception
   {
      ServletContextImpl servletContext = new ServletContextImpl(getClass(), "/webapp", "webapp");
      assertEquals("/webapp", servletContext.getContextPath());
   }
}
