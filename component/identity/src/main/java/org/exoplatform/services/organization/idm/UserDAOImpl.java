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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.impl.UserImpl;
import org.gatein.common.logging.LogLevel;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.AttributesManager;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.query.UserQueryBuilder;
import org.picketlink.idm.common.exception.IdentityException;
import org.picketlink.idm.impl.api.SimpleAttribute;
import org.picketlink.idm.impl.api.model.SimpleUser;

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

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class UserDAOImpl implements UserHandler
{
   private static Logger log = LoggerFactory.getLogger(UserDAOImpl.class);

   private final PicketLinkIDMService service_;

   private List<UserEventListener> listeners_ = new ArrayList<UserEventListener>(3);

   public static final String USER_PASSWORD = "password";

   public static final String USER_FIRST_NAME = "firstName";

   public static final String USER_LAST_NAME = "lastName";

   public static final String USER_EMAIL = "email";

   public static final String USER_CREATED_DATE = "createdDate";

   public static final String USER_LAST_LOGIN_TIME = "lastLoginTime";

   public static final String USER_ORGANIZATION_ID = "organizationId";

   public static final Set<String> USER_NON_PROFILE_KEYS;

   public static final DateFormat dateFormat = DateFormat.getInstance();

   private PicketLinkIDMOrganizationServiceImpl orgService;
   
   static
   {
      Set<String> keys = new HashSet<String>();
      keys.add(USER_PASSWORD);
      keys.add(USER_FIRST_NAME);
      keys.add(USER_LAST_NAME);
      keys.add(USER_EMAIL);
      keys.add(USER_CREATED_DATE);
      keys.add(USER_LAST_LOGIN_TIME);
      keys.add(USER_ORGANIZATION_ID);

      USER_NON_PROFILE_KEYS = Collections.unmodifiableSet(keys);
   }

   public UserDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService idmService)
      throws Exception
   {
      service_ = idmService;
      this.orgService = orgService;
   }

   final public List getUserEventListeners()
   {
      return listeners_;
   }

   public void addUserEventListener(UserEventListener listener)
   {
      listeners_.add(listener);
   }

   public User createUserInstance()
   {
      return new UserImpl();
   }

   public User createUserInstance(String username)
   {
      return new UserImpl(username);
   }

   public void createUser(User user, boolean broadcast) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "createUser",
            new Object[]{
               "user", user,
               "broadcast", broadcast
            }
         );
      }


      IdentitySession session = service_.getIdentitySession();
      if (broadcast)
      {
         preSave(user, true);
      }

      try
      {
         orgService.flush();

         session.getPersistenceManager().createUser(user.getUserName());
      }
      catch (IdentityException e)
      {
         log.info("Identity operation error: ", e);

      }

      persistUserInfo(user, session);

      if (broadcast)
      {
         postSave(user, true);
      }

   }

   public void saveUser(User user, boolean broadcast) throws Exception
   {

      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "saveUser",
            new Object[]{
               "user", user,
               "broadcast", broadcast
            }
         );
      }

      IdentitySession session = service_.getIdentitySession();
      if (broadcast)
      {
         preSave(user, false);
      }

      persistUserInfo(user, session);

      if (broadcast)
      {
         postSave(user, false);
      }
   }

   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "removeUser",
            new Object[]{
               "userName", userName,
               "broadcast", broadcast
            }
         );
      }


      IdentitySession session = service_.getIdentitySession();

      org.picketlink.idm.api.User foundUser = null;

      try
      {
         orgService.flush();

         foundUser = session.getPersistenceManager().findUser(userName);
      }
      catch (IdentityException e)
      {
         log.info("Cannot obtain user: " + userName + "; ", e);

      }

      if (foundUser == null)
      {
         return null;
      }

      try
      {
         // Remove all memberships and profile first
         orgService.getMembershipHandler().removeMembershipByUser(userName, false);
         orgService.getUserProfileHandler().removeUserProfile(userName, false);
      }
      catch (Exception e)
      {
         log.info("Cannot cleanup user relationships: " + userName + "; ", e);

      }

      User exoUser = getPopulatedUser(userName, session);

      if (broadcast)
      {
         preDelete(exoUser);
      }

      try
      {
         session.getPersistenceManager().removeUser(foundUser, true);
      }
      catch (IdentityException e)
      {
         log.info("Cannot remove user: " + userName + "; ", e);

      }

      if (getIntegrationCache() != null)
      {
         getIntegrationCache().invalidateAll();
      }

      if (broadcast)
      {
         postDelete(exoUser);
      }
      return exoUser;
   }

   //
   public User findUserByName(String userName) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "findUserByName",
            new Object[]{
               "userName", userName,
            }
         );
      }

      IdentitySession session = service_.getIdentitySession();

      User user = getPopulatedUser(userName, session);

      if (log.isTraceEnabled())
      {
        Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "findUserByName",
            user
         );
      }

      return user;
   }

   public LazyPageList getUserPageList(int pageSize) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "getUserPagetList",
            new Object[]{
               "pageSize", pageSize
            }
         );
      }

      UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

      return new LazyPageList(new IDMUserListAccess(this, service_, qb, pageSize, true), pageSize);
   }

   public ListAccess<User> findAllUsers() throws Exception
   {
      throw new UnsupportedOperationException();
   }

