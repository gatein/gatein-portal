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

package org.exoplatform.portal.webui.application;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletMode;

import org.exoplatform.commons.utils.ExceptionUtil;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NotHTMLTagValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.organization.UIListPermissionSelector;
import org.exoplatform.webui.organization.UIListPermissionSelector.EmptyIteratorValidator;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.info.PreferenceInfo;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.PropertyChange;

/** Author : Nhu Dinh Thuan nhudinhthuan@yahoo.com Jun 8, 2006 */
@ComponentConfigs({
        @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/portal/UIPortletForm.gtmpl", events = {
                @EventConfig(listeners = UIPortletForm.SaveActionListener.class),
                @EventConfig(listeners = UIPortletForm.CloseActionListener.class, phase = Phase.DECODE) }),
        @ComponentConfig(id = "PortletPermission", type = UIFormInputSet.class, lifecycle = UIContainerLifecycle.class) })
public class UIPortletForm extends UIFormTabPane {
    private static Log log = ExoLogger.getLogger("portal:UIPortletForm");

    private UIPortlet uiPortlet_;

    private UIComponent backComponent_;

    private static final String FIELD_THEME = "Theme";

    private static final String FIELD_PORTLET_PREF = "PortletPref";

    @SuppressWarnings("unchecked")
    public UIPortletForm() throws Exception {
        super("UIPortletForm");
        UIFormInputSet uiPortletPrefSet = new UIFormInputSet(FIELD_PORTLET_PREF).setRendered(false);
        addUIFormInput(uiPortletPrefSet);
        UIFormInputSet uiSettingSet = new UIFormInputSet("PortletSetting");
        uiSettingSet
                .addUIFormInput(new UIFormInputInfo("displayName", "displayName", null))
                .addUIFormInput(
                        new UIFormStringInput("title", "title", null).addValidator(StringLengthValidator.class, 3, 60)
                                .addValidator(NotHTMLTagValidator.class, "UIPortletForm.msg.InvalidPortletTitle"))
                .addUIFormInput(
                        new UIFormStringInput("width", "width", null).addValidator(ExpressionValidator.class,
                                "(^([1-9]\\d*)(?:px)?$)?", "UIPortletForm.msg.InvalidWidthHeight"))
                .addUIFormInput(
                        new UIFormStringInput("height", "height", null).addValidator(ExpressionValidator.class,
                                "(^([1-9]\\d*)(?:px)?$)?", "UIPortletForm.msg.InvalidWidthHeight"))
                .addUIFormInput(new UICheckBoxInput("showInfoBar", "showInfoBar", false))
                .addUIFormInput(new UICheckBoxInput("showPortletMode", "showPortletMode", false))
                .addUIFormInput(new UICheckBoxInput("showWindowState", "showWindowState", false))
                .addUIFormInput(
                        new UIFormTextAreaInput("description", "description", null).addValidator(NotHTMLTagValidator.class,
                                "UIPortletForm.msg.InvalidPortletDescription"));
        addUIFormInput(uiSettingSet);
        UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "icon");
        addUIFormInput(uiIconSelector);

        UIFormInputThemeSelector uiThemeSelector = new UIFormInputThemeSelector(FIELD_THEME, null);
        SkinService skinService = getApplicationComponent(SkinService.class);
        uiThemeSelector.getChild(UIItemThemeSelector.class).setValues(skinService.getPortletThemes());
        addUIFormInput(uiThemeSelector);

