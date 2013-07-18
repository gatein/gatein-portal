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
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.CollectionXmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.gatein.common.xml.stax.writer.WritableValueType;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;
import static org.gatein.common.xml.stax.navigator.StaxNavUtils.*;
import static org.gatein.common.xml.stax.writer.StaxWriterUtils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class DevicePropertyConditionXmlHandler extends CollectionXmlHandler<DevicePropertyCondition, Element> {

    private static final ValueType<Float> FLOAT_VALUE_TYPE = new ValueType<Float>() {
        @Override
        protected Float parse(String s) throws Exception {
            return Float.valueOf(s.trim());
        }
    };

    private static final WritableValueType<Float> FLOAT_WRITABLE_VALUE_TYPE = new WritableValueType<Float>() {
        @Override
        public String format(Float value) throws StaxNavException {
            return value.toString();
        }
    };

    public DevicePropertyConditionXmlHandler() {
        super(Element.DEVICE_PROPERTIES, Element.DEVICE_PROPERTY);
    }

    @Override
    protected DevicePropertyCondition readElement(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.DEVICE_PROPERTY) {
            throw unexpectedElement(navigator);
        }

        DevicePropertyCondition condition = new DevicePropertyCondition();

        // property-name
        String propertyName = getRequiredContent(child(navigator, Element.PROPERTY_NAME), true);
        condition.setPropertyName(propertyName);

        // greater-than
        while (navigator.sibling() != null) {
            switch (navigator.getName()) {
                case GREATER_THAN:
                    if (condition.getGreaterThan() != null || condition.getLessThan() != null ||
                            condition.getMatches() != null || condition.getEquals() != null) {
                        throw unexpectedElement(navigator);
                    }
                    condition.setGreaterThan(parseRequiredContent(navigator, FLOAT_VALUE_TYPE));
                    break;
                case LESS_THAN:
                    if (condition.getLessThan() != null || condition.getMatches() != null || condition.getEquals() != null) {
                        throw unexpectedElement(navigator);
                    }
                    condition.setLessThan(parseRequiredContent(navigator, FLOAT_VALUE_TYPE));
                    break;
                case MATCHES:
                    if (condition.getMatches() != null || condition.getEquals() != null) {
                        throw unexpectedElement(navigator);
                    }
                    condition.setMatches(getRequiredContent(navigator, false));
                    break;
                case EQUALS:
                    if (condition.getEquals() != null) {
                        throw unexpectedElement(navigator);
                    }
                    condition.setEquals(getRequiredContent(navigator, false));
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
    protected void writeElement(StaxWriter<Element> writer, DevicePropertyCondition condition) {
        if (condition == null) return;

        writer.writeStartElement(Element.DEVICE_PROPERTY);

        writer.writeElement(Element.PROPERTY_NAME, condition.getPropertyName());

        writeOptionalElement(writer, Element.GREATER_THAN, FLOAT_WRITABLE_VALUE_TYPE, condition.getGreaterThan());
        writeOptionalElement(writer, Element.LESS_THAN, FLOAT_WRITABLE_VALUE_TYPE, condition.getLessThan());
        writeOptionalElement(writer, Element.EQUALS, condition.getEquals());
        writeOptionalElement(writer, Element.MATCHES, condition.getMatches());

        writer.writeEndElement();
    }

    @Override
    protected Collection<DevicePropertyCondition> createCollection() {
        return new ArrayList<DevicePropertyCondition>();
    }
}
