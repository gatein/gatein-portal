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

package org.gatein.api.composition;

import java.io.Serializable;
import java.util.List;

import org.gatein.api.page.PageImpl;
import org.gatein.api.security.Permission;

/**
 * The default implementation of {@link BareContainer}.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class BareContainerImpl implements BareContainer, Serializable {

    /**
     * List fo children
     */
    protected List<ContainerItem> children;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * access permissions. Therefore it is generally safer to use
     * {@code myContainer.getAccessPermission()} rather than {@code myContainer.accessPermissions}.
     */
    private Permission accessPermission = Container.DEFAULT_ACCESS_PERMISSION;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * move applications permissions. Therefore it is generally safer to use
     * {@code myContainer.getMoveAppsPermission()} rather than {@code myContainer.moveAppsPermissions}.
     */
    private Permission moveAppsPermission = Container.DEFAULT_MOVE_APPS_PERMISSION;

    /**
     * Beware that {@link PageImpl} which is a subclass of this uses another mechanism to store
     * move containers permissions. Therefore it is generally safer to use
     * {@code myContainer.getMoveContainersPermission()} rather than {@code myContainer.moveContainersPermissions}.
     */
    private Permission moveContainersPermission = Container.DEFAULT_MOVE_CONTAINERS_PERMISSION;

    /**
     * Needed for subclasses to be able to comply with {@link Serializable}.
     */
    protected BareContainerImpl() {
    }

    public BareContainerImpl(List<ContainerItem> children) {
        super();
        this.children = children;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#getChildren()
     */
    @Override
    public List<ContainerItem> getChildren() {
        return children;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#setChildren(java.util.List)
     */
    @Override
    public void setChildren(List<ContainerItem> children) {
        this.children = children;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#getAccessPermission()
     */
    @Override
    public Permission getAccessPermission() {
        return accessPermission;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#setAccessPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setAccessPermission(Permission accessPermission) {
        this.accessPermission = accessPermission;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#getMoveAppsPermission()
     */
    @Override
    public Permission getMoveAppsPermission() {
        return moveAppsPermission;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#setMoveAppsPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setMoveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermission = moveAppsPermission;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#getMoveContainersPermission()
     */
    @Override
    public Permission getMoveContainersPermission() {
        return moveContainersPermission;
    }

    /**
     * @see org.gatein.api.composition.BareContainer#setMoveContainersPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setMoveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermission = moveContainersPermission;
    }

    /**
     * @return {@code true} if {@link #children} is not {@code null} and {@code false} otherwise.
     */
    public boolean isChildrenSet() {
        return null != children;
    }

}
