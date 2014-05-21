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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.gatein.common.io.IOTools;
import org.xml.sax.SAXException;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 5/23/12
 */
public class TestXSDValidator extends TestCase {
    private static final Validator VALIDATOR;

    private static final ClassLoader CTX_LOADER;

    private static final Map<String, Boolean> testScenarios = new HashMap<String, Boolean>();

    static {
        CTX_LOADER = Thread.currentThread().getContextClassLoader();
        URL xsdFile = CTX_LOADER.getResource("gatein_resources_1_5.xsd");
        VALIDATOR = createXSDValidator(xsdFile);

        testScenarios.put("f0.xml", false);
        testScenarios.put("f1.xml", false);
        testScenarios.put("f2.xml", true);
        testScenarios.put("f3.xml", true);
        testScenarios.put("f4.xml", true);
        testScenarios.put("f5.xml", false);
        testScenarios.put("f6.xml", false);
        testScenarios.put("f7.xml", true);
        testScenarios.put("f8.xml", true);
        testScenarios.put("f9.xml", false);
        testScenarios.put("differScopes.xml", false);
        testScenarios.put("duplicateShared.xml", true);
        testScenarios.put("duplicatePortal.xml", true);
        testScenarios.put("duplicatePortlet.xml", true);
    }

    public static Validator createXSDValidator(URL xsdFile) {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            // Set the feature XMLSchemaFactory.SCHEMA_FULL_CHECKING to false to turn off "Unique Particle Attribution"
            // validation
            factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking", false);
            return factory.newSchema(xsdFile).newValidator();
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void testXSDValidation() {
        for (Map.Entry<String, Boolean> testScene : testScenarios.entrySet()) {
            validateDocument(testScene.getKey(), testScene.getValue());
        }
    }

    private void validateDocument(String fileName, boolean failureExpect) {
        InputStream in = CTX_LOADER.getResourceAsStream("validator/" + fileName);
        try {
            VALIDATOR.validate(new StreamSource(in));
            if (failureExpect) {
                fail();
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
        } finally {
            IOTools.safeClose(in);
            VALIDATOR.reset();
        }
    }
}
