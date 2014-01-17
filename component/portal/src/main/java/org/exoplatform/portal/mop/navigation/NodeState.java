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
import java.util.Date;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.page.PageKey;

/**
 * An immutable node state class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class NodeState implements Serializable {

    /** . */
    public static final NodeState INITIAL = new NodeState.Builder().build();

    /**
     * Builder class.
     */
    public static class Builder {

        /** . */
        private String label;

        /** . */
        private String icon;

        /** . */
        private long startPublicationTime;

        /** . */
        private long endPublicationTime;

        /** . */
        private Visibility visibility;

        /** . */
        private PageKey pageRef;

        /** . */
        private boolean restrictOutsidePublicationWindow;

        public Builder() {
            this.icon = null;
            this.label = null;
            this.startPublicationTime = -1;
            this.endPublicationTime = -1;
            this.visibility = Visibility.DISPLAYED;
            this.pageRef = null;
            this.restrictOutsidePublicationWindow = false;
        }

        /**
         * Creates a builder from a specified state.
         *
         * @param state the state to copy
         * @throws NullPointerException if the stateis null
         */
        public Builder(NodeState state) throws NullPointerException {
            if (state == null) {
                throw new NullPointerException();
            }
            this.label = state.label;
            this.icon = state.icon;
            this.startPublicationTime = state.startPublicationTime;
            this.endPublicationTime = state.endPublicationTime;
            this.visibility = state.visibility;
            this.pageRef = state.pageRef;
            this.restrictOutsidePublicationWindow = state.restrictOutsidePublicationWindow;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder startPublicationTime(long startPublicationTime) {
            this.startPublicationTime = startPublicationTime;
            return this;
        }

        public Builder endPublicationTime(long endPublicationTime) {
            this.endPublicationTime = endPublicationTime;
            return this;
        }

        public Builder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder pageRef(PageKey pageRef) {
            this.pageRef = pageRef;
            return this;
        }

        public Builder restrictOutsidePublicationWindow(boolean restrictOutsidePublicationWindow) {
            this.restrictOutsidePublicationWindow = restrictOutsidePublicationWindow;
            return this;
        }

        public NodeState build() {
            return new NodeState(label, icon, startPublicationTime, endPublicationTime, visibility, pageRef,
                    restrictOutsidePublicationWindow);
        }
    }

    /** . */
    private final String label;

    /** . */
    private final String icon;

    /** . */
    private final long startPublicationTime;

    /** . */
    private final long endPublicationTime;

    /** . */
    private final Visibility visibility;

    /** . */
    private final PageKey pageRef;

    /** . */
    private final boolean restrictOutsidePublicationWindow;

    public NodeState(String label, String icon, long startPublicationTime, long endPublicationTime, Visibility visibility,
            PageKey pageRef, boolean restrictOutsidePublicationWindow) {
        this.label = label;
        this.icon = icon;
        this.startPublicationTime = startPublicationTime;
        this.endPublicationTime = endPublicationTime;
        this.visibility = visibility;
        this.pageRef = pageRef;
        this.restrictOutsidePublicationWindow = restrictOutsidePublicationWindow;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public long getStartPublicationTime() {
        return startPublicationTime;
    }

    Date getStartPublicationDate() {
        return startPublicationTime != -1 ? new Date(startPublicationTime) : null;
    }

    public long getEndPublicationTime() {
        return endPublicationTime;
    }

    Date getEndPublicationDate() {
        return endPublicationTime != -1 ? new Date(endPublicationTime) : null;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public PageKey getPageRef() {
        return pageRef;
    }

    public boolean isRestrictOutsidePublicationWindow() {
        return restrictOutsidePublicationWindow;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof NodeState) {
            NodeState that = (NodeState) o;
            return Safe.equals(label, that.label) && Safe.equals(icon, that.icon)
                    && Safe.equals(startPublicationTime, that.startPublicationTime)
                    && Safe.equals(endPublicationTime, that.endPublicationTime)
                    && Safe.equals(visibility, that.visibility)
                    && Safe.equals(pageRef, that.pageRef)
                    && Safe.equals(restrictOutsidePublicationWindow, that.restrictOutsidePublicationWindow);
        }
        return false;
    }

    @Override
    public String toString() {
        return "NodeState[label=" + label + ",icon=" + icon + ",startPublicationTime=" + startPublicationTime
                + ",endPublicationTime=" + endPublicationTime + ",visibility=" + visibility + ",pageRef=" + pageRef
                + ",restrictOutsidePublicationWindow=" + restrictOutsidePublicationWindow + "]";
    }

    public Builder builder() {
        return new Builder(this);
    }
}
