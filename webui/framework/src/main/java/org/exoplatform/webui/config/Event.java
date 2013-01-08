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

package org.exoplatform.webui.config;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS May 9, 2006
 */
public class Event {

    private String name;

    private String confirm;

    private InitParams initParams;

    private ArrayList<String> listeners;

    private boolean csrfCheck;

    private transient List<EventListener> eventListeners_;

    private String phase = "process";

    private transient Phase executionPhase_ = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String s) {
        phase = s;
    }

    public List<String> getListeners() {
        return listeners;
    }

    public void setListeners(ArrayList<String> listeners) {
        this.listeners = listeners;
    }

    public InitParams getInitParams() {
        return initParams;
    }

    public void setInitParams(InitParams initParams) {
        this.initParams = initParams;
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public Phase getExecutionPhase() {
        if (executionPhase_ != null)
            return executionPhase_;
        try {
            executionPhase_ = Phase.valueOf(phase.toUpperCase());
        } catch (Exception e) {
            executionPhase_ = Phase.PROCESS;
        }
        return executionPhase_;
    }

    public void setExecutionPhase(Phase executionPhase) {
        this.executionPhase_ = executionPhase;
    }

    public List<EventListener> getCachedEventListeners() {
        return eventListeners_;
    }

    public void setCachedEventListeners(List<EventListener> list) {
        eventListeners_ = list;
    }

    public boolean isCsrfCheck() {
        return csrfCheck;
    }

    public void setCsrfCheck(boolean csrfCheck) {
        this.csrfCheck = csrfCheck;
    }

}
