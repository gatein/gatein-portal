/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.gatein.portal.content;

import java.util.ArrayList;

import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.staxnav.StaxNavigator;

/**
 * @author Julien Viet
 */
class PortletContentType extends ContentType<Portlet> {

    /** . */
    static final ContentType<Portlet> INSTANCE = new PortletContentType();

    private PortletContentType() {
    }

    @Override
    public String getValue() {
        return "application/portlet";
    }

    @Override
    public ApplicationType<Portlet> getApplicationType() {
        return ApplicationType.PORTLET;
    }

    @Override
    public String getTagName() {
        return "portlet";
    }

    @Override
    public Content<Portlet> readState(StaxNavigator<String> xml) {
        validate(xml.sibling("application-ref"));
        String applicationRef = xml.getContent();
        validate(xml.sibling("portlet-ref"));
        String portletRef = xml.getContent();
        String contentId = applicationRef + "/" + portletRef;
        Portlet state;
        if (xml.sibling("preferences")) {
            PortletBuilder builder = new PortletBuilder();
            for (boolean b = xml.child("preference");b;b = xml.sibling("preference")) {
                validate(xml.child("name"));
                String preferenceName = xml.getContent();
                ArrayList<String> values = new ArrayList<String>();
                while (xml.sibling("value")) {
                    values.add(xml.getContent());
                }
                boolean readOnly;
                readOnly = xml.sibling("read-only") && "true".equals(xml.getContent());
                builder.add(preferenceName, values, readOnly);
            }
            state = builder.build();
        } else {
            state = null;
        }
        return new Content<Portlet>(contentId, state);
    }
}
