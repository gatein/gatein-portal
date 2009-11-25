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
import org.picketlink.idm.api.query.UserQuery;
import org.picketlink.idm.api.query.UserQueryBuilder;

import java.util.List;

public class IDMUserListAccess implements ListAccess<User>
{
   private final UserDAOImpl userDAO;

   private final PicketLinkIDMService idmService;

   private final UserQueryBuilder userQueryBuilder;

   private final int pageSize;

   private final boolean countAll;

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
      userQueryBuilder.page(index, length);
      UserQuery query = userQueryBuilder.createQuery();
      List<org.picketlink.idm.api.User> users = idmService.getIdentitySession().list(query);

      User[] exoUsers = new User[users.size()];

      for (int i = 0; i < users.size(); i++)
      {
         org.picketlink.idm.api.User user = users.get(i);

         exoUsers[i] = UserDAOImpl.getPopulatedUser(user.getId(), idmService.getIdentitySession());
      }

      return exoUsers;
   }

   public int getSize() throws Exception
   {
      if (countAll)
      {
         return idmService.getIdentitySession().getPersistenceManager().getUserCount();
      }
      else
      {
         userQueryBuilder.page(0, 0);
         UserQuery query = userQueryBuilder.createQuery();
         return idmService.getIdentitySession().execute(query).size();
      }

   }
}
