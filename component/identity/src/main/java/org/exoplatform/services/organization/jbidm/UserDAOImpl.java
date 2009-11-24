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

package org.exoplatform.services.organization.jbidm;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.impl.UserImpl;
import org.jboss.identity.idm.api.Attribute;
import org.jboss.identity.idm.api.AttributesManager;
import org.jboss.identity.idm.api.IdentitySession;
import org.jboss.identity.idm.api.query.UserQueryBuilder;
import org.jboss.identity.idm.impl.api.SimpleAttribute;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class UserDAOImpl implements UserHandler
{

   private final JBossIDMService service_;

   private ExoCache cache_;

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

   private JBossIDMOrganizationServiceImpl orgService;

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

   public UserDAOImpl(JBossIDMOrganizationServiceImpl orgService, JBossIDMService idmService, CacheService cservice)
      throws Exception
   {
      service_ = idmService;
      cache_ = cservice.getCacheInstance(UserImpl.class.getName());
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
      IdentitySession session = service_.getIdentitySession();
      if (broadcast)
      {
         preSave(user, true);
      }

      session.getPersistenceManager().createUser(user.getUserName());

      persistUserInfo(user, session);

      if (broadcast)
      {
         postSave(user, true);
      }

   }

   public void saveUser(User user, boolean broadcast) throws Exception
   {
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
      cache_.put(user.getUserName(), user);
   }

   public User removeUser(String userName, boolean broadcast) throws Exception
   {
      IdentitySession session = service_.getIdentitySession();

      org.jboss.identity.idm.api.User foundUser = session.getPersistenceManager().findUser(userName);

      if (foundUser == null)
      {
         cache_.remove(userName);
         return null;
      }

      User exoUser = getPopulatedUser(userName, session);

      if (broadcast)
      {
         preDelete(exoUser);
      }

      session.getPersistenceManager().removeUser(foundUser, true);
      if (broadcast)
      {
         postDelete(exoUser);
      }
      cache_.remove(userName);
      return exoUser;
   }

   //
   public User findUserByName(String userName) throws Exception
   {
      IdentitySession session = service_.getIdentitySession();

      User user = (User)cache_.get(userName);
      if (user != null)
      {
         return user;
      }
      user = getPopulatedUser(userName, session);
      if (user != null)
      {
         cache_.put(userName, user);
      }
      return user;
   }

   public LazyPageList getUserPageList(int pageSize) throws Exception
   {
      UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

      return new LazyPageList(new IDMUserListAccess(this, service_, qb, pageSize, true), pageSize);
   }

   //
   public boolean authenticate(String username, String password) throws Exception
   {
      User user = findUserByName(username);
      if (user == null)
      {
         return false;
      }

      boolean authenticated = false;

      if (orgService.isPasswordAsAttribute())
      {
         authenticated = user.getPassword().equals(password);
      }
      else
      {
         IdentitySession session = service_.getIdentitySession();
         org.jboss.identity.idm.api.User idmUser = session.getPersistenceManager().findUser(user.getUserName());

         authenticated = session.getAttributesManager().validatePassword(idmUser, password);
      }

      if (authenticated)
      {
         UserImpl userImpl = (UserImpl)user;
         userImpl.setLastLoginTime(Calendar.getInstance().getTime());
         saveUser(userImpl, false);
      }
      return authenticated;
   }

   public LazyPageList findUsers(Query q) throws Exception
   {

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

      return new LazyPageList(new IDMUserListAccess(this, service_, qb, 20, false), 20);
   }

   //
   public LazyPageList findUsersByGroup(String groupId) throws Exception
   {
      UserQueryBuilder qb = service_.getIdentitySession().createUserQueryBuilder();

      org.jboss.identity.idm.api.Group jbidGroup = orgService.getJBIDMGroup(groupId);

      qb.addRelatedGroup(jbidGroup);

      return new LazyPageList(new IDMUserListAccess(this, service_, qb, 20, false), 20);
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

      AttributesManager am = session.getAttributesManager();

      ArrayList attributes = new ArrayList();

      if (user.getCreatedDate() != null)
      {
         attributes.add(new SimpleAttribute(USER_CREATED_DATE, dateFormat.format(user.getCreatedDate())));
      }
      if (user.getLastLoginTime() != null)
      {
         attributes.add(new SimpleAttribute(USER_LAST_LOGIN_TIME, dateFormat.format(user.getLastLoginTime())));
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
         if (orgService.isPasswordAsAttribute())
         {
            attributes.add(new SimpleAttribute(USER_PASSWORD, user.getPassword()));
         }
         else
         {
            am.updatePassword(session.getPersistenceManager().findUser(user.getUserName()), user.getPassword());
         }
      }

      Attribute[] attrs = new Attribute[attributes.size()];
      attrs = (Attribute[])attributes.toArray(attrs);
      am.addAttributes(user.getUserName(), attrs);
   }

   public static User getPopulatedUser(String userName, IdentitySession session) throws Exception
   {

      if (session.getPersistenceManager().findUser(userName) == null)
      {
         return null;
      }

      AttributesManager am = session.getAttributesManager();

      Map<String, Attribute> attrs = am.getAttributes(userName);

      User user = new UserImpl(userName);

      if (attrs == null)
      {

         return user;
      }
      else
      {
         if (attrs.containsKey(USER_CREATED_DATE))
         {
            user.setCreatedDate(dateFormat.parse(attrs.get(USER_CREATED_DATE).getValue().toString()));
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
            user.setLastLoginTime(dateFormat.parse(attrs.get(USER_LAST_LOGIN_TIME).getValue().toString()));
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

      return user;

   }

}
