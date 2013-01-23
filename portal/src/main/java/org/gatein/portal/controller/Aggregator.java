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

package org.gatein.portal.controller;

import java.io.IOException;

import javax.inject.Inject;

import juzu.Response;
import juzu.View;
import org.exoplatform.container.PortalContainer;
import org.gatein.portal.impl.mop.ram.RamStore;
import org.gatein.portal.portlet.PortletAppManager;

/**
 * The controller for aggregation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Aggregator {

    @Inject
    PortalContainer current;

    @Inject
    PortletAppManager manager;

    @Inject
    RamStore persistence;

    @View
    public Response.Render index() throws IOException {
        System.out.println("Portal container " + current);
        System.out.println("Persistence " + persistence);
        persistence.dump(System.out);
        return Response.render("<div class='gatein'>Hello GateIn<div>");
    }
}
