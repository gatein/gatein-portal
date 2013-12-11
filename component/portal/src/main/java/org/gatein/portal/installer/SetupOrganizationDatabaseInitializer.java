/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.portal.installer;

import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationConfig;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.OrganizationServiceInitializer;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserStatus;

/**
 * Clone of OrganizationDatabaseInitializer class.
 * It detects if at first boot, root user is initialized with a encoded user defined in configuration.properties.
 * If not, it will store a random password and it will set up a flag to launch a setup.jsp to change the root password.
 * Root password defined in organization-configuration.xml will be ignored.
 */
@SuppressWarnings("deprecation")
public class SetupOrganizationDatabaseInitializer extends BaseComponentPlugin implements OrganizationServiceInitializer,
        ComponentPlugin {
    protected static final Log LOG = ExoLogger.getLogger("exo.core.component.organization.api.SetupOrganizationDatabaseInitializer");

    private OrganizationConfig config_;

    private static int CHECK_EMPTY = 0, CHECK_ENTRY = 1;

    private int checkDatabaseAlgorithm_ = CHECK_EMPTY;

    private boolean printInfo_ = true;

    private boolean updateUsers = false;

    public SetupOrganizationDatabaseInitializer(InitParams params) throws Exception {
        String checkConfig = params.getValueParam("checkDatabaseAlgorithm").getValue();
        if (checkConfig.trim().equalsIgnoreCase("entry")) {
            checkDatabaseAlgorithm_ = CHECK_ENTRY;
        } else {
            checkDatabaseAlgorithm_ = CHECK_EMPTY;
        }
        String printInfoConfig = params.getValueParam("printInformation").getValue();
        if (printInfoConfig.trim().equalsIgnoreCase("true"))
            printInfo_ = true;
        else
            printInfo_ = false;

        ValueParam usParam = params.getValueParam("updateUsers");
        if (usParam != null) {
            String updateUsersParam = usParam.getValue();
            if (updateUsersParam != null && updateUsersParam.trim().equalsIgnoreCase("true")) {
                updateUsers = true;
            }
        }
        config_ = (OrganizationConfig) params.getObjectParamValues(OrganizationConfig.class).get(0);
    }

    public void init(OrganizationService service) throws Exception {
        // Check setup flag
        //PortalSetupService.checkJcrFlag();
        if (checkDatabaseAlgorithm_ == CHECK_EMPTY && checkExistDatabase(service)) {
            return;
        }
        String alg = "check empty database";
        if (checkDatabaseAlgorithm_ == CHECK_ENTRY)
            alg = "check entry database";
        printInfo("=======> Initialize the  organization service data  using algorithm " + alg);
        createGroups(service);
        createMembershipTypes(service);
        createUsers(service);
        printInfo("<=======");
    }

    private boolean checkExistDatabase(OrganizationService service) throws Exception {
        ListAccess<User> users = service.getUserHandler().findAllUsers(UserStatus.BOTH);
        if (users != null && users.getSize() > 0)
            return true;
        return false;
    }

    private void createGroups(OrganizationService orgService) throws Exception {
        printInfo("  Init  Group Data");
        List<?> groups = config_.getGroup();
        for (int i = 0; i < groups.size(); i++) {
            OrganizationConfig.Group data = (OrganizationConfig.Group) groups.get(i);
            String groupId = null;
            String parentId = data.getParentId();
            if (parentId == null || parentId.length() == 0)
                groupId = "/" + data.getName();
            else
                groupId = data.getParentId() + "/" + data.getName();

            if (orgService.getGroupHandler().findGroupById(groupId) == null) {
                Group group = orgService.getGroupHandler().createGroupInstance();
                group.setGroupName(data.getName());
                group.setDescription(data.getDescription());
                group.setLabel(data.getLabel());
                if (parentId == null || parentId.length() == 0) {
                    orgService.getGroupHandler().addChild(null, group, true);
                } else {
                    Group parentGroup = orgService.getGroupHandler().findGroupById(parentId);
                    orgService.getGroupHandler().addChild(parentGroup, group, true);
                }
                printInfo("    Create Group " + groupId);
            } else {
                printInfo("    Group " + groupId + " already exists, ignoring the entry");
            }
        }
    }

    private void createMembershipTypes(OrganizationService service) throws Exception {
        printInfo("  Init  Membership Type  Data");
        List<?> types = config_.getMembershipType();
        for (int i = 0; i < types.size(); i++) {
            OrganizationConfig.MembershipType data = (OrganizationConfig.MembershipType) types.get(i);
            if (service.getMembershipTypeHandler().findMembershipType(data.getType()) == null) {
                MembershipType type = service.getMembershipTypeHandler().createMembershipTypeInstance();
                type.setName(data.getType());
                type.setDescription(data.getDescription());
                service.getMembershipTypeHandler().createMembershipType(type, true);
                printInfo("    Created Membership Type " + data.getType());
            } else {
                printInfo("    Membership Type " + data.getType() + " already exists, ignoring the entry");
            }
        }
    }

    private void createUsers(OrganizationService service) throws Exception {
        printInfo("  Init  User  Data");
        List<?> users = config_.getUser();
        boolean setupEnable = Boolean.parseBoolean(System.getProperty(PortalSetupService.GATEIN_SETUP_ENABLE, "false"));

        MembershipHandler mhandler = service.getMembershipHandler();
        for (int i = 0; i < users.size(); i++) {
            OrganizationConfig.User data = (OrganizationConfig.User) users.get(i);
            User user = service.getUserHandler().createUserInstance(data.getUserName());
            user.setPassword(data.getPassword());
            user.setFirstName(data.getFirstName());
            user.setLastName(data.getLastName());
            user.setEmail(data.getEmail());
            user.setDisplayName(data.getDisplayName());

            // Check root user and JCR flag
//            if ("root".equals(user.getUserName()) && !PortalSetupService.isSetup() && setupEnable) {
//                user.setPassword(PortalSetupService.rootPassword());
//            }

            if (service.getUserHandler().findUserByName(data.getUserName(), UserStatus.BOTH) == null) {
                service.getUserHandler().createUser(user, true);
                printInfo("    Created user " + data.getUserName());
            } else if (updateUsers) {
                service.getUserHandler().saveUser(user, true);
                printInfo("    User " + data.getUserName() + " updated");
            } else {
                printInfo("    User " + data.getUserName() + " already exists, ignoring the entry");
            }

            String groups = data.getGroups();
            String[] entry = groups.split(",");
            for (int j = 0; j < entry.length; j++) {
                String[] temp = entry[j].trim().split(":");
                String membership = temp[0];
                String groupId = temp[1];
                if (mhandler.findMembershipByUserGroupAndType(data.getUserName(), groupId, membership) == null) {
                    Group group = service.getGroupHandler().findGroupById(groupId);
                    MembershipType mt = service.getMembershipTypeHandler().createMembershipTypeInstance();
                    mt.setName(membership);
                    mhandler.linkMembership(user, group, mt, true);
                    printInfo("    Created membership " + data.getUserName() + ", " + groupId + ", " + membership);
                } else {
                    printInfo("    Ignored membership " + data.getUserName() + ", " + groupId + ", " + membership);
                }
            }

        }
    }

    private void printInfo(String message) {
        if (printInfo_)
            LOG.info(message);
    }
}
