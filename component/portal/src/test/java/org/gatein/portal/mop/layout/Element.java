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

package org.gatein.portal.mop.layout;

import java.io.Serializable;
import java.util.UUID;

import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.CloneApplicationState;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.mop.Properties;
import org.gatein.portal.mop.hierarchy.ModelNode;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.hierarchy.NodeModel;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Element extends ModelNode<Element, ElementState> {

    /** . */
    public static final NodeModel<Element, ElementState> MODEL = new NodeModel<Element, ElementState>() {
        public NodeContext<Element, ElementState> getContext(Element node) {
            return node.context;
        }

        public Element create(NodeContext<Element, ElementState> context) {
            return new Element(context);
        }
    };

    public Element(NodeContext<Element, ElementState> context) {
        super(context);
    }

    public static ElementState.WindowBuilder portlet(String portletId) {
        return portlet(portletId, null);
    }

    public static ElementState.WindowBuilder portlet(String portletId, Serializable portletState) {
        return new ElementState.WindowBuilder(
                ContentType.PORTLET,
                new TransientApplicationState(portletId, portletState),
                Properties.EMPTY);
    }

    public static ElementState.WindowBuilder clonePortlet(String customizationId) {
        return new ElementState.WindowBuilder(
                ContentType.PORTLET,
                new CloneApplicationState(customizationId),
                Properties.EMPTY);
    }

    public static ElementState.ContainerBuilder container() {
        return new ElementState.ContainerBuilder(
                null,
                Properties.EMPTY,
                false
        );
    }

    public static ElementState.BodyBuilder body() {
        return new ElementState.BodyBuilder();
    }

    public Element add(ElementState state) {
        return addChild(UUID.randomUUID().toString(), state);
    }

    public Element add(ElementState.Builder<?> builder) {
        return addChild(UUID.randomUUID().toString(), builder.build());
    }

}
