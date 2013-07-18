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
import java.util.List;

import org.exoplatform.portal.config.model.UserAgentConditions;
import org.exoplatform.portal.mop.management.binding.xml.Element;
import org.gatein.common.xml.stax.XmlHandler;
import org.gatein.common.xml.stax.writer.StaxWriter;
import org.staxnav.StaxNavigator;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class UserAgentConditionXmlHandler implements XmlHandler<UserAgentConditions, Element> {
    @Override
    public UserAgentConditions read(StaxNavigator<Element> navigator) {
        if (navigator.getName() != Element.USER_AGENT) {
            throw unexpectedElement(navigator);
        }
        navigator.child();

        UserAgentConditions userAgentConditions = new UserAgentConditions();

        // contains
        ArrayList<String> contains = null;
        for (StaxNavigator<Element> fork : navigator.fork(Element.CONTAINS)) {
            if (contains == null) {
                contains = new ArrayList<String>();
            }
            contains.add(fork.getContent());
        }
        if (contains != null) {
            userAgentConditions.setContains(contains);
        }

        // does-not-contain
        ArrayList<String> doesNotContain = null;
        for (StaxNavigator<Element> fork : navigator.fork(Element.DOES_NOT_CONTAIN)) {
            if (doesNotContain == null) {
                doesNotContain = new ArrayList<String>();
            }
            doesNotContain.add(fork.getContent());
        }
        if (doesNotContain != null) {
            userAgentConditions.setDoesNotContain(doesNotContain);
        }

        return userAgentConditions;
    }

    @Override
    public void write(StaxWriter<Element> writer, UserAgentConditions conditions) {
        if (conditions == null) return;

        writer.writeStartElement(Element.USER_AGENT);

        List<String> contains = conditions.getContains();
        if (contains != null && !contains.isEmpty()) {
            for (String contain : contains) {
                writer.writeElement(Element.CONTAINS, contain);
            }
        }

        List<String> doesNotContain = conditions.getDoesNotContain();
        if (doesNotContain != null && !doesNotContain.isEmpty()) {
            for (String not : doesNotContain) {
                writer.writeElement(Element.DOES_NOT_CONTAIN, not);
            }
        }

        writer.writeEndElement();
    }
}
