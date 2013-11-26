/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.portlet.Portlet;

import junit.framework.AssertionFailedError;
import juzu.impl.common.RunMode;
import juzu.impl.common.Tools;
import juzu.impl.inject.spi.InjectorProvider;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.portletapp20.PortletDescriptor;
import org.jboss.shrinkwrap.descriptor.api.portletapp20.PortletType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class AbstractPortalTestCase {

    public static WebArchive createPortal(InjectorProvider injector, RunMode runMode) {
        WebArchive portal = ShrinkWrap.create(WebArchive.class, "portal.war");
        
      String servlet;
      try {
         servlet = Tools.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("web.xml"));
      } catch (IOException e) {
         AssertionFailedError afe = new AssertionFailedError("Could not read web xml deployment descriptor");
         afe.initCause(e);
         throw afe;
      }

      servlet = String.format(servlet, injector.getValue(), runMode.getValue());

        portal.setWebXML(new StringAsset(servlet));
        portal.merge(ShrinkWrap.
                create(GenericArchive.class).
                as(ExplodedImporter.class).
                importDirectory("src/test/resources/WEB-INF").
                as(GenericArchive.class), "/WEB-INF", Filters.exclude("web.xml"));
        return portal;
    }
    
    public static WebArchive createPortal() {
        return createPortal(InjectorProvider.GUICE, RunMode.DEV);
    }
    
    public static PortletDescriptor portletXML() {
        return Descriptors.create(PortletDescriptor.class);
    }

    public static PortletType<PortletDescriptor> descriptor(PortletDescriptor descriptor, Class<? extends Portlet> portlet) {
        return descriptor.
                createPortlet().
                portletName(portlet.getSimpleName()).
                portletClass(portlet.getName()).
                createSupports().mimeType("text/html").portletMode("edit", "help").up().
                getOrCreatePortletInfo().title("Hello").up();
    }

    public static PortletDescriptor descriptor(Class<? extends Portlet> portlet) {
        return descriptor(new Class[]{portlet});
    }

    public static PortletDescriptor descriptor(Class<? extends Portlet>... portlets) {
        PortletDescriptor desc = portletXML();
        for (Class<? extends Portlet> portlet : portlets) {
            desc = descriptor(desc, portlet).up();
        }
        return desc;
    }

    public static Map<String, String> parameterMap(URIBuilder uri) {
        HashMap<String, String> parameters = new HashMap<String, String>();
        for (NameValuePair pair : uri.getQueryParams()) {
            parameters.put(pair.getName(), pair.getValue());
        }
        return parameters;
    }
}
