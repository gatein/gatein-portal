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
package org.gatein.portal.web.content.simple;

import java.util.Collections;
import java.util.HashMap;

import org.gatein.portal.content.ContentDescription;
import org.gatein.portal.content.ContentProvider;
import org.gatein.portal.content.WindowContent;

/**
 * @author Julien Viet
 */
public class SimpleContentProvider implements ContentProvider<SimpleState> {

    /** . */
    private static final HashMap<String, SimpleContentLogic> contents = new HashMap<String, SimpleContentLogic>();

    public static void deploy(String id, SimpleContentLogic logic) {
        contents.put(id, logic);
    }

    public static void undeploy(String id) {
        contents.remove(id);
    }

    @Override
    public SimpleContentType getContentType() {
        return new SimpleContentType();
    }

    @Override
    public WindowContent<SimpleState> getContent(String id) {
        SimpleContentLogic logic = contents.get(id);
        return logic != null ? new SimpleContent(id, "normal", "view", logic) : null;
    }

    @Override
    public Iterable<ContentDescription> findContents(String filter, int offset, int limit) {
        return Collections.emptyList();
    }
}
