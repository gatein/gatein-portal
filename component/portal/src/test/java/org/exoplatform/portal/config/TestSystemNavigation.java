/*
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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestSystemNavigation extends AbstractPortalTest
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
      PortalContainer container = PortalContainer.getInstance();
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

   public void testWeirdBug() throws Exception
   {
      PageNavigation nav = storage_.getPageNavigation("portal::system");

      //
      for (Iterator<PageNode> i = nav.getNodes().iterator();i.hasNext();)
      {
         PageNode node = i.next();
         if (node.getVisibility() != Visibility.SYSTEM)
         {
            i.remove();
         }
      }

      //
      storage_.save(nav);

      end(true);
      begin();

      //
      nav = storage_.getPageNavigation("portal::system");

      //
      PageNode b = new PageNode();
      b.setName("b");
      b.setUri("b");
      b.setVisibility(Visibility.DISPLAYED);
      b.setLabel("b");

      //
      System.out.println("nav.getNodes() = " + nav.getNodes());

      //
      nav.getNodes().clear();
      nav.addNode(b);

      // Need to uncomment to make it fail for now
//      storage_.save(nav);
   }
}
