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

package org.exoplatform.portal.resource;

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

    public void testGateInResources1_x() throws Exception {
        assertHash("a5df21458f0bea86789f5bfb264ee099", "gatein_resources_1_0.xsd");
        assertHash("f5e48fdbfd1631ab0278a097447a3892", "gatein_resources_1_1.xsd");
        assertHash("7925f1cd0a91dcb6a95f8dcb87973792", "gatein_resources_1_2.xsd");
        assertHash("5a8602c916aee90249d7e5b8885de56e", "gatein_resources_1_3.xsd");
        assertHash("dad312d2e0db8bc140b6ab88e5d2793e", "gatein_resources_1_4.xsd");
        assertHash("399e51acf080fab3a416791932230935", "gatein_resources_1_5.xsd");
    }
}
