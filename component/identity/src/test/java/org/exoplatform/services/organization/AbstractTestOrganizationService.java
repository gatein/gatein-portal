/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.services.organization;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.idm.Config;
import org.exoplatform.services.organization.idm.PicketLinkIDMOrganizationServiceImpl;
import org.exoplatform.services.organization.idm.UserDAOImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 27, 2005
 */
public class AbstractTestOrganizationService {
    private static String Group1 = "Group1";

    private static String Group2 = "Group2";

    private static String Group3 = "Group3";

    private static String Benj = "Benj";

    private static String Tuan = "Tuan";

    private OrganizationService service_;

    private UserHandler userHandler_;

    private UserProfileHandler profileHandler_;

    private GroupHandler groupHandler_;

    private MembershipTypeHandler mtHandler_;

    private MembershipHandler membershipHandler_;

    boolean runtest = true;

    private static final String USER = "test";

    private static final List<String> USERS;

    private static final int USERS_LIST_SIZE = 15;

    private PortalContainer manager;

    static {
        USERS = new ArrayList<String>(USERS_LIST_SIZE);
        for (int i = 0; i < USERS_LIST_SIZE; i++)
            USERS.add(USER + "_" + i);
    }

    @Before
    public void setUp() throws Exception {
        if (!runtest)
            return;

        manager = PortalContainer.getInstance();
        service_ = (OrganizationService) manager.getComponentInstanceOfType(OrganizationService.class);
        userHandler_ = service_.getUserHandler();
        profileHandler_ = service_.getUserProfileHandler();
        groupHandler_ = service_.getGroupHandler();
        mtHandler_ = service_.getMembershipTypeHandler();
        membershipHandler_ = service_.getMembershipHandler();

        RequestLifeCycle.begin((ComponentRequestLifecycle) service_);

    }

    @After
    public void tearDown() throws Exception {
        Query query = new Query();
        query.setUserName(USER + "*");
        PageList users = userHandler_.findUsers(query);

        List<User> allUsers = users.getAll();

        for (int i = allUsers.size() - 1; i >= 0; i--) {
            String userName = allUsers.get(i).getUserName();
            userHandler_.removeUser(userName, true);
        }

        RequestLifeCycle.end();
    }

    @Test
    public void testSimple() throws Exception {
        assertTrue(true);
        Config config = ((PicketLinkIDMOrganizationServiceImpl) service_).getConfiguration();

        assertNotNull(config);
        assertNotNull(config.getGroupTypeMappings());
        assertNotNull(config.getGroupTypeMappings().keySet());

        assertEquals(config.getGroupTypeMappings().keySet().size(), 5);
        assertEquals(config.getGroupTypeMappings().get("/"), "root_type");

        assertEquals(config.getGroupType("/"), "root_type");
        assertEquals(config.getGroupType(null), "root_type");
        assertEquals(config.getGroupType("/platform"), "platform_type");
        assertEquals(config.getGroupType("/platform/administrators"), "platform_type");
        assertEquals(config.getGroupType("/platform/guests"), "platform_type");
        assertEquals(config.getGroupType("/platform/users"), "users_type");
        assertEquals(config.getGroupType("/platform/users/john"), "platform_type");
        assertEquals(config.getGroupType("/organization/acme/france/offices"), ".organization.acme.france.offices");
        assertEquals(config.getGroupType("/organization/acme/france/offices/paris"), ".organization.acme.france.offices.paris");
        assertEquals(config.getGroupType("/organization/acme/france"), "france_type");
        assertEquals(config.getGroupType("/organization/acme"), ".organization.acme");
        assertEquals(config.getGroupType("/foo/bar"), ".foo.bar");
        assertEquals(config.getGroupType("/foo"), ".foo");
        assertEquals(config.getGroupType("/toto"), "toto_type");
        assertEquals(config.getGroupType("/toto/lolo"), "toto_type");
        assertEquals(config.getGroupType("/toto/lolo/tutu"), "toto_type");
    }

    @Test
    public void testUserPageSize() throws Exception {
        for (String name : USERS)
            createUser(name);

        Query query = new Query();
        PageList users = userHandler_.findUsers(query);
        // newly created plus one 'demo' from configuration
        assertEquals(USERS_LIST_SIZE + 1, users.getAll().size());
        assertEquals(1, users.getAvailablePage());
        for (Object o : users.getPage(1)) {
            User u = (User) o;
            if (!u.getUserName().equals("demo"))
                assertTrue(USERS.contains(u.getUserName()));
        }
    }

