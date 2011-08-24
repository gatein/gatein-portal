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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.tasks.PreferencesTask;
import org.exoplatform.portal.pom.data.ModelChange;

import java.util.Comparator;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Apr 19, 2007
 * 
 * This interface is used to load the PortalConfig, Page config  and  Navigation config from the 
 * database
 */
public interface DataStorage
{
   public final static String PAGE_CREATED = "org.exoplatform.portal.config.DataStorage.pageCreated".intern();
   
   public final static String PAGE_REMOVED = "org.exoplatform.portal.config.DataStorage.pageRemoved".intern();

   public final static String PAGE_UPDATED = "org.exoplatform.portal.config.DataStorage.pageUpdated".intern();

   public final static String PORTAL_CONFIG_CREATED = "org.exoplatform.portal.config.DataStorage.portalConfigCreated".intern();

   public final static String PORTAL_CONFIG_REMOVED = "org.exoplatform.portal.config.DataStorage.portalConfigRemoved".intern();

   public final static String PORTAL_CONFIG_UPDATED = "org.exoplatform.portal.config.DataStorage.portalConfigUpdated".intern();
   
   /**
    * Create a PortalConfig in database <br/>
    * Then broadcast PORTAL_CONFIG_CREATED event
    * @param config
    */
   public void create(PortalConfig config) throws Exception;

   /**
    * This method should update the PortalConfig  object <br/>
    * Then broadcast PORTAL_CONFIG_UPDATED event
    * @param config 
    */
   public void save(PortalConfig config) throws Exception;

   /**
    * This method should load the PortalConfig object from db according to the portalName 
    * @param portalName
    */
   public PortalConfig getPortalConfig(String portalName) throws Exception;

   /**
    * This method should load the PortalConfig object from db according to the portalName and ownerType
    * @param portalName
    * @param ownerType
    */
   public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception;

   /**
    * Remove the PortalConfig from the database <br/>
    * Then broadcast PORTAL_CONFIG_REMOVED event 
    * @param config
    * @throws Exception
    */
   public void remove(PortalConfig config) throws Exception;

   /**
    * This method  should load the Page object from the database according to the pageId
    * @param pageId - String represent id of page, it must be valid pageId (3 parts saparate by :: )
    */
   public Page getPage(String pageId) throws Exception;

   /**
    * Clones a page.
    *
    * @param pageId the id of the page to clone
    * @param clonedOwnerType the target owner type of the clone
    * @param clonedOwnerId the target owner id of the clone
    * @param clonedName the target name of the clone
    * @return the cloned page
    * @throws Exception any exception
    */
   public Page clonePage(String pageId, String clonedOwnerType, String clonedOwnerId, String clonedName)
      throws Exception;

   /**
    * Remove Page from database <br />
    * Then broadcast PAGE_REMOVED event
    * @param page
    * @throws Exception
    */
   public void remove(Page page) throws Exception;

   /**
    * This method should create  or  udate the given page object <br />
    * Then broasdcast PAGE_CREATED event
    * @param page
    * @throws Exception
    */
   public void create(Page page) throws Exception;

   /**
    * Saves a page. If a page with the same id already exists then a merge operation will occur, otherwise
    * a new page will be created from the provided argument. <br />
    *
    * The operation returns a list of the change object that describes the changes that occured during the
    * save operation. <br/>
    *
    *Then broadcast PAGE_UPDATED event
    *
    * @param page the page to save
    * @return the list of model changes that occured during the save operation
    * @throws Exception any exception
    */
   public List<ModelChange> save(Page page) throws Exception;

   /**
    * Save PortletPreferences config node
    * @param portletPreferences - PortletPreferences object
    */
   public void save(PortletPreferences portletPreferences) throws Exception;

   /**
    * Return contentId according to each state (transient, persitent, clone)
    * @param state
    */
   public <S> String getId(ApplicationState<S> state) throws Exception;

   /**
    * Return content state. If can't find, return null
    * @see PreferencesTask
    * @param state - ApplicationState object
    * @param type - ApplicationType object
    */
   public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception;

   /**
    * Save content state <br />
    * @param state - ApplicationState object. It must be CloneApplicationState or PersistentApplicationState object
    * @param preferences - object to be saved
    */
   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception;

   /**
    * Return PortletPreferences from database, if can't find it, return null
    * @param windowID
    */
   public PortletPreferences getPortletPreferences(String windowID) throws Exception;

   /**
    * Return LazyPageList of object (unsorted) which type and other info determined in Query object
    * @param q - Query object
    */
   public <T> LazyPageList<T> find(Query<T> q) throws Exception;

   /**
    * Return LazyPageList of object (sorted) which type and other info determined in Query object
    * @param q - Query object
    */
   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception;

   /**
    * Return ListAccess, we can retrieved array of object (unsorted) in database through this. 
    * @param q - Query object
    */
   public <T> ListAccess<T> find2(Query<T> q) throws Exception;

   /**
    * Return ListAccess, we can retrieved array of object (sorted) in database through this. 
    * @param q - Query object
    * @param sortComparator - Comparator object, used to sort the result list
    */
   public <T> ListAccess<T> find2(Query<T> q, Comparator<T> sortComparator) throws Exception;

   /**
    * Return Container object - info that be used to build this Container is retrieved from /conf/portal/portal/sharedlayout.xml
    */
   public Container getSharedLayout() throws Exception;

   /**
    * Return Dashboard object from database according to dashboard id
    * If can't find out, return null
    * @param dashboardId
    */
   public Dashboard loadDashboard(String dashboardId) throws Exception;

   /**
    * Save Dashboard (its data : DashboadData) to database
    * @param dashboard - Dashboard object to be saved
    */
   public void saveDashboard(Dashboard dashboard) throws Exception;

   public void save() throws Exception;

   /**
    * Returns the list of all portal names.
    *
    * @return the portal names
    * @throws Exception any exception
    */
   public List<String> getAllPortalNames() throws Exception;
   
   public List<String> getAllGroupNames() throws Exception;

   /*************************************************************
     Public API to access/modify MOP mixin, temporarily put here
   **************************************************************/
   
   public <A> A adapt(ModelObject modelObject, Class<A> type);
   
   public <A> A adapt(ModelObject modelObject, Class<A> type, boolean create);
   
}