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
package org.gatein.api.navigation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.navigation.NodeChange;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.NodeState.Builder;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.gatein.api.EntityAlreadyExistsException;
import org.gatein.api.EntityNotFoundException;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.Util;
import org.gatein.api.common.Attributes;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.internal.ObjectToStringBuilder;
import org.gatein.api.internal.Parameters;
import org.gatein.api.navigation.Visibility.Status;
import org.gatein.api.page.PageId;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteType;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ApiNode implements Node {
    transient NodeContext<ApiNode> context;
    transient NavigationImpl navigation;

    private LocalizedString displayName;
    private String resolvedDisplayName;
    private String resolvedURI;
    private boolean displayNameChanged;
    private Attributes attributes;

    private final SiteId siteId;

    ApiNode(NavigationImpl navigation, NodeContext<ApiNode> context) {
        this.navigation = navigation;
        this.siteId = navigation.getSiteId();
        this.context = context;
    }

    @Override
    public Node addChild(int index, String childName) {
        checkChildrenLoaded();

        Parameters.requireNonNull(childName, "childName");

        return context.add(index, childName).getNode();
    }

    @Override
    public Node addChild(String childName) {
        checkChildrenLoaded();

        Parameters.requireNonNull(childName, "childName");

        if (hasChild(childName)) {
            throw new EntityAlreadyExistsException("child " + childName + " already exists");
        }

        return context.add(null, childName).getNode();
    }

    @Override
    public FilteredNode filter() {
        return new ApiFilteredNode(navigation, context);
    }

    @Override
    public Node getChild(int index) {
        checkChildrenLoaded();

        return context.isExpanded() ? context.getNode(index) : null;
    }

    @Override
    public Node getChild(String childName) {
        Parameters.requireNonNull(childName, "childName");

        checkChildrenLoaded();

        return context.isExpanded() ? context.getNode(childName) : null;
    }

    @Override
    public int getChildCount() {
        checkChildrenLoaded();

        return context.getNodeSize();
    }

    @Override
    public Node getNode(String... nodePath) {
        return getNode(NodePath.path(nodePath));
    }

    @Override
    public Node getNode(NodePath nodePath) {
        checkChildrenLoaded();

        Parameters.requireNonNull(nodePath, "nodePath");

        Node node = this;
        for (String name : nodePath) {
            node = node.getChild(name);
            if (node == null)
                return null;
        }

        return node;
    }

    @Override
    public String getIconName() {
        return context.getState().getIcon();
    }

    @Override
    public String getDisplayName() {
        if (resolvedDisplayName == null) {
            // Avoid any resolving if we're dealing with a simple non resource-bundle label
            String simple = context.getState().getLabel();
            if (simple != null && !ExpressionUtil.isResourceBindingExpression(simple)) {
                resolvedDisplayName = simple;
            } else {
                resolvedDisplayName = navigation.resolve(context);
            }
        }

        return resolvedDisplayName;
    }

    @Override
    public LocalizedString getDisplayNames() {
        if (displayName == null) {
            String simple = context.getState().getLabel();
            if (simple != null) {
                displayName = resolveExpression(simple);
            } else if (context.getId() != null) {
                Map<Locale, Described.State> descriptions = navigation.loadDescriptions(context.getId());
                displayName = ObjectFactory.createLocalizedString(descriptions);
            }
        }
        return displayName;
    }

    /**
     * Resolves the given WebUI style i18n place holder. Under the hood a similar thing happens in
     * {@link NavigationImpl#resolve(NodeContext)}.
     * <p>
     * In particuler, it checks whether {@code expression} is a WebUI style i18n place holder of form
     * <code>#{resource.bundle.key}</code>. In case it is, it iterates over {@link LocaleConfigService#getLocalConfigs()} and
     * resolves the key against each available {@link LocaleConfig#getNavigationResourceBundle(String, String)}. The resolved
     * values are stored in a new {@link LocalizedString} and returned.
     * <p>
     * If {@code expression} is not a WebUI style i18n place holder or if the resoltion does not succed for any locale,
     * {@code new LocalizedString(expression) is returned.}
     *
     * @param expression possibly a WebUI style i18n place holder
     * @return
     */
    private LocalizedString resolveExpression(String expression) {
        LocalizedString result = null;
        if (ExpressionUtil.isResourceBindingExpression(expression)) {
            String key = expression.substring(2, expression.length() - 1);
            LocaleConfigService configService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
                    LocaleConfigService.class);
            String ownerId = siteId.getName();
            if (siteId.getType() == SiteType.SPACE) {
                /* Remove the initial '/' in a group name */
                ownerId = ownerId.substring(1);
            }
            for (LocaleConfig localeConfig : configService.getLocalConfigs()) {
                Locale locale = localeConfig.getLocale();
                ResourceBundle rb = localeConfig.getNavigationResourceBundle(Util.from(siteId).getTypeName(), ownerId);
                try {
                    String value = rb.getString(key);
                    if (value != null) {
                        if (result == null) {
                            result = new LocalizedString(locale, value);
                        } else {
                            result.setLocalizedValue(locale, value);
                        }
                    }
                } catch (MissingResourceException e) {
                }
            }
        }
        return result == null ? new LocalizedString(expression) : result;
    }

    @Override
    public String getName() {
        return isRoot() ? null : context.getName();
    }

    @Override
    public NodePath getNodePath() {
        String name = getName();
        ApiNode parent = context.getParentNode();

        NodePath path = isRoot() ? NodePath.root() : NodePath.path(name);
        if (parent != null) {
            path = parent.getNodePath().append(path);
        }

        return path;
    }

    @Override
    public PageId getPageId() {
        return Util.from(context.getState().getPageRef());
    }

    @Override
    public Node getParent() {
        return context.getParentNode();
    }

    /**
     * @see org.gatein.api.navigation.Node#getAttributes()
     */
    @Override
    public Attributes getAttributes() {
        if (attributes == null) {
            attributes = new ApiNodeAttributes(context);
        }
        return attributes;
    }

    @Override
    public String getURI() {
        if (resolvedURI == null) {
            if (isRoot()) {
                resolvedURI = PortalRequest.getInstance().getURIResolver().resolveURI(siteId);
            } else {
                resolvedURI = getParent().getURI() + "/" + getName();
            }
        }

        return resolvedURI;
    }

    void clearCached() {
        resolvedDisplayName = null;
        resolvedURI = null;
    }

    @Override
    public Visibility getVisibility() {
        return ObjectFactory.createVisibility(context.getState());
    }

    @Override
    public boolean hasChild(String childName) {
        return getChild(childName) != null;
    }

    @Override
    public int indexOf(String childName) {
        ApiNode node = (ApiNode) getChild(childName);
        return node == null ? -1 : node.context.getIndex();
    }

    @Override
    public boolean isChildrenLoaded() {
        return context.isExpanded();
    }

    @Override
    public boolean isRoot() {
        return context.getParent() == null;
    }

    @Override
    public boolean isVisible() {
        return getVisibility().isVisible();
    }

    @Override
    public Iterator<Node> iterator() {
        return new ApiNodeModelIterator();
    }

    @Override
    public void moveTo(int index) {
        checkNonRoot();
        if (context.getIndex() < index) {
            index++;
        }
        context.getParent().add(index, context);
    }

    @Override
    public void moveTo(int index, Node parent) {
        moveTo(new Integer(index), parent);
    }

    @Override
    public void moveTo(Node parent) {
        moveTo(null, parent);
    }

    private void moveTo(Integer index, Node parent) {
        checkNonRoot();

        ((ApiNode) parent).checkChildrenLoaded();

        if (this.getNodePath().isParent(parent.getNodePath())) {
            throw new IllegalArgumentException("Can't move node to a child node of itself");
        }

        Node root = this;
        while (root.getParent() != null) {
            root = root.getParent();
        }

        if (((ApiNode) root.getNode(parent.getNodePath())).getContext() != ((ApiNode) parent).getContext()) {
            throw new IllegalArgumentException("Can't move node to a different branch");
        }

        ((ApiNode) parent).context.add(index, context);
    }

    @Override
    public boolean removeChild(String childName) {
        checkChildrenLoaded();

        if (!hasChild(childName)) {
            throw new EntityNotFoundException("Cannot remove child '" + childName
                    + "' because it does not exist for parent node " + getNodePath());
        }

        return context.removeNode(childName);
    }

    @Override
    public void setName(String name) throws IllegalArgumentException {
        Parameters.requireNonNull(name, "name");

        context.setName(name);
    }

    @Override
    public void setIconName(String iconName) {
        checkNonRoot();

        setState(getStateBuilder().icon(iconName));
    }

    @Override
    public void setDisplayName(String displayName) {
        Parameters.requireNonNull(displayName, "displayName");
        setDisplayNames(new LocalizedString(displayName));
    }

    @Override
    public void setDisplayNames(LocalizedString displayName) {
        checkNonRoot();

        if (displayName == null && this.displayName == null)
            return;

        if (displayName != null || !this.displayName.equals(displayName)) {
            if (displayName != null && !displayName.isLocalized()) {
                setState(getStateBuilder().label(displayName.getValue()));
            } else {
                setState(getStateBuilder().label(null));
            }
            this.displayName = displayName;
            this.resolvedDisplayName = null;
            displayNameChanged = true;
        }
    }

    @Override
    public void setPageId(PageId pageId) {
        checkNonRoot();

        setState(getStateBuilder().pageRef(Util.from(pageId)));
    }

    @Override
    public void setVisibility(boolean visible) {
        checkNonRoot();

        Builder b = getStateBuilder();
        if (visible) {
            b.visibility(org.exoplatform.portal.mop.Visibility.DISPLAYED);
        } else {
            b.visibility(org.exoplatform.portal.mop.Visibility.HIDDEN);
        }
        setState(b);
    }

    @Override
    public void setVisibility(PublicationDate publicationDate) {
        checkNonRoot();

        Parameters.requireNonNull(publicationDate, "publicationDate");

        long start = publicationDate.getStart() != null ? publicationDate.getStart().getTime() : -1;
        long end = publicationDate.getEnd() != null ? publicationDate.getEnd().getTime() : -1;

        setState(getStateBuilder().startPublicationTime(start).endPublicationTime(end)
                .visibility(org.exoplatform.portal.mop.Visibility.TEMPORAL));
    }

    @Override
    public void setVisibility(Visibility visibility) {
        checkNonRoot();

        Parameters.requireNonNull(visibility, "visibility");

        if (visibility.getStatus() == Status.PUBLICATION) {
            setVisibility(visibility.getPublicationDate());
        } else {
            setState(getStateBuilder().startPublicationTime(-1).endPublicationTime(-1)
                    .visibility(ObjectFactory.createVisibility(visibility.getStatus())));
        }
    }

    @Override
    public void sort(Comparator<Node> comparator) {
        Parameters.requireNonNull(comparator, "comparator");

        if (context.isExpanded()) {
            ApiNode[] a = new ApiNode[context.getNodeSize()];
            for (NodeContext<ApiNode> c = context.getFirst(); c != null; c = c.getNext()) {
                a[c.getIndex()] = c.getNode();
            }

            Arrays.sort(a, comparator);

            for (int i = 0; i < a.length; i++) {
                ApiNode n = a[i];
                NodeContext<ApiNode> c = n.getContext();
                if (c.getIndex() != i) {
                    c.getNode().moveTo(i);
                }
            }
        }
    }

    @Override
    public String toString() {
        return ObjectToStringBuilder.toStringBuilder(getClass()).add("name", getName()).add("path", getNodePath())
                .add("visibility", getVisibility()).add("iconName", getIconName()).add("pageId", getPageId()).toString();
    }

    NodeContext<ApiNode> getContext() {
        return context;
    }

    SiteId getSiteId() {
        return siteId;
    }

    boolean isDisplayNameChanged() {
        return displayNameChanged;
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        // deserialize serialization only fields
        NodePath nodePath = (NodePath) in.readObject();
        ApiNode parent = (ApiNode) in.readObject();
        boolean expanded = in.readBoolean();

        PortalRequest request = PortalRequest.getInstance();
        Portal portal = (request == null) ? null : request.getPortal();
        if (portal != null) {
            navigation = (NavigationImpl) portal.getNavigation(siteId);
            if (navigation == null)
                throw new IOException("Could not retrieve navigation for site " + siteId);
        } else {
            throw new IOException("Could not retrieve portal API during deserialization.");
        }

        if (parent != null) {
            context = parent.context.get(nodePath.getLastSegment());
            if (expanded) {
                navigation.rebaseNodeContext(context, new NodeVisitorScope(Nodes.visitChildren()), null);
            }
        } else {
            NodeVisitor visitor = (expanded) ? Nodes.visitChildren() : Nodes.visitNone();
            context = navigation.getNodeContext(nodePath, visitor);
        }

        if (expanded && parent == null) {
            MultiPathNodeVisitor visitor = new MultiPathNodeVisitor();
            readTree(visitor, in);
            navigation.rebaseNodeContext(context, new NodeVisitorScope(visitor), null);
        }

        boolean hasChanges = in.readBoolean();
        if (hasChanges && parent == null) { // re-apply changes from root node
            @SuppressWarnings("unchecked")
            List<ApiNodeChange> changes = (List<ApiNodeChange>) in.readObject();
            for (ApiNodeChange change : changes) {
                change.apply(this);
            }
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        // write serialization only fields
        out.writeObject(getNodePath());
        ApiNode parent = (context.getParent() != null) ? context.getParent().getNode() : null;
        out.writeObject(parent);

        boolean expanded = context.isExpanded();
        out.writeBoolean(expanded);
        if (expanded && parent == null) { // rebuild tree (using node paths)
            writeTree(this, out);
        }

        // serialize uncommitted changes
        boolean hasChanges = context.hasChanges() || displayNameChanged;
        out.writeBoolean(hasChanges);
        if (hasChanges && parent == null) // ensures we only do this once since the changes are for the entire tree
        {
            List<ApiNodeChange> changes = new ArrayList<ApiNodeChange>();
            for (NodeChange<NodeContext<ApiNode>> change : context.getChanges()) {
                if (change instanceof NodeChange.Created) {
                    NodeChange.Created<NodeContext<ApiNode>> created = (NodeChange.Created<NodeContext<ApiNode>>) change;
                    changes.add(new ApiNodeChange.Created(created));
                } else if (change instanceof NodeChange.Destroyed) {
                    NodeChange.Destroyed<NodeContext<ApiNode>> destroyed = (NodeChange.Destroyed<NodeContext<ApiNode>>) change;
                    changes.add(new ApiNodeChange.Destroyed(destroyed));
                } else if (change instanceof NodeChange.Moved) {
                    NodeChange.Moved<NodeContext<ApiNode>> moved = (NodeChange.Moved<NodeContext<ApiNode>>) change;
                    changes.add(new ApiNodeChange.Moved(moved));
                } else if (change instanceof NodeChange.Renamed) {
                    NodeChange.Renamed<NodeContext<ApiNode>> renamed = (NodeChange.Renamed<NodeContext<ApiNode>>) change;
                    changes.add(new ApiNodeChange.Renamed(renamed));
                } else if (change instanceof NodeChange.Updated) {
                    NodeChange.Updated<NodeContext<ApiNode>> updated = (NodeChange.Updated<NodeContext<ApiNode>>) change;
                    changes.add(new ApiNodeChange.Updated(updated));
                } else {
                    throw new IOException("Cannot serialize: Non-compatible node change object " + change);
                }
            }
            out.writeObject(changes);
        }
    }

    private void readTree(MultiPathNodeVisitor visitor, ObjectInputStream in) throws IOException, ClassNotFoundException {
        NodePath path = (NodePath) in.readObject();
        visitor.add(path);
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            readTree(visitor, in);
        }
    }

    private void writeTree(Node node, ObjectOutputStream out) throws IOException {
        out.writeObject(node.getNodePath());
        if (node.isChildrenLoaded()) {
            out.writeInt(node.getChildCount());
            for (Node child : node) {
                writeTree(child, out);
            }
        } else {
            out.writeInt(0);
        }
    }

    private void checkNonRoot() {
        if (isRoot()) {
            throw new UnsupportedOperationException("Operation not supported on root node");
        }
    }

    private void checkChildrenLoaded() {
        if (!context.isExpanded()) {
            throw new IllegalStateException("Children are not loaded");
        }
    }

    private Builder getStateBuilder() {
        return new NodeState.Builder(context.getState());
    }

    private void setState(Builder builder) {
        context.setState(builder.build());
    }

    private class ApiNodeModelIterator implements Iterator<Node> {
        private Iterator<ApiNode> itr = context.iterator();
        private ApiNode last;

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public Node next() {
            last = itr.next();
            return last;
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }

            last.context.remove();
        }
    }
}
