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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.picocontainer.Startable;

public class PicketLinkIDMOrganizationServiceImpl extends BaseOrganizationService implements Startable,
   ComponentRequestLifecycle
{

   // We may have several portal containers thus we need one PicketLinkIDMService per portal container
   //   private static PicketLinkIDMService jbidmService_;
   private PicketLinkIDMService idmService_;

   public static final String GTN_GROUP_TYPE_OPTION = "gtnGroupTypeName";

   public static final String GTN_ROOT_GROUP_NAME_OPTION = "gtnRootGroupName";

   public static final String GTN_ROOT_GROUP_TYPE_NAME_OPTION = "gtnRootGroupTypeName";

   public static final String PASSWORD_AS_ATTRIBUTE_OPTION = "passwordAsAttribute";

   private String gtnGroupType = "GTN_GROUP_TYPE";

   private String gtnRootGroupName = "GTN_ROOT_GROUP";

   private String gtnRootGroupType = gtnGroupType;

   private boolean passwordAsAttribute = false;

   public PicketLinkIDMOrganizationServiceImpl(InitParams params, CacheService cservice, PicketLinkIDMService idmService)
      throws Exception
   {
      groupDAO_ = new GroupDAOImpl(this, idmService);
      userDAO_ = new UserDAOImpl(this, idmService, cservice);
      userProfileDAO_ = new UserProfileDAOImpl(this, idmService, cservice);
      membershipDAO_ = new MembershipDAOImpl(this, idmService);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(this, idmService);

      idmService_ = idmService;

      if (params != null)
      {
         //Options
         ValueParam gtnGroupTypeNameParam = params.getValueParam(GTN_GROUP_TYPE_OPTION);
         ValueParam gtnRootGroupTypeNameParam = params.getValueParam(GTN_ROOT_GROUP_TYPE_NAME_OPTION);
         ValueParam gtnRootGroupNameParam = params.getValueParam(GTN_ROOT_GROUP_NAME_OPTION);
         ValueParam passwordAsAttributeParam = params.getValueParam(PASSWORD_AS_ATTRIBUTE_OPTION);

         if (gtnGroupTypeNameParam != null)
         {
            this.gtnGroupType = gtnGroupTypeNameParam.getValue();
         }

         if (gtnRootGroupNameParam != null)
         {
            this.gtnRootGroupName = gtnRootGroupNameParam.getValue();
         }

         if (gtnRootGroupTypeNameParam != null)
         {
            this.gtnRootGroupType = gtnRootGroupTypeNameParam.getValue();
         }
         else if (gtnRootGroupTypeNameParam != null)
         {
            this.gtnRootGroupType = this.gtnGroupType;
         }

         if (passwordAsAttributeParam != null && passwordAsAttributeParam.getValue().equalsIgnoreCase("true"))
         {
            this.passwordAsAttribute = true;
         }
      }

   }

   public final org.picketlink.idm.api.Group getJBIDMGroup(String groupId) throws Exception
   {
      String[] ids = groupId.split("/");
      String name = ids[ids.length - 1];
      return idmService_.getIdentitySession().getPersistenceManager().findGroup(name, getGtnGroupType());
   }

   @Override
   public void start()
   {

      try
      {
         // Wrap within transaction so all initializers can work
         idmService_.getIdentitySession().beginTransaction();
         super.start();
         idmService_.getIdentitySession().getTransaction().commit();

      }
      catch (Exception e)
      {
         e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
      }

   }

   @Override
   public void stop()
   {
      //toto
   }

   public void startRequest(ExoContainer container)
   {
      try
      {
         idmService_.getIdentitySession().beginTransaction();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public void endRequest(ExoContainer container)
   {
      try
      {
         idmService_.getIdentitySession().getTransaction().commit();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public String getGtnGroupType()
   {
      return gtnGroupType;
   }

   public String getExoRootGroupName()
   {
      return gtnRootGroupName;
   }

   public String getGtnRootGroupType()
   {
      return gtnRootGroupType;
   }

   public boolean isPasswordAsAttribute()
   {
      return passwordAsAttribute;
   }
}
