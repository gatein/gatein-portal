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

package org.exoplatform.portal.mop.management.operations.navigation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.LocalizedString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationUtils {
    private NavigationUtils() {
    }

    public static PageNavigation loadPageNavigation(NavigationKey key, NavigationService navigationService,
            DescriptionService descriptionService) {
        NavigationContext navigation = navigationService.loadNavigation(key.getSiteKey());
        if (navigation == null)
            return null;

        NodeContext<NodeContext<?>> node = loadNode(navigationService, navigation, key.getNavUri());
        if (node == null)
            return null;

        if (key.getNavUri() != null) {
            return createFragmentedPageNavigation(descriptionService, navigation, node);
        } else {
            return createPageNavigation(descriptionService, navigation, node);
        }
    }

    public static NodeContext<NodeContext<?>> loadNode(NavigationService navigationService, NavigationContext navigation,
            String navUri) {
        if (navigation == null)
            return null;

        if (navUri != null) {
            String[] path = trim(navUri.split("/"));
            NodeContext<NodeContext<?>> node = navigationService.loadNode(NodeModel.SELF_MODEL, navigation,
                    GenericScope.branchShape(path, Scope.ALL), null);
            for (String name : path) {
                node = node.get(name);
                if (node == null)
                    break;
            }

            return node;
        } else {
            return navigationService.loadNode(NodeModel.SELF_MODEL, navigation, Scope.ALL, null);
        }
    }

    public static PageNavigation createPageNavigation(DescriptionService service, NavigationContext navigation,
            NodeContext<NodeContext<?>> node) {
        PageNavigation pageNavigation = new PageNavigation();
        pageNavigation.setPriority(navigation.getState().getPriority());
        pageNavigation.setOwnerType(navigation.getKey().getTypeName());
        pageNavigation.setOwnerId(navigation.getKey().getName());

        ArrayList<PageNode> children = new ArrayList<PageNode>(node.getNodeCount());
        for (NodeContext<?> child : node.getNodes()) {
            @SuppressWarnings("unchecked")
            NodeContext<NodeContext<?>> childNode = (NodeContext<NodeContext<?>>) child;
            children.add(createPageNode(service, childNode));
        }

        NavigationFragment fragment = new NavigationFragment();
        fragment.setNodes(children);
        pageNavigation.addFragment(fragment);

        return pageNavigation;
    }

    public static PageNavigation copy(PageNavigation existingPageNavigation) {
        PageNavigation pageNavigation = new PageNavigation();
        pageNavigation.setOwnerType(existingPageNavigation.getOwnerType());
        pageNavigation.setOwnerId(existingPageNavigation.getOwnerId());
        pageNavigation.setPriority(existingPageNavigation.getPriority());
        for (NavigationFragment existingFragment : existingPageNavigation.getFragments()) {
            NavigationFragment fragment = copy(existingFragment);
            pageNavigation.addFragment(fragment);
        }
        return pageNavigation;
    }

    public static NavigationFragment copy(NavigationFragment existingFragment) {
        NavigationFragment fragment = new NavigationFragment();
        fragment.setParentURI(existingFragment.getParentURI());
        for (PageNode existingPageNode : existingFragment.getNodes()) {
            PageNode pageNode = copy(existingPageNode);
            fragment.getNodes().add(pageNode);
        }
        return fragment;
    }

    public static PageNode copy(PageNode existingPageNode) {
        PageNode pageNode = new PageNode();

        if (existingPageNode.getLabels() != null) {
            pageNode.setLabels(new I18NString(existingPageNode.getLabels()));
        }
        pageNode.setIcon(existingPageNode.getIcon());
        pageNode.setName(existingPageNode.getName());
        if (existingPageNode.getStartPublicationDate() != null) {
            pageNode.setStartPublicationDate( (Date)existingPageNode.getStartPublicationDate().clone() );
        }
        if (existingPageNode.getEndPublicationDate() != null) {
            pageNode.setEndPublicationDate( (Date)existingPageNode.getEndPublicationDate().clone() );
        }
        pageNode.setVisibility(existingPageNode.getVisibility());
        pageNode.setPageReference(existingPageNode.getPageReference());
        pageNode.setProperties(new Properties(existingPageNode.getProperties()));
        for (PageNode existingChild : existingPageNode.getNodes()) {
            PageNode child = copy(existingChild);
            pageNode.getNodes().add(child);
        }

        return pageNode;
    }

    private static PageNavigation createFragmentedPageNavigation(DescriptionService service, NavigationContext navigation,
            NodeContext<NodeContext<?>> node) {
        PageNavigation pageNavigation = new PageNavigation();
        pageNavigation.setPriority(navigation.getState().getPriority());
        pageNavigation.setOwnerType(navigation.getKey().getTypeName());
        pageNavigation.setOwnerId(navigation.getKey().getName());

        ArrayList<PageNode> children = new ArrayList<PageNode>(1);
        children.add(createPageNode(service, node));

        NavigationFragment fragment = new NavigationFragment();
        StringBuilder parentUri = new StringBuilder("");
        getPath(node.getParent(), parentUri);
        fragment.setParentURI(parentUri.toString());
        fragment.setNodes(children);

        pageNavigation.addFragment(fragment);

        return pageNavigation;
    }

    private static void getPath(NodeContext<NodeContext<?>> node, StringBuilder parentUri) {
        if (node == null)
            return;
        if (node.getParent() == null)
            return; // since "default" is the root node, we ignore it

        parentUri.insert(0, node.getName()).insert(0, "/");
        getPath(node.getParent(), parentUri);
    }

    private static PageNode createPageNode(DescriptionService service, NodeContext<NodeContext<?>> node) {
        PageNode pageNode = new PageNode();
        pageNode.setName(node.getName());

        NodeState nodeState = node.getState();
        if (nodeState.getLabel() == null) {
            Map<Locale, Described.State> descriptions = service.getDescriptions(node.getId());
            if (descriptions != null && !descriptions.isEmpty()) {
                I18NString labels = new I18NString();
                for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet()) {
                    labels.add(new LocalizedString(entry.getValue().getName(), entry.getKey()));
                }

                pageNode.setLabels(labels);
            }
        } else {
            pageNode.setLabel(nodeState.getLabel());
        }

        pageNode.setIcon(nodeState.getIcon());
        long startPublicationTime = nodeState.getStartPublicationTime();
        if (startPublicationTime != -1) {
            pageNode.setStartPublicationDate(new Date(startPublicationTime));
        }

        long endPublicationTime = nodeState.getEndPublicationTime();
        if (endPublicationTime != -1) {
            pageNode.setEndPublicationDate(new Date(endPublicationTime));
        }

        pageNode.setVisibility(nodeState.getVisibility());
        pageNode.setPageReference(nodeState.getPageRef() != null ? nodeState.getPageRef().format() : null);

        pageNode.setProperties(new Properties(nodeState.getAttributesState()));

        if (node.getNodes() != null) {
            ArrayList<PageNode> children = new ArrayList<PageNode>(node.getNodeCount());
            for (NodeContext<?> child : node.getNodes()) {
                @SuppressWarnings("unchecked")
                NodeContext<NodeContext<?>> childNode = (NodeContext<NodeContext<?>>) child;
                children.add(createPageNode(service, childNode));
            }

            pageNode.setChildren(children);
        } else {
            pageNode.setChildren(new ArrayList<PageNode>(0));
        }

        return pageNode;
    }

    private static String[] trim(String[] array) {
        List<String> trimmed = new ArrayList<String>(array.length);
        for (String s : array) {
            if (s != null && !"".equals(s)) {
                trimmed.add(s);
            }
        }

        return trimmed.toArray(new String[trimmed.size()]);
    }
}