//
   public boolean authenticate(String username, String password) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "authenticate",
            new Object[]{
               "userName", username,
               "password", "****"
            }
         );
      }

      User user = findUserByName(username);
      if (user == null)
      {
         if (log.isTraceEnabled())
         {
            Tools.logMethodOut(
               log,
               LogLevel.TRACE,
               "authenticate",
               false
            );
         }

         return false;
      }

      boolean authenticated = false;

      if (orgService.getConfiguration().isPasswordAsAttribute())
      {
         authenticated = user.getPassword().equals(password);
      }
      else
      {
         try
         {
            orgService.flush();

            IdentitySession session = service_.getIdentitySession();
            org.picketlink.idm.api.User idmUser = session.getPersistenceManager().findUser(user.getUserName());

            authenticated = session.getAttributesManager().validatePassword(idmUser, password);
         }
         catch (Exception e)
         {
            log.info("Cannot authenticate user: " + username + "; ",  e);

         }
      }

      if (authenticated)
      {
         UserImpl userImpl = (UserImpl)user;
         userImpl.setLastLoginTime(Calendar.getInstance().getTime());
         saveUser(userImpl, false);
      }

      if (log.isTraceEnabled())
      {
         Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "authenticate",
            authenticated
         );
      }

      return authenticated;
   }

   public LazyPageList findUsers(Query q) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "findUsers",
            new Object[]{
               "q", q
            }
         );
      }

      // if only condition is email which is unique then delegate to other method as it will be more efficient
      if (q.getUserName() == null &&
         q.getEmail() != null &&
         q.getFirstName() == null &&
         q.getLastName() == null)
      {
         final User uniqueUser = findUserByEmail(q.getEmail());

         if (uniqueUser != null)
         {
            return new LazyPageList<User>( new ListAccess<User>()
            {
               public User[] load(int index, int length) throws Exception, IllegalArgumentException
               {
                  return new User[]{uniqueUser};
               }

               public int getSize() throws Exception
               {
                  return 1;
               }
            }, 1);
         }
      }


      // otherwise use PLIDM queries

      IDMUserListAccess list;

      IntegrationCache cache = getIntegrationCache();

      if (cache != null)
      {
         list = cache.getGtnUserLazyPageList(getCacheNS(), q);
         if (list != null)
         {
            return new LazyPageList(list, 20);
         }
      }

      orgService.flush();

      UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

      if (q.getUserName() != null)
      {
         qb.idFilter(q.getUserName());
      }
      if (q.getEmail() != null)
      {
         qb.attributeValuesFilter(UserDAOImpl.USER_EMAIL, new String[]{q.getEmail()});
      }
      if (q.getFirstName() != null)
      {
         qb.attributeValuesFilter(UserDAOImpl.USER_FIRST_NAME, new String[]{q.getFirstName()});
      }

      //TODO: from/to login date

      if (q.getLastName() != null)
      {
         qb.attributeValuesFilter(UserDAOImpl.USER_LAST_NAME, new String[]{q.getLastName()});
      }



      if (q.getUserName() == null &&
         q.getEmail() == null &&
         q.getFirstName() == null &&
         q.getLastName() == null)
      {
         list = new IDMUserListAccess(this, service_, qb, 20, true);
      }
      else
      {
         list = new IDMUserListAccess(this, service_, qb, 20, false);
      }

      if (cache != null)
      {
         cache.putGtnUserLazyPageList(getCacheNS(), q, list);
      }

      return new LazyPageList(list, 20);
   }

   //

   public ListAccess<User> findUsersByQuery(Query query) throws Exception
   {
      throw new UnsupportedOperationException();
   }

   public LazyPageList findUsersByGroup(String groupId) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "findUsersByGroup",
            new Object[]{
               "groupId", groupId
            }
         );
      }


      UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

      org.picketlink.idm.api.Group jbidGroup = null;
      try
      {
         jbidGroup = orgService.getJBIDMGroup(groupId);
      }
      catch (Exception e)
      {
         log.info("Cannot obtain group: " + groupId + "; ", e);

      }

      qb.addRelatedGroup(jbidGroup);

      return new LazyPageList(new IDMUserListAccess(this, service_, qb, 20, false), 20);
   }

   public User findUserByEmail(String email) throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "findUserByEmail",
            new Object[]{
               "findUserByEmail", email
            }
         );
      }

      IdentitySession session = service_.getIdentitySession();


      org.picketlink.idm.api.User plUser = null;

      try
      {
         orgService.flush();

         plUser = session.getAttributesManager().findUserByUniqueAttribute(USER_EMAIL, email);
      }
      catch (IdentityException e)
      {
         log.info("Cannot find user by email: " + email + "; ", e );

      }

      User user = null;

      if (plUser != null)
      {
         user = new UserImpl(plUser.getId());
         populateUser(user, session);

      }

      if (log.isTraceEnabled())
      {
        Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "findUserByEmail",
            user
         );
      }

      return user;
   }

   public ListAccess<User> findUsersByGroupId(String groupId) throws Exception
   {
      throw new UnsupportedOperationException();
   }

