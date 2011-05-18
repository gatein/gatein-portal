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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

import org.gatein.common.logging.LogLevel;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picketlink.idm.api.SortOrder;
import org.picketlink.idm.api.query.UserQuery;
import org.picketlink.idm.api.query.UserQueryBuilder;


import java.io.Serializable;
import java.util.List;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class IDMUserListAccess implements ListAccess<User>, Serializable
{
   private static Logger log = LoggerFactory.getLogger(IDMUserListAccess.class);

   private final UserDAOImpl userDAO;

   private final PicketLinkIDMService idmService;

   private final UserQueryBuilder userQueryBuilder;

   private final int pageSize;

   private final boolean countAll;

   private List<org.picketlink.idm.api.User> fullResults;

   private int size = -1;

   public IDMUserListAccess(UserDAOImpl userDAO, PicketLinkIDMService idmService, UserQueryBuilder userQueryBuilder,
      int pageSize, boolean countAll)
   {
      this.userDAO = userDAO;
      this.idmService = idmService;
      this.userQueryBuilder = userQueryBuilder;
      this.pageSize = pageSize;
      this.countAll = countAll;
   }

   public User[] load(int index, int length) throws Exception, IllegalArgumentException
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "load",
            new Object[]{
               "index", index,
               "length", length
            }
         );
      }

      List<org.picketlink.idm.api.User> users = null;

      if (fullResults == null)
      {
         userDAO.getOrgService().commitTransaction();

         userQueryBuilder.page(index, length);
         UserQuery query = userQueryBuilder.sort(SortOrder.ASCENDING).createQuery();
         users = idmService.getIdentitySession().list(query);
      }
      else
      {
         users = fullResults.subList(index, index + length);
      }

      User[] exoUsers = new User[users.size()];

      for (int i = 0; i < users.size(); i++)
      {
         org.picketlink.idm.api.User user = users.get(i);

         User gtnUser = new UserImpl(user.getId());
         userDAO.populateUser(gtnUser, idmService.getIdentitySession());
         exoUsers[i] = gtnUser;
      }

      if (log.isTraceEnabled())
      {
        Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "load",
            exoUsers
         );
      }

      return exoUsers;
   }

   public int getSize() throws Exception
   {
      if (log.isTraceEnabled())
      {
         Tools.logMethodIn(
            log,
            LogLevel.TRACE,
            "getSize",
            null
         );
      }

      userDAO.getOrgService().commitTransaction();

      int result;

      if (size < 0)
      {

         if (fullResults != null)
         {
            result = fullResults.size();
         }
         else if (countAll)
         {
            result = idmService.getIdentitySession().getPersistenceManager().getUserCount();
         }
         else
         {
            userQueryBuilder.page(0, 0);
            UserQuery query = userQueryBuilder.sort(SortOrder.ASCENDING).createQuery();
            fullResults = idmService.getIdentitySession().list(query);
            result = fullResults.size();
         }

         size = result;
      }
      else
      {
         result = size;
      }

      if (log.isTraceEnabled())
      {
         Tools.logMethodOut(
            log,
            LogLevel.TRACE,
            "getSize",
            result
         );
      }

      return result;

   }
}
