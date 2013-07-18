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
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.XmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.staxnav.StaxNavigator;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class NodeMapXmlHandler implements XmlHandler<NodeMap, Element> {
    @Override
    public NodeMap read(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.NODE_MAP) {
            throw unexpectedElement(navigator);
        }

        String originNode = getRequiredContent(child(navigator, Element.ORIGIN_NODE), false);
        String redirectNode = getRequiredContent(sibling(navigator, Element.REDIRECT_NODE), false);

        return new NodeMap(originNode, redirectNode);
    }

    @Override
    public void write(StaxWriter<Element> writer, NodeMap nodeMap) {
        if (nodeMap == null) return;

        writer.writeStartElement(Element.NODE_MAP);

        writer.writeElement(Element.ORIGIN_NODE, nodeMap.getOriginNode());
        writer.writeElement(Element.REDIRECT_NODE, nodeMap.getRedirectNode());

        writer.writeEndElement();
    }
}
