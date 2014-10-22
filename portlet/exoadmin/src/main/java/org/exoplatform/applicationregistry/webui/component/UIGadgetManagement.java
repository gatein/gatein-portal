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

package org.exoplatform.applicationregistry.webui.component;

import java.util.List;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.applicationregistry.webui.Util;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Jun 24, 2008
 */

@ComponentConfig(template = "app:/groovy/applicationregistry/webui/component/UIGadgetManagement.gtmpl", events = {
        @EventConfig(listeners = UIGadgetManagement.AddRemoteGadgetActionListener.class),
        @EventConfig(listeners = UIGadgetManagement.RemoveGadgetActionListener.class, confirm = "UIGadgetManagement.msg.deleteGadget"),
        @EventConfig(listeners = UIGadgetManagement.AddLocalGadgetActionListener.class),
        @EventConfig(listeners = UIGadgetManagement.SelectGadgetActionListener.class) })
@Serialized
public class UIGadgetManagement extends UIContainer {

    public static final String EXO_GADGET_GROUP = "eXoGadgets";

    private Gadget selectedGadget_;

    public UIGadgetManagement() throws Exception {
        reload();
    }

    public void reload() throws Exception {
        List<Gadget> gadgets = getGadgets();

        if (gadgets == null || gadgets.isEmpty()) {
            selectedGadget_ = null;
            getChildren().clear();
            UIMessageBoard uiMessageBoard = addChild(UIMessageBoard.class, null, null);
            uiMessageBoard.setMessage(new ApplicationMessage("UIGadgetManagement.msg.noGadget", null));
        } else {
            setSelectedGadget(getGadgets().get(0));
        }
    }

    public List<Gadget> getGadgets() {
        GadgetRegistryService service = getApplicationComponent(GadgetRegistryService.class);
        List<Gadget> gadgets = service.getAllGadgets(new Util.GadgetComparator());
        return gadgets;
    }

    public Gadget getGadget(String name) {
        for (Gadget ele : getGadgets()) {
            if (ele.getName().equals(name))
                return ele;
        }
        return null;
    }

    public Gadget getSelectedGadget() {
        return selectedGadget_;
    }

    public void setSelectedGadget(String name) throws Exception {
        setSelectedGadget(getGadget(name));
    }

    public void setSelectedGadget(Gadget gadget) throws Exception {
        selectedGadget_ = gadget;
        getChildren().clear();
        UIGadgetInfo uiGadgetInfo = addChild(UIGadgetInfo.class, null, null);
        uiGadgetInfo.setGadget(selectedGadget_);
        uiGadgetInfo.getChild(UICategorySelector.class).setRendered(false);
    }

    public static class AddRemoteGadgetActionListener extends EventListener<UIGadgetManagement> {

        public void execute(Event<UIGadgetManagement> event) throws Exception {
            UIGadgetManagement uiManagement = event.getSource();
            uiManagement.getChildren().clear();
            uiManagement.addChild(UIAddGadget.class, null, null);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
        }

    }

    public static class RemoveGadgetActionListener extends EventListener<UIGadgetManagement> {

        public void execute(Event<UIGadgetManagement> event) throws Exception {
            UIGadgetManagement uiManagement = event.getSource();
            WebuiRequestContext ctx = event.getRequestContext();
            String name = ctx.getRequestParameter(OBJECTID);
            GadgetRegistryService service = uiManagement.getApplicationComponent(GadgetRegistryService.class);
            if (service.getGadget(name) == null) {
                uiManagement.reload();
                ctx.addUIComponentToUpdateByAjax(uiManagement);
                return;
            }
            UIGadgetEditor uiEditor = uiManagement.getChild(UIGadgetEditor.class);
            if (uiEditor != null) {
                Source source = uiEditor.getSource();
                if (source != null && name.equals(uiEditor.getSourceName())) {
                    UIApplication uiApp = ctx.getUIApplication();
                    uiApp.addMessage(new ApplicationMessage("UIGadgetManagement.msg.deleteGadgetInUse", null));
                    return;
                }
            }
            Gadget gadget = uiManagement.getGadget(name);
            service.removeGadget(name);
            WebAppController webController = uiManagement.getApplicationComponent(WebAppController.class);
            webController.removeApplication(EXO_GADGET_GROUP + "/" + name);
            if (gadget.isLocal()) {
                // get dir path of gadget
                String gadgetUrl = gadget.getUrl();
                String[] gaggetUrlPart = gadgetUrl.split("/");
                String dirPath = gaggetUrlPart[gaggetUrlPart.length - 2];
                SourceStorage sourceStorage = uiManagement.getApplicationComponent(SourceStorage.class);
                sourceStorage.removeSource(dirPath + "/" + name + ".xml");
            }
            uiManagement.reload();

            // update to ApplicationOrganizer
            removeFromApplicationRegistry(name);
            UIApplicationOrganizer uiOrganizer = uiManagement.getParent()
                    .findFirstComponentOfType(UIApplicationOrganizer.class);
            String selectedCateName = uiOrganizer.getSelectedCategory().getName();
            uiOrganizer.reload();
            uiOrganizer.setSelectedCategory(selectedCateName);

            ctx.addUIComponentToUpdateByAjax(uiManagement);
        }

        private void removeFromApplicationRegistry(String name) throws Exception {
            ApplicationRegistryService appRegService = org.exoplatform.portal.webui.util.Util.getUIPortalApplication()
                    .getApplicationComponent(ApplicationRegistryService.class);
            List<ApplicationCategory> cates = appRegService.getApplicationCategories();
            for (ApplicationCategory cate : cates) {
                Application app = appRegService.getApplication(cate.getName(), name);
                if (app != null)
                    appRegService.remove(app);
            }
        }
    }

    public static class AddLocalGadgetActionListener extends EventListener<UIGadgetManagement> {

        public void execute(Event<UIGadgetManagement> event) throws Exception {
            UIGadgetManagement uiManagement = event.getSource();
            uiManagement.getChildren().clear();
            uiManagement.addChild(UIGadgetEditor.class, null, null);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
        }

    }

    public static class SelectGadgetActionListener extends EventListener<UIGadgetManagement> {

        public void execute(Event<UIGadgetManagement> event) throws Exception {
            UIGadgetManagement uiManagement = event.getSource();
            String name = event.getRequestContext().getRequestParameter(OBJECTID);
            Gadget gadget = uiManagement.getGadget(name);
            if (gadget == null) {
                UIApplication uiApp = event.getRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIGadgetManagement.msg.gadgetNotExist", null, ApplicationMessage.WARNING));
                return;
            }
            uiManagement.setSelectedGadget(gadget);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
        }

    }
}