    @Test
    public void testUser() throws Exception {
        createUser(USER);
        User user = userHandler_.findUserByName(USER);
        assertTrue("Found user instance ", user != null);
        assertEquals("Expect user name is: ", USER, user.getUserName());
        UserProfile userProfile = profileHandler_.findUserProfileByName(USER);
        assertNull(profileHandler_.removeUserProfile(USER, true));
        assertNull(profileHandler_.findUserProfileByName(USER));

        userProfile = profileHandler_.createUserProfileInstance(USER);
        userProfile.getUserInfoMap().put("key", "value");
        profileHandler_.saveUserProfile(userProfile, true);
        userProfile = profileHandler_.findUserProfileByName(USER);
        assertTrue("Expect user profile is found: ", userProfile != null);
        assertEquals(userProfile.getUserInfoMap().get("key"), "value");

        PageList users = userHandler_.findUsers(new Query());
        assertTrue("Expect 1 user found ", users.getAvailable() >= 1);

        /* Update user's information */
        user.setFirstName("Exo(Update)");
        userHandler_.saveUser(user, false);
        userProfile.getUserInfoMap().put("user.gender", "male");
        profileHandler_.saveUserProfile(userProfile, true);
        userProfile = profileHandler_.findUserProfileByName(USER);

        assertEquals("expect first name is", "Exo(Update)", user.getFirstName());
        assertEquals("Expect profile is updated: user.gender is ", "male", userProfile.getUserInfoMap().get("user.gender"));

        PageList piterator = userHandler_.getUserPageList(10);
        // newly created 'test' and 'demo'
        assertEquals(2, piterator.currentPage().size());

        // membershipHandler_.removeMembershipByUser(USER,false);
        userHandler_.removeUser(USER, true);
        piterator = userHandler_.getUserPageList(10);
        // one 'demo'
        assertEquals(1, piterator.currentPage().size());
        assertNull("User: USER is removed: ", userHandler_.findUserByName(USER));
        assertNull(" user's profile of USER was removed:", profileHandler_.findUserProfileByName(USER));
    }

    @Test
    public void testUniqueAttribute() throws Exception {
        if (userHandler_ instanceof UserDAOImpl) {
            UserDAOImpl ud = (UserDAOImpl) userHandler_;

            User user = userHandler_.createUserInstance("toto");
            user.setEmail("toto@gatein.org");
            userHandler_.createUser(user, true);

            user = userHandler_.createUserInstance("lolo");
            user.setEmail("lolo@gatein.org");
            userHandler_.createUser(user, true);

            // Find by unique attribute
            assertNull(ud.findUserByEmail("foobar"));
            user = ud.findUserByEmail("toto@gatein.org");
            assertNotNull(user);
            assertEquals("toto", user.getUserName());

            user = ud.findUserByEmail("lolo@gatein.org");
            assertNotNull(user);
            assertEquals("lolo", user.getUserName());

            ud.removeUser("toto", false);
            ud.removeUser("lolo", false);

        }
    }

    @Test
    public void testFindUsers() throws Exception {
        if (userHandler_ instanceof UserDAOImpl) {
            UserDAOImpl ud = (UserDAOImpl) userHandler_;
            User user1 = ud.createUserInstance("foo");
            user1.setFirstName("foo");
            user1.setLastName("bar");
            user1.setEmail("foo@bar.com");
            ud.createUser(user1, true);

            User user2 = ud.createUserInstance("foobar");
            user2.setFirstName("foobar");
            user2.setLastName("foobar");
            user2.setEmail("foobar@foobar.com");
            ud.createUser(user2, true);

            Query query = new Query();
            List<User> users = ud.findUsers(query).getAll();
            assertEquals(3, users.size());
            assertEquals("demo", users.get(0).getUserName());
            assertEquals("foo", users.get(1).getUserName());
            assertEquals("foobar", users.get(2).getUserName());

            query.setEmail("*foo*");
            users = ud.findUsers(query).getAll();
            assertEquals(2, users.size());
            assertEquals("foo", users.get(0).getUserName());
            assertEquals("foobar", users.get(1).getUserName());

            query.setEmail("*bar*");
            users = ud.findUsers(query).getAll();
            assertEquals(2, users.size());
            assertEquals("foo", users.get(0).getUserName());
            assertEquals("foobar", users.get(1).getUserName());

            query.setEmail("*bar.com*");
            users = ud.findUsers(query).getAll();
            assertEquals(2, users.size());
            assertEquals("foo", users.get(0).getUserName());
            assertEquals("foobar", users.get(1).getUserName());

            query.setEmail("*foobar*");
            users = ud.findUsers(query).getAll();
            assertEquals(1, users.size());
            assertEquals("foobar", users.get(0).getUserName());

            // Cleanup after test
            ud.removeUser("foo", true);
            ud.removeUser("foobar", true);
        }
    }

