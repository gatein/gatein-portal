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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.commons.xml.DocumentSource;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A DOM builder and validator for {@code gatein-resources.xml} files.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class GateInResourcesSchemaValidator {
    /**
     * A simple {@link ErrorHandler} to collect details about issues reported by a SAX parser.
     * @see #throwSummary()
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    protected static class ErrorCollector implements ErrorHandler {

        private StringBuilder messageBuffer;
        private SAXParseException firstException;

        private final String resourceId;
        /**
         * @param resourceId
         */
        public ErrorCollector(String resourceId) {
            super();
            this.resourceId = resourceId;
        }

        /**
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override
        public void warning(SAXParseException e) {
            log.warn("'"+ resourceId +"' parse warning:", e);
        }

        /**
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException e) {
            if (firstException == null) {
                firstException = e;
                messageBuffer = new StringBuilder(64);
                messageBuffer.append('\'').append(resourceId).append('\'').append("' validation error:");
            }
            messageBuffer.append('\n').append(e.toString());
        }

        /**
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException e) {
            error(e);
        }

        /**
         * Throws a {@link SAXParseException} containing details about all issues reported
         * through {@link #error(SAXParseException)} or {@link #fatalError(SAXParseException)},
         * the root cause being set to the first {@link SAXParseException} met.
         *
         * @throws SAXParseException see above
         */
        public void throwSummary() throws SAXParseException {
            if (firstException != null) {
                throw new SAXParseException(messageBuffer.toString(), firstException.getPublicId(), firstException.getSystemId(), firstException.getLineNumber(), firstException.getColumnNumber(), firstException);
            }
        }

    }

    /** . */
    private static final Logger log = LoggerFactory.getLogger(GateInResourcesSchemaValidator.class);

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

    private static final Map<String, String> SYSTEM_ID_TO_XSD_PATH;
    private static final Map<String, Integer> NAMESPACE_URI_ORDERING;

    /** We validate since gatein_resources_1_5 including. */
    private static final int VALIDATE_SINCE_NAMESPACE_URI_INDEX;

    private static final GateInResourcesSchemaValidator VALIDATOR;

    static {

        /* How many entries will there be in SYSTEM_ID_TO_XSD_PATH and NAMESPACE_URI_ORDERING */
        final int mapEntriesCount = 6;
        final int initialCapacity = mapEntriesCount + mapEntriesCount/2 + 1;
        Map<String, String> systemIdToResourcePath = new HashMap<String, String>(initialCapacity);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_0_SYSTEM_ID, GATEIN_RESOURCE_1_0_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_1_SYSTEM_ID, GATEIN_RESOURCE_1_1_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_2_SYSTEM_ID, GATEIN_RESOURCE_1_2_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_3_SYSTEM_ID, GATEIN_RESOURCE_1_3_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_4_SYSTEM_ID, GATEIN_RESOURCE_1_4_XSD_PATH);
        systemIdToResourcePath.put(GATEIN_RESOURCES_1_5_SYSTEM_ID, GATEIN_RESOURCE_1_5_XSD_PATH);
        SYSTEM_ID_TO_XSD_PATH = Collections.unmodifiableMap(systemIdToResourcePath);

        Map<String, Integer> ordering = new HashMap<String, Integer>(initialCapacity);
        int i = 0;
        ordering.put(GATEIN_RESOURCES_1_0_SYSTEM_ID, Integer.valueOf(i++));
        ordering.put(GATEIN_RESOURCES_1_1_SYSTEM_ID, Integer.valueOf(i++));
        ordering.put(GATEIN_RESOURCES_1_2_SYSTEM_ID, Integer.valueOf(i++));
        ordering.put(GATEIN_RESOURCES_1_3_SYSTEM_ID, Integer.valueOf(i++));
        ordering.put(GATEIN_RESOURCES_1_4_SYSTEM_ID, Integer.valueOf(i++));
        ordering.put(GATEIN_RESOURCES_1_5_SYSTEM_ID, Integer.valueOf(i));
        /* WARNING: Do not put more items into ordering here. See below. */

        /* we validate since gatein_resources_1_5 */
        VALIDATE_SINCE_NAMESPACE_URI_INDEX = i++;

        /* Add future SYSTEM_IDs to ordering here:
         * e.g. ordering.put(GATEIN_RESOURCES_1_6_SYSTEM_ID, Integer.valueOf(i++));
         * ... and do not forget to adjust mapEntriesCount above.
         * */

        NAMESPACE_URI_ORDERING = Collections.unmodifiableMap(ordering);

        VALIDATOR = new GateInResourcesSchemaValidator();
    }


    /**
     * Builds a DOM from a {@code gatein-resources.xml} and validates against GateIn resources XML schema, if the the root element's
     * namespace URI is http://www.gatein.org/xml/ns/gatein_resources_1_5 or newer.
     *
     * @param url the URL to read {@code gatein-resources.xml} from.
     * @return a DOM document
     * @throws IOException if there are I/O problems.
     * @throws SAXException on document validation problems.
     */
    public static Document validate(URL url) throws IOException, SAXException {
        return validate(DocumentSource.create(url));
    }

    /**
     * Builds a DOM from a {@code gatein-resources.xml} and validates against GateIn resources XML schema, if the the root element's
     * namespace URI is http://www.gatein.org/xml/ns/gatein_resources_1_5 or newer.
     *
     * @param source the {@link DocumentSource} to read from
     * @return a DOM document
     * @throws IOException if there are I/O problems.
     * @throws SAXException on document validation problems.
     */
    public static Document validate(DocumentSource source) throws IOException, SAXException {
        return VALIDATOR.validateInternal(source);
    }

    /**
     * Returns {@code true} if a document with the given {@code namespaceUri} should be validated
     * or {@code false} otherwise. We return {@code true} for namespace URIs equal to
     * http://www.gatein.org/xml/ns/gatein_resources_1_5 or newer.
     *
     * @param namespaceUri
     * @return see above.
     */
    private static boolean shouldValidate(String namespaceUri) {
        Integer index = NAMESPACE_URI_ORDERING.get(namespaceUri);
        boolean result = index != null && index >= VALIDATE_SINCE_NAMESPACE_URI_INDEX;
        if (log.isDebugEnabled()) {
            log.debug("Should validate with XML schema for namspace URI '"+ namespaceUri +"'? "+ result);
        }
        return result;
    }

    /**
     * Performs some integrity checks.
     */
    static void assertValid() {
        /* all system IDs need to be valid namespace URIs
         * but not namespace URIs need to be valid system IDs */
        if (SYSTEM_ID_TO_XSD_PATH.size() < NAMESPACE_URI_ORDERING.size()) {
            throw new IllegalStateException("All system IDs need to be valid namespace URIs but not all namespace URIs need to be valid system IDs");
        }
        for (String namespaceUri : NAMESPACE_URI_ORDERING.keySet()) {
            if (SYSTEM_ID_TO_XSD_PATH.get(namespaceUri) == null) {
                throw new IllegalStateException("All system IDs need to be valid namespace URIs but not all namespace URIs need to be valid system IDs");
            }
        }
    }

    /** A place to keep {@link Schema}s parsed once at startup. */
    private final Map<String, Schema> namespaceUriToSchemaMap;

    private GateInResourcesSchemaValidator() {
        ClassLoader loader = getClass().getClassLoader();
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Map<String, Schema> nsSchemaMap = new HashMap<String, Schema>(NAMESPACE_URI_ORDERING.size());
        try {
            for (String namespaceUri : NAMESPACE_URI_ORDERING.keySet()) {
                if (shouldValidate(namespaceUri)) {
                    String path = SYSTEM_ID_TO_XSD_PATH.get(namespaceUri);
                    URL url = loader.getResource(path);
                    if (url == null) {
                        throw new IllegalStateException("Cannot load a schema from path '"+ path +"'");
                    }
                    Schema schema = factory.newSchema(url);
                    nsSchemaMap.put(namespaceUri, schema);
                }
            }
            this.namespaceUriToSchemaMap = Collections.unmodifiableMap(nsSchemaMap);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a DOM from a {@code gatein-resources.xml} and validates against GateIn resources XML schema, if the the root element's
     * namespace URI is http://www.gatein.org/xml/ns/gatein_resources_1_5 or newer.
     *
     * @param source the {@link DocumentSource} to read from
     * @return a DOM document
     * @throws IOException if there are I/O problems.
     * @throws SAXException on document validation problems.
     */
    private Document validateInternal(DocumentSource source) throws IOException, SAXException {
        InputStream documentStream = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilderFactory.setValidating(false);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();

            documentStream = source.getStream();
            Document result = builder.parse(documentStream);

            Element root = result.getDocumentElement();
            if (root != null) {
                String namespaceURI = root.getNamespaceURI();
                if (namespaceURI != null) {
                    Schema schema = namespaceUriToSchemaMap.get(namespaceURI);
                    if (schema != null) {
                        /* schema != null should implicitly mean that
                         * shouldValidate(namespaceUri) is true. */
                        Validator validator = schema.newValidator();
                        ErrorCollector errorCollector = new ErrorCollector(source.getIdentifier());
                        builder.setErrorHandler(errorCollector);
                        InputStream validationStream = null;
                        try {
                            validationStream = source.getStream();
                            /* We could actually use a DOMSource(result) here. It would perhaps be faster, but
                             * When with a StreamSource, line and column numbers are provided
                             * for every validation issue, which is better to debug */
                            validator.validate(new StreamSource(validationStream));
                        } finally {
                            Safe.close(validationStream);
                        }
                        errorCollector.throwSummary();
                    }
                }
            }
            return result;
        } catch (ParserConfigurationException e) {
            /* Should never happen */
            throw new RuntimeException(e);
        } finally {
            Safe.close(documentStream);
        }
    }

}
