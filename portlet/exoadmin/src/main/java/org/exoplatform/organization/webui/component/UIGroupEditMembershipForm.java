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

package org.exoplatform.organization.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com Dec 2, 2008
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", events = {
        @EventConfig(listeners = UIGroupEditMembershipForm.SaveActionListener.class),
        @EventConfig(listeners = UIGroupEditMembershipForm.CancelActionListener.class) })
@Serialized
public class UIGroupEditMembershipForm extends UIForm {

    private List<SelectItemOption<String>> listOption = new ArrayList<SelectItemOption<String>>();

    private static final String USER_NAME = "username";

    private static final String MEMBER_SHIP = "membership";

    private String membershipId;

    private String groupId;

    public UIGroupEditMembershipForm() {
        addUIFormInput(new UIFormStringInput(USER_NAME, USER_NAME, null).setReadOnly(true));
        addUIFormInput(new UIFormSelectBox(MEMBER_SHIP, MEMBER_SHIP, listOption).setSize(1));
    }

    public void setValue(Membership memberShip, Group selectedGroup) throws Exception {
        this.membershipId = memberShip.getId();
        this.groupId = selectedGroup.getId();
        getUIStringInput(USER_NAME).setValue(memberShip.getUserName());
        OrganizationService service = getApplicationComponent(OrganizationService.class);
        List<?> collection = (List<?>) service.getMembershipTypeHandler().findMembershipTypes();
        for (Object ele : collection) {
            MembershipType mt = (MembershipType) ele;
            SelectItemOption<String> option = new SelectItemOption<String>(mt.getName(), mt.getName(), mt.getDescription());
            if (mt.getName().equals(memberShip.getMembershipType()))
                option.setSelected(true);
            listOption.add(option);
        }
    }

    public static class SaveActionListener extends EventListener<UIGroupEditMembershipForm> {
        public void execute(Event<UIGroupEditMembershipForm> event) throws Exception {
            UIGroupEditMembershipForm uiForm = event.getSource();
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            UIPopupWindow uiPopup = uiForm.getParent();
            OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);

            Membership formMembership = service.getMembershipHandler().findMembership(uiForm.membershipId);
            if (formMembership == null) {
                uiApp.addMessage(new ApplicationMessage("UIGroupEditMembershipForm.msg.membership-delete", null));
                uiPopup.setUIComponent(null);
                uiPopup.setShow(false);
                return;
            }
            String userName = formMembership.getUserName();
            Group group = service.getGroupHandler().findGroupById(uiForm.groupId);
            User user = service.getUserHandler().findUserByName(userName);
            if(user == null) {
                String messageBundle = (user == null ? "UIAccountInputSet.msg.user-is-deleted" : "UIAccountInputSet.msg.user-is-disabled");
                uiApp.addMessage(new ApplicationMessage(messageBundle, null, ApplicationMessage.WARNING));
                return;
            }

            MembershipHandler memberShipHandler = service.getMembershipHandler();
            String memberShipTypeStr = uiForm.getUIFormSelectBox(MEMBER_SHIP).getValue();
            MembershipType membershipType = service.getMembershipTypeHandler().findMembershipType(memberShipTypeStr);
            Membership membership = memberShipHandler.findMembershipByUserGroupAndType(userName, group.getId(),
                    membershipType.getName());
            if (membership != null) {
                uiApp.addMessage(new ApplicationMessage("UIGroupEditMembershipForm.msg.membership-exist", null));
                return;
            }
            memberShipHandler.removeMembership(uiForm.membershipId, true);
            memberShipHandler.linkMembership(user, group, membershipType, true);

            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
        }
    }

    public static class CancelActionListener extends EventListener<UIGroupEditMembershipForm> {
        public void execute(Event<UIGroupEditMembershipForm> event) throws Exception {
            UIGroupEditMembershipForm uiForm = event.getSource();
            UIPopupWindow uiPopup = uiForm.getParent();
            uiPopup.setUIComponent(null);
            uiPopup.setShow(false);
        }
    }
}
