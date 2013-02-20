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

import org.apache.commons.lang.LocaleUtils;
import org.gatein.api.Portal;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.NodeVisitor;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.navigation.PublicationDate;
import org.gatein.api.navigation.Visibility;
import org.gatein.api.page.PageId;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.ManagedRole;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationNames;

import java.util.Date;
import java.util.Locale;

import static org.gatein.api.management.GateInApiManagementResource.*;
import static org.gatein.api.management.Utils.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
@SuppressWarnings("unused")
public class NavigationManagementResource {
    private final Navigation navigation;
    private final ModelProvider modelProvider;

    public NavigationManagementResource(Navigation navigation, ModelProvider modelProvider) {
        this.navigation = navigation;
        this.modelProvider = modelProvider;
    }

    @Managed
    public ModelObject getNavigation(@ManagedContext OperationContext context,
                                     @MappedAttribute("scope") String scopeAttribute,
                                     @MappedAttribute("showAll") String showAllAttribute) {
        // Populate the model
        ModelObject model = modelProvider.newModel(ModelObject.class);

        NodeVisitor visitor = Nodes.visitChildren();
        int scope = 0;
        if (scopeAttribute != null) {
            scope = Integer.parseInt(scopeAttribute);
            visitor = Nodes.visitNodes(scope);
        }

        Node node = getNode(NodePath.root(), true, visitor);
        boolean showAll = showAllAttribute != null && Boolean.parseBoolean(showAllAttribute);
        if (!showAll || !context.getExternalContext().isUserInRole("administrators")) {
            node = node.filter().showDefault();
        }

        populateNavigationModel(node, scope, model, context);

        return model;
    }

    @Managed("{path: .*}")
    public ModelObject getNode(@MappedPath("path") String path, @MappedAttribute("scope") String scopeAttribute,
                               @MappedAttribute("showAll") String showAllAttribute, @ManagedContext OperationContext context) {
        NodeVisitor visitor = Nodes.visitChildren();
        int scope = 0;
        if (scopeAttribute != null) {
            scope = Integer.parseInt(scopeAttribute);
            visitor = Nodes.visitNodes(scope);
        }
        Node node = getNode(path, true, visitor);
        boolean showAll = showAllAttribute != null && Boolean.parseBoolean(showAllAttribute);
        if (showAll && context.getExternalContext().isUserInRole("administrators")) {
            node = node.filter().showDefault();
        } else {
            node = node.filter().showDefault();
        }

        // Populate the model
        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateNode(node, scope, model, context.getAddress());

        return model;
    }

    @Managed("{path: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the navigation node")
    public void removeNode(@MappedPath("path") String path) {
        Node node = getNode(path, true);

        Node parent = node.getParent();
        parent.removeChild(node.getName());
        navigation.saveNode(parent);
    }

    @Managed("{path: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the navigation node")
    public ModelObject addNode(@MappedPath("path") String path, @ManagedContext PathAddress address) {
        NodePath nodePath = NodePath.fromString(path);
        Node parent = getNode(nodePath.parent(), true, Nodes.visitChildren());
        String name = nodePath.getLastSegment();

        if (parent.hasChild(name)) {
            throw new OperationException(OperationNames.ADD_RESOURCE, "Node already exists for " + nodePath);
        }

        // Add child and save
        Node child = parent.addChild(name);
        navigation.saveNode(parent);

        // Populate model
        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateNode(child, 0, model, address);

        return model;
    }

    @Managed("{path: .*}")
    @ManagedRole("administrators")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates the navigation node")
    public ModelObject updateNode(@MappedPath("path") String path, @ManagedContext ModelObject nodeModel, @ManagedContext PathAddress address) {

        // Update node from model
        Node node = getNode(path, true);
        updateNodeFromModel(node, nodeModel);

        // Save node
        navigation.saveNode(node);

        // Populate model for update node
        ModelObject updateNodeModel = modelProvider.newModel(ModelObject.class);
        populateNode(node, 0, updateNodeModel, address);

        return updateNodeModel;
    }

    private Node getNode(String pathString, boolean require) {
        return getNode(pathString, require, Nodes.visitNone());
    }

    private Node getNode(String pathString, boolean require, NodeVisitor visitor) {
        return getNode(NodePath.fromString(pathString), require, visitor);
    }

    private Node getNode(NodePath path, boolean require) {
        return getNode(path, require, Nodes.visitNone());
    }

