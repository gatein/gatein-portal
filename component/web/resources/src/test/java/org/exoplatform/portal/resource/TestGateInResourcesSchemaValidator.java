/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import junit.framework.TestCase;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @author <a href="ppalaga@redhat.com">Peter Palaga</a>
 */
public class TestGateInResourcesSchemaValidator extends TestCase {

    public void testIntegrity() {
        GateInResourcesSchemaValidator.assertValid();
    }

    public void testXSDValidation() {
        validateDocument("amd-cdn.xml", false);
        validateDocument("amd-cdn-invalid-prefix.xml", true);
        validateDocument("amd-cdn-invalid-target-path.xml", true);
        validateDocument("invalid-1_3.xml", false);
        validateDocument("f0.xml", false);
        validateDocument("f1.xml", false);
        validateDocument("f2.xml", true);
        validateDocument("f3.xml", true);
        validateDocument("f4.xml", true);
        validateDocument("f5.xml", false);
        validateDocument("f6.xml", false);
        validateDocument("f7.xml", true);
        validateDocument("f8.xml", true);
        validateDocument("f9.xml", false);
        validateDocument("differScopes.xml", false);
        validateDocument("duplicateShared.xml", true);
        validateDocument("duplicatePortal.xml", true);
        validateDocument("duplicatePortlet.xml", true);
    }

    private void validateDocument(String fileName, boolean failureExpect) {
        System.out.println("Checking file "+ fileName);
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = loader.getResource("validator/" + fileName);
        try {
            GateInResourcesSchemaValidator.validate(url);
            if (failureExpect) {
                fail("Validation failure expected for '"+ fileName +"'.");
            }
        } catch (Exception ex) {
            if (!failureExpect) {
                StringWriter sw = new StringWriter();
                PrintWriter out = new PrintWriter(sw);
                out.println("Validation failed for file '"+ fileName +"'.");
                ex.printStackTrace(out);
                out.close();
                fail(sw.toString());
            }
        }
    }
}
