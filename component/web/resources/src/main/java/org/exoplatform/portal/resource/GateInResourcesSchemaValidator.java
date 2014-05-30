/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
    * as indicated by the @author tags. See the copyright.txt file in the
    * distribution for a full listing of individual contributors.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.xml.DocumentSource;
import org.exoplatform.commons.xml.StrictSchemaValidator;
import org.exoplatform.portal.resource.config.xml.SkinConfigParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class GateInResourcesSchemaValidator {

    /** . */
    public static final String GATEIN_RESOURCES_1_0_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_0";

    /** . */
    public static final String GATEIN_RESOURCES_1_1_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_1";

    /** . */
    public static final String GATEIN_RESOURCES_1_2_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_2";

    /** . */
    public static final String GATEIN_RESOURCES_1_3_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_3";

    /** . */
    public static final String GATEIN_RESOURCES_1_4_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_4";

    /** . */
    public static final String GATEIN_RESOURCES_1_5_SYSTEM_ID = "http://www.gatein.org/xml/ns/gatein_resources_1_5";

    /** . */
    private static final String GATEIN_RESOURCE_1_0_XSD_PATH = "gatein_resources_1_0.xsd";

    /** . */
    private static final String GATEIN_RESOURCE_1_1_XSD_PATH = "gatein_resources_1_1.xsd";

    /** . */
    private static final String GATEIN_RESOURCE_1_2_XSD_PATH = "gatein_resources_1_2.xsd";

    /** . */
    private static final String GATEIN_RESOURCE_1_3_XSD_PATH = "gatein_resources_1_3.xsd";

    /** . */
    private static final String GATEIN_RESOURCE_1_4_XSD_PATH = "gatein_resources_1_4.xsd";

    /** . */
    private static final String GATEIN_RESOURCE_1_5_XSD_PATH = "gatein_resources_1_5.xsd";

    /** . */
    private static final StrictSchemaValidator VALIDATOR;

    static {

        Map<String, String> systemIdToResourcePath = new HashMap<String, String>();
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_0_SYSTEM_ID, GATEIN_RESOURCE_1_0_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_1_SYSTEM_ID, GATEIN_RESOURCE_1_1_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_2_SYSTEM_ID, GATEIN_RESOURCE_1_2_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_3_SYSTEM_ID, GATEIN_RESOURCE_1_3_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_4_SYSTEM_ID, GATEIN_RESOURCE_1_4_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_5_SYSTEM_ID, GATEIN_RESOURCE_1_5_XSD_PATH);
        VALIDATOR = new StrictSchemaValidator(SkinConfigParser.class, systemIdToResourcePath);
    }

    public static Document validate(DocumentSource source) throws IOException, IllegalArgumentException, SAXException {
        return VALIDATOR.validate(source);
    }
}