    @Test
    public void testGroup() throws Exception {
        /* Create a parent group with name is: GroupParent */
        String parentName = "GroupParent";
        Group groupParent = groupHandler_.createGroupInstance();
        groupParent.setGroupName(parentName);
        groupParent.setDescription("This is description");
        groupHandler_.addChild(null, groupParent, true);
        groupParent = groupHandler_.findGroupById(groupParent.getId());
        assertEquals("GroupParent", groupParent.getGroupName());
        /* Create a child group with name: Group1 */
        Group groupChild1 = groupHandler_.createGroupInstance();
        groupChild1.setGroupName(Group1);
        groupChild1.setLabel("Group1 Label");
        groupHandler_.addChild(groupParent, groupChild1, true);

        assertEquals(groupHandler_.findGroupById(groupChild1.getId()).getLabel(), "Group1 Label");

        groupChild1 = groupHandler_.findGroupById(groupChild1.getId());
        assertEquals(groupChild1.getParentId(), groupParent.getId());
        assertEquals("Expect group child's name is: ", Group1, groupChild1.getGroupName());
        /* Update groupChild's information */
        groupChild1.setLabel("Group1 Label renamed");
        groupChild1.setDescription("new description ");
        groupHandler_.saveGroup(groupChild1, true);

        assertEquals(groupHandler_.findGroupById(groupChild1.getId()).getLabel(), "Group1 Label renamed");

        /* Create a group child with name is: Group2 */
        Group groupChild2 = groupHandler_.createGroupInstance();
        groupChild2.setGroupName(Group2);
        groupHandler_.addChild(groupParent, groupChild2, true);
        groupChild2 = groupHandler_.findGroupById(groupChild2.getId());
        assertEquals(groupChild2.getParentId(), groupParent.getId());
        assertEquals("Expect group child's name is: ", Group2, groupChild2.getGroupName());
        /*
         * find all child group in groupParent Expect result: 2 child group: group1, group2
         */
        assertEquals("Expect number of child group in parent group is: ", 2, groupHandler_.findGroups(groupParent).size());
        /* Remove a child group */
        groupHandler_.removeGroup(groupHandler_.findGroupById(groupChild1.getId()), true);
        assertNull("Expect child group has been removed: ", groupHandler_.findGroupById(groupChild1.getId()));
        assertEquals("Expect only 1 child group in parent group", 1, groupHandler_.findGroups(groupParent).size());
        /* Remove Parent group, expect exception thrown */
        try {
            groupHandler_.removeGroup(groupParent, true);
            fail("Exception should be thrown when try return parennt group");
        } catch (Exception e) {}
        assertNotNull("Expect ParentGroup is not removed:", groupHandler_.findGroupById(groupParent.getId()));
        assertEquals("Expect all child group is not removed: ", 1, groupHandler_.findGroups(groupParent).size());
        
        Collection<Group> groups = groupHandler_.findGroupByMembership("demo", "member");
        assertNotNull(groups);
        assertEquals(1, groups.size());
        
        groups = groupHandler_.resolveGroupByMembership("demo", "member");
        assertNotNull(groups);
        assertEquals(2, groups.size());
    }