    private Node getNode(NodePath path, boolean require, NodeVisitor visitor) {
        Node node = navigation.getNode(path, visitor);
        if (node == null && require)
            throw notFound("Cannot retrieve node", navigation.getSiteId(), path);

        return node;
    }

    private void populateNavigationModel(Node rootNode, int scope, ModelObject model, OperationContext context) {
        PathAddress address = context.getAddress();

        // Populate navigation fields
        model.set("priority", navigation.getPriority());
        model.set("siteType", navigation.getSiteId().getType().getName());
        model.set("siteName", navigation.getSiteId().getName());
        ModelList nodesModel = model.get("nodes").setEmptyList();
        if (rootNode.isChildrenLoaded()) {
            for (Node child : rootNode) {
                Model childModel = nodesModel.add();
                PathAddress childAddress = address.append(child.getName());
                if (scope > 0 || scope < 0) // Continue populating nodes in response
                {
                    populateNode(child, scope - 1, childModel.setEmptyObject(), childAddress);
                } else { // Populate node reference which can be followed
                    ModelReference nodeRef = childModel.set(childAddress);
                    nodeRef.set("name", child.getName());
                }
            }
        }
    }

    private void populateNode(Node node, int scope, ModelObject model, PathAddress address) {
        model.set("name", node.getName());
        set("uri", node.getURI(), model);
        model.set("isVisible", node.isVisible());
        populateVisibility(node.getVisibility(), model.get("visibility", ModelObject.class));
        model.set("iconName", node.getIconName());

        // Display name
        model.set("displayName", node.getDisplayName());
        populate("displayNames", node.getDisplayNames(), model);

        // Children nodes
        ModelList children = model.get("children", ModelList.class);
        if (node.isChildrenLoaded()) {
            for (Node child : node) {
                Model childModel = children.add();
                PathAddress childAddress = address.append(child.getName());
                if (scope > 0 || scope < 0) // Continue populating nodes in response
                {
                    populateNode(child, scope - 1, childModel.setEmptyObject(), childAddress);
                } else { // Populate node reference which can be followed
                    ModelReference nodeRef = childModel.set(childAddress);
                    nodeRef.set("name", child.getName());
                }
            }
        }
        // Page reference
        ModelReference pageRef = model.get("page").asValue(ModelReference.class);
        if (node.getPageId() != null) {
            PageId pageId = node.getPageId();
            pageRef.set("pageName", pageId.getPageName());
            pageRef.set("siteName", pageId.getSiteId().getName());
            pageRef.set("siteType", pageId.getSiteId().getType().getName());

            // Set the address for the ref
            PathAddress pageAddress = getPagesAddress(pageId.getSiteId()).append(pageId.getPageName());
            pageRef.set(pageAddress);
        }
    }

    private void populateVisibility(Visibility visibility, ModelObject model) {
        if (visibility != null) {
            set("status", visibility.getStatus(), model);
            if (visibility.getPublicationDate() != null) {
                ModelObject pubDateModel = model.get("publication-date", ModelObject.class);
                Date start = visibility.getPublicationDate().getStart();
                Date end = visibility.getPublicationDate().getEnd();
                set("start", start, pubDateModel);
                set("end", end, pubDateModel);
            }
        }
    }

    private void updateNodeFromModel(Node node, ModelObject nodeModel) {

        // Update name
        if (nodeModel.has("name")) {
            node.setName(nonNullString(nodeModel, "name"));
        }

        // Update visibility
        if (nodeModel.has("visibility")) {
            Visibility visibility = getVisibility(nodeModel, node.getVisibility());
            if (visibility != null) {
                node.setVisibility(visibility);
            }
        }

        // Update iconName
        if (nodeModel.has("iconName")) {
            String iconName = get(nodeModel, ModelString.class, "iconName").getValue();
            node.setIconName(iconName);
        }

        // Update pageId
        if (nodeModel.has("page")) {
            node.setPageId(getPageId(nodeModel));
        }

        //TODO: Support adding and not just overwriting. i.e. one locale/value pair is added to rest.
        // Update displayName(s)
        if (nodeModel.has("displayName")) {
            if (nodeModel.has("displayNames")) {
                throw invalidData("Cannot define both displayName and displayNames");
            }
            String displayName = get(nodeModel, ModelString.class, "displayName").getValue();
            node.setDisplayName(displayName);
        } else if (nodeModel.has("displayNames")) {
            LocalizedString displayName = getDisplayNames(nodeModel);
            node.setDisplayNames(displayName);
        }
    }

