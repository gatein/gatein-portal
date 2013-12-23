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

package org.exoplatform.portal.webui.portal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalProperties;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputItemSelector;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.organization.UIListPermissionSelector;
import org.exoplatform.webui.organization.UIListPermissionSelector.EmptyIteratorValidator;
import org.exoplatform.webui.organization.UIPermissionSelector;

@ComponentConfigs({
        @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
                @EventConfig(listeners = UIPortalForm.SaveActionListener.class),
                @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIPortalForm.CheckShowActionListener.class) }),
        @ComponentConfig(id = "CreatePortal", lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
                @EventConfig(name = "Save", listeners = UIPortalForm.CreateActionListener.class),
                @EventConfig(listeners = UIPortalForm.SelectItemOptionActionListener.class, phase = Phase.DECODE),
                @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE) }),
        @ComponentConfig(type = UIFormInputSet.class, id = "PermissionSetting", template = "system:/groovy/webui/core/UITabSelector.gtmpl", events = { @EventConfig(listeners = UIFormInputSet.SelectComponentActionListener.class) }) })
public class UIPortalForm extends UIFormTabPane {

    private static final String FIELD_NAME = "name";

    private static final String FIELD_SKIN = "skin";

    private static final String FIELD_LOCALE = "locale";

    private static final String FIELD_SESSION_ALIVE = "sessionAlive";

    private static final String FIELD_SHOW_INFOBAR = "showInfobar";

    private static final String FIELD_LABEL = "label";

    private static final String FIELD_DESCRIPTION = "description";

    private String portalOwner_;

    private List<SelectItemOption<String>> languages = new ArrayList<SelectItemOption<String>>();

    public void initPortalTemplateTab() throws Exception {
        UIFormInputItemSelector uiTemplateInput = new UIFormInputItemSelector("PortalTemplate", null);
        addUIFormInput(uiTemplateInput);
        setSelectedTab(uiTemplateInput.getId());

        UIFormInputSet uiPortalSetting = this.<UIFormInputSet> getChildById("PortalSetting");
        UIFormStringInput uiNameInput = uiPortalSetting.getUIStringInput(FIELD_NAME);
        uiNameInput.setReadOnly(false);

        setSelectedTab(uiPortalSetting.getId());

        setActions(new String[] { "Save", "Close" });

        UserPortalConfigService configService = this.getApplicationComponent(UserPortalConfigService.class);
        Set<String> portalTemplates = configService.getPortalTemplates();
        for (String tempName : portalTemplates) {
            SelectItemCategory category = new SelectItemCategory(tempName);
            PortalConfig config = configService.getPortalConfigFromTemplate(tempName);
            if (config != null) {
                SelectItemOption<String> option = new SelectItemOption<String>(config.getLabel(), tempName,
                        config.getDescription(), tempName);
                category.addSelectItemOption(option);
                uiTemplateInput.getItemCategories().add(category);
            }
        }
        if (uiTemplateInput.getSelectedItemOption() == null) {
            uiTemplateInput.getItemCategories().get(0).setSelected(true);
        }
    }

    public UIPortalForm() throws Exception {
        super("UIPortalForm");
        createDefaultItem();
        setSelectedTab("PortalSetting");
    }

    public void setBindingBean() throws Exception {

        DataStorage dataStorage = this.getApplicationComponent(DataStorage.class);

        UIPortal editPortal = null;
        UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
        UIEditInlineWorkspace uiEditWS = uiPortalApp.<UIWorkingWorkspace> getChildById(UIPortalApplication.UI_WORKING_WS_ID)
                .getChild(UIEditInlineWorkspace.class);
        if (uiPortalApp.getModeState() != UIPortalApplication.NORMAL_MODE && uiEditWS != null
                && uiEditWS.getUIComponent() != null && (uiEditWS.getUIComponent() instanceof UIPortal)) {
            editPortal = (UIPortal) uiEditWS.getUIComponent();
        } else {
            PortalConfig pConfig = dataStorage.getPortalConfig(getPortalOwner());
            editPortal = this.createUIComponent(UIPortal.class, null, null);
            PortalDataMapper.toUIPortal(editPortal, pConfig);
        }

        invokeGetBindingBean(editPortal);
        ((UIFormStringInput) getChild(UIFormInputSet.class).getChildById(FIELD_NAME)).setValue(getPortalOwner());

        LocaleConfigService localeConfigService = getApplicationComponent(LocaleConfigService.class);
        LocaleConfig localeConfig = localeConfigService.getLocaleConfig(editPortal.getLocale());
        String lang = localeConfig.getLanguage();
        if (localeConfig.getLocale().getCountry() != null && localeConfig.getLocale().getCountry().length() > 0) {
            lang += "_" + localeConfig.getLocale().getCountry();
        }

        this.<UIFormInputSet> getChildById("PortalSetting").<UIFormSelectBox> getChildById(FIELD_LOCALE).setValue(lang);
        setActions(new String[] { "Save", "Close" });
    }