    @Test
    public void testMembershipType() throws Exception {
        /* Create a membershipType */
        String testType = "testType";
        MembershipType mt = mtHandler_.createMembershipTypeInstance();
        mt.setName(testType);
        mt.setDescription("This is a test");
        mt.setOwner("exo");
        mtHandler_.createMembershipType(mt, true);
        assertEquals("Expect mebershiptype is:", testType, mtHandler_.findMembershipType(testType).getName());

        /* Update MembershipType's information */
        String desc = "This is a test (update)";
        mt.setDescription(desc);
        mtHandler_.saveMembershipType(mt, true);
        assertEquals("Expect membershiptype's description", desc, mtHandler_.findMembershipType(testType).getDescription());

        /* create another membershipType */
        mt = mtHandler_.createMembershipTypeInstance();
        mt.setName("anothertype");
        mt.setOwner("exo");
        mtHandler_.createMembershipType(mt, true);

        /*
         * find all membership type Expect result: 4 membershipType: "testmembership", "anothertype", "member" and "*" (default
         * membership type, it is created at startup time)
         */
        assertEquals("Expect 4 membership in collection: ", 4, mtHandler_.findMembershipTypes().size());
        assertEquals("The * should be the first one in collection: ", MembershipTypeHandler.ANY_MEMBERSHIP_TYPE, mtHandler_.findMembershipTypes().iterator().next().getName());

        /* remove "testmembership" */
        mtHandler_.removeMembershipType(testType, true);
        assertEquals("Membership type has been removed:", null, mtHandler_.findMembershipType(testType));
        assertEquals("Expect 2 membership in collection(1 is default): ", 3, mtHandler_.findMembershipTypes().size());

        /* remove "anothertype" */
        mtHandler_.removeMembershipType("anothertype", true);
        assertEquals("Membership type has been removed:", null, mtHandler_.findMembershipType("anothertype"));
        assertEquals("Expect 1 membership in collection(default type): ", 2, mtHandler_.findMembershipTypes().size());
        /* All membershipType was removed(except default membership) */
    }

