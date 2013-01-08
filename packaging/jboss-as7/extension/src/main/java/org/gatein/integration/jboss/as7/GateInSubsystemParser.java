/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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
package org.gatein.integration.jboss.as7;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

import java.util.EnumSet;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

/**
 * GateIn subsystem parser.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>,
        XMLElementWriter<SubsystemMarshallingContext> {

    private static final GateInSubsystemParser INSTANCE = new GateInSubsystemParser();

    static GateInSubsystemParser getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException {
        context.startSubsystemElement(GateInExtension.NAMESPACE, false);

        ModelNode node = context.getModelNode();

        if (node.hasDefined(Constants.PORTLET_WAR_DEPENDENCY)) {
            writePortletWarDependencies(writer, node.get(Constants.PORTLET_WAR_DEPENDENCY));
        }
        writer.writeEndElement();
    }

    private void writePortletWarDependencies(XMLExtendedStreamWriter writer, ModelNode warDependencies)
            throws XMLStreamException {
        if (warDependencies.isDefined() && warDependencies.asInt() > 0) {
            writer.writeStartElement(Element.PORTLET_WAR_DEPENDENCIES.getLocalName());
            for (Property dependency : warDependencies.asPropertyList()) {
                writer.writeStartElement(Element.DEPENDENCY.getLocalName());
                writer.writeAttribute(Attribute.NAME.getLocalName(), dependency.getName());
                ModelNode model = dependency.getValue();
                PortletWarDependencyDefinition.IMPORT_SERVICES.marshallAsAttribute(model, false, writer);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
        // no attributes
        if (reader.getAttributeCount() > 0) {
            throw unexpectedAttribute(reader, 0);
        }

        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, GateInExtension.SUBSYSTEM_NAME);
        address.protect();

        final ModelNode subsystem = new ModelNode();
        subsystem.get(OP).set(ADD);
        subsystem.get(OP_ADDR).set(address);
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            requireNoAttributes(reader);
        }
        list.add(subsystem);

        boolean hasArchives = false;
        boolean hasDependencies = false;

        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            if (GateInExtension.NAMESPACE.equals(reader.getNamespaceURI())) {
                final Element element = Element.forName(reader.getLocalName());
                switch (element) {
                    case PORTLET_WAR_DEPENDENCIES: {
                        parsePortletWarDependencies(reader, address, list);
                        hasDependencies = true;
                        break;
                    }
                    default: {
                        throw unexpectedElement(reader);
                    }
                }
            } else {
                throw unexpectedElement(reader);
            }
        }

        if (!hasDependencies)
            addDefaultPortletWarDependencies(reader, address, list);
    }

    static void parsePortletWarDependencies(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations)
            throws XMLStreamException {
        // no attributes
        requireNoAttributes(reader);

        boolean gotDependencies = false;

        // elements
        while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
            final Element element = Element.forName(reader.getLocalName());
            switch (element) {
                case DEPENDENCY: {
                    parseDependency(reader, parent, operations);
                    gotDependencies = true;
                    break;
                }
                default:
                    throw unexpectedElement(reader);
            }
        }

        if (!gotDependencies)
            addDefaultPortletWarDependencies(reader, parent, operations);
    }

    private static void addDefaultPortletWarDependencies(XMLExtendedStreamReader reader, ModelNode parent,
            List<ModelNode> operations) throws XMLStreamException {
        addPortletWarDependencyOperation(parent, new ModelNode(), "org.gatein.wci", operations);
        addPortletWarDependencyOperation(parent, new ModelNode(), "org.gatein.pc", operations);
        addPortletWarDependencyOperation(parent, new ModelNode(), "javax.portlet.api", operations);
    }

    static void parseDependency(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations)
            throws XMLStreamException {

        final ModelNode model = new ModelNode();
        // attributes
        final int count = reader.getAttributeCount();
        String name = null;
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME: {
                    name = value;
                    break;
                }
                case IMPORT_SERVICES: {
                    PortletWarDependencyDefinition.IMPORT_SERVICES.parseAndSetParameter(value, model, reader);
                    break;
                }
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        if (name == null) {
            throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
        }

        addPortletWarDependencyOperation(parent, model, name, operations);

        requireNoContent(reader);
    }

    private static void addPortletWarDependencyOperation(ModelNode parent, ModelNode model, String name,
            List<ModelNode> operations) {
        ModelNode address = parent.clone();
        address.add(Constants.PORTLET_WAR_DEPENDENCY, name);

        model.get(OP).set(ADD);
        model.get(OP_ADDR).set(address);
        operations.add(model);
    }
}
