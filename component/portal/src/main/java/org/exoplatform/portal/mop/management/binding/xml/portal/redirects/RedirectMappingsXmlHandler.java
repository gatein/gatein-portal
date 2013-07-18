/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.exoplatform.portal.mop.management.binding.xml.portal.redirects;

import org.exoplatform.portal.config.model.NodeMap;
import org.exoplatform.portal.config.model.RedirectMappings;
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.XmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class RedirectMappingsXmlHandler implements XmlHandler<RedirectMappings, Element> {

    private static final ValueType<RedirectMappings.UnknownNodeMapping> UNKNOWN_NODE_MAPPING_VALUE_TYPE = ValueType.get(RedirectMappings.UnknownNodeMapping.class);
    private final NodeMapXmlHandler nodeMapXmlHandler;

    public RedirectMappingsXmlHandler() {
        this(new NodeMapXmlHandler());
    }

    public RedirectMappingsXmlHandler(NodeMapXmlHandler nodeMapXmlHandler) {
        this.nodeMapXmlHandler = nodeMapXmlHandler;
    }

    @Override
    public RedirectMappings read(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.NODE_MAPPING) {
            throw unexpectedElement(navigator);
        }

        // Create mappings
        RedirectMappings mappings = new RedirectMappings();

        // boolean variables to indicate what we've parsed so far
        boolean unresolvedNodes = false;
        boolean nodeMap = false;

        // Start parsing
        Element element = navigator.child();
        while (element != null) {
            switch (element) {
                case USER_NODE_NAME_MATCHING:
                    if (unresolvedNodes || nodeMap) {
                        throw unexpectedElement(navigator);
                    }
                    mappings.setUseNodeNameMatching(parseRequiredContent(navigator, ValueType.BOOLEAN));
                    break;
                case UNRESOLVED_NODES:
                    unresolvedNodes = true;
                    if (nodeMap) {
                        throw unexpectedElement(navigator);
                    }
                    mappings.setUnresolvedNode(parseRequiredContent(navigator, UNKNOWN_NODE_MAPPING_VALUE_TYPE));
                    break;
                case NODE_MAP:
                    nodeMap = true;
                    mappings.getMappings().add(nodeMapXmlHandler.read(navigator.fork()));
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }

            element = navigator.sibling();
        }

        return mappings;
    }

    @Override
    public void write(StaxWriter<Element> writer, RedirectMappings redirectMappings) {
        if (redirectMappings == null) return;

        writer.writeStartElement(Element.NODE_MAPPING);

        writer.writeElement(Element.USER_NODE_NAME_MATCHING, WritableValueTypes.BOOLEAN, redirectMappings.isUseNodeNameMatching());
        writeOptionalElement(writer, Element.UNRESOLVED_NODES, WritableValueTypes.<RedirectMappings.UnknownNodeMapping>getEnumType(), redirectMappings.getUnresolvedNode());

        // node-map elements
        if (redirectMappings.getMappings() != null) {
            for (NodeMap nodeMap : redirectMappings.getMappings()) {
                nodeMapXmlHandler.write(writer, nodeMap);
            }
        }

        writer.writeEndElement();
    }
}
