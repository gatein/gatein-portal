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

package org.exoplatform.services.organization.idm;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.DisabledUserException;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.common.logging.LogLevel;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.AttributesManager;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.query.UserQueryBuilder;
import org.picketlink.idm.impl.api.SimpleAttribute;
import org.picketlink.idm.impl.api.model.SimpleUser;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class UserDAOImpl extends AbstractDAOImpl implements UserHandler {

    private List<UserEventListener> listeners_ = new ArrayList<UserEventListener>(3);

    public static final String USER_PASSWORD = "password";

    public static final String USER_PASSWORD_SALT = "passwordSalt";

    public static final String USER_FIRST_NAME = "firstName";

    public static final String USER_LAST_NAME = "lastName";

    public static final String USER_DISPLAY_NAME = "displayName";

    public static final String USER_EMAIL = "email";

    public static final String USER_CREATED_DATE = "createdDate";

    public static final String USER_LAST_LOGIN_TIME = "lastLoginTime";

    public static final String USER_ORGANIZATION_ID = "organizationId";

    public static final String USER_ENABLED = "enabled";

    public static final Set<String> USER_NON_PROFILE_KEYS;

    public static final DateFormat dateFormat = DateFormat.getInstance();

    static {
        Set<String> keys = new HashSet<String>();
        keys.add(USER_PASSWORD);
        keys.add(USER_PASSWORD_SALT);
        keys.add(USER_FIRST_NAME);
        keys.add(USER_LAST_NAME);
        keys.add(USER_DISPLAY_NAME);
        keys.add(USER_EMAIL);
        keys.add(USER_CREATED_DATE);
        keys.add(USER_LAST_LOGIN_TIME);
        keys.add(USER_ORGANIZATION_ID);
        keys.add(USER_ENABLED);

        USER_NON_PROFILE_KEYS = Collections.unmodifiableSet(keys);
    }

    public UserDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService idmService) {
        super(orgService, idmService);
    }

    public final List<UserEventListener> getUserEventListeners() {
        return Collections.unmodifiableList(listeners_);
    }

    public void addUserEventListener(UserEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.add(listener);
    }

    public void removeUserEventListener(UserEventListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners_.remove(listener);
    }

    public User createUserInstance() {
        return new UserImpl();
    }

    public User createUserInstance(String username) {
        return new UserImpl(username);
    }

    public void createUser(User user, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "createUser", new Object[] { "user", user, "broadcast", broadcast });
        }

        IdentitySession session = service_.getIdentitySession();
        if (broadcast) {
            preSave(user, true);
        }

        try {
            orgService.flush();

            session.getPersistenceManager().createUser(user.getUserName());
        } catch (Exception e) {
            handleException("Identity operation error: ", e);

        }

        if (getIntegrationCache() != null) {
            getIntegrationCache().invalidateAll();
        }

        persistUserInfo(user, session, true);

        if (broadcast) {
            postSave(user, true);
        }

    }

    public void saveUser(User user, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "saveUser", new Object[] { "user", user, "broadcast", broadcast });
        }

        if (user != null && !user.isEnabled()) {
            throw new DisabledUserException(user.getUserName());
        }

        IdentitySession session = service_.getIdentitySession();
        if (broadcast) {
            preSave(user, false);
        }

        persistUserInfo(user, session, false);

        if (broadcast) {
            postSave(user, false);
        }
    }

    @Override
    public User setEnabled(String userName, boolean enabled, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "setEnabled", new Object[] { "userName", userName, "enabled", enabled,
                    "broadcast", broadcast });
        }

        orgService.flush();
        IdentitySession session = service_.getIdentitySession();
        User foundUser = getPopulatedUser(userName, session, UserStatus.BOTH);

        if (foundUser == null || foundUser.isEnabled() == enabled) {
            return foundUser;
        }
        ((UserImpl) foundUser).setEnabled(enabled);
        if (broadcast)
            preSetEnabled(foundUser);

        Attribute[] attrs = new Attribute[] { new SimpleAttribute(USER_ENABLED, String.valueOf(enabled)) };

        AttributesManager am = session.getAttributesManager();
        try {
            am.updateAttributes(userName, attrs);
        } catch (Exception e) {
            handleException("Cannot update enabled status for user: " + userName + "; ", e);
        }

        if (getIntegrationCache() != null) {
            getIntegrationCache().invalidateAll();
        }

        if (broadcast)
            postSetEnabled(foundUser);

        return foundUser;
    }

    public User removeUser(String userName, boolean broadcast) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "removeUser", new Object[] { "userName", userName, "broadcast", broadcast });
        }

        IdentitySession session = service_.getIdentitySession();

        org.picketlink.idm.api.User foundUser = null;

        try {
            orgService.flush();

            foundUser = session.getPersistenceManager().findUser(userName);
        } catch (Exception e) {
            handleException("Cannot obtain user: " + userName + "; ", e);

        }

        if (foundUser == null) {
            return null;
        }

        User exoUser = getPopulatedUser(userName, session, UserStatus.BOTH);

        if (broadcast) {
            preDelete(exoUser);
        }

        try {
            // Remove all memberships and profile first
            orgService.getMembershipHandler().removeMembershipByUser(userName, false);
            orgService.getUserProfileHandler().removeUserProfile(userName, false);
        } catch (Exception e) {
            handleException("Cannot cleanup user relationships: " + userName + "; ", e);

        }

        try {
            session.getPersistenceManager().removeUser(foundUser, true);
        } catch (Exception e) {
            handleException("Cannot remove user: " + userName + "; ", e);

        }

        if (getIntegrationCache() != null) {
            getIntegrationCache().invalidateAll();
        }

        if (broadcast) {
            postDelete(exoUser);
        }
        return exoUser;
    }

    //
    public User findUserByName(String userName) throws Exception {
        return findUserByName(userName, UserStatus.ENABLED);
    }

    @Override
    public User findUserByName(String userName, UserStatus userStatus) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUserByName", new Object[] { "userName", userName, "userStatus",
                    userStatus });
        }

        IdentitySession session = service_.getIdentitySession();
        User user = getPopulatedUser(userName, session, userStatus);

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findUserByName", user);
        }

        return user;
    }

    public LazyPageList<User> getUserPageList(int pageSize) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "getUserPagetList", new Object[] { "pageSize", pageSize });
        }

        UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();
        boolean enabledOnly = filterDisabledUsersInQueries();
        if (enabledOnly) {
            qb = addDisabledUserFilter(qb);
        }
        return new LazyPageList<User>(new IDMUserListAccess(qb, pageSize, true, enabledOnly ? UserStatus.ENABLED : UserStatus.DISABLED), pageSize);
    }

    public ListAccess<User> findAllUsers() throws Exception {
        return findAllUsers(UserStatus.ENABLED);
    }

    @Override
    public ListAccess<User> findAllUsers(UserStatus userStatus) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findAllUsers", new Object[] { "userStatus", userStatus });
        }

        UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();
        switch (userStatus) {
            case DISABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addEnabledUserFilter(qb);
                }
                break;
            case BOTH:
                break;
            case ENABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addDisabledUserFilter(qb);
                }
                break;
        }
        return new IDMUserListAccess(qb, 20, !countPaginatedUsers(), userStatus);
    }

    //
    public boolean authenticate(String username, String password) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "authenticate", new Object[] { "userName", username, "password", "****" });
        }

        User user = findUserByName(username, UserStatus.BOTH);
        if (user == null) {
            if (log.isTraceEnabled()) {
                Tools.logMethodOut(log, LogLevel.TRACE, "authenticate", false);
            }

            return false;
        }

        if (!user.isEnabled()) {
            throw new DisabledUserException(username);
        }

        boolean authenticated = false;

        if (orgService.getConfiguration().isPasswordAsAttribute()) {
            authenticated = user.getPassword().equals(password);
        } else {
            try {
                orgService.flush();

                IdentitySession session = service_.getIdentitySession();
                org.picketlink.idm.api.User idmUser = session.getPersistenceManager().findUser(user.getUserName());

                authenticated = session.getAttributesManager().validatePassword(idmUser, password);
            } catch (Exception e) {
                handleException("Cannot authenticate user: " + username + "; ", e);

            }
        }

        if (authenticated && orgService.getConfiguration().isUpdateLastLoginTimeAfterAuthentication()) {
            UserImpl userImpl = (UserImpl) user;
            userImpl.setLastLoginTime(Calendar.getInstance().getTime());
            saveUser(userImpl, false);
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "authenticate", authenticated);
        }

        return authenticated;
    }

    public LazyPageList findUsers(Query q) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUsers", new Object[] { "q", q });
        }

        ListAccess list = findUsersByQuery(q);

        return new LazyPageList(list, 20);
    }

    //

    public ListAccess<User> findUsersByQuery(Query q) throws Exception {
        return findUsersByQuery(q, UserStatus.ENABLED);
    }

    @Override
    public ListAccess<User> findUsersByQuery(Query q, UserStatus userStatus) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUsersByQuery", new Object[] { q, userStatus });
        }

        // if only condition is email which is unique then delegate to other method as it will be more efficient
        if (q.getUserName() == null && q.getEmail() != null && q.getFirstName() == null && q.getLastName() == null) {
            final User uniqueUser = findUserByUniqueAttribute(USER_EMAIL, q.getEmail(), userStatus);

            if (uniqueUser != null) {
                return new ListAccess<User>() {
                    public User[] load(int index, int length) throws Exception {
                        return new User[] { uniqueUser };
                    }

                    public int getSize() throws Exception {
                        return 1;
                    }
                };
            } else if (!q.getEmail().contains("*")) {
                return new ListAccess<User>() {
                    public User[] load(int index, int length) throws Exception {
                        return new User[0];
                    }

                    public int getSize() throws Exception {
                        return 0;
                    }
                };
            }
        }

        // otherwise use PLIDM queries

        IDMUserListAccess list;

        IntegrationCache cache = getIntegrationCache();

        if (cache != null) {
            list = cache.getGtnUserLazyPageList(getCacheNS(), q, userStatus);
            if (list != null) {
                return list;
            }
        }

        orgService.flush();

        UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

        if (q.getUserName() != null) {
            //Process username
            String username = q.getUserName();
            if(!username.startsWith("*")) {
                username = "*" + username;
            }
            if(!username.endsWith("*")) {
                username = username + "*";
            }
            qb.idFilter(username);
        }
        if (q.getEmail() != null) {
            qb.attributeValuesFilter(UserDAOImpl.USER_EMAIL, new String[] { q.getEmail() });
        }
        if (q.getFirstName() != null) {
            qb.attributeValuesFilter(UserDAOImpl.USER_FIRST_NAME, new String[] { q.getFirstName() });
        }

        // TODO: from/to login date

        if (q.getLastName() != null) {
            qb.attributeValuesFilter(UserDAOImpl.USER_LAST_NAME, new String[] { q.getLastName() });
        }

        switch (userStatus) {
            case DISABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addEnabledUserFilter(qb);
                }
                break;
            case BOTH:
                break;
            case ENABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addDisabledUserFilter(qb);
                }
                break;
        }

        if (q.getUserName() == null && q.getEmail() == null && q.getFirstName() == null && q.getLastName() == null) {
            list = new IDMUserListAccess(qb, 20, !countPaginatedUsers(), userStatus);
        } else {
            list = new IDMUserListAccess(qb, 20, false, userStatus);
        }

        if (cache != null) {
            cache.putGtnUserLazyPageList(getCacheNS(), q, list, userStatus);
        }

        return list;
    }

    public LazyPageList findUsersByGroup(String groupId) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUsersByGroup", new Object[] { "groupId", groupId });
        }

        return new LazyPageList(findUsersByGroupId(groupId), 20);
    }

    public User findUserByEmail(String email) throws Exception {
        return findUserByUniqueAttribute(USER_EMAIL, email, UserStatus.ENABLED);
    }

    public User findUserByUniqueAttribute(String attributeName, String attributeValue, UserStatus userStatus) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUserByUniqueAttribute", new Object[] { "findUserByUniqueAttribute",
                    attributeName, attributeValue, userStatus });
        }

        IdentitySession session = service_.getIdentitySession();

        org.picketlink.idm.api.User plUser = null;

        try {
            orgService.flush();

            plUser = session.getAttributesManager().findUserByUniqueAttribute(attributeName, attributeValue);
        } catch (Exception e) {
            handleException("Cannot find user by unique attribute: attrName=" + attributeName + ", attrValue=" + attributeValue
                    + "; ", e);

        }

        User user = null;

        if (plUser != null) {
            user = new UserImpl(plUser.getId());
            populateUser(user, session);

            if (!userStatus.matches(user.isEnabled())) {
                user = null;
            }
        }

        if (log.isTraceEnabled()) {
            Tools.logMethodOut(log, LogLevel.TRACE, "findUserByUniqueAttribute", user);
        }

        return user;
    }

    public ListAccess<User> findUsersByGroupId(String groupId) throws Exception {
        return findUsersByGroupId(groupId, UserStatus.ENABLED);
    }

    @Override
    public ListAccess<User> findUsersByGroupId(String groupId, UserStatus userStatus) throws Exception {
        if (log.isTraceEnabled()) {
            Tools.logMethodIn(log, LogLevel.TRACE, "findUsersByGroupId", new Object[] { groupId, userStatus });
        }

        UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

        org.picketlink.idm.api.Group jbidGroup = null;
        try {
            jbidGroup = orgService.getJBIDMGroup(groupId);
        } catch (Exception e) {
            handleException("Cannot obtain group: " + groupId + "; ", e);

        }

        //As test case supposed, we should return empty list instead of Exception if group is not exist.
        if(jbidGroup == null) {
            return new ListAccess<User>() {
                public User[] load(int index, int length) throws Exception {
                    if(index > 0 || length > 0) {
                        throw new IndexOutOfBoundsException("Try to access an empty list");
                    }
                    return new User[0];
                }

                public int getSize() throws Exception {
                    return 0;
                }
            };
        }

        qb.addRelatedGroup(jbidGroup);

        switch (userStatus) {
            case DISABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addEnabledUserFilter(qb);
                }
                break;
            case BOTH:
                break;
            case ENABLED:
                if (filterDisabledUsersInQueries()) {
                    qb = addDisabledUserFilter(qb);
                }
                break;
        }

        return new IDMUserListAccess(qb, 20, false, userStatus);
    }

    //

    private void preSave(User user, boolean isNew) throws Exception {
        for (UserEventListener listener : listeners_) {
            listener.preSave(user, isNew);
        }
    }

    private void postSave(User user, boolean isNew) throws Exception {
        for (UserEventListener listener : listeners_) {
            listener.postSave(user, isNew);
        }
    }

    private void preDelete(User user) throws Exception {
        for (UserEventListener listener : listeners_) {
            listener.preDelete(user);
        }
    }

    private void postDelete(User user) throws Exception {
        for (UserEventListener listener : listeners_) {
            listener.postDelete(user);
        }
    }

    private void preSetEnabled(User user) throws Exception {
        for (UserEventListener listener : listeners_)
            listener.preSetEnabled(user);
    }

    private void postSetEnabled(User user) throws Exception {
        for (UserEventListener listener : listeners_)
            listener.postSetEnabled(user);
    }

    public void persistUserInfo(User user, IdentitySession session, boolean isNew) {
        orgService.flush();

        AttributesManager am = session.getAttributesManager();

        ArrayList attributes = new ArrayList();

        if (user.getCreatedDate() != null) {
            attributes.add(new SimpleAttribute(USER_CREATED_DATE, "" + user.getCreatedDate().getTime()));
        }
        if (user.getLastLoginTime() != null) {
            attributes.add(new SimpleAttribute(USER_LAST_LOGIN_TIME, "" + user.getLastLoginTime().getTime()));
        }
        if (user.getEmail() != null) {
            attributes.add(new SimpleAttribute(USER_EMAIL, user.getEmail()));
        }
        if (user.getFirstName() != null) {
            attributes.add(new SimpleAttribute(USER_FIRST_NAME, user.getFirstName()));
        }
        if (user.getLastName() != null) {
            attributes.add(new SimpleAttribute(USER_LAST_NAME, user.getLastName()));
        }

        // TODO: GTNPORTAL-2358 Change once displayName will be available as part of Organization API
        if (user instanceof UserImpl) {
            UserImpl userImpl = (UserImpl) user;
            if (userImpl.getDisplayName() != null) {
                attributes.add(new SimpleAttribute(USER_DISPLAY_NAME, ((UserImpl) user).getDisplayName()));
            } else {
                removeDisplayNameIfNeeded(am, user);
            }

            if (isNew) {
                attributes.add(new SimpleAttribute(USER_ENABLED, Boolean.TRUE.toString()));
            }
        } else {
            log.warn("User is of class " + user.getClass() + " which is not instanceof " + UserImpl.class);
        }

        if (user.getOrganizationId() != null) {
            attributes.add(new SimpleAttribute(USER_ORGANIZATION_ID, user.getOrganizationId()));
        }
        if (user.getPassword() != null) {
            if (orgService.getConfiguration().isPasswordAsAttribute()) {
                attributes.add(new SimpleAttribute(USER_PASSWORD, user.getPassword()));
            } else {
                try {
                    am.updatePassword(session.getPersistenceManager().findUser(user.getUserName()), user.getPassword());
                } catch (Exception e) {
                    handleException("Cannot update password: " + user.getUserName() + "; ", e);

                }
            }
        }

        Attribute[] attrs = new Attribute[attributes.size()];
        attrs = (Attribute[]) attributes.toArray(attrs);

        try {
            am.updateAttributes(user.getUserName(), attrs);
        } catch (Exception e) {
            handleException("Cannot update attributes for user: " + user.getUserName() + "; ", e);

        }

    }

    public User getPopulatedUser(String userName, IdentitySession session, UserStatus userStatus) throws Exception {
        Object u = null;

        orgService.flush();

        try {
            u = session.getPersistenceManager().findUser(userName);
        } catch (Exception e) {
            handleException("Cannot obtain user: " + userName + "; ", e);

        }

        if (u == null) {
            return null;
        }

        User user = new UserImpl(userName);
        populateUser(user, session);

        return userStatus.matches(user.isEnabled()) ? user : null;
    }

    public void populateUser(User user, IdentitySession session) {
        orgService.flush();

        AttributesManager am = session.getAttributesManager();

        Map<String, Attribute> attrs = null;

        try {
            attrs = am.getAttributes(new SimpleUser(user.getUserName()));
        } catch (Exception e) {

            handleException("Cannot obtain attributes for user: " + user.getUserName() + "; ", e);

        }

        if (attrs == null) {
            return;
        } else {
            if (attrs.containsKey(USER_CREATED_DATE)) {
                try {
                    long date = Long.parseLong(attrs.get(USER_CREATED_DATE).getValue().toString());
                    user.setCreatedDate(new Date(date));
                } catch (NumberFormatException e) {
                    // For backward compatibility with GateIn 3.0 and EPP 5 Beta
                    try {
                        user.setCreatedDate(dateFormat.parse(attrs.get(USER_CREATED_DATE).getValue().toString()));
                    } catch (ParseException e2) {
                        log.error("Cannot parse the creation date for: " + user.getUserName());
                    }
                }
            }
            if (attrs.containsKey(USER_EMAIL)) {
                user.setEmail(attrs.get(USER_EMAIL).getValue().toString());
            }
            if (attrs.containsKey(USER_FIRST_NAME)) {
                user.setFirstName(attrs.get(USER_FIRST_NAME).getValue().toString());
            }
            if (attrs.containsKey(USER_LAST_LOGIN_TIME)) {
                try {
                    Long lastLoginMillis = null;
                    Attribute lastLoginAttr = attrs.get(USER_LAST_LOGIN_TIME);
                    if (lastLoginAttr != null) {
                        Object lastLoginValue = lastLoginAttr.getValue();
                        if (lastLoginValue != null) {
                            lastLoginMillis = Long.parseLong(lastLoginValue.toString());
                        }
                    }
                    if (lastLoginMillis != null) {
                        user.setLastLoginTime(new Date(lastLoginMillis));
                    }
                } catch (NumberFormatException e) {
                    // For backward compatibility with GateIn 3.0 and EPP 5 Beta
                    try {
                        user.setLastLoginTime(dateFormat.parse(attrs.get(USER_LAST_LOGIN_TIME).getValue().toString()));
                    } catch (ParseException e2) {
                        log.error("Cannot parse the last login date for: " + user.getUserName());
                    }
                }
            }
            if (attrs.containsKey(USER_LAST_NAME)) {
                user.setLastName(attrs.get(USER_LAST_NAME).getValue().toString());
            }
            if (attrs.containsKey(USER_DISPLAY_NAME)) {
                // TODO: GTNPORTAL-2358 Change once displayName will be available as part of Organization API
                user.setFullName(attrs.get(USER_DISPLAY_NAME).getValue().toString());
            }
            if (attrs.containsKey(USER_ORGANIZATION_ID)) {
                user.setOrganizationId(attrs.get(USER_ORGANIZATION_ID).getValue().toString());
            }
            if (attrs.containsKey(USER_PASSWORD)) {
                user.setPassword(attrs.get(USER_PASSWORD).getValue().toString());
            }
            if (attrs.containsKey(USER_ENABLED)) {
                ((UserImpl) user).setEnabled(Boolean.parseBoolean(attrs.get(USER_ENABLED).getValue().toString()));
            }
        }
    }

    public PicketLinkIDMOrganizationServiceImpl getOrgService() {
        return orgService;
    }

    private IntegrationCache getIntegrationCache() {
        // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
        // TODO: it should be there.
        return ((PicketLinkIDMServiceImpl) service_).getIntegrationCache();
    }

    /**
     * Returns namespace to be used with integration cache
     *
     * @return
     */
    private String getCacheNS() {
        // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
        // TODO: it should be there.
        return ((PicketLinkIDMServiceImpl) service_).getRealmName();
    }

    // Field displayName is not mandatory. We need to handle situation when user deleted displayName, which had been set
    // previously.
    // We need to ask if current User has displayName set previously and if yes, it needs to be removed.
    private void removeDisplayNameIfNeeded(AttributesManager am, User user) {
        try {
            Attribute attr = am.getAttribute(user.getUserName(), USER_DISPLAY_NAME);
            if (attr != null) {
                am.removeAttributes(user.getUserName(), new String[] { USER_DISPLAY_NAME });
            }
        } catch (Exception e) {
            handleException("Cannot remove displayName attribute of user: " + user.getUserName() + "; ", e);
        }
    }

    private UserQueryBuilder addDisabledUserFilter(UserQueryBuilder qb) throws Exception {
        return qb.attributeValuesFilter(UserDAOImpl.USER_ENABLED, new String[] {Boolean.TRUE.toString()});
    }

    private UserQueryBuilder addEnabledUserFilter(UserQueryBuilder qb) throws Exception {
        return qb.attributeValuesFilter(UserDAOImpl.USER_ENABLED, new String[] {Boolean.FALSE.toString()});
    }

    private boolean countPaginatedUsers() {
        return orgService.getConfiguration().isCountPaginatedUsers();
    }

    private boolean filterDisabledUsersInQueries() {
        return orgService.getConfiguration().isFilterDisabledUsersInQueries();
    }
}