    @Test
    public void testMembership() throws Exception {
        /* Create 2 user: benj and tuan */
        User userBenj = createUser(Benj);
        User userTuan = createUser(Tuan);

        /* Create "Group1" */
        Group group1 = groupHandler_.createGroupInstance();
        group1.setGroupName(Group1);
        groupHandler_.addChild(null, group1, true);
        /* Create "Group2" */
        Group group2 = groupHandler_.createGroupInstance();
        group2.setGroupName(Group2);
        groupHandler_.addChild(null, group2, true);

        /* Create membership1 and assign Benj to "Group1" with this membership */
        MembershipType mt = mtHandler_.createMembershipTypeInstance();
        mt.setName("testmembership");
        mtHandler_.createMembershipType(mt, true);

        membershipHandler_.linkMembership(userBenj, group1, mt, true);
        membershipHandler_.linkMembership(userBenj, group2, mt, true);
        membershipHandler_.linkMembership(userTuan, group2, mt, true);

        mt = mtHandler_.createMembershipTypeInstance();
        mt.setName("membershipType2");
        mtHandler_.createMembershipType(mt, true);
        membershipHandler_.linkMembership(userBenj, group2, mt, true);

        mt = mtHandler_.createMembershipTypeInstance();
        mt.setName("membershipType3");
        mtHandler_.createMembershipType(mt, true);
        membershipHandler_.linkMembership(userBenj, group2, mt, true);

        /*
         * find all memberships in group2 Expect result: 4 membership: 3 for Benj(testmebership, membershipType2,
         * membershipType3) : 1 for Tuan(testmembership)
         */
        assertEquals("Expect number of membership in group 2 is: ", 4, membershipHandler_.findMembershipsByGroup(group2).size());

        /*
         * find all memberships in "Group2" relate with Benj Expect result: 3 membership
         */
        assertEquals("Expect number of membership in " + Group2 + " relate with benj is: ", 3, membershipHandler_
                .findMembershipsByUserAndGroup(Benj, group2.getId()).size());

        /*
         * find all memberships of Benj in all group Expect result: 5 membership: 3 memberships in "Group2", 1 membership in
         * "Users" (default) : 1 membership in "group1"
         */
        assertEquals("expect membership is: ", 5, membershipHandler_.findMembershipsByUser(Benj).size());

        /*
         * find memberships of Benj in "Group2" with membership type: testType Expect result: 1 membership with membershipType
         * is "testType" (testmembership)
         */
        Membership membership = membershipHandler_.findMembershipByUserGroupAndType(Benj, group2.getId(), "testmembership");
        assertNotNull("Expect membership is found:", membership);
        assertEquals("Expect membership type is: ", "testmembership", membership.getMembershipType());
        assertEquals("Expect groupId of this membership is: ", group2.getId(), membership.getGroupId());
        assertEquals("Expect user of this membership is: ", Benj, membership.getUserName());

        /*
         * find all groups of Benj Expect result: 3 group: "Group1", "Group2" and "user" ("user" is default group)
         */
        assertEquals("expect group is: ", 3, groupHandler_.findGroupsOfUser(Benj).size());

        /*
         * find all groups has membership type "TYPE" relate with Benj expect result: 2 group: "Group1" and "Group2"
         */
        assertEquals("expect group is: ", 2, groupHandler_.findGroupByMembership(Benj, "testmembership").size());

        /* remove a membership */
        String memId = membershipHandler_.findMembershipByUserGroupAndType(Benj, group2.getId(), "membershipType3").getId();
        membershipHandler_.removeMembership(memId, true);
        assertNull("Membership was removed: ",
                membershipHandler_.findMembershipByUserGroupAndType(Benj, "/" + Group2, "membershipType3"));

        /*
         * remove a user Expect result: all membership related with user will be remove
         */
        userHandler_.removeUser(Tuan, true);
        assertNull("This user was removed", userHandler_.findUserByName(Tuan));

        assertTrue("All membership related with this user was removed: ", membershipHandler_.findMembershipsByUser(Tuan)
                .isEmpty());

        /*
         * Remove a group Expect result: all membership associate with this group will be removed
         */
        groupHandler_.removeGroup(group1, true);
        assertNull("This group was removed ", groupHandler_.findGroupById(group1.getId()));
        assertTrue(membershipHandler_.findMembershipsByGroup(group1).isEmpty());

        /*
         * Remove a MembershipType Expect result: All membership have this type will be removed
         */
        mtHandler_.removeMembershipType("testmembership", true);

        assertNull("This membershipType was removed: ", mtHandler_.findMembershipType("testmembership"));
        /*
         * Check all memberships associate with all groups to guarantee that no membership associate with removed membershipType
         */
        for (Object o : groupHandler_.findGroups(null)) {
            Group g = (Group) o;
            for (Object o1 : membershipHandler_.findMembershipsByGroup(g)) {
                Membership m = (Membership) o1;
                assertFalse("MembershipType of this membership is not: \"testmembership\"", m.getMembershipType()
                        .equalsIgnoreCase("testmembership"));
            }
        }

        // Cleanup after test
        RequestLifeCycle.end();
        RequestLifeCycle.begin((ComponentRequestLifecycle) service_);
        membershipHandler_.removeMembershipByUser(Benj, true);
        userHandler_.removeUser(Benj, true);
        groupHandler_.removeGroup(group2, true);
        mtHandler_.removeMembershipType("membershipType2", true);
        mtHandler_.removeMembershipType("membershipType3", true);
    }

    @Test
    public void testRemoveMembershipByUser() throws Exception {
        String Benj = "B";
        String Tuan = "T";
        User userBenj = createUser(Benj);
        User userTuan = createUser(Tuan);

        String Group1 = "G1";
        String Group2 = "G2";
        String Group3 = "G3";
        Group group1 = groupHandler_.createGroupInstance();
        group1.setGroupName(Group1);
        groupHandler_.addChild(null, group1, true);
        Group group2 = groupHandler_.createGroupInstance();
        group2.setGroupName(Group2);
        groupHandler_.addChild(null, group2, true);
        Group group3 = groupHandler_.createGroupInstance();
        group3.setGroupName(Group3);
        groupHandler_.addChild(null, group3, true);

        MembershipType mt = mtHandler_.createMembershipTypeInstance();
        mt.setName("testmembership_");
        mtHandler_.createMembershipType(mt, true);

        membershipHandler_.linkMembership(userBenj, group1, mt, true);
        membershipHandler_.linkMembership(userBenj, group2, mt, true);
        membershipHandler_.linkMembership(userBenj, group3, mt, true);
        membershipHandler_.linkMembership(userTuan, group1, mt, true);

        assertEquals(membershipHandler_.removeMembershipByUser(Tuan, true).size(), 2);
        assertEquals(membershipHandler_.removeMembershipByUser(Benj, true).size(), 4);

        mtHandler_.removeMembershipType("testmembership_", true);
        userHandler_.removeUser(Tuan, true);
        userHandler_.removeUser(Benj, true);
        groupHandler_.removeGroup(group1, true);
        groupHandler_.removeGroup(group2, true);
        groupHandler_.removeGroup(group3, true);
    }