    private class LanguagesComparator implements Comparator<SelectItemOption> {
        public int compare(SelectItemOption o1, SelectItemOption o2) {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    }

    private void createDefaultItem() throws Exception {
        LocaleConfigService localeConfigService = getApplicationComponent(LocaleConfigService.class);
        Collection<?> listLocaleConfig = localeConfigService.getLocalConfigs();
        LocaleConfig defaultLocale = localeConfigService.getDefaultLocaleConfig();
        String defaultLanguage = defaultLocale.getLanguage();
        Locale currentLocale = Util.getPortalRequestContext().getLocale();
        Iterator<?> iterator = listLocaleConfig.iterator();
        while (iterator.hasNext()) {
            LocaleConfig localeConfig = (LocaleConfig) iterator.next();
            ResourceBundle localeResourceBundle = getResourceBundle(currentLocale);
            Locale local = localeConfig.getLocale();
            String lang = local.getLanguage();
            if (local.getCountry() != null && local.getCountry().length() > 0) {
                lang += "_" + local.getCountry();
            }

            String displayName = null;
            try {
                String key = "Locale." + lang;
                String translation = localeResourceBundle.getString(key);
                displayName = translation;
            } catch (MissingResourceException e) {
                displayName = capitalizeFirstLetter(local.getDisplayName(currentLocale));
            }

            SelectItemOption<String> option = new SelectItemOption<String>(displayName, lang);
            if (defaultLanguage.equals(lang)) {
                option.setSelected(true);
            }
            languages.add(option);
        }
        Collections.sort(languages, new LanguagesComparator());

        UIFormInputSet uiSettingSet = new UIFormInputSet("PortalSetting");
        UIFormInputSet uiPropertiesSet = new UIFormInputSet("Properties");
        uiSettingSet.addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(MandatoryValidator.class)
                .addValidator(StringLengthValidator.class, 3, 30).addValidator(IdentifierValidator.class).setReadOnly(true));

        uiSettingSet.addUIFormInput(new UIFormStringInput(FIELD_LABEL, FIELD_LABEL, null)
                .addValidator(SpecialCharacterValidator.class));
        uiSettingSet.addUIFormInput(new UIFormStringInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null));
        uiSettingSet.addUIFormInput(new UIFormSelectBox(FIELD_LOCALE, FIELD_LOCALE, languages)
                .addValidator(MandatoryValidator.class));

        List<SelectItemOption<String>> listSkin = new ArrayList<SelectItemOption<String>>();
        SkinService skinService = getApplicationComponent(SkinService.class);
        for (String skin : skinService.getAvailableSkinNames()) {
            SelectItemOption<String> skinOption = new SelectItemOption<String>(skin, skin);
            listSkin.add(skinOption);
        }
        listSkin.get(0).setSelected(true);

        UIFormSelectBox uiSelectBox = new UIFormSelectBox(FIELD_SKIN, FIELD_SKIN, listSkin);
        uiSettingSet.addUIFormInput(uiSelectBox);

        addUIFormInput(uiSettingSet);