        PortalRequestContext prc = Util.getPortalRequestContext();
        if (prc.getSiteType() != SiteType.USER) {
            UIListPermissionSelector uiListPermissionSelector = createUIComponent(UIListPermissionSelector.class, null, null);
            uiListPermissionSelector.configure(WebuiRequestContext.generateUUID("UIListPermissionSelector"), "accessPermissions");
            uiListPermissionSelector.addValidator(EmptyIteratorValidator.class);
            UIFormInputSet uiPermissionSet = createUIComponent(UIFormInputSet.class, "PortletPermission", null);
            uiPermissionSet.addChild(uiListPermissionSelector);
            addUIFormInput(uiPermissionSet);
        }
    }

    public UIComponent getBackComponent() {
        return backComponent_;
    }

    public void setBackComponent(final UIComponent uiComp) {
        backComponent_ = uiComp;
    }

    public UIPortlet getUIPortlet() {
        return uiPortlet_;
    }

    public boolean hasEditMode() {
        return uiPortlet_.getSupportModes().contains("edit");
    }

    public String getEditModeContent() {
        try {
            PortalRequestContext prcontext = (PortalRequestContext) WebuiRequestContext.getCurrentInstance();
            prcontext.ignoreAJAXUpdateOnPortlets(true);

            PortletInvocation portletInvocation = uiPortlet_.create(RenderInvocation.class, prcontext);
            RenderInvocation renderInvocation = (RenderInvocation) portletInvocation;
            // make sure we are in the EDIT mode, and not whatever the current portlet mode is for the Portlet
            renderInvocation.setMode(Mode.create(PortletMode.EDIT.toString()));

            PortletInvocationResponse portletResponse = uiPortlet_.invoke(renderInvocation);
            StringWriter writer = new StringWriter();
            uiPortlet_.generateRenderMarkup(portletResponse, prcontext).writeTo(writer);

            return writer.toString();
        } catch (Throwable ex) {
            WebuiRequestContext webuiRequest = WebuiRequestContext.getCurrentInstance();
            log.error("The portlet " + uiPortlet_.getName() + " could not be loaded. Check if properly deployed.",
                    ExceptionUtil.getRootCause(ex));
            return webuiRequest.getApplicationResourceBundle().getString("UIPortlet.message.RuntimeError");
        }
    }

    public boolean setValues(final UIPortlet uiPortlet) throws Exception {
        uiPortlet_ = uiPortlet;
        org.gatein.pc.api.Portlet portlet = uiPortlet.getProducedOfferedPortlet();
        if (portlet == null) {
            return false;
        }
        invokeGetBindingBean(uiPortlet_);
        String icon = uiPortlet.getIcon();

        if (icon == null || icon.length() < 0) {
            icon = "PortletIcon";
        }
        getChild(UIFormInputIconSelector.class).setSelectedIcon(icon);
        getChild(UIFormInputThemeSelector.class).getChild(UIItemThemeSelector.class).setSelectedTheme(
                uiPortlet.getSuitedTheme(null));
        if (hasEditMode()) {
            uiPortlet.setCurrentPortletMode(PortletMode.EDIT);
        } else {
            Map<String, String> portletPreferenceMaps = new HashMap<String, String>();
            Set<String> keySet = portlet.getInfo().getPreferences().getKeys();

            for (String key : keySet) {
                PreferenceInfo preferenceInfo = portlet.getInfo().getPreferences().getPreference(key);
                if (!preferenceInfo.isReadOnly()) {
                    String ppValue = (preferenceInfo.getDefaultValue().size() > 0) ? preferenceInfo.getDefaultValue().get(0)
                            : "";
                    portletPreferenceMaps.put(key, ppValue);
                }
            }

            Portlet pp = uiPortlet.getPreferences();
            if (pp != null) {
                for (Preference pref : pp) {
                    if (!pref.isReadOnly()) {
                        portletPreferenceMaps.put(pref.getName(), (pref.getValues().size() > 0) ? pref.getValues().get(0) : "");
                    }
                }
            }

            if (portletPreferenceMaps.size() > 0) {
                Set<String> ppKeySet = portletPreferenceMaps.keySet();
                UIFormInputSet uiPortletPrefSet = getChildById(FIELD_PORTLET_PREF);
                uiPortletPrefSet.getChildren().clear();
                for (String ppKey : ppKeySet) {
                    String ppValue = portletPreferenceMaps.get(ppKey);
                    UIFormStringInput preferenceStringInput = new UIFormStringInput(ppKey, null, ppValue);
                    preferenceStringInput.setLabel(ppKey);
                    preferenceStringInput.addValidator(MandatoryValidator.class);
                    uiPortletPrefSet.addUIFormInput(preferenceStringInput);
                }

                uiPortletPrefSet.setRendered(true);
                setSelectedTab(FIELD_PORTLET_PREF);
            } else {
                setSelectedTab("PortletSetting");
            }
        }
        return true;
    }

    private void savePreferences() throws Exception {
        UIFormInputSet uiPortletPrefSet = getChildById(FIELD_PORTLET_PREF);
        List<UIFormStringInput> uiFormInputs = new ArrayList<UIFormStringInput>(3);
        uiPortletPrefSet.findComponentOfType(uiFormInputs, UIFormStringInput.class);
        if (uiFormInputs.size() < 1) {
            return;
        }

        PropertyChange[] propertyChanges = new PropertyChange[uiFormInputs.size()];

        for (int i = 0; i < uiFormInputs.size(); i++) {
            String name = uiFormInputs.get(i).getName();
            String value = uiFormInputs.get(i).getValue();
            propertyChanges[i] = PropertyChange.newUpdate(name, value);
        }

        // Now save it
        uiPortlet_.update(propertyChanges);
    }

    public static class SaveActionListener extends EventListener<UIPortletForm> {
        public void execute(final Event<UIPortletForm> event) throws Exception {
            UIPortletForm uiPortletForm = event.getSource();
            UIPortlet uiPortlet = uiPortletForm.getUIPortlet();
            UIFormInputIconSelector uiIconSelector = uiPortletForm.getChild(UIFormInputIconSelector.class);
            uiPortletForm.invokeSetBindingBean(uiPortlet);
            if (uiIconSelector.getSelectedIcon().equals("Default")) {
                uiPortlet.setIcon("PortletIcon");
            } else {
                uiPortlet.setIcon(uiIconSelector.getSelectedIcon());
            }
            UIFormInputThemeSelector uiThemeSelector = uiPortletForm.getChild(UIFormInputThemeSelector.class);
            uiPortlet.putSuitedTheme(null, uiThemeSelector.getChild(UIItemThemeSelector.class).getSelectedTheme());
            uiPortletForm.savePreferences();
            UIMaskWorkspace uiMaskWorkspace = uiPortletForm.getParent();
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            uiMaskWorkspace.createEvent("Close", Phase.DECODE, pcontext).broadcast();
            if (uiPortletForm.hasEditMode()) {
                uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
            }

            String width = uiPortletForm.getUIStringInput("width").getValue();
            if (width == null || width.length() == 0) {
                uiPortlet.setWidth(null);
            } else {
                if (!width.endsWith("px")) {
                    width = width.concat("px");
                }
                uiPortlet.setWidth(width);
            }

            String height = uiPortletForm.getUIStringInput("height").getValue();
            if (height == null || height.length() == 0) {
                uiPortlet.setHeight(null);
            } else {
                if (!height.endsWith("px")) {
                    height = height.concat("px");
                }
                uiPortlet.setHeight(height);
            }

            pcontext.getJavascriptManager().require("SHARED/portalComposer", "portalComposer")
                    .addScripts("portalComposer.toggleSaveButton();");
            UIPortalApplication uiPortalApp = uiPortlet.getAncestorOfType(UIPortalApplication.class);
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            pcontext.addUIComponentToUpdateByAjax(uiWorkingWS);
            pcontext.ignoreAJAXUpdateOnPortlets(true);
        }
    }

    public static class CloseActionListener extends EventListener<UIPortletForm> {
        public void execute(final Event<UIPortletForm> event) throws Exception {
            UIPortletForm uiPortletForm = event.getSource();
            UIPortlet uiPortlet = uiPortletForm.getUIPortlet();
            if (uiPortletForm.hasEditMode()) {
                uiPortlet.setCurrentPortletMode(PortletMode.VIEW);
            }
            UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            // add by Pham Dinh Tan
            UIMaskWorkspace uiMaskWorkspace = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            uiMaskWorkspace.broadcast(event, Phase.DECODE);
            pcontext.ignoreAJAXUpdateOnPortlets(true);
        }
    }

}
