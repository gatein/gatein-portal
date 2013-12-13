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
import java.util.List;

import org.exoplatform.portal.Constants;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileHandler;
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

@ComponentConfig(template = "app:/groovy/portal/webui/portal/UISkinSelector.gtmpl", events = {
        @EventConfig(listeners = UISkinSelector.SaveActionListener.class),
        @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class) })
public class UISkinSelector extends UIContainer {
    private String name_;

    @SuppressWarnings("unchecked")
    public UISkinSelector() {
        name_ = "UIChangeSkin";
        UIPortal uiPortal = Util.getUIPortal();
        List<SelectItemCategory> itemCategories = new ArrayList<SelectItemCategory>();
        SkinService skinService = uiPortal.getApplicationComponent(SkinService.class);
        for (String skin : skinService.getAvailableSkinNames()) {
            SelectItemCategory skinCategory = new SelectItemCategory(skin, false);
            skinCategory.addSelectItemOption(new SelectItemOption(skin, skin, skin));
            itemCategories.add(skinCategory);
        }
        itemCategories.get(0).setSelected(true);

        UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
        String currentSkin = uiPortalApp.getSkin();

        if (currentSkin == null)
            currentSkin = SkinService.DEFAULT_SKIN;
        for (SelectItemCategory ele : itemCategories) {
            if (ele.getName().equals(currentSkin))
                ele.setSelected(true);
            else
                ele.setSelected(false);
        }

        UIItemSelector selector = new UIItemSelector("Skin");
        selector.setItemCategories(itemCategories);
        selector.setRendered(true);
        addChild(selector);
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public static class SaveActionListener extends EventListener<UISkinSelector> {
        public void execute(Event<UISkinSelector> event) throws Exception {
            WebuiRequestContext rContext = event.getRequestContext();
            String skin = rContext.getRequestParameter("skin");
            UIPortal uiPortal = Util.getUIPortal();
            UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
            UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
            uiMaskWS.createEvent("Close", Phase.DECODE, rContext).broadcast();
            // event.getRequestContext().addUIComponentToUpdateByAjax(uiApp) ;
            Util.getPortalRequestContext().ignoreAJAXUpdateOnPortlets(false);
            if (skin == null || skin.trim().length() < 1)
                return;
            uiApp.setSkin(skin);
            String remoteUser = rContext.getRemoteUser();

            // Save the skin selection to the User Profile
            OrganizationService orgService = event.getSource().getApplicationComponent(OrganizationService.class);
            if (remoteUser != null) {
                UserProfile userProfile = orgService.getUserProfileHandler().findUserProfileByName(remoteUser);
                if(userProfile == null) {
                    userProfile = orgService.getUserProfileHandler().createUserProfileInstance(remoteUser);
                }
                userProfile.getUserInfoMap().put(Constants.USER_SKIN, skin);
                UserProfileHandler hanlder = orgService.getUserProfileHandler();
                hanlder.saveUserProfile(userProfile, true);
            }
        }
    }

}
