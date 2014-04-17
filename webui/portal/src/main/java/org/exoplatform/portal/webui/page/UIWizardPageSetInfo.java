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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.mop.navigation.NavigationServiceException;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.navigation.UIPageNodeSelector;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

/**
 * Created by The eXo Platform SARL Author : Nguyen Thi Hoa hoa.nguyen@exoplatform.com Oct 31, 2006
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/page/UIWizardPageSetInfo.gtmpl", events = {
        @EventConfig(listeners = UIWizardPageSetInfo.ChangeNodeActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIWizardPageSetInfo.SwitchVisibleActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIWizardPageSetInfo.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIWizardPageSetInfo.ChangeLanguageActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIWizardPageSetInfo.SwitchLabelModeActionListener.class, phase = Phase.DECODE) })
public class UIWizardPageSetInfo extends UIForm {

    public static final String PAGE_NAME = "pageName";

    public static final String PAGE_DISPLAY_NAME = "pageDisplayName";

    public static final String VISIBLE = "visible";

    public static final String SHOW_PUBLICATION_DATE = "showPublicationDate";

    public static final String START_PUBLICATION_DATE = "startPublicationDate";

    public static final String END_PUBLICATION_DATE = "endPublicationDate";

    public static final String I18N_LABEL = "i18nizedLabel";

    private static final String LANGUAGES = "languages";

    private static final String LANGUAGES_ONCHANGE = "ChangeLanguage";

    private static final String SWITCH_MODE = "switchmode";

    private static final String SWITCH_MODE_ONCHANGE = "SwitchLabelMode";

    private boolean isEditMode = false;

    private boolean firstTime = true;

    private String selectedLocale;

    private Map<String, String> cachedLabels;

    public UIWizardPageSetInfo() throws Exception {
        UICheckBoxInput uiDateInputCheck = new UICheckBoxInput(SHOW_PUBLICATION_DATE, null, false);
        UICheckBoxInput uiVisibleCheck = new UICheckBoxInput(VISIBLE, null, false);
        UICheckBoxInput uiSwitchLabelMode = new UICheckBoxInput(SWITCH_MODE, null, true);
        uiDateInputCheck.setOnChange("SwitchPublicationDate");
        uiVisibleCheck.setOnChange("SwitchVisible");
        uiSwitchLabelMode.setOnChange(SWITCH_MODE_ONCHANGE);

        UIFormSelectBox uiFormLanguagesSelectBox = new UIFormSelectBox(LANGUAGES, null, null);
        initLanguageSelectBox(uiFormLanguagesSelectBox);
        uiFormLanguagesSelectBox.setOnChange(LANGUAGES_ONCHANGE);

        addChild(UIPageNodeSelector.class, null, null);
        addUIFormInput(new UIFormStringInput(PAGE_NAME, "name", null).addValidator(MandatoryValidator.class)
                .addValidator(UserConfigurableValidator.class, UserConfigurableValidator.PAGE_NAME));
        addUIFormInput(uiSwitchLabelMode);
        addUIFormInput(new UIFormStringInput(PAGE_DISPLAY_NAME, "label", null).setMaxLength(255).addValidator(
                StringLengthValidator.class, 3, 120));
        addUIFormInput(uiFormLanguagesSelectBox);
        addUIFormInput(new UIFormStringInput(I18N_LABEL, null, null).setMaxLength(255).addValidator(
                StringLengthValidator.class, 3, 120));
        addUIFormInput(uiVisibleCheck.setChecked(true));
        addUIFormInput(uiDateInputCheck);
        UIFormInputBase<String> startPubDateInput = new UIFormDateTimeInput(START_PUBLICATION_DATE, null, null)
                .addValidator(DateTimeValidator.class);
        UIFormInputBase<String> endPubDateInput = new UIFormDateTimeInput(END_PUBLICATION_DATE, null, null)
                .addValidator(DateTimeValidator.class);
        addUIFormInput(startPubDateInput);
        addUIFormInput(endPubDateInput);

        boolean isUserNav = Util.getUIPortal().getSiteType().equals(SiteType.USER);
        if (isUserNav) {
            uiVisibleCheck.setRendered(false);
            uiDateInputCheck.setRendered(false);
            startPubDateInput.setRendered(false);
            endPubDateInput.setRendered(false);
        }

        this.selectedLocale = getUIFormSelectBox(LANGUAGES).getValue();
        cachedLabels = new HashMap<String, String>();
        switchLabelMode(true);
    }

    // TODO: it looks like this method is not used
    public void setEditMode() {
        isEditMode = true;
        UIFormStringInput uiNameInput = getChildById(PAGE_NAME);
        uiNameInput.setReadOnly(true);
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public Map<String, String> getCachedLabels() {
        return cachedLabels;
    }

    public String getSelectedLocale() {
        return selectedLocale;
    }

    public void invokeSetBindingBean(Object bean) throws Exception {
        UIFormStringInput nameTextBox = getUIStringInput(PAGE_NAME);
        // this help to ignore name textbox
        nameTextBox.setReadOnly(true);
        super.invokeSetBindingBean(bean);
        nameTextBox.setReadOnly(false);

        UserNode node = (UserNode) bean;

        if (getUICheckBoxInput(SWITCH_MODE).isChecked()) {
            node.setLabel(null);
        } else if (node.getLabel() == null || node.getLabel().trim().length() == 0) {
            node.setLabel(node.getName());
        }

        Visibility visibility;
        if (getUICheckBoxInput(VISIBLE).isChecked()) {
            UICheckBoxInput showPubDate = getUICheckBoxInput(SHOW_PUBLICATION_DATE);
            visibility = showPubDate.isChecked() ? Visibility.TEMPORAL : Visibility.DISPLAYED;
        } else {
            visibility = Visibility.HIDDEN;
        }
        node.setVisibility(visibility);

        Calendar cal = getUIFormDateTimeInput(START_PUBLICATION_DATE).getCalendar();
        long time = (cal != null) ? cal.getTimeInMillis() : -1;
        node.setStartPublicationTime(time);
        cal = getUIFormDateTimeInput(END_PUBLICATION_DATE).getCalendar();
        time = (cal != null) ? cal.getTimeInMillis() : -1;
        node.setEndPublicationTime(time);
    }

    public UserNode createUserNode(UserNode parent) throws Exception {
        UIFormStringInput nameTextBox = getUIStringInput(PAGE_NAME);
        String nodeName = nameTextBox.getValue();

        UserNode child = parent.addChild(nodeName);
        invokeSetBindingBean(child);
        return child;
    }

    public void setShowCheckPublicationDate(boolean show) {
        getUICheckBoxInput(VISIBLE).setChecked(show);
        UICheckBoxInput uiForm = getUICheckBoxInput(SHOW_PUBLICATION_DATE);
        uiForm.setRendered(show);
        setShowPublicationDate(show && uiForm.isChecked());
    }

    public void setShowPublicationDate(boolean show) {
        getUIFormDateTimeInput(START_PUBLICATION_DATE).setRendered(show);
        getUIFormDateTimeInput(END_PUBLICATION_DATE).setRendered(show);
    }

    public UserNode getSelectedPageNode() {
        UIPageNodeSelector uiPageNodeSelector = getChild(UIPageNodeSelector.class);
        return uiPageNodeSelector.getSelectedNode();
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        if (isEditMode && getChild(UIPageNodeSelector.class).getSelectedNode() == null)
            reset();
        super.processRender(context);
    }

    public boolean isFirstTime() {
        return firstTime;
    }

    public void setFirstTime(boolean firstTime) {
        this.firstTime = firstTime;
    }

    private void initLanguageSelectBox(UIFormSelectBox langSelectBox) {
        List<SelectItemOption<String>> lang = new ArrayList<SelectItemOption<String>>();
        LocaleConfigService localeService = getApplicationComponent(LocaleConfigService.class);
        Locale currentLocale = WebuiRequestContext.getCurrentInstance().getLocale();
        Iterator<LocaleConfig> i = localeService.getLocalConfigs().iterator();
        String displayName = null;
        String language = null;
        String country = null;
        String defaultValue = null;
        SelectItemOption<String> option;
        while (i.hasNext()) {
            LocaleConfig config = i.next();
            Locale locale = config.getLocale();

            language = locale.getLanguage();
            country = locale.getCountry();
            if (country != null && country.length() > 0) {
                language = language + "_" + country;
            }

            ResourceBundle localeResourceBundle;

            displayName = null;
            try {
                localeResourceBundle = getResourceBundle(currentLocale);
                String key = "Locale." + language;
                String translation = localeResourceBundle.getString(key);
                displayName = translation;
            } catch (MissingResourceException e) {
                displayName = capitalizeFirstLetter(locale.getDisplayName(currentLocale));
            } catch (Exception e) {

            }

            option = new SelectItemOption<String>(displayName, language);
            if (locale.getDisplayName().equals(currentLocale.getDisplayName())) {
                option.setSelected(true);
                defaultValue = language;
            }

            lang.add(option);
        }

        Collections.sort(lang, new LanguagesComparator());
        langSelectBox.setOptions(lang);
        langSelectBox.setValue(defaultValue);
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        ExoContainer appContainer = ExoContainerContext.getCurrentContainer();
        ResourceBundleService service = (ResourceBundleService) appContainer
                .getComponentInstanceOfType(ResourceBundleService.class);
        ResourceBundle res = service.getResourceBundle("locale.portal.webui", locale);
        return res;
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

    private class LanguagesComparator implements Comparator<SelectItemOption<String>> {
        public int compare(SelectItemOption<String> o1, SelectItemOption<String> o2) {
            return o1.getLabel().compareToIgnoreCase(o2.getLabel());
        }
    }

    private void switchLabelMode(boolean isExtendedMode) {
        getUIStringInput(PAGE_DISPLAY_NAME).setRendered(!isExtendedMode);
        getUIStringInput(I18N_LABEL).setRendered(isExtendedMode);
        getUIFormSelectBox(LANGUAGES).setRendered(isExtendedMode);
    }

    private String getLabelOnLocale(String locale) {
        return cachedLabels.get(locale);
    }

    public void updateCachedLabels(String locale, String label) {
        if (label != null) {
            cachedLabels.put(locale, label);
        }
    }

    public static class ChangeNodeActionListener extends EventListener<UIWizardPageSetInfo> {
        public void execute(Event<UIWizardPageSetInfo> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            UIWizardPageSetInfo pageSetInfo = event.getSource();
            UIPageCreationWizard uiWizard = (UIPageCreationWizard) pageSetInfo.getAncestorOfType(UIPageCreationWizard.class);

            String uri = context.getRequestParameter(OBJECTID);
            UIPageNodeSelector uiPageNodeSelector = pageSetInfo.getChild(UIPageNodeSelector.class);
            try {
                uiPageNodeSelector.setSelectedURI(uri);
                if (!uiPageNodeSelector.getSelectedNode().getURI().equals(uri)) {
                    context.getUIApplication().addMessage(new ApplicationMessage("UIWizardPageSetInfo.msg.node.deleted", null));
                }
            } catch (NavigationServiceException ex) {
                context.getUIApplication().addMessage(
                        new ApplicationMessage("UIWizardPageSetInfo.msg.navigation.deleted", null));
                uiWizard.createEvent("Abort", Phase.PROCESS, context).broadcast();
                return;
            }

            event.getRequestContext().addUIComponentToUpdateByAjax(uiWizard);
        }
    }

    public static class SwitchPublicationDateActionListener extends EventListener<UIWizardPageSetInfo> {
        public void execute(Event<UIWizardPageSetInfo> event) throws Exception {
            UIWizardPageSetInfo uiForm = event.getSource();
            boolean isCheck = uiForm.getUICheckBoxInput(SHOW_PUBLICATION_DATE).isChecked();
            uiForm.getUIFormDateTimeInput(START_PUBLICATION_DATE).setRendered(isCheck);
            uiForm.getUIFormDateTimeInput(END_PUBLICATION_DATE).setRendered(isCheck);
            UIWizard uiWizard = uiForm.getAncestorOfType(UIWizard.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiWizard);
        }

    }

    public static class SwitchVisibleActionListener extends EventListener<UIWizardPageSetInfo> {
        @Override
        public void execute(Event<UIWizardPageSetInfo> event) throws Exception {
            UIWizardPageSetInfo uiForm = event.getSource();
            boolean isCheck = uiForm.getUICheckBoxInput(VISIBLE).isChecked();
            uiForm.setShowCheckPublicationDate(isCheck);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }

    /**
     * Update the transient label from cache to I18N_LABEL field and add value in this field to the cached labels.
     */
    public static class ChangeLanguageActionListener extends EventListener<UIWizardPageSetInfo> {
        @Override
        public void execute(Event<UIWizardPageSetInfo> event) throws Exception {
            UIWizardPageSetInfo uiForm = event.getSource();
            UIFormSelectBox languageSelection = uiForm.getUIFormSelectBox(LANGUAGES);
            UIFormStringInput label = uiForm.getUIStringInput(I18N_LABEL);
            uiForm.updateCachedLabels(uiForm.selectedLocale, label.getValue());

            uiForm.selectedLocale = languageSelection.getValue();
            label.setValue(uiForm.getLabelOnLocale(uiForm.selectedLocale));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }

    /**
     * Change between simple and extended mode of label.
     */
    public static class SwitchLabelModeActionListener extends EventListener<UIWizardPageSetInfo> {
        @Override
        public void execute(Event<UIWizardPageSetInfo> event) throws Exception {
            UIWizardPageSetInfo uiForm = event.getSource();
            boolean isExtendedMode = uiForm.getUICheckBoxInput(SWITCH_MODE).isChecked();
            uiForm.switchLabelMode(isExtendedMode);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
        }
    }
}
