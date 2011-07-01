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

package org.exoplatform.portal.pom.data;

import java.util.Comparator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PortalData;

/**
 * Created by The eXo Platform SAS
 * Apr 19, 2007
 *
 * This interface is used to load the PortalConfig, Page config  and  Navigation config from the
 * database
 */
public interface ModelDataStorage
{

   public void create(PortalData config) throws Exception;

   public void save(PortalData config) throws Exception;

   public PortalData getPortalConfig(PortalKey key) throws Exception;

   public void remove(PortalData config) throws Exception;

   public PageData getPage(PageKey key) throws Exception;

   /**
    * Clones a page.
    *
    * @param key the key of the page to clone
    * @param cloneKey the key of the clone
    * @return the cloned page
    * @throws Exception any exception
    */
   public PageData clonePage(PageKey key, PageKey cloneKey)
      throws Exception;

   public void remove(PageData page) throws Exception;

   public void create(PageData page) throws Exception;

   /**
    * Saves a page. If a page with the same id already exists then a merge operation will occur, otherwise
    * a new page will be created from the provided argument.
    *
    * The operation returns a list of the change object that describes the changes that occured during the
    * save operation.
    *
    * @param page the page to save
    * @return the list of model changes that occured during the save operation
    * @throws Exception any exception
    */
   public List<ModelChange> save(PageData page) throws Exception;


   public void save(PortletPreferences portletPreferences) throws Exception;

   public <S> String getId(ApplicationState<S> state) throws Exception;

   public <S> S load(ApplicationState<S> state, ApplicationType<S> type) throws Exception;

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception;

   public PortletPreferences getPortletPreferences(String windowID) throws Exception;

   public <T> LazyPageList<T> find(Query<T> q) throws Exception;

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception;

   public Container getSharedLayout() throws Exception;

   public DashboardData loadDashboard(String dashboardId) throws Exception;

   public void saveDashboard(DashboardData dashboard) throws Exception;

   public void save() throws Exception;
   
   /****************************************************************
    * Proxy methods of public API to access/modify MOP mixins, 
    * 
    * temporarily put here
    ***************************************************************/
   public <A> A adapt(ModelData modelData, Class<A> type);
   
   public <A> A adapt(ModelData modelData, Class<A> type, boolean create);
   
}