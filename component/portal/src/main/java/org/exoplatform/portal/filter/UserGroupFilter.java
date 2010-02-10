/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.portal.filter;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;


/*
 * This is mostly fork of the code from UserPortalConfigListener and GroupPortalConfigListener. If user was placed in
 * the identity store externally it will lazy create portal objects
 * 
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class UserGroupFilter extends AbstractFilter
{
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
   {

      HttpServletRequest httpRequest = (HttpServletRequest)request;
      ExoContainer container = getContainer();



      if (httpRequest.getRemoteUser() != null)
      {
         OrganizationService orgService = (OrganizationService)container.
            getComponentInstanceOfType(OrganizationService.class);
         UserPortalConfigService portalConfigService = (UserPortalConfigService)container.
            getComponentInstanceOfType(UserPortalConfigService.class);
         DataStorage dataStorage = (DataStorage)container.
            getComponentInstanceOfType(DataStorage.class);

         String userName = httpRequest.getRemoteUser();

         try
         {
            try
            {
               RequestLifeCycle.begin(PortalContainer.getInstance());
               checkUser(orgService, portalConfigService, dataStorage, userName);
               checkUserGroups(orgService, portalConfigService, dataStorage, userName);
            }
            catch (Exception e)
            {
               throw e;
            }
            finally
            {
               RequestLifeCycle.end();
            }
         }
         catch (Exception e)
         {
            System.out.println("Error while checking user portal config and navigations");
            e.printStackTrace();
         }



      }

      chain.doFilter(request, response);

   }

   public void checkUser(OrganizationService orgService,
                         UserPortalConfigService portalConfigService,
                         DataStorage dataStorage,
                         String userName) throws Exception
   {

      PortalConfig config = dataStorage.getPortalConfig(PortalConfig.USER_TYPE, userName);

      if (config == null)
      {
         System.out.println("Detected user without PortalConfig present: " + userName + "; lazy creation initiated.");

         // Create the portal from the template
         portalConfigService.createUserPortalConfig(PortalConfig.USER_TYPE, userName, "user");

         // Need to insert the corresponding user site if needed
         PortalConfig cfg = dataStorage.getPortalConfig(PortalConfig.USER_TYPE, userName);
         if (cfg == null)
         {
            cfg = new PortalConfig(PortalConfig.USER_TYPE);
            cfg.setPortalLayout(new Container());
            cfg.setName(userName);
            dataStorage.create(cfg);
         }
      }
      // Create a blank navigation if needed
      PageNavigation navigation = dataStorage.getPageNavigation(PortalConfig.USER_TYPE, userName);
      if (navigation == null)
      {
         PageNavigation pageNav = new PageNavigation();
         pageNav.setOwnerType(PortalConfig.USER_TYPE);
         pageNav.setOwnerId(userName);
         pageNav.setPriority(5);
         pageNav.setNodes(new ArrayList<PageNode>());
         portalConfigService.create(pageNav);
      }

   }

   public void checkUserGroups(OrganizationService orgService,
                               UserPortalConfigService portalConfigService,
                               DataStorage dataStorage,
                               String userName) throws Exception
   {

      Collection<Group> groups = orgService.getGroupHandler().findGroupsOfUser(userName);

      for (Group group : groups)
      {
         String groupId = group.getId();

         if (dataStorage.getPortalConfig(PortalConfig.GROUP_TYPE, groupId) == null)
         {
            System.out.println("Detected group without PortalConfig present: " + groupId + "; lazy creation initiated.");


            // Create the portal from the template
            portalConfigService.createUserPortalConfig(PortalConfig.GROUP_TYPE, groupId, "group");

            // Need to insert the corresponding group site
            PortalConfig cfg = dataStorage.getPortalConfig(PortalConfig.GROUP_TYPE, groupId);
            if (cfg == null)
            {
               cfg = new PortalConfig(PortalConfig.GROUP_TYPE);
               cfg.setPortalLayout(new Container());
               cfg.setName(groupId);
               dataStorage.create(cfg);
            }
         }
      }
   }




   public void destroy()
   {

   }
}
