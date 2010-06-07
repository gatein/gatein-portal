/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class TestMovedPOM extends AbstractPortalTest
{
   
   private DataStorage dataStorage;
   
   private POMSessionManager sessionManager;
   
   private POMSession session;
   
   public TestMovedPOM(String name)
   {
      super(name);
   }
   
   private final static Map<String, String> MOVE_CHILD_IN_PAGE_SCENARIOS;
   
   private final static Map<String, String> MOVE_CHILD_IN_PORTAL_SCENARIOS;
   
   /**
    * Moving child scenario 's parameters is a pair of source container and destination container
    */
   static{
      MOVE_CHILD_IN_PAGE_SCENARIOS = new HashMap<String, String>();      
      MOVE_CHILD_IN_PORTAL_SCENARIOS = new HashMap<String, String>();
   }
   
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      begin();
      PortalContainer container = getContainer();
      dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
      sessionManager = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      session = sessionManager.openSession();
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      session.close();
      end();
      super.tearDown();
   }
   
   public void testMoveChildInPage() throws Exception
   {
      Page page = dataStorage.getPage("portal::test::testMoveChild");
      assertNotNull(page);
      
      String testApplication = "application_test";
      String testContainer = "container_test";
      int scenarioIndex =0;
      
      for(String srcContainer : MOVE_CHILD_IN_PAGE_SCENARIOS.keySet())
      {
         String dstContainer = MOVE_CHILD_IN_PAGE_SCENARIOS.get(srcContainer);
         String scenarioName = "Test on page, scenario_" + scenarioIndex;
         
         executeMoveChildInPageScenario(scenarioName, page, srcContainer, dstContainer, testApplication);
         executeMoveChildInPageScenario(scenarioName, page, srcContainer, dstContainer, testContainer);
         
         scenarioIndex++;
      }
   }
   
   public void testMoveChildInPortal() throws Exception
   {
      PortalConfig portalConfig = dataStorage.getPortalConfig("test");
      assertNotNull(portalConfig);
      
      String testApplication = "site_layout_application_test";
      String testContainer = "site_layout_container_test";
      
      int scenarioIndex = 0;
      
      for(String srcContainer : MOVE_CHILD_IN_PORTAL_SCENARIOS.keySet())
      {
         String dstContainer = MOVE_CHILD_IN_PORTAL_SCENARIOS.get(srcContainer);
         String scenarioName = "Test on site layout, scenario_" + scenarioIndex;
         
         executeMoveChildInSiteLayoutScenario(scenarioName, portalConfig, srcContainer, dstContainer, testApplication);
         executeMoveChildInSiteLayoutScenario(scenarioName, portalConfig, srcContainer, dstContainer, testContainer);
         
         scenarioIndex++;
      }
   }
   
   public void executeMoveChildInSiteLayoutScenario(String scenarioName, PortalConfig portalConfig, String srcContainerName, String dstContainerName, String movedObjectName) throws Exception
   {
      
   }
   
   private void executeMoveChildInPageScenario(String scenarioName, Page page, String srcContainerName, String dstContainerName, String movedObjectName) throws Exception
   {
      Container srcContainer = findDescendant(page, Container.class, srcContainerName);
      Container dstContainer = findDescendant(page, Container.class, dstContainerName);
      ModelObject movedObject = findDescendant(page, ModelObject.class, movedObjectName);

      assertNotNull("Source container is null in " + scenarioName, srcContainer);
      assertNotNull("Destination container is null in " + scenarioName, dstContainer);
      assertNotNull("Moved object is null in " + scenarioName, movedObject);
      
      moveChild(srcContainer, dstContainer, movedObject);
      dataStorage.save(page);
      page = dataStorage.getPage(page.getStorageId());
      
      Container updatedSrcContainer = findDescendant(page, Container.class, srcContainerName);
      Container updatedDstContainer = findDescendant(page, Container.class, dstContainerName);
      ModelObject updatedMovedObject = findDescendant(page, ModelObject.class, movedObjectName);
      
      assertFalse("Source container still contains moved child in " + scenarioName, checkChildExistence(updatedSrcContainer, movedObjectName));
      assertTrue("Destination container does not contain moved child in " + scenarioName, checkChildExistence(updatedDstContainer, movedObjectName));
      
      //Backup the moving changes, needed to run consecutive and pairwise independent scenariosc
      moveChild(updatedDstContainer, updatedSrcContainer, updatedMovedObject);
      dataStorage.save(page);
      page = dataStorage.getPage(page.getStorageId());
      
   }
   
   private void moveChild(Container srcContainer, Container dstContainer, ModelObject modelObject) throws Exception
   {
      List<ModelObject> children = srcContainer.getChildren();
      boolean found = false;
      
      //Clone children list to avoid fail-fast iterator exception
      for(ModelObject child : new ArrayList<ModelObject>(children))
      {
         if(modelObject.getStorageName().equals(modelObject.getStorageName()))
         {
            found = true;
            children.remove(child);
            break;
         }
      }
      
      if(found)
      {
         dstContainer.getChildren().add(modelObject);
      }
   }
   
   private boolean checkChildExistence(Container srcContainer, String childName)
   {
      for(ModelObject child : srcContainer.getChildren())
      {
         if(childName.equals(child.getStorageName()))
         {
            return true;
         }
      }
      return false;
   }
   
   /**
    * Find descendant by name and type
    * 
    * @param <T>
    * @param rootContainer
    * @param clazz
    * @param name
    * @return
    */
   private <T> T findDescendant(Container rootContainer, Class<T> clazz, String name)
   {
      if(rootContainer.getStorageName().equals(name))
      {
         return (T)rootContainer;
      }
      
      for(ModelObject child : rootContainer.getChildren())
      {
         if(child.getStorageName().equals(name) && (clazz.isAssignableFrom(child.getClass())))
         {
            return (T)child;
         }
         else if(child instanceof Container)
         {
            T re = findDescendant((Container)child, clazz, name);
            if(re != null)
            {
               return re;
            }
         }
      }
      
      return null;
   }
}
