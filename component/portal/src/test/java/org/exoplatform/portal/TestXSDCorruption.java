/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal;

import junit.framework.TestCase;

import org.gatein.common.io.IOTools;

import java.io.InputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestXSDCorruption extends TestCase
{

   private void assertHash(String expected, String resourcePath) throws Exception
   {
      InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
      assertNotNull(in);
      byte[] bytes = IOTools.getBytes(in);
      java.security.MessageDigest digester = java.security.MessageDigest.getInstance("MD5");
      digester.update(bytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : digester.digest())
      {
         String hex = Integer.toHexString(b);
         if (hex.length() == 1)
         {
            sb.append('0');
            sb.append(hex.charAt(0));
         }
         else
         {
            sb.append(hex.substring(hex.length() - 2));
         }
      }
      assertEquals(expected, sb.toString());
   }

   public void testGateInObjects1_x() throws Exception
   {
      assertHash("d0591b0a022a0c2929e1aed8979857cd", "gatein_objects_1_0.xsd");
      assertHash("99ae24c9bbfe1b59e066756a29ab6c79", "gatein_objects_1_1.xsd");
      assertHash("852e20df61c51199e3425f0882f2581d", "gatein_objects_1_2.xsd");
   }
}
