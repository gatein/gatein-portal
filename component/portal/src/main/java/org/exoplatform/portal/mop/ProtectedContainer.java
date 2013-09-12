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

package org.exoplatform.portal.mop;

import java.util.Collections;
import java.util.List;

import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
@MixinType(name = "gtn:protectedcontainer")
public abstract class ProtectedContainer {

    public static final List<String> DEFAULT_MOVE_APPLICATIONS_PERMISSIONS = Collections.singletonList(ProtectedResource.EVERYONE);
    public static final List<String> DEFAULT_MOVE_CONTAINERS_PERMISSIONS = Collections.singletonList(ProtectedResource.EVERYONE);

    @Property(name = "gtn:move-apps-permissions")
    public abstract List<String> getMoveAppsPermissions();

    public abstract void setMoveAppsPermissions(List<String> permissions);

    @Property(name = "gtn:move-containers-permissions")
    public abstract List<String> getMoveContainersPermissions();

    public abstract void setMoveContainersPermissions(List<String> permissions);
}
