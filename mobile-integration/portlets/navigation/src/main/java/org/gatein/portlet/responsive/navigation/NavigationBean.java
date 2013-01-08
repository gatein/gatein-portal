/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.navigation;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class NavigationBean {
    // PortalRequest portalRequest;

    public NavigationBean() {
        // this.portalRequest = PortalRequest.getInstance();
    }

    public String getCurrentNode() {
        return "Home";
    }

    public List<Node> getNodes() throws URISyntaxException {
        List<Node> nodes = new ArrayList<Node>();

        Node homeNode = new Node("Home", new URI("http://localhost:8080/portal/mobile/home"));
        Node siteMapNode = new Node("SiteMap", new URI("http://localhost:8080/portal/mobile/sitemap"));
        Node test = new Node("Title", null);
        Node testChild = new Node("entry", new URI("http://localhost:8080/portal/mobile/title/entry"));
        test.getChildren().add(testChild);

        nodes.add(homeNode);
        nodes.add(siteMapNode);
        nodes.add(test);

        return nodes;
    }
}
