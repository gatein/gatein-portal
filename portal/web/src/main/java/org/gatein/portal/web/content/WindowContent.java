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
package org.gatein.portal.web.content;

import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.Response;
import org.gatein.portal.web.page.WindowContext;

/**
 * The relationship between a content and a window.
 *
 * @author Julien Viet
 */
public abstract class WindowContent {

    /**
     * Create a render task for the specified window.
     *
     * @param window the window
     * @return the render task
     */
    public abstract RenderTask createRender(WindowContentContext window);

    // todo : use WindowContentContext instead of WindowContext
    public abstract Response processAction(
            WindowContext window,
            String windowState,
            String mode,
            Map<String, String[]> interactionState);

    // todo : use WindowContentContext instead of WindowContext
    public abstract Response serveResource(
            WindowContext window,
            String id,
            Map<String, String[]> resourceState);

    /**
     * Resolve the title for the specified locale.
     *
     * @param locale the locale
     * @return the title or null
     */
    public abstract String resolveTitle(Locale locale);

    public abstract String getParameters();

    public abstract void setParameters(String s);

    public abstract String getWindowState();

    public abstract void setWindowState(String ws);

    public abstract String getMode();

    public abstract void setMode(String m);

    public abstract Map<String, String[]> computePublicParameters(Map<QName, String[]> parameters);

    public abstract Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes);

    public abstract WindowContent copy();

}
