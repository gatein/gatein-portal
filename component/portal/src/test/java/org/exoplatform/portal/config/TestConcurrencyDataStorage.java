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

import java.util.concurrent.CountDownLatch;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * Nov 10, 2010
 */

public class TestConcurrencyDataStorage extends AbstractPortalTest
{

   private DataStorage storage_;

   private POMSessionManager mgr;

   public TestConcurrencyDataStorage(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = PortalContainer.getInstance();
      storage_ = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      
   }
   
   public void testCreatePageConcurrently() throws Exception
   {
      CountDownLatch startSignal = new CountDownLatch(1);
      CountDownLatch stopSignal = new CountDownLatch(5);

      for (int i = 0; i < 5; i++)
      {
         Thread thread = new Thread(new CreatePageTask(mgr, storage_, startSignal, stopSignal, "test" + i, "foo" + i));
         thread.start();
      }
      
      startSignal.countDown();
      stopSignal.await();
   }
   
   public void testCreatePageSequentially() throws Exception
   {
      for (int i = 5; i < 10; i++)
      {
         CountDownLatch stopSignal = new CountDownLatch(1);
         Thread thread = new Thread(new CreatePageTask(mgr, storage_, null, stopSignal, "test" + i, "foo" + i));
         thread.start();
         stopSignal.await();
      }
   }

   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }
   
   public class CreatePageTask implements Runnable
   {
      private DataStorage dataStorage;
      
      private POMSessionManager sessionManager;
      
      private String pageName;
      
      private String pageTitle;
      
      private CountDownLatch startSignal;

      private final CountDownLatch stopSignal;
      
      public CreatePageTask(POMSessionManager _sessionManager, DataStorage _dataStorage, CountDownLatch _startSignal, CountDownLatch stopSignal, String _pageName, String _pageTitle)
      {
         dataStorage = _dataStorage;
         pageName = _pageName;
         pageTitle = _pageTitle;
         sessionManager = _sessionManager;
         startSignal = _startSignal;
         this.stopSignal = stopSignal;
      }
      
      public void run()
      {
         try
         {
            if(startSignal != null)
               startSignal.await();

            sessionManager.openSession();

            Page page = new Page();
            page.setOwnerType(PortalConfig.PORTAL_TYPE);
            page.setOwnerId("test");
            page.setTitle(pageTitle);
            page.setName(pageName);

            dataStorage.create(page);

            Page createdPage = dataStorage.getPage(page.getPageId());
            assertNotNull(createdPage);
            assertEquals(pageName, createdPage.getName());
            assertEquals(pageTitle, createdPage.getTitle());

            System.out.println("Current POMSession: " + sessionManager.getSession().toString());
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
            System.out.println("Could not create the page: " + pageName + " , " + pageTitle);
         }
         finally
         {
            stopSignal.countDown();
         }
      }
   }

}
