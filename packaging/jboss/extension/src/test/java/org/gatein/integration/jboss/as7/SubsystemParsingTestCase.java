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
package org.gatein.integration.jboss.as7;

import java.io.IOException;

import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemBaseTest {
    private static final String SUBSYSTEM_XML =
        "<subsystem xmlns=\"" + GateInExtension.NAMESPACE + "\">\n" +
            "   <portlet-war-dependencies>\n" +
            "      <dependency name=\"" + Module.ORG_GATEIN_LIB.getLocalName() + "\" import-services=\"true\" />\n" +
            "      <dependency name=\"" + Module.ORG_GATEIN_WCI.getLocalName() + "\" />\n" +
            "      <dependency name=\"" + Module.JAVAX_PORTLET_API.getLocalName() + "\" />\n" +
            "   </portlet-war-dependencies>\n" +
        "</subsystem>";

    static enum Module {
        ORG_GATEIN_LIB("org.gatein.lib"),
        ORG_GATEIN_WCI("org.gatein.wci"),
        JAVAX_PORTLET_API("javax.portlet.api");

        private String module;

        public String getLocalName() {
            return module;
        }

        private Module(String name) {
            module = name;
        }
    }

    public SubsystemParsingTestCase() {
        super(GateInExtension.SUBSYSTEM_NAME, new GateInExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
        return SUBSYSTEM_XML;
    }
}
