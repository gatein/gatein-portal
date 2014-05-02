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

package org.exoplatform.portal.mop.management.binding.xml;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public enum Namespace {
    GATEIN_OBJECTS_1_1("http://www.gatein.org/xml/ns/gatein_objects_1_1"),
    GATEIN_OBJECTS_1_2("http://www.gatein.org/xml/ns/gatein_objects_1_2"),
    GATEIN_OBJECTS_1_3("http://www.gatein.org/xml/ns/gatein_objects_1_3"),
    GATEIN_OBJECTS_1_4("http://www.gatein.org/xml/ns/gatein_objects_1_4"),
    GATEIN_OBJECTS_1_5("http://www.gatein.org/xml/ns/gatein_objects_1_5"),
    GATEIN_OBJECTS_1_6("http://www.gatein.org/xml/ns/gatein_objects_1_6"),
    GATEIN_OBJECTS_1_7("http://www.gatein.org/xml/ns/gatein_objects_1_7");

    /**
     * The current namespace version.
     */
    public static final Namespace CURRENT = GATEIN_OBJECTS_1_7;

    private final String name;

    Namespace(final String name) {
        this.name = name;
    }

    /**
     * Get the URI of this namespace.
     *
     * @return the URI
     */
    public String getUri() {
        return name;
    }
}
