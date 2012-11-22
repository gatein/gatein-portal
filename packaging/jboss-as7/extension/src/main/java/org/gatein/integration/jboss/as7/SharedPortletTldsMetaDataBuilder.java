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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.jboss.metadata.parser.jsp.TldMetaDataParser;
import org.jboss.metadata.parser.util.NoopXMLResolver;
import org.jboss.metadata.web.spec.TldMetaData;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
class SharedPortletTldsMetaDataBuilder {
    private static final String[] PORTLET_TLDS = { "portlet.tld", "portlet_2_0.tld" };

    private final List<TldMetaData> tlds = new ArrayList<TldMetaData>();

    SharedPortletTldsMetaDataBuilder() {
        init();
    }

    private void init() {
        try {
            ModuleClassLoader jstl = Module.getModuleFromCallerModuleLoader(ModuleIdentifier.create("org.gatein.pc"))
                    .getClassLoader();
            for (String tld : PORTLET_TLDS) {
                InputStream is = jstl.getResourceAsStream("META-INF/" + tld);
                if (is != null) {
                    TldMetaData tldMetaData = parseTLD(tld, is);
                    tlds.add(tldMetaData);
                }
            }
        } catch (ModuleLoadException e) {
            // Ignore
        } catch (Exception e) {
            // Ignore
        }
    }

    List<TldMetaData> create() {
        final List<TldMetaData> metadata = new ArrayList<TldMetaData>();
        metadata.addAll(tlds);
        return metadata;
    }

    private TldMetaData parseTLD(String tld, InputStream is) throws Exception {
        try {
            final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            inputFactory.setXMLResolver(NoopXMLResolver.create());
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(is);
            return TldMetaDataParser.parse(xmlReader);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // Ignore
            }
        }
    }

}
