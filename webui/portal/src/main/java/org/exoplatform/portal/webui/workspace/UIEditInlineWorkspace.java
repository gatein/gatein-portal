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

package org.exoplatform.portal.webui.workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.portal.webui.portal.UIPortalComposer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIConfirmation.ActionConfirm;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Tan Pham Dinh pdtanit@gmail.com Aug 27, 2009
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
        @EventConfig(listeners = UIEditInlineWorkspace.ConfirmCloseActionListener.class),
        @EventConfig(listeners = UIEditInlineWorkspace.AbortCloseActionListener.class),
        @EventConfig(listeners = UIEditInlineWorkspace.ConfirmFinishActionListener.class)})
public class UIEditInlineWorkspace extends UIContainer {

    public UIEditInlineWorkspace() throws Exception {
        addChild(UIPortalComposer.class, null, null);
        addChild(UIPortalToolPanel.class, null, null);

        UIConfirmation uiConfirmation = addChild(UIConfirmation.class, null, null);
        uiConfirmation.setCaller(this);

        createActionConfirms(uiConfirmation);
    }

    public void setUIComponent(UIComponent uiComp) {
        getChild(UIPortalToolPanel.class).setUIComponent(uiComp);
    }

    public UIComponent getUIComponent() {
        return getChild(UIPortalToolPanel.class).getUIComponent();
    }

    public UIPortalComposer getComposer() {
        return getChild(UIPortalComposer.class);
    }

    public void showConfirmWindow(String message) {
        UIConfirmation uiConfirmation = getChild(UIConfirmation.class);
        uiConfirmation.setMessage(message);
        createActionConfirms(uiConfirmation);
        ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiConfirmation);
    }

    public void createActionConfirms(UIConfirmation uiConfirmation) {
        ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
        String yes = resourceBundle.getString("UIEditInlineWorkspace.confirm.yes");
        String no = resourceBundle.getString("UIEditInlineWorkspace.confirm.no");

        List<ActionConfirm> actionConfirms = new ArrayList<ActionConfirm>();
        actionConfirms.add(new ActionConfirm("ConfirmClose", yes));
        actionConfirms.add(new ActionConfirm("AbortClose", no));
        uiConfirmation.setActions(actionConfirms);
    }

    public void createActionConfirm(String message, String confirmAction, String abortAction) {
        UIConfirmation uiConfirmation = getChild(UIConfirmation.class);
        ResourceBundle resourceBundle = WebuiRequestContext.getCurrentInstance().getApplicationResourceBundle();
        String yes = resourceBundle.getString("UIEditInlineWorkspace.confirm.yes");
        String no = resourceBundle.getString("UIEditInlineWorkspace.confirm.no");
        uiConfirmation.setMessage(resourceBundle.getString(message));

        List<ActionConfirm> actionConfirms = new ArrayList<ActionConfirm>();
        actionConfirms.add(new ActionConfirm(confirmAction, yes));
        actionConfirms.add(new ActionConfirm(abortAction, no));
        uiConfirmation.setActions(actionConfirms);
        ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(uiConfirmation);
    }

    public static class ConfirmCloseActionListener extends EventListener<UIEditInlineWorkspace> {

        @Override
        public void execute(Event<UIEditInlineWorkspace> event) throws Exception {
            UIEditInlineWorkspace uiEditInlineWorkspace = event.getSource();

            UIConfirmation uiConfirmation = uiEditInlineWorkspace.getChild(UIConfirmation.class);
            uiConfirmation.createEvent("Close", event.getExecutionPhase(), event.getRequestContext()).broadcast();

            UIPortalComposer uiPortalComposer = uiEditInlineWorkspace.getChild(UIPortalComposer.class);
            Event<UIComponent> abortEvent = uiPortalComposer.createEvent("Abort", event.getExecutionPhase(),
                    event.getRequestContext());
            abortEvent.broadcast();
        }
    }

    public static class ConfirmFinishActionListener extends EventListener<UIEditInlineWorkspace> {
        @Override
        public void execute(Event<UIEditInlineWorkspace> event) throws Exception {
            UIConfirmation uiConfirmation = event.getSource().getChild(UIConfirmation.class);
            uiConfirmation.createEvent("Close", event.getExecutionPhase(), event.getRequestContext()).broadcast();

            UIPortalComposer uiPortalComposer = event.getSource().getChild(UIPortalComposer.class);
            if (uiPortalComposer.getId().equals(UIPortalComposer.UIPAGE_EDITOR)) {
                uiPortalComposer.savePage(event.getSource(), event);
            } else {
                uiPortalComposer.saveSite();
            }
        }
    }

    public static class AbortCloseActionListener extends EventListener<UIEditInlineWorkspace> {

        @Override
        public void execute(Event<UIEditInlineWorkspace> event) throws Exception {
            UIEditInlineWorkspace uiEditInlineWorkspace = event.getSource();
            UIConfirmation uiConfirmation = uiEditInlineWorkspace.getChild(UIConfirmation.class);
            uiConfirmation.createEvent("Close", event.getExecutionPhase(), event.getRequestContext()).broadcast();
        }
    }
}
