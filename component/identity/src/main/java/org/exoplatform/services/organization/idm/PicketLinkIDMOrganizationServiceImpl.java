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
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParam;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.picocontainer.Startable;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class PicketLinkIDMOrganizationServiceImpl extends BaseOrganizationService implements Startable,
   ComponentRequestLifecycle
{

   // We may have several portal containers thus we need one PicketLinkIDMService per portal container
   //   private static PicketLinkIDMService jbidmService_;
   private PicketLinkIDMServiceImpl idmService_;

   public static final String CONFIGURATION_OPTION = "configuration";

   private Config configuration = new Config();

   public PicketLinkIDMOrganizationServiceImpl(InitParams params, PicketLinkIDMService idmService)
      throws Exception
   {
      groupDAO_ = new GroupDAOImpl(this, idmService);
      userDAO_ = new UserDAOImpl(this, idmService);
      userProfileDAO_ = new UserProfileDAOImpl(this, idmService);
      membershipDAO_ = new MembershipDAOImpl(this, idmService);
      membershipTypeDAO_ = new MembershipTypeDAOImpl(this, idmService);

      idmService_ = (PicketLinkIDMServiceImpl)idmService;

      if (params != null)
      {
         //Options
         ObjectParameter configurationParam = params.getObjectParam(CONFIGURATION_OPTION);

         if (configurationParam != null)
         {
            this.configuration = (Config)configurationParam.getObject(); 
         }

      }

   }                             

   public final org.picketlink.idm.api.Group  getJBIDMGroup(String groupId) throws Exception
   {
      String[] ids = groupId.split("/");
      String name = ids[ids.length - 1];
      String parentId = null;
      if (groupId.contains("/"))
      {
         parentId = groupId.substring(0, groupId.lastIndexOf("/"));
      }

      String plGroupName = configuration.getPLIDMGroupName(name);

      return idmService_.getIdentitySession().getPersistenceManager().
         findGroup(plGroupName, getConfiguration().getGroupType(parentId));
   }

   @Override
   public void start()
   {

      try
      {

         RequestLifeCycle.begin(this);

         super.start();

      }
      catch (Exception e)
      {
         e.printStackTrace(); //To change body of catch statement use File | Settings | File Templates.
      }
      finally
      {
         RequestLifeCycle.end();
      }

   }

   @Override
   public void stop()
   {
      //toto
   }

/*
   */
/**
    * Used to allow nested requests (as done by the authenticator during unit tests) and avoid
    * to commit two times the same transaction.
    *//*

   private ThreadLocal<AtomicInteger> currentRequestCount = new ThreadLocal<AtomicInteger>()
   {
      @Override
      protected AtomicInteger initialValue()
      {
         return new AtomicInteger();
      }
   };
*/

   public void startRequest(ExoContainer container)
   {
      try
      {
         if (configuration.isUseJTA())
         {
            UserTransaction tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
            if (tx.getStatus() == Status.STATUS_NO_TRANSACTION)
            {
               tx.begin();
            }
         }
         else
         {

            if (!idmService_.getIdentitySession().getTransaction().isActive())
            {
               idmService_.getIdentitySession().beginTransaction();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }


   public void flush()
   {
      try
      {


         if (configuration.isUseJTA())
         {
            UserTransaction tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");

            if (tx.getStatus() != Status.STATUS_NO_TRANSACTION)
            {
               tx.commit();
            }

            if (tx.getStatus() == Status.STATUS_NO_TRANSACTION)
            {
               tx.begin();
            }
         }
         else
         {
            if (idmService_.getIdentitySession().getTransaction().isActive())
            {
               idmService_.getIdentitySession().save();
            }
         }

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
         if (configuration.isUseJTA())
         {
            UserTransaction tx = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
            if(tx.getStatus() == Status.STATUS_ACTIVE)
            {
               tx.commit();
            }
            
         }            
         else
         {
            if(idmService_.getIdentitySession().getTransaction().isActive())
            {
               idmService_.getIdentitySession().getTransaction().commit();
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }


   public Config getConfiguration()
   {
      return configuration;
   }

   public void setConfiguration(Config configuration)
   {
      this.configuration = configuration;
   }
}
