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

package org.gatein.api.management;

import org.gatein.api.EntityAlreadyExistsException;
import org.gatein.api.EntityNotFoundException;
import org.gatein.api.Portal;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.page.PageQuery;
import org.gatein.api.site.SiteId;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedRole;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.operation.OperationNames;

import java.util.List;

import static org.gatein.api.management.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
@SuppressWarnings("unused")
public class PageManagementResource {
    private final Portal portal;
    private final ModelProvider modelProvider;
    private final SiteId siteId;

    public PageManagementResource(Portal portal, ModelProvider modelProvider, SiteId siteId) {
        this.portal = portal;
        this.modelProvider = modelProvider;
        this.siteId = siteId;
    }

    @Managed(description = "Retrieves all pages for given site")
    public ModelList getPages(@ManagedContext PathAddress address) {
        // Populate model
        ModelList list = modelProvider.newModel(ModelList.class);
        populateModel(portal.findPages(new PageQuery.Builder().withSiteId(siteId).build()), list, address);

        return list;
    }

    @Managed("{page-name}")
    public ModelObject getPage(@MappedPath("page-name") String name) {
        PageId pageId = new PageId(siteId, name);
        Page page = portal.getPage(pageId);
        if (page == null) {
            throw notFound("Could not retrieve page", pageId);
        }

        // Populate model
        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateModel(page, model);

        return model;
    }

    @Managed("{page-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given page from the portal")
    public void removePage(@MappedPath("page-name") String name) {
        PageId pageId = new PageId(siteId, name);
        try {
            if (!portal.removePage(pageId)) {
                throw notFound("Could not remove page", pageId);
            }
        } catch (EntityNotFoundException e) {
            throw notFound("Could not remove page", siteId);
        }
    }

    @Managed("{page-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the given page to the portal")
    public ModelObject addPage(@MappedPath("page-name") String name, @MappedAttribute("displayName") String displayName) {

        PageId pageId = new PageId(siteId, name);
        Page page;
        try {
            page = portal.createPage(pageId);
        } catch (EntityAlreadyExistsException e) {
            throw alreadyExists("Could not add page", pageId);
        } catch (EntityNotFoundException e) {
            throw alreadyExists("Cannot add page", siteId);
        }
        page.setDisplayName(displayName);
        portal.savePage(page);

        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateModel(page, model);

        return model;
    }

    @Managed("{page-name}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates a page of the portal")
    public ModelObject addPage(@MappedPath("page-name") String name, @ManagedContext ModelObject pageModel) {
        throw new UnsupportedOperationException();
    }

    private void populateModel(List<Page> pages, ModelList list, PathAddress address) {
        for (Page page : pages) {
            if (Utils.hasPermission(page.getAccessPermission())) {
                ModelReference pageRef = list.add().asValue(ModelReference.class);
                pageRef.set("name", page.getName());
                pageRef.set("siteType", page.getId().getSiteId().getType().name().toLowerCase());
                pageRef.set("siteName", page.getId().getSiteId().getName());
                pageRef.set(address.append(page.getName()));
            }
        }
    }

    private void populateModel(Page page, ModelObject model) {
        model.set("name", page.getName());
        model.set("displayName", page.getDisplayName());
        populate("access-permissions", page.getAccessPermission(), model);
        populate("edit-permissions", page.getEditPermission(), model);
    }
}
