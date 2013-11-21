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
package org.gatein.portal.content;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * @author Julien Viet
 */
public abstract class Result {

    public static class Error extends Result {

        /** . */
        private final boolean internal;

        /** . */
        private final Throwable cause;

        public Error(boolean internal, Throwable cause) {
            this.internal = internal;
            this.cause = cause;
        }

        public boolean isInternal() {
            return internal;
        }

        public Throwable getCause() {
            return cause;
        }
    }

    public static class Fragment extends Result {

        /** . */
        public final String title;

        /** . */
        public final String content;

        /** . */
        public final List<Map.Entry<String, String>> headers;

        /** . */
        public final List<Element> headerTags;

        public Fragment(
                List<Map.Entry<String, String>> headers,
                List<Element> headerTags,
                String title,
                String content) {
            this.headers = headers;
            this.headerTags = headerTags;
            this.title = title;
            this.content = content;
        }
    }

    public static class Update<S extends Serializable> extends Result {

        public final String parameters;

        public final String windowState;

        public final String mode;

        public final Map<String, String[]> changes;

        public final S state;

        public Update(String parameters, String windowState, String mode, Map<String, String[]> changes, S state) {
            this.parameters = parameters;
            this.windowState = windowState;
            this.mode = mode;
            this.changes = changes;
            this.state = state;
        }
    }
}