        // add to properties tab
        List<SelectItemOption<String>> listSessionAlive = new ArrayList<SelectItemOption<String>>();
        listSessionAlive.add(new SelectItemOption<String>(PortalProperties.SESSION_ALWAYS, PortalProperties.SESSION_ALWAYS));
        listSessionAlive.add(new SelectItemOption<String>(PortalProperties.SESSION_ON_DEMAND,
                PortalProperties.SESSION_ON_DEMAND));
        listSessionAlive.add(new SelectItemOption<String>(PortalProperties.SESSION_NEVER, PortalProperties.SESSION_NEVER));
        UIFormSelectBox uiSessionAliveBox = new UIFormSelectBox(FIELD_SESSION_ALIVE, FIELD_SESSION_ALIVE, listSessionAlive);
        uiSessionAliveBox.setValue(PortalProperties.SESSION_ON_DEMAND);
        uiPropertiesSet.addUIFormInput(uiSessionAliveBox);

        // TODO add more box for showPortletMode and showWindowState if needed
        UIFormCheckBoxInput<Boolean> uiShowInfobarBox = new UIFormCheckBoxInput<Boolean>(FIELD_SHOW_INFOBAR,
                FIELD_SHOW_INFOBAR, true);
        uiShowInfobarBox.setOnChange("CheckShowInfobar");
        uiPropertiesSet.addChild(uiShowInfobarBox);
        addUIFormInput(uiPropertiesSet);

        UIFormInputSet uiPermissionSetting = createUIComponent(UIFormInputSet.class, "PermissionSetting", null);
        addUIComponentInput(uiPermissionSetting);

        UIListPermissionSelector uiListPermissionSelector = createUIComponent(UIListPermissionSelector.class, null, null);
        uiListPermissionSelector.configure(WebuiRequestContext.generateUUID("UIListPermissionSelector"), "accessPermissions");
        uiListPermissionSelector.addValidator(EmptyIteratorValidator.class);
        uiPermissionSetting.addChild(uiListPermissionSelector);
        uiPermissionSetting.setSelectedComponent(uiListPermissionSelector.getId());

