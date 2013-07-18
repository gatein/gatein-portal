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

import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.CollectionXmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.staxnav.StaxNavigator;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ConditionXmlHandler extends CollectionXmlHandler<RedirectCondition, Element> {

    private final UserAgentConditionXmlHandler userAgentXmlHandler;
    private final DevicePropertyConditionXmlHandler devicePropertyXmlHandler;

    public ConditionXmlHandler() {
        this(new UserAgentConditionXmlHandler(), new DevicePropertyConditionXmlHandler());
    }

    public ConditionXmlHandler(UserAgentConditionXmlHandler userAgentXmlHandler, DevicePropertyConditionXmlHandler devicePropertyXmlHandler) {
        super(Element.CONDITIONS, Element.CONDITION);
        this.userAgentXmlHandler = userAgentXmlHandler;
        this.devicePropertyXmlHandler = devicePropertyXmlHandler;
    }

    @Override
    protected RedirectCondition readElement(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.CONDITION) {
            throw expectedElement(navigator, Element.CONDITION);
        }

        RedirectCondition condition = new RedirectCondition();
        Element element = navigator.child();
        if (element != Element.NAME) {
            throw expectedElement(navigator, Element.NAME);
        }
        condition.setName(getRequiredContent(navigator, true));

        boolean userAgent = false;
        boolean deviceProperties = false;
        while (navigator.sibling() != null) {
            switch (navigator.getName()) {
                case USER_AGENT:
                    if (userAgent || deviceProperties) {
                        throw unexpectedElement(navigator);
                    }
                    userAgent = true;
                    condition.setUserAgentConditions(userAgentXmlHandler.read(navigator.fork()));
                    break;
                case DEVICE_PROPERTIES:
                    if (deviceProperties) {
                        throw unexpectedElement(navigator);
                    }
                    deviceProperties = true;
                    condition.setDeviceProperties((ArrayList<DevicePropertyCondition>) devicePropertyXmlHandler.read(navigator.fork()));
                    break;
                case UNKNOWN:
                    throw unknownElement(navigator);
                default:
                    throw unexpectedElement(navigator);
            }
        }
        return condition;
    }

    @Override
    protected void writeElement(StaxWriter<Element> writer, RedirectCondition condition) {
        if (condition == null) return;

        writer.writeStartElement(Element.CONDITION);

        writer.writeElement(Element.NAME, condition.getName());
        userAgentXmlHandler.write(writer, condition.getUserAgentConditions());
        devicePropertyXmlHandler.write(writer, condition.getDeviceProperties());

        writer.writeEndElement();
    }

    @Override
    protected Collection<RedirectCondition> createCollection() {
        return new ArrayList<RedirectCondition>();
    }
}
