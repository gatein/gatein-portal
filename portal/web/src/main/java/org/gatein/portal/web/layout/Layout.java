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
package org.gatein.portal.web.layout;

import java.io.IOException;
import java.util.Map;

import org.gatein.portal.mop.hierarchy.NodeContext;
import org.gatein.portal.mop.layout.ElementState;
import org.gatein.portal.web.page.PageContext;
import org.gatein.portal.web.page.Result;
import org.gatein.portal.web.page.PageContext;
import org.gatein.portal.web.page.Result;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class Layout {

    /**
     * Renders the layout.
     *
     *
     *
     * @param fragments the fragments
     * @param body the optional body
     * @param state the page state
     * @param to the target stream
     * @throws IOException
     */
    public abstract void render(
            Map<String, Result.Fragment> fragments,
            String body, PageContext state,
            Appendable to) throws IOException;

    public static <N> Layout build(NodeContext<N, ElementState> node, LayoutBuilder builder) {
        build_(node, builder);
        return builder.build();
    }

    private static <N> void build_(NodeContext<N, ElementState> node, LayoutBuilder builder) {
        ElementState state = node.getState();
        if (state instanceof ElementState.Container) {
            ElementState.Container containerState = (ElementState.Container) state;
            builder.beginContainer(node.getName(), containerState);
            for (NodeContext<N, ElementState> child : node) {
                build_(child, builder);
            }
            builder.endContainer(node.getName(), containerState);
        } else if (state instanceof ElementState.Window) {
            ElementState.Window windowState = (ElementState.Window) state;
            builder.window(node.getName(), windowState);
        }
    }
}
