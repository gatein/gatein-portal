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

package org.exoplatform.portal.webui.page;

import java.util.List;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputContainer;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormPopupWindow;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NotHTMLTagValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

/**
 * Author : Dang Van Minh minhdv81@yahoo.com Jun 14, 2006
 */
@ComponentConfigs({
        @ComponentConfig(template = "system:/groovy/portal/webui/page/UIPageSelector.gtmpl", events = @EventConfig(listeners = UIPageSelector.OpenSelectPagePopUp.class)),
        @ComponentConfig(id = "SelectPage", type = UIPageBrowser.class, template = "system:/groovy/portal/webui/page/UIPageBrowser.gtmpl", events = @EventConfig(listeners = UIPageSelector.SelectPageActionListener.class)),
        @ComponentConfig(type = UIFormPopupWindow.class, template = "system:/groovy/webui/core/UIPopupWindow.gtmpl", events = @EventConfig(listeners = UIPageSelector.CloseActionListener.class, name = "CloseFormPopup", phase = Event.Phase.DECODE)) })
public class UIPageSelector extends UIFormInputContainer<String> {

    private PageContext page_;

    private static Log logger = ExoLogger.getExoLogger(UIPageSelector.class);

    public UIPageSelector() throws Exception {
        super("UIPageSelector", null);
        UIFormPopupWindow uiPopup = addChild(UIFormPopupWindow.class, null, "PopupPageSelector2");
        uiPopup.setWindowSize(900, 400);
        uiPopup.setShow(false);

        UIFormInputSet uiInputSet = new UIFormInputSet("PageNodeSetting");

        uiInputSet.addChild(new UIFormStringInput("pageId", "pageId", null));
        uiInputSet.addChild(new UIFormStringInput("pageName", "pageName", null).addValidator(UserConfigurableValidator.class, UserConfigurableValidator.PAGE_NAME)
                .addValidator(MandatoryValidator.class));
        uiInputSet.addChild(new UIFormStringInput("pageTitle", "pageTitle", null).addValidator(StringLengthValidator.class, 3,
                120).addValidator(NotHTMLTagValidator.class));

        addChild(uiInputSet);
    }

    private static void configureVirtualList(UIVirtualList vList) {
        UIRepeater repeater;
        try {
            repeater = (UIRepeater) vList.getUIComponent();
            repeater.configure("pageId", UIPageBrowser.BEAN_FIELD, new String[] { "SelectPage" });
        } catch (ClassCastException clCastEx) {
            logger.info("Could not upcast to UIRepeater", clCastEx);
        }
    }

    public void configure(String iname, String bfield) {
        setId(iname);
        setName(iname);
        setBindingField(bfield);
    }

    public UIFormInput<?> setValue(String value) throws Exception {
        UserPortalConfigService service = getApplicationComponent(UserPortalConfigService.class);
        PageContext page = value != null ? service.getPage(PageKey.parse(value)) : null;
        page_ = page;
        super.setValue(value);
        return this;
    }

    public PageContext getPage() {
        return page_;
    }

    public void setPage(PageContext page) {
        page_ = page;
    }

    public Class<String> getTypeValue() {
        return String.class;
    }

    public void processDecode(WebuiRequestContext context) throws Exception {
        super.processDecode(context);
        UIPageBrowser uiPageBrowser = findFirstComponentOfType(UIPageBrowser.class);
        if (uiPageBrowser != null) {
            uiPageBrowser.processDecode(context);
        }

        UIFormInputSet uiInputSet = getChild(UIFormInputSet.class);

        List<UIComponent> children = uiInputSet.getChildren();
        for (UIComponent ele : children) {
            ele.processDecode(context);
        }
        // UIFormStringInput uiPageId = getChildById("pageId");
        // uiPageId.processDecode(context);
        //
        // UIFormStringInput uiPageName = getChildById("pageName");
        // uiPageName.processDecode(context);
        //
        // UIFormStringInput uiPageTitle = getChildById("pageTitle");
        // uiPageTitle.processDecode(context);
    }

    public static class OpenSelectPagePopUp extends EventListener<UIPageSelector> {
        @Override
        public void execute(Event<UIPageSelector> event) throws Exception {
            UIPageSelector pageSelector = event.getSource();
            UIFormPopupWindow uiPopup = pageSelector.getChild(UIFormPopupWindow.class);

            UIPageBrowser uiPageBrowser = pageSelector.createUIComponent(UIPageBrowser.class, "SelectPage", null);
            uiPopup.setUIComponent(uiPageBrowser);
            uiPopup.setShow(true);

            UIVirtualList uiVirtualList = uiPageBrowser.getChild(UIVirtualList.class);
            configureVirtualList(uiVirtualList);
        }
    }

    public static class CloseActionListener extends UIFormPopupWindow.CloseActionListener {
        @Override
        public void execute(Event<UIPopupWindow> event) throws Exception {
            UIPopupWindow popWindow = event.getSource();
            popWindow.setUIComponent(null);
            super.execute(event);
        }
    }

    public static class SelectPageActionListener extends EventListener<UIPageBrowser> {
        public void execute(Event<UIPageBrowser> event) throws Exception {
            UIPageBrowser uiPageBrowser = event.getSource();
            String id = event.getRequestContext().getRequestParameter(OBJECTID);
            WebuiRequestContext ctx = event.getRequestContext();
            UIApplication uiApp = ctx.getUIApplication();
            UIPageSelector uiPageSelector = uiPageBrowser.getAncestorOfType(UIPageSelector.class);
            UserPortalConfigService service = uiPageBrowser.getApplicationComponent(UserPortalConfigService.class);
            UserACL userACL = uiPageBrowser.getApplicationComponent(UserACL.class);
            if (!userACL.hasPermission(service.getPageService().loadPage(PageKey.parse(id)))) {
                uiApp.addMessage(new ApplicationMessage("UIPageBrowser.msg.NoPermission", new String[] { id }));
            }
            uiPageSelector.setValue(id);
            // uiPageBrowser.feedDataWithQuery(null);

            UIForm uiForm = uiPageSelector.getAncestorOfType(UIForm.class);
            if (uiForm != null) {
                ctx.addUIComponentToUpdateByAjax(uiForm.getParent());
            } else {
                ctx.addUIComponentToUpdateByAjax(uiPageSelector.getParent());
            }
            UIFormPopupWindow uiPopup = uiPageSelector.getChild(UIFormPopupWindow.class);
            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
        }
    }

}
