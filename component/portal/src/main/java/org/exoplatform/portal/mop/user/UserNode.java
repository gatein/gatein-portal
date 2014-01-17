/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.user;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.page.PageKey;

/**
 * A navigation node as seen by a user.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNode {

    /** . */
    final UserNodeContext owner;

    /** . */
    final NodeContext<UserNode> context;

    /** . */
    String uri;

    UserNode(UserNodeContext owner, NodeContext<UserNode> context) {
        this.owner = owner;
        this.context = context;
        this.uri = null;
    }

    public UserNavigation getNavigation() {
        return owner.navigation;
    }

    public String getId() {
        return context.getId();
    }

    UserNode filter() {
        owner.filter(this);
        return this;
    }

    public String getName() {
        return context.getName();
    }

    public void setName(String name) {
        context.setName(name);

        //
        this.uri = null;
    }

    public String getURI() {
        if (uri == null) {
            uri = buildURI().toString();
        }
        return uri;
    }

    private StringBuilder buildURI() {
        UserNode parent = context.getParentNode();
        if (parent != null) {
            StringBuilder builder = parent.buildURI();
            if (builder.length() > 0) {
                builder.append('/');
            }
            return builder.append(context.getName());
        } else {
            return new StringBuilder();
        }
    }

    public String getLabel() {
        return context.getState().getLabel();
    }

    public void setLabel(String label) {
        context.setState(new NodeState.Builder(context.getState()).label(label).build());
    }

    public String getIcon() {
        return context.getState().getIcon();
    }

    public void setIcon(String icon) {
        context.setState(new NodeState.Builder(context.getState()).icon(icon).build());
    }

    public long getStartPublicationTime() {
        return context.getState().getStartPublicationTime();
    }

    public void setStartPublicationTime(long startPublicationTime) {
        context.setState(new NodeState.Builder(context.getState()).startPublicationTime(startPublicationTime).build());
    }

    public boolean isRestrictOutsidePublicationWindow() {
        return context.getState().isRestrictOutsidePublicationWindow();
    }

    public void setRestrictOutsidePublicationWindow(boolean restrictOutsidePublicationWindow) {
        context.setState(
                new NodeState.Builder(context.getState())
                        .restrictOutsidePublicationWindow(restrictOutsidePublicationWindow)
                        .build()
        );
    }

    public long getEndPublicationTime() {
        return context.getState().getEndPublicationTime();
    }

    public void setEndPublicationTime(long endPublicationTime) {
        context.setState(new NodeState.Builder(context.getState()).endPublicationTime(endPublicationTime).build());
    }

    public Visibility getVisibility() {
        return context.getState().getVisibility();
    }

    public void setVisibility(Visibility visibility) {
        context.setState(new NodeState.Builder(context.getState()).visibility(visibility).build());
    }

    public PageKey getPageRef() {
        return context.getState().getPageRef();
    }

    public void setPageRef(PageKey pageRef) {
        context.setState(new NodeState.Builder(context.getState()).pageRef(pageRef).build());
    }

    public String getResolvedLabel() {
        String resolvedLabel = null;

        //
        String id = context.getId();

        //
        if (context.getState().getLabel() != null) {
            ResourceBundle bundle = owner.navigation.getBundle();
            resolvedLabel = ExpressionUtil.getExpressionValue(bundle, context.getState().getLabel());
        } else if (id != null) {
            Locale userLocale = owner.navigation.portal.context.getUserLocale();
            Locale portalLocale = owner.navigation.portal.getLocale();
            DescriptionService descriptionService = owner.navigation.portal.service.getDescriptionService();
            Described.State description = descriptionService.resolveDescription(id, portalLocale, userLocale);
            if (description != null) {
                resolvedLabel = description.getName();
            }
        }

        //
        if (resolvedLabel == null) {
            resolvedLabel = getName();
        }

        //
        return resolvedLabel;
    }

    public void setResolvedLabel(String label) {
        String id = context.getId();
        Locale userLocale = owner.navigation.portal.context.getUserLocale();
        DescriptionService descriptionService = owner.navigation.portal.service.getDescriptionService();

        Described.State description = new Described.State(label, null);

        descriptionService.setDescription(id, userLocale, description);
    }

    public String getEncodedResolvedLabel() {
        return HTMLEntityEncoder.getInstance().encode(getResolvedLabel());
    }

    public UserNode getParent() {
        return context.getParentNode();
    }

    /**
     * Returns true if the children relationship determined.
     *
     * @return ture if node has children
     */
    public boolean hasChildrenRelationship() {
        return context.isExpanded();
    }

    /**
     * Returns the number of children.
     *
     * @return the number of children
     */
    public int getChildrenCount() {
        return context.getNodeCount();
    }

    public int getChildrenSize() {
        return context.getNodeSize();
    }

    public Collection<UserNode> getChildren() {
        return context.isExpanded() ? context.getNodes() : Collections.<UserNode> emptyList();
    }

    /**
     * Returns a child by its name or null if the child does not exist or the children relationship has not been loaded.
     *
     * @param childName the child name
     * @return the corresponding user node
     * @throws NullPointerException if the child name is null
     */
    public UserNode getChild(String childName) throws NullPointerException {
        if (context.isExpanded()) {
            return context.getNode(childName);
        } else {
            return null;
        }
    }

    /**
     * Returns a child by its index or null if the children relationship has not been loaded.
     *
     * @param childIndex the child index
     * @return the corresponding user node
     * @throws IndexOutOfBoundsException if the children relationship is loaded and the index is outside of its bounds
     */
    public UserNode getChild(int childIndex) throws IndexOutOfBoundsException {
        if (context.isExpanded()) {
            return context.getNode(childIndex);
        } else {
            return null;
        }
    }

    public void addChild(UserNode child) {
        context.add(null, child.context);
        child.uri = null;
    }

    public void addChild(int index, UserNode child) {
        context.add(index, child.context);
        child.uri = null;
    }

    public UserNode addChild(String childName) {
        return context.add(null, childName).getNode();
    }

    public boolean removeChild(String childName) {
        return context.removeNode(childName);
    }

    // Keep this internal for now
    UserNode find(String nodeId) {
        return context.getDescendantNode(nodeId);
    }

    public String toString() {
        return toString(1);
    }

    public String toString(int depth) {
        return context.toString(depth, new StringBuilder("UserNode[")).append("]").toString();
    }
}
