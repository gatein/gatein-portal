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

package org.exoplatform.commons.xml;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @deprecated this class checks only if a document is well formed, but does not validate against XML schema (see GTNPORTAL-3501).
 */
public class XMLValidator {

    /** . */
    private final String[] schemas;

    /** . */
    private final ResourceEntityResolver resolver;

    /** . */
    private final Logger log = LoggerFactory.getLogger(XMLValidator.class);

    public XMLValidator(Class clazz, String systemId, String resourcePath) {
        schemas = new String[] { systemId };
        resolver = new ResourceEntityResolver(clazz, systemId, resourcePath);
    }

    public XMLValidator(Class clazz, Map<String, String> systemIdToResourcePath) {
        schemas = systemIdToResourcePath.keySet().toArray(new String[0]);
        resolver = new ResourceEntityResolver(clazz, systemIdToResourcePath);
    }

    /**
     * Parse and validate the specified resource.
     *
     * @param source the source to validate
     * @return the document when it is valid or null
     * @throws java.io.IOException any IOException thrown by using the provided URL
     * @throws NullPointerException if the provided URL is null
     * @deprecated this class checks only if a document is well formed, but does not validate against XML schema (see GTNPORTAL-3501).
     */
    public Document validate(DocumentSource source) throws NullPointerException, IOException {
        if (source == null) {
            throw new NullPointerException();
        }

        //
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemas);
        factory.setNamespaceAware(true);
        factory.setValidating(true);

        //
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            ValidationReporter reporter = new ValidationReporter(log, source.getIdentifier());
            builder.setErrorHandler(reporter);
            builder.setEntityResolver(resolver);
            return builder.parse(source.getStream());
        } catch (ParserConfigurationException e) {
            log.error("Got a parser configuration exception when doing XSD validation");
            return null;
        } catch (SAXException e) {
            log.error("Got a sax exception when doing XSD validation");
            return null;
        }
    }

}
