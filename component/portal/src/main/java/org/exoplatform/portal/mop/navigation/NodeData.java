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

package org.exoplatform.portal.mop.navigation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.RestrictAccess;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.Visible;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.link.Link;
import org.gatein.mop.api.workspace.link.PageLink;

/**
 * An immutable node data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeData implements Serializable {

    /** . */
    final String parentId;

    /** . */
    final String id;

    /** . */
    final String name;

    /** . */
    final NodeState state;

    /** . */
    final String[] children;

    NodeData(Navigation navigation) {
        String[] children;
        List<Navigation> _children = navigation.getChildren();
        if (_children == null) {
            children = Utils.EMPTY_STRING_ARRAY;
        } else {
            children = new String[_children.size()];
            int index = 0;
            for (Navigation child : _children) {
                children[index++] = child.getObjectId();
            }
        }

        //
        String label = null;
        if (navigation.isAdapted(Described.class)) {
            Described described = navigation.adapt(Described.class);
            label = described.getName();
        }

        //
        Visibility visibility = Visibility.DISPLAYED;
        Date startPublicationDate = null;
        Date endPublicationDate = null;
        boolean restrictOutsidePublicationWindow = false;
        if (navigation.isAdapted(RestrictAccess.class)) {
            RestrictAccess restrictAccess = navigation.adapt(RestrictAccess.class);
            restrictOutsidePublicationWindow = restrictAccess.isRestrictOutsidePublicationWindow();
            visibility = restrictAccess.getVisibility();
            startPublicationDate = restrictAccess.getStartPublicationDate();
            endPublicationDate = restrictAccess.getEndPublicationDate();
        }

        //
        PageKey pageRef = null;
        Link link = navigation.getLink();
        if (link instanceof PageLink) {
            PageLink pageLink = (PageLink) link;
            org.gatein.mop.api.workspace.Page target = pageLink.getPage();
            if (target != null) {
                Site site = target.getSite();
                pageRef = Utils.siteType(site.getObjectType()).key(site.getName()).page(target.getName());
            }
        }

        //
        Attributes attrs = navigation.getAttributes();
        AttributesState attributesState = new AttributesState.Builder()
                .attributes(NavigationServiceImpl.CUSTOM_NODE_ATTRIBUTE_PREFIX, attrs)
                .build();

        //
        NodeState state = new NodeState(label, attrs.getValue(MappedAttributes.ICON),
                startPublicationDate != null ? startPublicationDate.getTime() : -1,
                endPublicationDate != null ? endPublicationDate.getTime() : -1, visibility, pageRef,
                restrictOutsidePublicationWindow,
                attributesState);

        //
        String parentId;
        Navigation parent = navigation.getParent();
        if (parent != null) {
            parentId = parent.getObjectId();
        } else {
            parentId = null;
        }

        //
        this.parentId = parentId;
        this.id = navigation.getObjectId();
        this.name = navigation.getName();
        this.state = state;
        this.children = children;
    }

    NodeData(NodeContext<?> context) {
        int size = 0;
        for (NodeContext<?> current = context.getFirst(); current != null; current = current.getNext()) {
            size++;
        }
        String[] children = new String[size];
        for (NodeContext<?> current = context.getFirst(); current != null; current = current.getNext()) {
            children[children.length - size--] = current.handle;
        }
        String parentId = context.getParent() != null ? context.getParent().handle : null;
        String id = context.handle;
        String name = context.getName();
        NodeState state = context.getState();

        //
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.state = state;
        this.children = children;
    }

    NodeData(String parentId, String id, String name, NodeState state, String[] children) {
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.state = state;
        this.children = children;
    }

    public Iterator<String> iterator(boolean reverse) {
        if (reverse) {
            return new Iterator<String>() {
                int index = children.length;

                public boolean hasNext() {
                    return index > 0;
                }

                public String next() {
                    if (index > 0) {
                        return children[--index];
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return new Iterator<String>() {
                int index = 0;

                public boolean hasNext() {
                    return index < children.length;
                }

                public String next() {
                    if (index < children.length) {
                        return children[index++];
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NodeState getState() {
        return state;
    }

    @Override
    public String toString() {
        return "NodeData[id=" + id + ",name=" + name + ",state=" + state + ",children=" + Arrays.asList(children) + "]";
    }
}
