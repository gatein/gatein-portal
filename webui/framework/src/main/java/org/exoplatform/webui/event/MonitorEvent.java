/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.webui.event;

import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SAS Jun 10, 2006
 *
 * An event object used to monitor the lifecycle of a component
 */
public class MonitorEvent<T> extends Event<T> {

    public static final String PORTAL_APPLICATION_LIFECYCLE_EVENT = "portal.application.lifecycle.event";

    public static final String PORTAL_EXECUTION_LIFECYCLE_EVENT = "portal.execution.lifecycle.event";

    public static final String PORTLET_APPLICATION_LIFECYCLE_EVENT = "portlet.application.lifecycle.event";

    public static final String PORTLET_ACTION_LIFECYCLE_EVENT = "portlet.action.lifecycle.event";

    public static final String PORTLET_RENDER_LIFECYCLE_EVENT = "portlet.render.lifecycle.event";

    public static final String UICOMPONENT_LIFECYCLE_MONITOR_EVENT = "uicomponent.lifecycle.monitor.event";

    private long startExecutionTime_;

    private long endExecutionTime_;

    private Throwable error_;

    public MonitorEvent(T source, String name, WebuiRequestContext context) {
        super(source, name, context);
    }

    public long getStartExecutionTime() {
        return startExecutionTime_;
    }

    public void setStartExecutionTime(long t) {
        startExecutionTime_ = t;
    }

    public long getEndExecutionTime() {
        return endExecutionTime_;
    }

    public void setEndExecutionTime(long t) {
        endExecutionTime_ = t;
    }

    public Throwable getError() {
        return error_;
    }

    public void setError(Throwable t) {
        error_ = t;
    }
}
