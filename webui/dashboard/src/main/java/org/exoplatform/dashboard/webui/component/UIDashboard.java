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

package org.exoplatform.dashboard.webui.component;

import org.exoplatform.portal.webui.application.UIGadget;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({ @ComponentConfig(template = "classpath:groovy/dashboard/webui/component/UIDashboard.gtmpl", events = {
        @EventConfig(listeners = UIDashboardContainer.MoveGadgetActionListener.class),
        @EventConfig(listeners = UIDashboardContainer.AddNewGadgetActionListener.class),
        @EventConfig(listeners = UIDashboard.SetShowSelectContainerActionListener.class),
        @EventConfig(listeners = UIDashboardContainer.DeleteGadgetActionListener.class),
        @EventConfig(listeners = UIDashboard.MinimizeGadgetActionListener.class),
        @EventConfig(listeners = UIDashboard.MaximizeGadgetActionListener.class) }) })
public class UIDashboard extends UIContainer {

    public static String GADGET_POPUP_ID = "UIAddGadgetPopup";

    public static String APP_NOT_EXIST = "APP_NOT_EXIT";

    private static final String GTN_PREFIX = "gtn";

    private boolean isShowSelectPopup = false;

    private String aggregatorId;

    private UIGadget maximizedGadget;

    public UIDashboard() throws Exception {
        UIPopupWindow popup = addChild(UIPopupWindow.class, null, GADGET_POPUP_ID + "-" + Math.abs(hashCode()));
        popup.setUIComponent(createUIComponent(UIDashboardSelectContainer.class, null, null));
        addChild(UIDashboardContainer.class, null, null);
    }

    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        UIGadget uiGadget = this.getMaximizedGadget();
        if (uiGadget != null) {
            if (context.getAttribute(APP_NOT_EXIST) != null || context.getAttribute(UIGadget.SAVE_PREF_FAIL) != null) {
                this.setMaximizedGadget(null);
            }
        }

        super.processRender(context);
    }

    public void setColumns(int num) throws Exception {
        getChild(UIDashboardContainer.class).setColumns(num);
    }

    public void setContainerTemplate(String template) {
        getChild(UIDashboardContainer.class).setContainerTemplate(template);
    }

    public boolean canEdit() {
        DashboardParent parent = (DashboardParent) getParent();
        return parent.canEdit();
    }

    public boolean isShowSelectPopup() {
        return isShowSelectPopup;
    }

    public void setShowSelectPopup(final boolean value) {
        this.isShowSelectPopup = value;
        getChild(UIPopupWindow.class).setShow(value);
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public void setAggregatorId(String aggregatorId) {
        this.aggregatorId = aggregatorId;
    }

    public UIGadget getMaximizedGadget() {
        return maximizedGadget;
    }

    public void setMaximizedGadget(UIGadget gadget) {
        maximizedGadget = gadget;
    }

    public static class SetShowSelectContainerActionListener extends EventListener<UIDashboard> {
        public final void execute(final Event<UIDashboard> event) throws Exception {
            UIDashboard uiDashboard = (UIDashboard) event.getSource();
            if (!uiDashboard.canEdit()) {
                return;
            }
            PortletRequestContext pcontext = (PortletRequestContext) event.getRequestContext();
            boolean isShow = Boolean.parseBoolean(pcontext.getRequestParameter("isShow"));
            uiDashboard.setShowSelectPopup(isShow);
            String windowId = uiDashboard.getChild(UIDashboardContainer.class).getWindowId();
            event.getRequestContext().addUIComponentToUpdateByAjax(uiDashboard.getChild(UIPopupWindow.class));
            if (isShow) {
                event.getRequestContext().getJavascriptManager().require("SHARED/dashboard", "dashboard")
                        .addScripts("dashboard.UIDashboard.onLoad('" + GTN_PREFIX + windowId + "'," + uiDashboard.canEdit() + ");");
            }
        }
    }

    public static class MinimizeGadgetActionListener extends EventListener<UIDashboard> {
        public final void execute(final Event<UIDashboard> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIDashboard uiDashboard = event.getSource();
            String objectId = context.getRequestParameter(OBJECTID);
            String minimized = context.getRequestParameter("minimized");

            UIDashboardContainer uiDashboardCont = uiDashboard.getChild(UIDashboardContainer.class);
            UIGadget uiGadget = uiDashboard.getChild(UIDashboardContainer.class).getUIGadget(objectId);
            if (uiGadget.isLossData()) {
                UIPortalApplication uiApp = Util.getUIPortalApplication();
                uiApp.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
                context.setAttribute(APP_NOT_EXIST, true);
                context.addUIComponentToUpdateByAjax(uiDashboard);
            } else {
                uiGadget.getProperties().setProperty("minimized", minimized);
                uiDashboardCont.save();
                if (context.getAttribute(UIDashboardContainer.SAVE_FAIL) != null) {
                    return;
                }
                Util.getPortalRequestContext().setResponseComplete(true);
            }
        }
    }

    public static class MaximizeGadgetActionListener extends EventListener<UIDashboard> {
        public final void execute(final Event<UIDashboard> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIDashboard uiDashboard = event.getSource();
            String objectId = context.getRequestParameter(OBJECTID);
            String maximize = context.getRequestParameter("maximize");
            UIDashboardContainer uiDashboardCont = uiDashboard.getChild(UIDashboardContainer.class);
            UIGadget uiGadget = uiDashboardCont.getUIGadget(objectId);
            if (uiGadget == null || uiGadget.isLossData()) {
                UIPortalApplication uiApp = Util.getUIPortalApplication();
                uiApp.addMessage(new ApplicationMessage("UIDashboard.msg.ApplicationNotExisted", null));
                context.setAttribute(APP_NOT_EXIST, true);
                context.addUIComponentToUpdateByAjax(uiDashboard);
                return;
            }

            // TODO nguyenanhkien2a@gmail.comá
            // We need to expand unminimized state of uiGadget to view all body of
            // gadget, not just a title with no content
            uiGadget.getProperties().setProperty("minimized", "false");
            uiDashboardCont.save();

            if (maximize.equals("maximize") && context.getAttribute(UIDashboardContainer.SAVE_FAIL) == null) {
                uiGadget.setView(UIGadget.CANVAS_VIEW);
                uiDashboard.setMaximizedGadget(uiGadget);
            } else {
                uiGadget.setView(UIGadget.HOME_VIEW);
                uiDashboard.setMaximizedGadget(null);
            }
        }
    }
}
