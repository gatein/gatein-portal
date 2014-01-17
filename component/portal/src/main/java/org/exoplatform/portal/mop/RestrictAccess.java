/*
 * JBoss, a division of Red Hat
 * Copyright 2014, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.portal.mop;

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;

/**
 * This class represents a mixin embedded into a Node, and states whether the node should have restricted
 * access during outside of the publication window.
 *
 * User: jpkroehling
 * Date: 2014-01-23
 * Time: 2:55 PM
 */
@MixinType(name = "gtn:restrictAccess")
public abstract class RestrictAccess extends Visible {
    @Property(name = "gtn:restrictOutsidePublicationWindow")
    public abstract boolean isRestrictOutsidePublicationWindow();

    public abstract void setRestrictOutsidePublicationWindow(boolean restrictOutsidePublicationWindow);

}