    private static LocalizedString getDisplayNames(ModelObject nodeModel) {
        ModelList list = get(nodeModel, ModelList.class, "displayNames");
        if (!list.isDefined()) {
            throw invalidValue(null, "displayNames");
        }
        LocalizedString displayName = null;
        int i=0;
        for (ModelValue mv : list) {
            ModelObject displayNameModel = mv.asValue(ModelObject.class);

            // Parse value (required && non-null)
            if (!displayNameModel.has("value")) {
                throw requiredField("displayNames["+i+"].value");
            }
            String value = get(displayNameModel, ModelString.class, "value").getValue();
            if (value == null) {
                throw invalidValue(value, "displayNames[" + i + "].value");
            }

            // Parse lang (not-required but if defined must be non-null)
            if (displayNameModel.has("lang")) {
                ModelString langModel = get(displayNameModel, ModelString.class, "lang");
                String lang = langModel.getValue();
                if (lang == null) {
                    // Have to hard code the field for now to support array
                    throw invalidValue(lang, "displayNames[" + i + "].lang");
                }
                Locale locale;
                try {
                    locale = LocaleUtils.toLocale(lang);
                } catch (IllegalArgumentException e) {
                    throw invalidValue(lang, "displayNames["+i+"].lang");
                }
                if (displayName == null) {
                    displayName = new LocalizedString(locale, value);
                } else {
                    displayName.setLocalizedValue(locale, value);
                }
            } else if (displayName == null) {
                displayName = new LocalizedString(value);
            } else {
                throw invalidData("Cannot have multiple non localized values for displayNames");
            }
        }

        return displayName;
    }

    private static PageId getPageId(ModelObject nodeModel) {
        ModelObject pageModel = get(nodeModel, ModelObject.class, "page");
        if (pageModel.isDefined()) {
            String pageName = nonNullString(nodeModel, "page", "pageName");
            String siteName = nonNullString(nodeModel, "page", "siteName");
            String siteTypeString = nonNullString(nodeModel, "page", "siteType");
            SiteType siteType = SiteType.forName(siteTypeString);
            if (siteType == null) {
                throw invalidValue(siteTypeString, "page", "siteType");
            }
            return new PageId(new SiteId(siteType, siteName), pageName);
        } else {
            return null;
        }
    }

    private static Visibility getVisibility(ModelObject nodeModel, Visibility original) {
        Visibility.Status status = getStatus(nodeModel);
        if (status == Visibility.Status.PUBLICATION) {
            ModelObject pubDateModel = get(nodeModel, ModelObject.class, "visibility", "publication-date");
            // If status was set to PUBLICATION however no publication date was specified then throw exception
            if (!pubDateModel.isDefined()) {
                throw requiredFieldWhen("visibility status is " + status, "visibility", "publication-date");
            }
            PublicationDate publicationDate = getPublicationDate(nodeModel, original.getPublicationDate());
            return new Visibility(publicationDate);
        } else {
            return new Visibility(status);
        }
    }

    private static Visibility.Status getStatus(ModelObject nodeModel) {
        String statusString = nonNullString(nodeModel, "visibility", "status");
        Visibility.Status status;
        try {
            status = Visibility.Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw invalidValue(statusString, "visibility", "status");
        }
        return status;
    }

    private static PublicationDate getPublicationDate(ModelObject nodeModel, PublicationDate previous) {
        ModelString startModel = get(nodeModel, ModelString.class, "visibility", "publication-date", "start");
        Date start = (previous == null) ? null : previous.getStart();
        if (startModel.isDefined()) {
            start = getDate(nodeModel, "visibility", "publication-date", "start");
        }

        ModelString endModel = get(nodeModel, ModelString.class, "visibility", "publication-date", "end");
        Date end = (previous == null) ? null : previous.getEnd();
        if (endModel.isDefined()) {
            end = getDate(nodeModel, "visibility", "publication-date", "end");
        }

        if (start != null && end != null) {
            return PublicationDate.between(start, end);
        } else if (start == null && end != null) {
            return PublicationDate.endingOn(end);
        } else if (start != null) {
            return PublicationDate.startingOn(start);
        } else {
            throw invalidData("Either 'start' or 'end' is required for visibility.publication-date");
        }
    }
}
