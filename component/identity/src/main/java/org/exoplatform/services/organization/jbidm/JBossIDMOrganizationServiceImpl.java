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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.picocontainer.Startable;

public class JBossIDMOrganizationServiceImpl extends BaseOrganizationService implements Startable,
   ComponentRequestLifecycle
{

   // We may have several portal containers thus we need one JBossIDMService per portal container   
   //   private static JBossIDMService jbidmService_;
   private JBossIDMService jbidmService_;

   public static final String EXO_GROUP_TYPE_OPTION = "exoGroupTypeName";

   public static final String EXO_ROOT_GROUP_NAME_OPTION = "exoRootGroupName";

   public static final String EXO_ROOT_GROUP_TYPE_NAME_OPTION = "exoRootGroupTypeName";

   public static final String PASSWORD_AS_ATTRIBUTE_OPTION = "passwordAsAttribute";

   private String exoGroupType = "EXO_GROUP_TYPE";

   private String exoRootGroupName = "EXO_ROOT_GROUP";

   private String exoRootGroupType = exoGroupType;

   private boolean passwordAsAttribute = false;

   public JBossIDMOrganizationServiceImpl(InitParams params, CacheService cservice, JBossIDMService jbidmService)
      throws Exception
   {
      groupDAO_ = new GroupDAOImpl(this, jbidmService);
      userDAO_ = new UserDAOImpl(this, jbidmService, cservice);
      userProfileDAO_ = new UserProfileDAOImpl(this, jbidmService, cservice);
      membershipDAO_ = new MembershipDAOImpl(this, jbidmService);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(this, jbidmService);

      jbidmService_ = jbidmService;

      if (params != null)
      {
         //Options
         ValueParam exoGroupTypeNameParam = params.getValueParam(EXO_GROUP_TYPE_OPTION);
         ValueParam exoRootGroupTypeNameParam = params.getValueParam(EXO_ROOT_GROUP_TYPE_NAME_OPTION);
         ValueParam exoRootGroupNameParam = params.getValueParam(EXO_ROOT_GROUP_NAME_OPTION);
         ValueParam passwordAsAttributeParam = params.getValueParam(PASSWORD_AS_ATTRIBUTE_OPTION);

         if (exoGroupTypeNameParam != null)
         {
            this.exoGroupType = exoGroupTypeNameParam.getValue();
         }

         if (exoRootGroupNameParam != null)
         {
            this.exoRootGroupName = exoRootGroupNameParam.getValue();
         }

         if (exoRootGroupTypeNameParam != null)
         {
            this.exoRootGroupType = exoRootGroupTypeNameParam.getValue();
         }
         else if (exoRootGroupTypeNameParam != null)
         {
            this.exoRootGroupType = this.exoGroupType;
         }

         if (passwordAsAttributeParam != null && passwordAsAttributeParam.getValue().equalsIgnoreCase("true"))
         {
            this.passwordAsAttribute = true;
         }
      }

   }

   public final org.jboss.identity.idm.api.Group getJBIDMGroup(String groupId) throws Exception
   {
      String[] ids = groupId.split("/");
      String name = ids[ids.length - 1];
      return jbidmService_.getIdentitySession().getPersistenceManager().findGroup(name, getExoGroupType());
   }

   @Override
   public void start()
   {

      try
      {
         // Wrap within transaction so all initializers can work
         jbidmService_.getIdentitySession().beginTransaction();
         super.start();
         jbidmService_.getIdentitySession().getTransaction().commit();

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
         jbidmService_.getIdentitySession().beginTransaction();
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
         jbidmService_.getIdentitySession().getTransaction().commit();
      }
      catch (Exception e)
      {
         e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
      }
   }

   public String getExoGroupType()
   {
      return exoGroupType;
   }

   public String getExoRootGroupName()
   {
      return exoRootGroupName;
   }

   public String getExoRootGroupType()
   {
      return exoRootGroupType;
   }

   public boolean isPasswordAsAttribute()
   {
      return passwordAsAttribute;
   }
}