        UIPermissionSelector uiEditPermission = createUIComponent(UIPermissionSelector.class, null, null);
        uiEditPermission.setRendered(false);
        uiEditPermission.addValidator(org.exoplatform.webui.organization.UIPermissionSelector.MandatoryValidator.class);
        uiEditPermission.configure("UIPermissionSelector", "editPermission");
        uiPermissionSetting.addChild(uiEditPermission);
    }

    public void setPortalOwner(String portalOwner) {
        this.portalOwner_ = portalOwner;
    }

    public String getPortalOwner() {
        return portalOwner_;
    }

    public static class SaveActionListener extends EventListener<UIPortalForm> {
        public void execute(Event<UIPortalForm> event) throws Exception {
            UIPortalForm uiForm = event.getSource();

            DataStorage dataService = uiForm.getApplicationComponent(DataStorage.class);
            UserACL acl = uiForm.getApplicationComponent(UserACL.class);
            PortalRequestContext prContext = Util.getPortalRequestContext();
            UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();

            PortalConfig pConfig = dataService.getPortalConfig(uiForm.getPortalOwner());
            if (pConfig != null && acl.hasPermission(pConfig)) {
                UIPortal uiPortal = uiForm.createUIComponent(UIPortal.class, null, null);
                PortalDataMapper.toUIPortal(uiPortal, pConfig);

                uiForm.invokeSetBindingBean(uiPortal);
                // uiPortal.refreshNavigation(localeConfigService.getLocaleConfig(uiPortal.getLocale()).getLocale()) ;
                if (uiPortalApp.getModeState() == UIPortalApplication.NORMAL_MODE) {
                    PortalConfig portalConfig = (PortalConfig) PortalDataMapper.buildModelObject(uiPortal);
                    dataService.save(portalConfig);
                    UserPortalConfigService service = uiForm.getApplicationComponent(UserPortalConfigService.class);
                    if (prContext.getPortalOwner().equals(uiForm.getPortalOwner())) {
                        prContext.setUserPortalConfig(service.getUserPortalConfig(uiForm.getPortalOwner(),
                                prContext.getRemoteUser(), PortalRequestContext.USER_PORTAL_CONTEXT));
                        uiPortalApp.reloadPortalProperties();
                    }

                    // We should use IPC to update some portlets in the future instead of
                    UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);

                    // TODO: Raise Portlet Event instead
                    uiWorkingWS.updatePortletsByName("PortalNavigationPortlet");
                    uiWorkingWS.updatePortletsByName("UserToolbarSitePortlet");
                } else {
                    UIWorkingWorkspace uiWorkingWS = uiPortalApp.findFirstComponentOfType(UIWorkingWorkspace.class);
                    UIEditInlineWorkspace uiEditWS = uiWorkingWS.getChild(UIEditInlineWorkspace.class);
                    UIPortal editPortal = (UIPortal) uiEditWS.getUIComponent();
                    uiForm.invokeSetBindingBean(editPortal);
                }
            } else {
                UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null));

                UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
                prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
                prContext.setFullRender(true);
            }

            UIMaskWorkspace uiMaskWorkspace = uiForm.getParent();
            WebuiRequestContext rContext = event.getRequestContext();
            uiMaskWorkspace.createEvent("Close", Phase.DECODE, rContext).broadcast();
            if (!uiForm.getId().equals("CreatePortal") && uiPortalApp.getModeState() != UIPortalApplication.NORMAL_MODE) {
                rContext.getJavascriptManager().require("SHARED/portalComposer", "portalComposer")
                        .addScripts("portalComposer.toggleSaveButton();");
            }
        }
    }

    public static class CreateActionListener extends EventListener<UIPortalForm> {
        public synchronized void execute(Event<UIPortalForm> event) throws Exception {
            UIPortalForm uiForm = event.getSource();
            PortalRequestContext pcontext = (PortalRequestContext) event.getRequestContext();
            String template = uiForm.getChild(UIFormInputItemSelector.class).getSelectedItemOption().getValue().toString();
            String portalName = uiForm.getUIStringInput(FIELD_NAME).getValue();
            DataStorage dataService = uiForm.getApplicationComponent(DataStorage.class);
            PortalConfig config = dataService.getPortalConfig(portalName);
            if (config != null) {
                UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
                uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.sameName", null));
                return;
            }

            UserPortalConfigService service = uiForm.getApplicationComponent(UserPortalConfigService.class);
            service.createUserPortalConfig(SiteType.PORTAL.getName(), portalName, template);

            PortalConfig pconfig = dataService.getPortalConfig(portalName);
            uiForm.invokeSetBindingBean(pconfig);
            dataService.save(pconfig);
            UIPortalApplication uiPortalApp = event.getSource().getAncestorOfType(UIPortalApplication.class);
            UIMaskWorkspace uiMaskWS = uiPortalApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            uiMaskWS.createEvent("Close", Phase.DECODE, pcontext).broadcast();

            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);
            uiWorkingWS.updatePortletsByName("PortalNavigationPortlet");
            uiWorkingWS.updatePortletsByName("UserToolbarSitePortlet");
        }
    }

    public static class SelectItemOptionActionListener extends EventListener<UIPortalForm> {
        public void execute(Event<UIPortalForm> event) throws Exception {
            UIPortalForm uiForm = event.getSource();
            UIFormInputItemSelector templateInput = uiForm.getChild(UIFormInputItemSelector.class);
            uiForm.setSelectedTab(templateInput.getId());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }

    public static class CheckShowActionListener extends EventListener<UIPortalForm> {
        public void execute(Event<UIPortalForm> event) throws Exception {
            UIPortalForm uiForm = event.getSource();
            UIFormInputSet InfoForm = uiForm.getChildById("Properties");
            UIFormCheckBoxInput<Boolean> showInfobarForm = InfoForm.getUIFormCheckBoxInput(UIPortalForm.FIELD_SHOW_INFOBAR);

            // TODO: When we need to implement for showPortletMode or showWindowState
            // we can change how to get/set value for showInfobarForm (as well as of PortalConfig)
            showInfobarForm.setValue(showInfobarForm.isChecked());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }

    private String capitalizeFirstLetter(String word) {
        if (word == null) {
            return null;
        }
        if (word.length() == 0) {
            return word;
        }
        StringBuilder result = new StringBuilder(word);
        result.replace(0, 1, result.substring(0, 1).toUpperCase());
        return result.toString();
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        ExoContainer appContainer = ExoContainerContext.getCurrentContainer();
        ResourceBundleService service = (ResourceBundleService) appContainer
                .getComponentInstanceOfType(ResourceBundleService.class);
        ResourceBundle res = service.getResourceBundle("locale.portal.webui", locale);
        return res;
    }
}
