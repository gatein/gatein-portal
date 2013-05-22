/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop.importer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.config.model.I18NString;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PageNodeContainer;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.hierarchy.GenericScope;
import org.gatein.portal.mop.navigation.NavigationContext;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.navigation.NodeState;
import org.gatein.portal.mop.hierarchy.Scope;
import org.exoplatform.portal.tree.diff.Adapters;
import org.exoplatform.portal.tree.diff.ListAdapter;
import org.exoplatform.portal.tree.diff.ListChangeIterator;
import org.exoplatform.portal.tree.diff.ListChangeType;
import org.exoplatform.portal.tree.diff.ListDiff;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NavigationNodeImporter {

    private static final ListAdapter<PageNodeContainer, String> PAGE_NODE_CONTAINER_ADAPTER = new ListAdapter<PageNodeContainer, String>() {
        public int size(PageNodeContainer list) {
            List<PageNode> nodes = list.getNodes();
            if (nodes == null) {
                return 0;
            } else {
                return nodes.size();
            }
        }

        public Iterator<String> iterator(PageNodeContainer list, boolean reverse) {
            List<PageNode> nodes = list.getNodes();
            if (nodes == null) {
                return Collections.<String> emptyList().iterator();
            } else {
                String[] names = new String[nodes.size()];
                int index = 0;
                for (PageNode child : nodes) {
                    names[index++] = child.getName();
                }
                return Adapters.<String>array().iterator(names, reverse);
            }
        }
    };

    private static final ListAdapter<NodeContext<?, NodeState>, String> NODE_ADAPTER = new ListAdapter<NodeContext<?, NodeState>, String>() {
        public int size(NodeContext<?, NodeState> list) {
            return list.getNodeCount();
        }

        public Iterator<String> iterator(NodeContext<?, NodeState> list, boolean reverse) {
            int size = list.getNodeCount();
            String[] names = new String[size];
            int index = 0;
            for (NodeContext<?, NodeState> child = list.getFirst(); child != null; child = child.getNext()) {
                names[index++] = child.getName();
            }
            return Adapters.<String>array().iterator(names, reverse);
        }
    };

    /** . */
    private final String[] path;

    /** . */
    private final NavigationService navigationService;

    /** . */
    private final SiteKey navigationKey;

    /** . */
    private final Locale portalLocale;

    /** . */
    private final DescriptionService descriptionService;

    /** . */
    private final NavigationFragment src;

    /** . */
    private final ImportConfig config;

    public NavigationNodeImporter(String[] path, NavigationService navigationService, SiteKey navigationKey,
                                  Locale portalLocale, DescriptionService descriptionService, NavigationFragment src, ImportConfig config) {
        this.path = path;
        this.navigationService = navigationService;
        this.navigationKey = navigationKey;
        this.portalLocale = portalLocale;
        this.descriptionService = descriptionService;
        this.src = src;
        this.config = config;
    }

    public ImportConfig getConfig() {
        return config;
    }

    public NodeContext<?, NodeState> perform() {
        NavigationContext navigationCtx = navigationService.loadNavigation(navigationKey);

        //
        if (navigationCtx != null) {
            NodeContext<?, NodeState> root = navigationService.loadNode(NodeState.model(), navigationCtx, GenericScope.branchShape(path),
                    null);

            //
            NodeContext<?, NodeState> from = root;
            for (String name : path) {
                NodeContext<?, NodeState> a = from.get(name);
                if (a != null) {
                    from = a;
                } else {
                    from = from.add(null, name, NodeState.INITIAL);
                }
            }

            // Collect labels
            Map<NodeContext<?, NodeState>, Map<Locale, DescriptionState>> labelMap = new HashMap<NodeContext<?, NodeState>, Map<Locale, DescriptionState>>();

            // Update the root node
            update(src, from, labelMap);

            // Perform save
            perform(src, from, labelMap);

            // Save the node
            navigationService.saveNode(root, null);

            //
            for (Map.Entry<NodeContext<?, NodeState>, Map<Locale, DescriptionState>> entry : labelMap.entrySet()) {
                String id = entry.getKey().getId();
                descriptionService.saveDescriptions(id, entry.getValue());
            }

            //
            return from;
        } else {
            return null;
        }
    }

    private void perform(PageNodeContainer src, final NodeContext<?, NodeState> dst,
            final Map<NodeContext<?, NodeState>, Map<Locale, DescriptionState>> labelMap) {
        navigationService.rebaseNode(dst, Scope.CHILDREN, null);

        //
        ListDiff<PageNodeContainer, NodeContext<?, NodeState>, String> diff = new ListDiff<PageNodeContainer, NodeContext<?, NodeState>, String>(
                PAGE_NODE_CONTAINER_ADAPTER, NODE_ADAPTER);

        //
        List<PageNode> srcChildren = src.getNodes();
        ListChangeIterator<PageNodeContainer, NodeContext<?, NodeState>, String> it = diff.iterator(src, dst);

        class Change {
            final ListChangeType type;
            final String name;
            final int index1;
            final int index2;

            Change(ListChangeType type, String name, int index1, int index2) {
                this.type = type;
                this.name = name;
                this.index1 = index1;
                this.index2 = index2;
            }
        }

        // Buffer the changes in a list
        LinkedList<Change> foo = new LinkedList<Change>();
        while (it.hasNext()) {
            ListChangeType type = it.next();
            foo.add(new Change(type, it.getElement(), it.getIndex1(), it.getIndex2()));
        }

        // The last encountered child
        NodeContext<?, NodeState> previousChild = null;

        // Replay the changes and apply them
        for (Change change : foo) {
            PageNode srcChild = src.getNode(change.name);
            NodeContext<?, NodeState> dstChild = dst.get(change.name);

            //
            switch (change.type) {
                case SAME:
                    // Perform recursively
                    perform(srcChild, dstChild, labelMap);

                    //
                    if (config.updatedSame) {
                        update(srcChild, dstChild, labelMap);
                    }

                    //
                    previousChild = dstChild;
                    break;
                case REMOVE:
                    if (dst.getNode(change.name) != null) {
                    } else {
                        if (config.createMissing) {
                            previousChild = add(srcChild, previousChild, dst, labelMap);
                        }
                    }
                    break;
                case ADD:
                    if (src.getNode(change.name) != null) {
                        if (config.updatedSame) {
                            update(srcChild, dstChild, labelMap);
                        }
                        previousChild = dstChild;
                    } else {
                        if (config.destroyOrphan) {
                            dstChild.removeNode();
                        } else {
                            previousChild = dstChild;
                        }
                    }
                    break;
            }
        }
    }

    private NodeContext<?, NodeState> add(PageNode target, NodeContext<?, NodeState> previous, NodeContext<?, NodeState> parent,
            Map<NodeContext<?, NodeState>, Map<Locale, DescriptionState>> labelMap) {
        I18NString labels = target.getLabels();

        //
        Map<Locale, DescriptionState> description;
        if (labels.isSimple()) {
            description = null;
        } else if (labels.isEmpty()) {
            description = null;
        } else {
            description = new HashMap<Locale, DescriptionState>();
            for (Map.Entry<Locale, String> entry : labels.getExtended(portalLocale).entrySet()) {
                description.put(entry.getKey(), new DescriptionState(entry.getValue(), null));
            }
        }

        //
        String name = target.getName();
        int index;
        if (previous != null) {
            index = parent.get((previous).getName()).getIndex() + 1;
        } else {
            index = 0;
        }
        NodeContext<?, NodeState> child = parent.add(index, name, target.getState());

        //
        if (description != null) {
            labelMap.put(child, description);
        }

        // We recurse to create the descendants
        List<PageNode> targetChildren = target.getNodes();
        if (targetChildren != null) {
            NodeContext<?, NodeState> targetPrevious = null;
            for (PageNode targetChild : targetChildren) {
                targetPrevious = add(targetChild, targetPrevious, child, labelMap);
            }
        }

        //
        return child;
    }

    private void update(PageNodeContainer src, NodeContext<?, NodeState> target, Map<NodeContext<?, NodeState>, Map<Locale, DescriptionState>> labelMap) {

        //
        if (src.getState() != null) {
            target.setState(src.getState());
        }

        // Update extended labels if necessary
        I18NString labels = src.getLabels();
        if (labels != null) {
            Map<Locale, DescriptionState> description;
            if (labels.isSimple()) {
                description = null;
            } else if (labels.isEmpty()) {
                description = null;
            } else {
                description = new HashMap<Locale, DescriptionState>();
                for (Map.Entry<Locale, String> entry : labels.getExtended(portalLocale).entrySet()) {
                    description.put(entry.getKey(), new DescriptionState(entry.getValue(), null));
                }
            }

            if (description != null) {
                labelMap.put(target, description);
            } else {
                labelMap.put(target, Collections.<Locale, DescriptionState> emptyMap());
            }
        }
    }
}
