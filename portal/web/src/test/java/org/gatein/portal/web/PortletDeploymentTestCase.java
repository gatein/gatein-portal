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

import java.io.InputStream;
import java.util.Set;

import static org.junit.Assert.*;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.portal.web.content.portlet.PortletDeployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.PackagingType;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinate;
import org.jboss.shrinkwrap.resolver.api.maven.coordinate.MavenCoordinates;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
@RunWith(Arquillian.class)
public class PortletDeploymentTestCase {

    @Deployment()
    public static WebArchive createPortal() {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.merge(ShrinkWrap.
                create(GenericArchive.class).
                as(ExplodedImporter.class).
                importDirectory("src/main/webapp").
                as(GenericArchive.class), "/", Filters.includeAll());
        return war;
    }

    @Deployment(name = "portlet")
    public static WebArchive createPortletApp() {
        MavenCoordinate coordinates = MavenCoordinates.createCoordinate(
                "org.gatein.pc",
                "pc-samples",
                "2.3.2-GA",
                PackagingType.WAR,
                "basic-portlet"
        );
        MavenResolvedArtifact resolved = Maven.resolver().
                resolve(coordinates.toCanonicalForm()).
                withoutTransitivity().asSingleResolvedArtifact();
        InputStream in = resolved.asInputStream();
        WebArchive war = ShrinkWrap.create(WebArchive.class, "samples.war");
        war.as(ZipImporter.class).importFrom(in);
        return war;
    }

    @Test
    @RunAsClient
    public void testDeployed() throws Exception {
        PortalContainer portal = RootContainer.getInstance().getPortalContainer("portal");
        PortletDeployer manager = (PortletDeployer) portal.getComponentInstanceOfType(PortletDeployer.class);
        PortletInvoker invoker = manager.getInvoker();
        Set<Portlet> portlets = invoker.getPortlets();
        System.out.println(portlets);
        assertEquals(13, portlets.size());
    }
}
