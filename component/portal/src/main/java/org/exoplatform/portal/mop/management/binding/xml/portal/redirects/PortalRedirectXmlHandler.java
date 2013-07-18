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

import java.util.ArrayList;
import java.util.Collection;

import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.CollectionXmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueTypes;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalRedirectXmlHandler extends CollectionXmlHandler<PortalRedirect, Element> {

    private final ConditionXmlHandler conditionXmlHandler;
    private final RedirectMappingsXmlHandler mappingsXmlHandler;

    public PortalRedirectXmlHandler() {
        this(new ConditionXmlHandler(), new RedirectMappingsXmlHandler());
    }

    public PortalRedirectXmlHandler(ConditionXmlHandler conditionXmlHandler, RedirectMappingsXmlHandler mappingsXmlHandler) {
        super(Element.PORTAL_REDIRECTS, Element.PORTAL_REDIRECT);
        this.conditionXmlHandler = conditionXmlHandler;
        this.mappingsXmlHandler = mappingsXmlHandler;
    }

    @Override
    protected PortalRedirect readElement(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.PORTAL_REDIRECT) {
            throw expectedElement(navigator, Element.PORTAL_REDIRECT);
        }
        PortalRedirect redirect = new PortalRedirect();

        // redirect-site
        String redirectSite = getRequiredContent(child(navigator, Element.REDIRECT_SITE), true);
        redirect.setRedirectSite(redirectSite);

        // name
        String name = getRequiredContent(sibling(navigator, Element.NAME), true);
        redirect.setName(name);

        // enabled
        boolean enabled = parseRequiredContent(sibling(navigator, Element.REDIRECT_ENABLED), ValueType.BOOLEAN);
        redirect.setEnabled(enabled);

        // conditions and node-mapping
        boolean conditions = false;
        boolean mappings = false;
        while (navigator.sibling() != null) {
            switch (navigator.getName()) {
                case CONDITIONS:
                    if (conditions || mappings) {
                        throw unexpectedElement(navigator);
                    }
                    conditions = true;
                    redirect.setConditions((ArrayList<RedirectCondition>) conditionXmlHandler.read(navigator.fork()));
                    break;
                case NODE_MAPPING:
                    if (mappings) {
                        throw unexpectedElement(navigator);
                    }
                    mappings = true;
                    redirect.setMappings(mappingsXmlHandler.read(navigator.fork()));
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }
        }

        return redirect;
    }

    @Override
    protected void writeElement(StaxWriter<Element> writer, PortalRedirect redirect) {
        if (redirect == null) return;

        writer.writeStartElement(Element.PORTAL_REDIRECT);

        writer.writeElement(Element.REDIRECT_SITE, redirect.getRedirectSite());
        writer.writeElement(Element.NAME, redirect.getName());
        writer.writeElement(Element.REDIRECT_ENABLED, WritableValueTypes.BOOLEAN, redirect.isEnabled());

        conditionXmlHandler.write(writer, redirect.getConditions());
        mappingsXmlHandler.write(writer, redirect.getMappings());

        writer.writeEndElement();
    }

    @Override
    protected Collection<PortalRedirect> createCollection() {
        return new ArrayList<PortalRedirect>();
    }
}