    @Test
    public void testUserProfileListener() throws Exception {
        System.out.println("Trigger testUserProfileListener");
        UserProfileListener l = new UserProfileListener();
        profileHandler_.addUserProfileEventListener(l);
        User user = createUser(USER);
        assertNotNull(user);
        UserProfile profile = profileHandler_.createUserProfileInstance(user.getUserName());
        profile.setAttribute("blah", "blah");
        System.out.println("Going to save userProfiel");
        profileHandler_.saveUserProfile(profile, true);
        assertTrue(l.preSave && l.postSave);
        assertEquals(l.preSaveCreations, 1);
        assertEquals(l.postSaveCreations, 1);
        assertEquals(l.preSaveUpdates, 0);
        assertEquals(l.postSaveUpdates, 0);

        // Upgrade userProfile
        profile.setAttribute("blah", "blah2");
        profileHandler_.saveUserProfile(profile, true);
        assertEquals(l.preSaveCreations, 1);
        assertEquals(l.postSaveCreations, 1);
        assertEquals(l.preSaveUpdates, 1);
        assertEquals(l.postSaveUpdates, 1);

        // Another upgrade of userProfile
        profile.setAttribute("blah", "blah3");
        profileHandler_.saveUserProfile(profile, true);
        assertEquals(l.preSaveCreations, 1);
        assertEquals(l.postSaveCreations, 1);
        assertEquals(l.preSaveUpdates, 2);
        assertEquals(l.postSaveUpdates, 2);

        // Delete profile
        assertFalse(l.preDelete || l.postDelete);
        profileHandler_.removeUserProfile(user.getUserName(), true);
        assertTrue(l.preDelete && l.postDelete);
        userHandler_.removeUser(user.getUserName(), false);
    }

    @Test
    public void testLinkMembership() throws Exception {
        String g1 = "grp1";
        String usr1 = "usr1";
        String mstype1 = "mstype1";

        Group group1 = groupHandler_.createGroupInstance();
        group1.setGroupName(g1);
        groupHandler_.addChild(null, group1, true);

        User user = createUser(usr1);

        MembershipType mt = mtHandler_.createMembershipTypeInstance();
        mt.setName(mstype1);

        try {

            membershipHandler_.linkMembership(user, group1, mt, true);
            fail();
        } catch (Exception e) {
            // expected as membership type was not created first
        }

        assertNull(mtHandler_.findMembershipType(mstype1));

        userHandler_.removeUser(usr1, true);
        groupHandler_.removeGroup(group1, true);
    }

    @Test
    public void testFindUsersByGroupId() throws Exception {
        PageList users = userHandler_.findUsersByGroup("/users");
        assertTrue(users.getAvailable() > 0);
    }

    private static class UserProfileListener extends UserProfileEventListener {

        boolean preSave;

        boolean postSave;

        boolean preDelete;

        boolean postDelete;

        int preSaveCreations = 0;
        int preSaveUpdates = 0;
        int postSaveCreations = 0;
        int postSaveUpdates = 0;

        @Override
        public void postDelete(UserProfile profile) throws Exception {
            assertEquals(USER, profile.getUserName());
            postDelete = true;
        }

        @Override
        public void postSave(UserProfile profile, boolean isNew) throws Exception {
            assertEquals(USER, profile.getUserName());
            postSave = true;

            if (isNew) {
                postSaveCreations++;
            } else {
                postSaveUpdates++;
            }
        }

        @Override
        public void preDelete(UserProfile profile) throws Exception {
            assertEquals(USER, profile.getUserName());
            preDelete = true;
        }

        @Override
        public void preSave(UserProfile profile, boolean isNew) throws Exception {
            assertEquals(USER, profile.getUserName());
            preSave = true;

            if (isNew) {
                preSaveCreations++;
            } else {
                preSaveUpdates++;
            }
        }

    }

    public User createUser(String userName) throws Exception {
        User user = userHandler_.createUserInstance(userName);
        user.setPassword("default");
        user.setFirstName("default");
        user.setLastName("default");
        user.setEmail("exo@exoportal.org");
        userHandler_.createUser(user, true);
        return user;
    }
}
