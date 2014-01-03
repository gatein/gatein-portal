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

import static org.gatein.common.xml.stax.navigator.Exceptions.unexpectedElement;
import static org.gatein.common.xml.stax.navigator.Exceptions.unknownElement;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.createWriter;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.writeOptionalElement;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.gatein.management.api.binding.BindingException;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageMarshaller extends AbstractMarshaller<Page.PageSet> {
    @Override
    public void marshal(Page.PageSet pageSet, OutputStream outputStream) throws BindingException {
        try {
            StaxWriter<Element> writer = createWriter(Element.class, outputStream);

            writer.writeStartElement(Element.PAGE_SET);
            writeGateinObjectsNamespace(writer);

            // Marshal pages
            for (Page page : pageSet.getPages()) {
                marshalPage(writer, page);
            }

            writer.finish();
        } catch (StaxNavException e) {
            throw new BindingException(e);
        } catch (XMLStreamException e) {
            throw new BindingException(e);
        }
    }

    @Override
    public Page.PageSet unmarshal(InputStream inputStream) throws BindingException {
        try {
            StaxNavigator<Element> navigator = createNavigator(Element.class, Element.UNKNOWN, inputStream);
            if (navigator.getName() == Element.PAGE_SET) {
                ArrayList<Page> pages = new ArrayList<Page>();
                Element next = navigator.child();
                if (next == Element.PAGE) {
                    for (StaxNavigator<Element> fork : navigator.fork(Element.PAGE)) {
                        pages.add(unmarshalPage(fork));
                    }
                } else if (next != null) {
                    throw unexpectedElement(navigator);
                }

                // Seems like next should be null here...
                if (navigator.sibling() != null) {
                    throw unexpectedElement(navigator);
                }

                Page.PageSet pageSet = new Page.PageSet();
                pageSet.setPages(pages);

                return pageSet;
            } else {
                throw unknownElement(navigator);
            }
        } catch (StaxNavException e) {
            throw new BindingException(e);
        } catch (XMLStreamException e) {
            throw new BindingException(e);
        }
    }

    private void marshalPage(StaxWriter<Element> writer, Page page) throws XMLStreamException {
        writer.writeStartElement(Element.PAGE);

        // name, title description
        writer.writeElement(Element.NAME, page.getName());
        writeOptionalElement(writer, Element.TITLE, page.getTitle());
        writeOptionalElement(writer, Element.DESCRIPTION, page.getDescription());

        // Access/Edit permissions
        marshalAccessPermissions(writer, page.getAccessPermissions());
        marshalEditPermission(writer, page.getEditPermissions());

        writeOptionalElement(writer, Element.SHOW_MAX_WINDOW, WritableValueTypes.BOOLEAN, page.isShowMaxWindow());

        List<ModelObject> children = page.getChildren();
        for (ModelObject child : children) {
            marshalModelObject(writer, child);
        }

        writer.writeEndElement(); // End of page element
    }

    private Page unmarshalPage(StaxNavigator<Element> navigator) throws XMLStreamException {
        requiresChild(navigator, Element.NAME);
        String name = getRequiredContent(navigator, true);

        Page page = new Page();
        page.setName(name);

        // TODO: Need valid way to ensure a sequence of xml elements, with a mix of required and optional elements.
        Element current = navigator.sibling();
        while (current != null) {
            switch (current) {
                case TITLE:
                    page.setTitle(getContent(navigator, false));
                    current = navigator.sibling();
                    break;
                case DESCRIPTION:
                    page.setDescription(getContent(navigator, false));
                    current = navigator.sibling();
                    break;
                case ACCESS_PERMISSIONS:
                    page.setAccessPermissions(unmarshalAccessPermissions(navigator, true));
                    current = navigator.sibling();
                    break;
                case EDIT_PERMISSION:
                    page.setEditPermissions(unmarshalEditPermission(navigator));
                    current = navigator.sibling();
                    break;
                case SHOW_MAX_WINDOW:
                    page.setShowMaxWindow(parseRequiredContent(navigator, ValueType.BOOLEAN));
                    current = navigator.sibling();
                    break;
                case CONTAINER:
                    if (page.getChildren() == null) {
                        page.setChildren(new ArrayList<ModelObject>());
                    }
                    page.getChildren().add(unmarshalContainer(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case PORTLET_APPLICATION:
                    if (page.getChildren() == null) {
                        page.setChildren(new ArrayList<ModelObject>());
                    }
                    page.getChildren().add(unmarshalPortletApplication(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case GADGET_APPLICATION:
                    if (page.getChildren() == null) {
                        page.setChildren(new ArrayList<ModelObject>());
                    }
                    page.getChildren().add(unmarshalGadgetApplication(navigator.fork()));
                    current = navigator.sibling();
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }
        }

        return page;
    }
}
