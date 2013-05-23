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
package org.gatein.portal.ui.navigation;

import java.util.Locale;

import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.description.DescriptionState;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;
import org.gatein.portal.mop.navigation.NodeState;

/**
 * @author Julien Viet
 */
public class UserNode {

    public static class Model implements NodeModel<UserNode, NodeState> {

        /** . */
        final DescriptionService descriptionService;

        /** . */
        final Locale locale;

        public Model(DescriptionService descriptionService, Locale locale) {
            this.descriptionService = descriptionService;
            this.locale = locale;
        }

        @Override
        public NodeContext<UserNode, NodeState> getContext(UserNode node) {
            return node.context;
        }

        @Override
        public UserNode create(NodeContext<UserNode, NodeState> context) {
            return new UserNode(this, context);
        }
    }

    /** . */
    private final Model model;

    /** . */
    private final NodeContext<UserNode, NodeState> context;

    /** The cached label. */
    private String label;

    public UserNode(Model model, NodeContext<UserNode, NodeState> context) {
        this.model = model;
        this.context = context;
    }

    public Iterable<UserNode> getChildren() {
        return context.getNodes();
    }

    public String getLabel() {
        if (label == null) {
            DescriptionState description = model.descriptionService.loadDescription(context.getId(), model.locale);
            if (description != null) {
                label = description.getName();
            } else {
                label = context.getState().getLabel();
                if (label == null) {
                    label = context.getName();
                }
            }
        }
        return label;
    }

    public String getLink() {
        return makeLink().toString();
    }

    private StringBuilder makeLink() {
        UserNode parent = context.getParentNode();
        if (parent == null) {
            return new StringBuilder("/portal");
        } else {
            return parent.makeLink().append("/").append(context.getName());
        }
    }

}
