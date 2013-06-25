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
package org.gatein.portal.common.kernel;

import java.net.HttpURLConnection;
import java.net.URL;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/**
 * @author Julien Viet
 */
@RunWith(Arquillian.class)
public class KernelLifeCycleTestCase {

    @Deployment
    public static WebArchive getDeployment() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        WebAppDescriptor desc = Descriptors.create(WebAppDescriptor.class).
                displayName("portal").
                createFilter().filterName("KernelLifeCycle").filterClass(KernelLifeCycle.class.getName()).up().
                createFilterMapping().filterName("KernelLifeCycle").servletName("Servlet").up().
                createServlet().servletName("Servlet").servletClass(ServletImpl.class.getName()).up().
                createServletMapping().urlPattern("/").servletName("Servlet").up();
        war.addAsWebInfResource("conf/portal.common-configuration.xml", "conf/configuration.xml");
        war.setWebXML(new StringAsset(desc.exportAsString()));
        return war;
    }

    /** . */
    public static PortalContainer container1;

    /** . */
    public static PortalContainer container2;

    @ArquillianResource
    URL deploymentURL;

    @Test
    @RunAsClient
    public void testContainer() throws Exception {
        container1 = null;
        HttpURLConnection conn = (HttpURLConnection) deploymentURL.openConnection();
        conn.connect();
        assertEquals(200, conn.getResponseCode());
        assertNotNull(container1);
        assertNotNull(container2);
        assertSame(container1, container2);
        TheService ts = (TheService) container1.getComponentInstance(TheService.class);
        assertNotNull(ts);
    }
}
