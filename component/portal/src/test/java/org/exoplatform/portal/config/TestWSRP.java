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

package org.exoplatform.portal.config;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestWSRP extends AbstractPortalTest
{

   /** . */
   private DataStorage storage_;

   /** . */
   private POMSessionManager mgr;

   /** . */
   private POMSession session;

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = getContainer();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = mgr.openSession();
   }

   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }

   public void testBilto() throws Exception
   {
      WSRP wsrp = new WSRP();
      String id = "portlet id";
      wsrp.setPortletId(id);
      TransientApplicationState<WSRP> state = new TransientApplicationState<WSRP>("test", wsrp);
      Application<WSRP> wsrpApplication = Application.createWSRPApplication();
      wsrpApplication.setState(state);

      Page container = new Page();
      String pageId = "portal::test::wsrp_page";
      container.setPageId(pageId);
      container.getChildren().add(wsrpApplication);

      storage_.create(container);

      container = storage_.getPage(pageId);
      wsrpApplication = (Application<WSRP>)container.getChildren().get(0);

      wsrp = storage_.load(wsrpApplication.getState(), ApplicationType.WSRP_PORTLET);
      assertNotNull(wsrp);
      assertEquals(id, wsrp.getPortletId());
   }
}
