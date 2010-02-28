/*
* JBoss, a division of Red Hat
* Copyright 2009, Red Hat Middleware, LLC, and individual contributors as indicated
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


package org.exoplatform.component.test.organization;

import org.exoplatform.component.test.*;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.database.HibernateService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.UserImpl;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.organization-configuration.xml")})
public class BootstrapTestCase extends AbstractKernelTest
{

   public void testWorkspace() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      HibernateService hibernate = (HibernateService)container.getComponentInstanceOfType(HibernateService.class);
      assertNotNull(hibernate);
      OrganizationService organization = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      assertNotNull(organization);
   }

   public void testBasicOperation() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      OrganizationService organization = (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      assertNotNull(organization);

      begin();
      User test = new UserImpl("testUser");
      organization.getUserHandler().createUser(test, false);

      test = organization.getUserHandler().findUserByName("toto");
      assertNull(test);
      test = organization.getUserHandler().findUserByName("testUser");
      assertNotNull(test);
      end();
   }

}