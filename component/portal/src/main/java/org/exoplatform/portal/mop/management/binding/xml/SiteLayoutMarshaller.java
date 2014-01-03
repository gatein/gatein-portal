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

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.getRequiredAttribute;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.getRequiredContent;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.createWriter;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.writeOptionalElement;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Properties;
import org.gatein.common.xml.stax.navigator.StaxNavUtils;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.management.api.binding.BindingException;
import org.staxnav.Axis;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteLayoutMarshaller extends AbstractMarshaller<PortalConfig> {
    @Override
    public void marshal(PortalConfig object, OutputStream outputStream) throws BindingException {
        try {
            StaxWriter<Element> writer = createWriter(Element.class, outputStream);

            // root element
            writer.writeStartElement(Element.PORTAL_CONFIG);
            writeGateinObjectsNamespace(writer);

            marshalPortalConfig(writer, object);

            writer.finish();
        } catch (StaxNavException e) {
            throw new BindingException(e);
        } catch (XMLStreamException e) {
            throw new BindingException(e);
        }
    }

    @Override
    public PortalConfig unmarshal(InputStream is) throws BindingException {
        try {
            StaxNavigator<Element> navigator = StaxNavUtils.createNavigator(Element.class, Element.UNKNOWN, is);

            if (navigator.getName() == Element.PORTAL_CONFIG) {
                return unmarshalPortalConfig(navigator);
            } else {
                throw unknownElement(navigator);
            }
        } catch (StaxNavException e) {
            throw new BindingException(e);
        } catch (XMLStreamException e) {
            throw new BindingException(e);
        }
    }

    private void marshalPortalConfig(StaxWriter<Element> writer, PortalConfig portalConfig) throws XMLStreamException {
        writer.writeElement(Element.PORTAL_NAME, portalConfig.getName());
        writeOptionalElement(writer, Element.LABEL, portalConfig.getLabel());
        writeOptionalElement(writer, Element.DESCRIPTION, portalConfig.getDescription());
        writeOptionalElement(writer, Element.LOCALE, portalConfig.getLocale());

        // Access permissions
        marshalAccessPermissions(writer, portalConfig.getAccessPermissions());

        // Edit permission
        marshalEditPermission(writer, portalConfig.getEditPermissions());

        writeOptionalElement(writer, Element.SKIN, portalConfig.getSkin());

        boolean propertiesWritten = false;
        Map<String, String> properties = portalConfig.getProperties();
        if (properties != null) {
            for (String key : properties.keySet()) {
                if (!propertiesWritten) {
                    writer.writeStartElement(Element.PROPERTIES);
                    propertiesWritten = true;
                }
                String value = properties.get(key);
                if (value != null) {
                    writer.writeStartElement(Element.PROPERTIES_ENTRY);
                    writer.writeAttribute(Attribute.PROPERTIES_KEY.getLocalName(), key);
                    writer.writeContent(value).writeEndElement();
                }
            }
            if (propertiesWritten) {
                writer.writeEndElement();
            }
        }

        Container container = portalConfig.getPortalLayout();
        if (container != null) {
            writer.writeStartElement(Element.PORTAL_LAYOUT);
            List<ModelObject> children = container.getChildren();
            if (children != null && !children.isEmpty()) {
                for (ModelObject child : children) {
                    marshalModelObject(writer, child);
                }
            }
            writer.writeEndElement();
        }
    }

    private PortalConfig unmarshalPortalConfig(StaxNavigator<Element> navigator) throws XMLStreamException {
        PortalConfig portalConfig = new PortalConfig();

        Container portalLayout = null;
        Element current = navigator.child();
        while (current != null) {
            switch (current) {
                case PORTAL_NAME:
                    portalConfig.setName(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case LOCALE:
                    portalConfig.setLocale(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case LABEL:
                    portalConfig.setLabel(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case DESCRIPTION:
                    portalConfig.setDescription(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case SKIN:
                    portalConfig.setSkin(navigator.getContent());
                    current = navigator.sibling();
                    break;
                case PROPERTIES:
                    Properties properties = new Properties();
                    if (navigator.navigate(Axis.CHILD, Element.PROPERTIES_ENTRY)) {
                        for (StaxNavigator<Element> fork : navigator.fork(Element.PROPERTIES_ENTRY)) {
                            String key = getRequiredAttribute(fork, Attribute.PROPERTIES_KEY.getLocalName());
                            String value = getRequiredContent(fork, false);
                            properties.put(key, value);
                        }
                    } else {
                        throw expectedElement(navigator, Element.PROPERTIES_ENTRY);
                    }
                    portalConfig.setProperties(properties);
                    current = navigator.next();
                    break;
                case ACCESS_PERMISSIONS:
                    portalConfig.setAccessPermissions(unmarshalAccessPermissions(navigator, false));
                    current = navigator.sibling();
                    break;
                case EDIT_PERMISSION:
                    portalConfig.setEditPermissions(unmarshalEditPermission(navigator));
                    current = navigator.sibling();
                    break;
                case PORTAL_LAYOUT:
                    portalLayout = new Container();
                    current = navigator.child();
                    break;
                case PAGE_BODY:
                    if (portalLayout == null) {
                        throw expectedElement(navigator, Element.PORTAL_LAYOUT);
                    }
                    portalLayout.getChildren().add(new PageBody());
                    current = navigator.sibling();
                    break;
                case PORTLET_APPLICATION:
                    if (portalLayout == null) {
                        throw expectedElement(navigator, Element.PORTAL_LAYOUT);
                    }
                    portalLayout.getChildren().add(unmarshalPortletApplication(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case GADGET_APPLICATION:
                    if (portalLayout == null) {
                        throw expectedElement(navigator, Element.PORTAL_LAYOUT);
                    }
                    portalLayout.getChildren().add(unmarshalGadgetApplication(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case CONTAINER:
                    if (portalLayout == null) {
                        throw expectedElement(navigator, Element.PORTAL_LAYOUT);
                    }
                    portalLayout.getChildren().add(unmarshalContainer(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }
        }

        // TODO: We should raise this exception as soon as we know so location is accurate
        if (portalConfig.getAccessPermissions() == null)
            throw expectedElement(navigator, Element.ACCESS_PERMISSIONS);
        if (portalLayout == null) {
            portalLayout = PortalConfig.DEFAULT_LAYOUT;
        }

        portalConfig.setPortalLayout(portalLayout);

        return portalConfig;
    }

    private static enum Attribute {
        PROPERTIES_KEY("key");

        private final String name;

        Attribute(final String name) {
            this.name = name;
        }

        /**
         * Get the local name of this element.
         *
         * @return the local name
         */
        public String getLocalName() {
            return name;
        }
    }
}