//

   private void preSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
      {
         listener.preSave(user, isNew);
      }
   }

   private void postSave(User user, boolean isNew) throws Exception
   {
      for (UserEventListener listener : listeners_)
      {
         listener.postSave(user, isNew);
      }
   }

   private void preDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
      {
         listener.preDelete(user);
      }
   }

   private void postDelete(User user) throws Exception
   {
      for (UserEventListener listener : listeners_)
      {
         listener.postDelete(user);
      }
   }

   public void persistUserInfo(User user, IdentitySession session) throws Exception
   {
      orgService.flush();

      AttributesManager am = session.getAttributesManager();

      ArrayList attributes = new ArrayList();

      if (user.getCreatedDate() != null)
      {
         attributes.add(new SimpleAttribute(USER_CREATED_DATE, "" + user.getCreatedDate().getTime()));
      }
      if (user.getLastLoginTime() != null)
      {
         attributes.add(new SimpleAttribute(USER_LAST_LOGIN_TIME, "" + user.getLastLoginTime().getTime()));
      }
      if (user.getEmail() != null)
      {
         attributes.add(new SimpleAttribute(USER_EMAIL, user.getEmail()));
      }
      if (user.getFirstName() != null)
      {
         attributes.add(new SimpleAttribute(USER_FIRST_NAME, user.getFirstName()));
      }
      if (user.getLastName() != null)
      {
         attributes.add(new SimpleAttribute(USER_LAST_NAME, user.getLastName()));
      }
      if (user.getOrganizationId() != null)
      {
         attributes.add(new SimpleAttribute(USER_ORGANIZATION_ID, user.getOrganizationId()));
      }
      if (user.getPassword() != null)
      {
         if (orgService.getConfiguration().isPasswordAsAttribute())
         {
            attributes.add(new SimpleAttribute(USER_PASSWORD, user.getPassword()));
         }
         else
         {
            try
            {
               am.updatePassword(session.getPersistenceManager().findUser(user.getUserName()), user.getPassword());
            }
            catch (IdentityException e)
            {
               log.info("Cannot update password: " + user.getUserName() + "; ", e);

            }
         }
      }

      Attribute[] attrs = new Attribute[attributes.size()];
      attrs = (Attribute[])attributes.toArray(attrs);

      try
      {
         am.updateAttributes(user.getUserName(), attrs);
      }
      catch (IdentityException e)
      {
         log.info("Cannot update attributes for user: " + user.getUserName() + "; ", e);

      }

   }

   public User getPopulatedUser(String userName, IdentitySession session) throws Exception
   {
      Object u = null;

      orgService.flush();

      try
      {
         u = session.getPersistenceManager().findUser(userName);
      }
      catch (IdentityException e)
      {
         log.info("Cannot obtain user: " + userName + "; ", e);

      }

      if (u == null)
      {
         return null;
      }

      User user = new UserImpl(userName);

      populateUser(user, session);

      return user;

   }

   public void populateUser(User user, IdentitySession session) throws Exception
   {
      orgService.flush();

      AttributesManager am = session.getAttributesManager();

      Map<String, Attribute> attrs = null;

      try
      {
         attrs = am.getAttributes(new SimpleUser(user.getUserName()));
      }
      catch (IdentityException e)
      {

         log.info("Cannot obtain attributes for user: " + user.getUserName() + "; ", e);

      }

      if (attrs == null)
      {
         return;
      }
      else
      {
         if (attrs.containsKey(USER_CREATED_DATE))
         {
            try
            {
               long date = Long.parseLong(attrs.get(USER_CREATED_DATE).getValue().toString());
               user.setCreatedDate(new Date(date));
            }
            catch (NumberFormatException e)
            {
               // For backward compatibility with GateIn 3.0 and EPP 5 Beta
               try
               {
                  user.setCreatedDate(dateFormat.parse(attrs.get(USER_CREATED_DATE).getValue().toString()));
               }
               catch (ParseException e2)
               {
                  log.error("Cannot parse the creation date for: " + user.getUserName());
               }
            }
         }
         if (attrs.containsKey(USER_EMAIL))
         {
            user.setEmail(attrs.get(USER_EMAIL).getValue().toString());
         }
         if (attrs.containsKey(USER_FIRST_NAME))
         {
            user.setFirstName(attrs.get(USER_FIRST_NAME).getValue().toString());
         }
         if (attrs.containsKey(USER_LAST_LOGIN_TIME))
         {
            try
            {
               Long lastLoginMillis = null;
               Attribute lastLoginAttr = attrs.get(USER_LAST_LOGIN_TIME);
               if (lastLoginAttr != null)
               {
                  Object lastLoginValue = lastLoginAttr.getValue();
                  if (lastLoginValue != null) {
                     lastLoginMillis = Long.parseLong(lastLoginValue.toString());
                  }
               }
               if (lastLoginMillis != null)
               {
                  user.setLastLoginTime(new Date(lastLoginMillis));
               }
            }
            catch (NumberFormatException e)
            {
               // For backward compatibility with GateIn 3.0 and EPP 5 Beta
               try
               {
                  user.setLastLoginTime(dateFormat.parse(attrs.get(USER_LAST_LOGIN_TIME).getValue().toString()));
               }
               catch (ParseException e2)
               {
                  log.error("Cannot parse the last login date for: " + user.getUserName());
               }
            }
         }
         if (attrs.containsKey(USER_LAST_NAME))
         {
            user.setLastName(attrs.get(USER_LAST_NAME).getValue().toString());
         }
         if (attrs.containsKey(USER_ORGANIZATION_ID))
         {
            user.setOrganizationId(attrs.get(USER_ORGANIZATION_ID).getValue().toString());
         }
         if (attrs.containsKey(USER_PASSWORD))
         {
            user.setPassword(attrs.get(USER_PASSWORD).getValue().toString());
         }
      }
   }

   public PicketLinkIDMOrganizationServiceImpl getOrgService()
   {
      return orgService;
   }

   private IntegrationCache getIntegrationCache()
   {
      // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
      // TODO: it should be there.
      return ((PicketLinkIDMServiceImpl)service_).getIntegrationCache();
   }

   /**
    * Returns namespace to be used with integration cache
    * @return
    */
   private String getCacheNS()
   {
      // TODO: refactor to remove cast. For now to avoid adding new config option and share existing cache instannce
      // TODO: it should be there.
      return ((PicketLinkIDMServiceImpl)service_).getRealmName();
   }
}
