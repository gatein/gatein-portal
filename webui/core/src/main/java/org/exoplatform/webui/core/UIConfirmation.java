/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.webui.core;

import java.io.Serializable;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Nguyen Duc Khoi khoi.nguyen@exoplatform.com Apr 22, 2010
 *
 * Display a confirm popup message
 *
 */

@ComponentConfig(template = "system:/groovy/webui/core/UIConfirmation.gtmpl", events = {
        @EventConfig(listeners = UIConfirmation.CloseActionListener.class),
        @EventConfig(listeners = UIConfirmation.ClickActionListener.class) })
@Serialized
public class UIConfirmation extends UIPopupWindow {
    private String message_;

    private Object caller_;

    private List<ActionConfirm> actions_;

    public UIConfirmation() {
        this.message_ = "";
        this.caller_ = new Object();
        setShowMask(true);
        setShow(true);
    }

    public UIConfirmation(String message, Object caller) {
        this.message_ = message;
        this.caller_ = caller;
        setShowMask(true);
        setShow(true);
    }

    public void setMessage(String message) {
        this.message_ = message;
    }

    public String getMessage() {
        return message_;
    }

    public Object getCaller() {
        return caller_;
    }

    public void setCaller(Object caller) {
        this.caller_ = caller;
    }

    public void addMessage(String message) {
        this.message_ = message;
    }

    public void clearMessage() {
        this.message_ = "";
    }

    public void setActions(List<ActionConfirm> actions_) {
        this.actions_ = actions_;
    }

    public List<ActionConfirm> getActions() {
        return actions_;
    }

    /**
     * Check if message null or empty then don't display popup
     *
     * @return {@link Boolean}
     */
    public boolean hasMessage() {
        return (message_ != null) && (!message_.equals(""));
    }

    private void hidePopup(Event<UIConfirmation> event) throws Exception {
        WebuiRequestContext context = event.getRequestContext();
        this.clearMessage();
        context.addUIComponentToUpdateByAjax(this);
        UIComponent uiParent = getParent();
        Event<UIComponent> pEvent = uiParent.createEvent("ClosePopup", event.getExecutionPhase(), event.getRequestContext());
        if (pEvent != null)
            pEvent.broadcast();
    }

    public static class CloseActionListener extends EventListener<UIConfirmation> {
        @Override
        public void execute(Event<UIConfirmation> event) throws Exception {
            UIConfirmation uiConfirmation = event.getSource();
            uiConfirmation.hidePopup(event);
        }
    }

    public static class ClickActionListener extends EventListener<UIConfirmation> {
        @Override
        public void execute(Event<UIConfirmation> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIConfirmation uiConfirmation = event.getSource();

            UIComponent uiComponent = (UIComponent) uiConfirmation.getCaller();
            Event<UIComponent> xEvent = uiComponent.createEvent(context.getRequestParameter(OBJECTID),
                    event.getExecutionPhase(), context);

            if (xEvent != null) {
                xEvent.broadcast();
            }

            uiConfirmation.hidePopup(event);
        }
    }

    /**
     * Created by The eXo Platform SAS Author : Nguyen Duc Khoi khoi.nguyen@exoplatform.com Apr 22, 2010
     *
     * Define actions which are rendered
     *
     */

    public static class ActionConfirm implements Serializable {
        private String eventId_;

        private String actionKey_;

        public ActionConfirm() {
        }

        public ActionConfirm(String eventId, String actionKey) {
            this.eventId_ = eventId;
            this.actionKey_ = actionKey;
        }

        public void setEventId(String eventId) {
            this.eventId_ = eventId;
        }

        public String getEventId() {
            return eventId_;
        }

        public void setActionKey(String actionKey) {
            this.actionKey_ = actionKey;
        }

        public String getActionKey() {
            return actionKey_;
        }
    }
}
