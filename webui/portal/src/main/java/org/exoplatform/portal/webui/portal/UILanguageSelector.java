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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.LocaleContextInfo;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIItemSelector;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "system:/groovy/portal/webui/portal/UILanguageSelector.gtmpl", events = {
        @EventConfig(listeners = UILanguageSelector.SaveActionListener.class),
        @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class) })
public class UILanguageSelector extends UIContainer {
    private String name_;

    @SuppressWarnings("unchecked")
    public UILanguageSelector() throws Exception {
        name_ = "UIChangeLanguage";
        LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class);
        Locale currentLocale = Util.getPortalRequestContext().getLocale();
        SelectItemOption localeItem = null;
        List<SelectItemOption> optionsList = new ArrayList<SelectItemOption>();

        for (Object object : configService.getLocalConfigs()) {
            LocaleConfig localeConfig = (LocaleConfig) object;
            Locale locale = localeConfig.getLocale();
            String lang = locale.getLanguage();
            String country = locale.getCountry();

            ResourceBundle currentLocaleResourceBundle = getResourceBundle(currentLocale);
            ResourceBundle localeResourceBundle = getResourceBundle(locale);

            String key = "Locale." + lang;

            String displayName = null;
            try {
                String translation = currentLocaleResourceBundle.getString(key);
                displayName = translation;
            } catch (MissingResourceException e) {
                displayName = capitalizeFirstLetter(locale.getDisplayLanguage(currentLocale));
            }

            String localedName = null;
            try {
                String translation = localeResourceBundle.getString(key);
                localedName = translation;
            } catch (MissingResourceException e) {
                localedName = capitalizeFirstLetter(locale.getDisplayLanguage(locale));
            }

            if (country != null && country.length() > 0) {
                lang = lang + "_" + country;
                key = "Locale." + lang;

                try {
                    String translation = currentLocaleResourceBundle.getString(key);
                    displayName = translation;
                } catch (MissingResourceException e) {
                    displayName = capitalizeFirstLetter(locale.getDisplayLanguage(currentLocale)) + " - "
                            + capitalizeFirstLetter(locale.getDisplayCountry(currentLocale));
                }

                try {
                    String translation = localeResourceBundle.getString(key);
                    localedName = translation;
                } catch (MissingResourceException e) {
                    localedName = capitalizeFirstLetter(locale.getDisplayLanguage(locale)) + " - "
                            + capitalizeFirstLetter(locale.getDisplayCountry(locale));
                }
            }

            if (localedName == null || localedName.length() == 0)
                localedName = "???";
            if (locale.getDisplayName().equalsIgnoreCase(currentLocale.getDisplayName())) {
                localeItem = new SelectItemOption(displayName, lang, localedName, "", true);
            } else {
                localeItem = new SelectItemOption(displayName, lang, localedName, "");
            }
            optionsList.add(localeItem);
        }
        // TODO need use other UIComponent here
        Collections.sort(optionsList, new LanguagesComparator());
        List<SelectItemCategory> contientsCategories = new ArrayList<SelectItemCategory>();
        SelectItemCategory category = new SelectItemCategory("Languages");
        category.setSelectItemOptions(optionsList);
        contientsCategories.add(category);
        UIItemSelector selector = new UIItemSelector("Language");
        selector.setItemCategories(contientsCategories);
        selector.setRendered(true);
        addChild(selector);
    }

    public String getName() {
        return name_;
    }

    private class LanguagesComparator implements Comparator<SelectItemOption> {
        public int compare(SelectItemOption item0, SelectItemOption item1) {
            return item0.getLabel().compareToIgnoreCase(item1.getLabel());
        }
    }

    public static class SaveActionListener extends EventListener<UILanguageSelector> {
        public void execute(Event<UILanguageSelector> event) throws Exception {
            WebuiRequestContext rContext = event.getRequestContext();
            String language = event.getRequestContext().getRequestParameter("language");
            PortalRequestContext prqCtx = PortalRequestContext.getCurrentInstance();

            UIPortalApplication uiApp = Util.getUIPortalApplication();
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            uiMaskWS.createEvent("Close", Phase.DECODE, rContext).broadcast();

            if (language == null || language.isEmpty() || language.trim().equals(prqCtx.getLocale().getLanguage())) {
                // LocalizationLifecycle will save userProfile if locale is changed
                // We need to handle case : locale is not changed, but user's locale setting has not been initialized
                if (prqCtx.getRemoteUser() != null) {
                    saveLocaleToUserProfile(prqCtx);
                }
                return;
            }
            LocaleConfigService localeConfigService = event.getSource().getApplicationComponent(LocaleConfigService.class);
            LocaleConfig localeConfig = localeConfigService.getLocaleConfig(language);
            if (localeConfig == null)
                localeConfig = localeConfigService.getDefaultLocaleConfig();
            prqCtx.setLocale(localeConfig.getLocale());

            if (prqCtx.getRequestLocale() != null) {
                NodeURL url = prqCtx.createURL(NodeURL.TYPE).setNode(Util.getUIPortal().getNavPath());
                url.setLocale(prqCtx.getLocale());
                prqCtx.sendRedirect(url.toString());
            }
        }

        private void saveLocaleToUserProfile(PortalRequestContext context) throws Exception {
            ExoContainer container = context.getApplication().getApplicationServiceContainer();
            OrganizationService svc = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);

            // Don't rely on UserProfileLifecycle loaded UserProfile when doing
            // an update to avoid a potential overwrite of other changes
            UserProfile userProfile = svc.getUserProfileHandler().findUserProfileByName(context.getRemoteUser());
            if (userProfile != null && userProfile.getUserInfoMap() != null) {
                // Only save if user's locale has not been set
                String currLocale = userProfile.getUserInfoMap().get(Constants.USER_LANGUAGE);
                if (currLocale == null || currLocale.trim().equals("")) {
                    userProfile.getUserInfoMap().put(Constants.USER_LANGUAGE,
                            LocaleContextInfo.getLocaleAsString(context.getLocale()));
                    svc.getUserProfileHandler().saveUserProfile(userProfile, false);
                }
            }
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
