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
package org.gatein.portal.page.spi;

import java.util.Map;

import juzu.Response;
import org.gatein.portal.page.NodeState;
import org.gatein.portal.page.WindowContext;

/**
 * @author Julien Viet
 */
public interface ContentProvider {

    /**
     * Create a relationship between the window and the specified content
     *
     * @param id the content id
     * @param nodeState the node state
     * @return the window content
     */
    WindowContent getContent(String id, NodeState nodeState);

    Response processAction(
            WindowContext window,
            String windowState,
            String mode,
            Map<String, String[]> interactionState);

    Response serveResource(
            WindowContext window,
            String id,
            Map<String, String[]> resourceState);

    /**
     * Create a render task for the specified window.
     *
     * @param window the window
     * @return the render task
     */
    RenderTask createRender(WindowContext window);

}
