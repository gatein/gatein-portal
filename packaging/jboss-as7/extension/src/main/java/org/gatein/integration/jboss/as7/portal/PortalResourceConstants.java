/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.integration.jboss.as7.portal;

import org.jboss.as.controller.PathElement;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PortalResourceConstants {
    public static final String PORTAL = "portal";
    public static final PathElement PORTAL_PATH = PathElement.pathElement(PORTAL);

    public static final String SITE = "site";
    public static final PathElement SITE_PATH = PathElement.pathElement(SITE);

    public static final String APPLICATION = "application";
    public static final PathElement APPLICATION_PATH = PathElement.pathElement(APPLICATION);

    // statistics
    static final String AVERAGE_TIME = "averageTime";
    static final String MAX_TIME = "maxTime";
    static final String MIN_TIME = "minTime";
    static final String THROUGHPUT = "throughput";
    static final String EXECUTION_COUNT = "executionCount";

    private PortalResourceConstants() {
    }
}
