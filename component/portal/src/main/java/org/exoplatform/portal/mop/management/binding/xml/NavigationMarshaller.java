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

package org.exoplatform.portal.mop.management.binding.xml;

import static org.gatein.common.xml.stax.navigator.Exceptions.expectedElement;
import static org.gatein.common.xml.stax.navigator.Exceptions.unexpectedElement;
import static org.gatein.common.xml.stax.navigator.Exceptions.unknownElement;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.createNavigator;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.parseContent;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.buildDefaultWriter;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.writeOptionalElement;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.mop.Visibility;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.common.xml.stax.writer.builder.StaxWriterBuilder;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.Marshaller;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationMarshaller implements Marshaller<PageNavigation> {

    @Override
    public void marshal(PageNavigation navigation, OutputStream outputStream, boolean pretty) throws BindingException {
        try {
            StaxWriterBuilder builder = buildDefaultWriter(outputStream);
            if (!pretty) {
                builder.withFormatting(null);
            }

            StaxWriter<Element> writer = builder.build(Element.class);
            marshalNavigation(writer, navigation);
        } catch (StaxNavException e) {
            throw new BindingException(e);
        } catch (XMLStreamException e) {
            throw new BindingException(e);
        }
    }

    @Override
    public PageNavigation unmarshal(InputStream is) throws BindingException {
        try {
            StaxNavigator<Element> navigator = createNavigator(Element.class, Element.UNKNOWN, is);
            return unmarshalNavigation(navigator);
        } catch (StaxNavException e) {
            throw new BindingException(e);
        }
    }

    private void marshalNavigation(StaxWriter<Element> writer, PageNavigation navigation) throws XMLStreamException {
        writer.writeStartElement(Element.NODE_NAVIGATION);

        // Write gatein_objects xml namespace
        Utils.writeGateinObjectsNamespace(writer);

        // Priority
        writer.writeElement(Element.PRIORITY, WritableValueTypes.INTEGER, navigation.getPriority());

        // Page nodes
        ArrayList<NavigationFragment> fragments = navigation.getFragments();
        for (NavigationFragment fragment : fragments) {
            writer.writeStartElement(Element.PAGE_NODES);
            if (fragment.getParentURI() != null) {
                String parentUri = fragment.getParentURI();
                writeOptionalElement(writer, Element.PARENT_URI, parentUri);
            }

            Collection<PageNode> nodes = fragment.getNodes();
            if (nodes != null && !nodes.isEmpty()) {
                for (PageNode node : nodes) {
                    marshallNode(writer, node);
                }
            }
            writer.writeEndElement(); // End page-nodes
        }

        writer.writeEndElement(); // End node-navigation
    }

    public void marshallNode(StaxWriter<Element> writer, PageNode node) throws XMLStreamException {
        writer.writeStartElement(Element.NODE);
        writer.writeElement(Element.NAME, node.getName());

        if (node.getLabels() != null) {
            for (LocalizedString label : node.getLabels()) {
                if (label.getValue() == null)
                    continue;

                writer.writeStartElement(Element.LABEL);
                if (label.getLang() != null) {
                    String localeString = label.getLang().getLanguage();
                    if (localeString == null) {
                        throw new XMLStreamException("Language was null for locale " + label.getLang());
                    }
                    String country = label.getLang().getCountry();
                    if (country != null && country.length() > 0) {
                        localeString += "-" + country.toLowerCase();
                    }

                    writer.writeAttribute(new QName(XMLConstants.XML_NS_URI, "lang", XMLConstants.XML_NS_PREFIX), localeString);
                }
                writer.writeContent(label.getValue()).writeEndElement();
            }
        }

        writeOptionalElement(writer, Element.ICON, node.getIcon());

        writeOptionalElement(writer, Element.START_PUBLICATION_DATE, WritableValueTypes.DATE_TIME,
                node.getStartPublicationDate());
        writeOptionalElement(writer, Element.END_PUBLICATION_DATE, WritableValueTypes.DATE_TIME, node.getEndPublicationDate());

        String visibility = (node.getVisibility() == null) ? null : node.getVisibility().name();
        writeOptionalElement(writer, Element.VISIBILITY, visibility);
        writeOptionalElement(writer, Element.PAGE_REFERENCE, node.getPageReference());

        Utils.marshalProperties(writer, node.getProperties());

        // Marshall children
        List<PageNode> children = node.getNodes();
        if (children != null && !children.isEmpty()) {
            for (PageNode child : children) {
                marshallNode(writer, child);
            }
        }

        writer.writeEndElement(); // End of node
    }

    private PageNavigation unmarshalNavigation(StaxNavigator<Element> navigator) throws StaxNavException {
        PageNavigation navigation = new PageNavigation();

        if (navigator.getName() == Element.NODE_NAVIGATION) {
            Element next = navigator.child();
            if (next == Element.PRIORITY) {
                Integer priority = parseContent(navigator, ValueType.INTEGER, PageNavigation.UNDEFINED_PRIORITY);
                navigation.setPriority(priority < 1 ? PageNavigation.UNDEFINED_PRIORITY : priority);
            } else if (next == Element.PAGE_NODES) {
                unmarshalPageNode(navigation, navigator, next);
                return navigation;
            }

            next = navigator.sibling();
            if (next == Element.PAGE_NODES) {
                unmarshalPageNode(navigation, navigator, next);
            } else if (next != null) {
                throw expectedElement(navigator, Element.PAGE_NODES);
            }

            return navigation;
        } else {
            throw unknownElement(navigator);
        }
    }

    private void unmarshalPageNode(PageNavigation navigation, StaxNavigator<Element> navigator, Element element) throws StaxNavException {
        for (StaxNavigator<Element> fork : navigator.fork(Element.PAGE_NODES)) {
            NavigationFragment fragment = new NavigationFragment();
            navigation.addFragment(fragment);

            element = fork.child();
            if (element == Element.PARENT_URI) {
                String parentUri = fork.getContent();
                if (parentUri == null) {
                    parentUri = "";
                } else if (parentUri.charAt(0) == '/') {
                    parentUri = parentUri.substring(1, parentUri.length());
                }
                fragment.setParentURI(parentUri);

                element = fork.sibling();
            }

            if (element == Element.NODE) {
                ArrayList<PageNode> nodes = new ArrayList<PageNode>();
                for (StaxNavigator<Element> nodeFork : fork.fork(Element.NODE)) {
                    nodes.add(unmarshalNode(nodeFork));
                }
                fragment.setNodes(nodes);
            } else if (element != null) {
                throw unknownElement(fork);
            }
        }
    }

    private PageNode unmarshalNode(StaxNavigator<Element> navigator) throws StaxNavException {
        PageNode node = new PageNode();
        I18NString labels = new I18NString();
        ArrayList<PageNode> children = new ArrayList<PageNode>();

        Element current = navigator.child();
        while (current != null) {
            switch (navigator.getName()) {
                case URI: // For backwards compatibility
                    current = navigator.sibling();
                    break;
                case NAME:
                    node.setName(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case LABEL:
                    labels.add(Utils.parseLocalizedString(navigator));
                    current = navigator.sibling();
                    break;
                case ICON:
                    node.setIcon(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case START_PUBLICATION_DATE:
                    node.setStartPublicationDate(navigator.parseContent(ValueType.DATE_TIME));
                    current = navigator.sibling();
                    break;
                case END_PUBLICATION_DATE:
                    node.setEndPublicationDate(navigator.parseContent(ValueType.DATE_TIME));
                    current = navigator.sibling();
                    break;
                case VISIBILITY:
                    node.setVisibility(navigator.parseContent(ValueType.get(Visibility.class)));
                    current = navigator.sibling();
                    break;
                case PAGE_REFERENCE:
                    node.setPageReference(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case PROPERTIES:
                    node.setProperties(Utils.unmarshalProperties(navigator));
                    current = navigator.next();
                    break;
                case NODE:
                    PageNode child = unmarshalNode(navigator.fork());
                    children.add(child);
                    current = navigator.sibling();
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }
        }

        node.setLabels(labels);
        node.setChildren(children);

        return node;
    }
}
