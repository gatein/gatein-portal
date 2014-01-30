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

package org.gatein.api.page;

import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.PortalImpl;
import org.gatein.api.PortalRequest;
import org.gatein.api.Util;
import org.gatein.api.composition.BareContainerImpl;
import org.gatein.api.composition.ContainerItem;
import org.gatein.api.internal.Parameters;
import org.gatein.api.security.Permission;
import org.gatein.api.site.SiteId;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PageImpl extends BareContainerImpl implements Page, Serializable {
    private PageKey key;
    private PageState state;

    private boolean create;
    private String title;

    private transient PortalImpl portal;

    public PageImpl(PageContext pageContext) {
        super(null);
        this.key = pageContext.getKey();
        this.state = pageContext.getState();
    }

    public PageImpl(PortalImpl portal, PageContext pageContext) {
        super(null);
        this.portal = portal;
        this.key = pageContext.getKey();
        this.state = pageContext.getState();
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#getChildren()
     */
    @Override
    public List<ContainerItem> getChildren() {
        if (!isChildrenSet()) {
            setChildren(getPortal().getPageRootContainer(getPageContext()));
        }
        return super.getChildren();
    }

    public PortalImpl getPortal() {
        if (null == portal) {
            portal = (PortalImpl) PortalRequest.getInstance().getPortal();
        }

        return portal;
    }

    /**
     * @see org.gatein.api.page.Page#getId()
     */
    @Override
    public PageId getId() {
        return Util.from(key);
    }

    /**
     * @see org.gatein.api.page.Page#getSiteId()
     */
    @Override
    public SiteId getSiteId() {
        return Util.from(key.getSite());
    }

    /**
     * @see org.gatein.api.page.Page#getName()
     */
    @Override
    public String getName() {
        return key.getName();
    }

    /**
     * @see org.gatein.api.common.Describable#getDescription()
     */
    @Override
    public String getDescription() {
        return state.getDescription();
    }

    /**
     * @see org.gatein.api.common.Describable#setDescription(java.lang.String)
     */
    @Override
    public void setDescription(String description) {
       setState(builder().description(description));
    }

    /**
     * @see org.gatein.api.common.Displayable#setDisplayName(java.lang.String)
     */
    @Override
    public void setDisplayName(String displayName) {
       setState(builder().displayName(displayName));
    }

    /**
     * @see org.gatein.api.common.Displayable#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return state.getDisplayName();
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#getAccessPermission()
     */
    @Override
    public Permission getAccessPermission() {
        return Util.from(state.getAccessPermissions());
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#setAccessPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setAccessPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow access to everyone use Permission.everyone()");

        setState(builder().accessPermissions(Util.from(permission)));
    }

    /**
     * @see org.gatein.api.page.Page#getEditPermission()
     */
    @Override
    public Permission getEditPermission() {
        return Util.from(state.getEditPermission());
    }

    /**
     * @see org.gatein.api.page.Page#setEditPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setEditPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow edit for everyone use Permission.everyone()");

        // Only one edit permission (membership) is allowed at this time.
        String[] permissions = Util.from(permission);
        if (permissions.length != 1)
            throw new IllegalArgumentException("Invalid permission. Only one membership is allowed for an edit permission");

        setState(builder().editPermission(permissions[0]));
    }


    /**
     * @see org.gatein.api.composition.BareContainerImpl#getMoveAppsPermission()
     */
    @Override
    public Permission getMoveAppsPermission() {
        return Util.from(state.getMoveAppsPermissions());
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#setMoveAppsPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setMoveAppsPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow to move appliactions for everyone use Permission.everyone()");
        setState(builder().moveAppsPermissions(Util.from(permission)));
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#getMoveContainersPermission()
     */
    @Override
    public Permission getMoveContainersPermission() {
        return Util.from(state.getMoveContainersPermissions());
    }

    /**
     * @see org.gatein.api.composition.BareContainerImpl#setMoveContainersPermission(org.gatein.api.security.Permission)
     */
    @Override
    public void setMoveContainersPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow to move containers for everyone use Permission.everyone()");
        setState(builder().moveContainersPermissions(Util.from(permission)));
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    @Override
    public int compareTo(Page page) {
        return getName().compareTo(page.getName());
    }

    public PageContext getPageContext() {
        return new PageContext(key, state);
    }

    private PageState.Builder builder() {
        return state.builder();
    }

    private void setState(PageState.Builder builder) {
        this.state = builder.build();
    }

    /**
     * @see org.gatein.api.page.Page#getTitle()
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @see org.gatein.api.page.Page#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

}
