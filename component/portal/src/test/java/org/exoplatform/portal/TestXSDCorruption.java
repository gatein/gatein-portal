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

import java.io.InputStream;

import junit.framework.TestCase;

import org.gatein.common.io.IOTools;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestXSDCorruption extends TestCase {

    private void assertHash(String expected, String resourcePath) throws Exception {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        assertNotNull(in);
        byte[] bytes = IOTools.getBytes(in);
        java.security.MessageDigest digester = java.security.MessageDigest.getInstance("MD5");
        digester.update(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : digester.digest()) {
            String hex = Integer.toHexString(b);
            if (hex.length() == 1) {
                sb.append('0');
                sb.append(hex.charAt(0));
            } else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        assertEquals(expected, sb.toString());
    }

    public void testGateInObjects1_x() throws Exception {
        assertHash("4d9910ede9174952cc7b6ab1724004fc", "gatein_objects_1_0.xsd");
        assertHash("84f12eedf716ceda613b4eaf5e53b8b7", "gatein_objects_1_1.xsd");
        assertHash("a1c6ff34609376a873baa2a2f5513a69", "gatein_objects_1_1_1.xsd");
        assertHash("52a0042722b55085bb0c61996c137699", "gatein_objects_1_2.xsd");
        assertHash("949559e6559207a6c5effabb9367f3d1", "gatein_objects_1_3.xsd");
        assertHash("f8f04d14ff2a1e1e3279b84983dac359", "gatein_objects_1_4.xsd");
        assertHash("be0d30a87f5aee880b69cfbdcceb16f1", "gatein_objects_1_5.xsd");
        assertHash("1f7067931e791863474c012f6bdbba95", "gatein_objects_1_6.xsd");
    }
}
