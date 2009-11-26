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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIItemSelector;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@ComponentConfig(template = "system:/groovy/portal/webui/portal/UILanguageSelector.gtmpl", events = {
   @EventConfig(listeners = UILanguageSelector.SaveActionListener.class),
   @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class)})
public class UILanguageSelector extends UIContainer
{
   private String name_;

   @SuppressWarnings("unchecked")
   public UILanguageSelector() throws Exception
   {
      name_ = "UIChangeLanguage";
      LocaleConfigService configService = getApplicationComponent(LocaleConfigService.class);
      Locale currentLocale = Util.getPortalRequestContext().getLocale();
      SelectItemOption localeItem = null;
      List<SelectItemOption> optionsList = new ArrayList<SelectItemOption>();

      for (Object object : configService.getLocalConfigs())
      {
         LocaleConfig localeConfig = (LocaleConfig)object;
         Locale locale = localeConfig.getLocale();
         String displayName = locale.getDisplayLanguage(currentLocale);
         String lang = locale.getLanguage();
         String localedName = capitalizeFirstLetter(locale.getDisplayLanguage(locale));
         if (localedName == null || localedName.length() == 0)
            localedName = "???";
         if (locale.getDisplayName().equalsIgnoreCase(currentLocale.getDisplayName()))
         {
            localeItem = new SelectItemOption(displayName, lang, localedName, "", true);
         }
         else
         {
            localeItem = new SelectItemOption(displayName, lang, localedName, "");
         }
         optionsList.add(localeItem);
      }
      //TODO need use other UIComponent here
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

   public String getName()
   {
      return name_;
   }

   private class LanguagesComparator implements Comparator<SelectItemOption>
   {
      public int compare(SelectItemOption item0, SelectItemOption item1)
      {
         return item0.getLabel().compareToIgnoreCase(item1.getLabel());
      }
   }

   static public class SaveActionListener extends EventListener<UILanguageSelector>
   {
      public void execute(Event<UILanguageSelector> event) throws Exception
      {
         String language = event.getRequestContext().getRequestParameter("language");

         UIPortalApplication uiApp = Util.getUIPortalApplication();
         UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
         uiMaskWS.setUIComponent(null);
         //event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS) ;
         Util.getPortalRequestContext().setFullRender(false);
         if (language == null || language.trim().length() < 1)
            return;
         //if(!uiPortal.isModifiable()) return;

         LocaleConfigService localeConfigService = event.getSource().getApplicationComponent(LocaleConfigService.class);
         LocaleConfig localeConfig = localeConfigService.getLocaleConfig(language);
         if (localeConfig == null)
            localeConfig = localeConfigService.getDefaultLocaleConfig();
         uiApp.setLocale(localeConfig.getLocale());
         uiApp.setOrientation(localeConfig.getOrientation());
         UIPortal uiPortal = uiApp.findFirstComponentOfType(UIPortal.class);
         uiPortal.refreshNavigation(localeConfig.getLocale());
         OrganizationService orgService = event.getSource().getApplicationComponent(OrganizationService.class);
         String remoteUser = event.getRequestContext().getRemoteUser();
         if (remoteUser != null)
         {
            UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(remoteUser);
            userProfile.getUserInfoMap().put("user.language", language);
            UserProfileHandler hanlder = orgService.getUserProfileHandler();
            hanlder.saveUserProfile(userProfile, true);
         }
      }
   }
   
   private String capitalizeFirstLetter(String word)
   {
      if (word == null)
      {
         return null;
      }
      if (word.length() == 0)
      {
         return word;
      }
      StringBuilder result = new StringBuilder(word);
      result.replace(0, 1, result.substring(0, 1).toUpperCase());
      return result.toString();
   }
}