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

package org.exoplatform.webui.organization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.Constants;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 28, 2006
 */
@ComponentConfig(template = "system:/groovy/webui/form/UIVTabInputSet.gtmpl")
@Serialized
public class UIUserProfileInputSet extends UIFormInputSet {

    public static final String MALE = "male";

    public static final String FEMALE = "female";

    public static final String JOB_TITLE = "jobtitle";

    public UIUserProfileInputSet() {
    }

    public UIUserProfileInputSet(String name) throws Exception {
        super(name);
        setComponentConfig(UIUserProfileInputSet.class, null);

        UIFormInputSet personalInputSet = new UIFormInputSet("Profile");
        addInput(personalInputSet, UserProfile.PERSONAL_INFO_KEYS);
        addUIFormInput(personalInputSet);

        UIFormInputSet homeInputSet = new UIFormInputSet("HomeInfo");
        addInput(homeInputSet, UserProfile.HOME_INFO_KEYS);
        homeInputSet.setRendered(false);
        addUIFormInput(homeInputSet);

        UIFormInputSet businessInputSet = new UIFormInputSet("BusinessInfo");
        addInput(businessInputSet, UserProfile.BUSINESE_INFO_KEYS);
        businessInputSet.setRendered(false);
        addUIFormInput(businessInputSet);
    }

    public void reset() {
        for (UIComponent uiChild : getChildren()) {
            if (uiChild instanceof UIFormInputSet || uiChild instanceof UIFormInput) {
                ((UIFormInputSet) uiChild).reset();
            }
        }
    }


    private void addInput(UIFormInputSet set, String[] keys) throws Exception {

        for (String key : keys) {
            if (key.equalsIgnoreCase("user.gender")) {
                List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
                ls.add(new SelectItemOption<String>(MALE, MALE));
                ls.add(new SelectItemOption<String>(FEMALE, FEMALE));
                UIFormSelectBox genderSelectBox = new UIFormSelectBox(key, key, ls);
                set.addUIFormInput(genderSelectBox);
                continue;
            } else if (key.equalsIgnoreCase("user.jobtitle")) {
                set.addUIFormInput(new UIFormStringInput(key, null, null).addValidator(UserConfigurableValidator.class,
                        JOB_TITLE, UserConfigurableValidator.KEY_PREFIX + JOB_TITLE, false));
                continue;
            } else if (key.equalsIgnoreCase(Constants.USER_LANGUAGE)) {
                UIFormSelectBox langSelectBox = new UIFormSelectBox(key, key, null);
                set.addUIFormInput(langSelectBox);
                initLanguageCombo(langSelectBox);
                continue;
            }
            set.addUIFormInput(new UIFormStringInput(key, null, null));
        }
    }

    /**
     * Update language select box
     */
    @Override
    public void processRender(WebuiRequestContext context) throws Exception {
        UIFormSelectBox langSelectBox = this.findComponentById(Constants.USER_LANGUAGE);
        initLanguageCombo(langSelectBox);
        super.processRender(context);
    }

    private void initLanguageCombo(UIFormSelectBox langSelectBox) {
        if (langSelectBox == null)
            return;
        String selectedLang = langSelectBox.getSelectedValues()[0];

        List<SelectItemOption<String>> lang = new ArrayList<SelectItemOption<String>>();
        langSelectBox.setOptions(lang); // Clear

        LocaleConfigService localeService = getApplicationComponent(LocaleConfigService.class);
        Locale currentLocale = ((PortletRequestContext) WebuiRequestContext.getCurrentInstance()).getLocale();
        Iterator<LocaleConfig> i = localeService.getLocalConfigs().iterator();
        String displayName = null;
        String language = null;
        String country = null;
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
            if (language.equals(selectedLang)) {
                option.setSelected(true);
            }
            lang.add(option);
        }

        // Set default language for new user is empty
        lang.add(new SelectItemOption<String>("", ""));

        Collections.sort(lang, new LanguagesComparator());

        langSelectBox.setOptions(lang);
    }

    @SuppressWarnings("deprecation")
    public void setUserProfile(String user) throws Exception {
        if (user == null)
            return;
        OrganizationService service = getApplicationComponent(OrganizationService.class);
        UserProfile userProfile = service.getUserProfileHandler().findUserProfileByName(user);
        if (userProfile == null) {
            userProfile = service.getUserProfileHandler().createUserProfileInstance();
            userProfile.setUserName(user);
        }

        if (userProfile.getUserInfoMap() == null)
            return;
        for (UIComponent set : getChildren()) {
            UIFormInputSet inputSet = (UIFormInputSet) set;
            for (UIComponent uiComp : inputSet.getChildren()) {
                UIFormStringInput uiInput = (UIFormStringInput) uiComp;
                uiInput.setValue(userProfile.getAttribute(uiInput.getName()));
            }
        }
    }

    @SuppressWarnings("deprecation")
    public void save(OrganizationService service, String user, boolean isnewUser) throws Exception {

        UserProfileHandler hanlder = service.getUserProfileHandler();
        UserProfile userProfile = hanlder.findUserProfileByName(user);

        if (userProfile == null) {
            userProfile = hanlder.createUserProfileInstance();
            userProfile.setUserName(user);
        }

        for (UIComponent set : getChildren()) {
            UIFormInputSet inputSet = (UIFormInputSet) set;
            for (UIComponent uiComp : inputSet.getChildren()) {
                UIFormStringInput uiInput = (UIFormStringInput) uiComp;
                // if(uiInput.getValue() == null || uiInput.getValue().length() < 1)
                // continue;
                userProfile.getUserInfoMap().put(uiInput.getName(), uiInput.getValue());
            }
        }

        hanlder.saveUserProfile(userProfile, true);

        Object[] args = { "UserProfile", user };
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        UIApplication uiApp = context.getUIApplication();

        if (isnewUser) {
            uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.successful.create.user", args));
            return;
        }
        uiApp.addMessage(new ApplicationMessage("UIUserProfileInputSet.msg.sucsesful.update.userprofile", args));
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

    private class LanguagesComparator implements Comparator<SelectItemOption> {
        public int compare(SelectItemOption item0, SelectItemOption item1) {
            return item0.getLabel().compareToIgnoreCase(item1.getLabel());
        }
    }
}
