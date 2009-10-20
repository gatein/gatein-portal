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
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.POMTask;

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

   public <T extends POMTask> T execute(T task) throws Exception;

   public void create(PortalConfig config) throws Exception;

   public void save(PortalConfig config) throws Exception;

   public PortalConfig getPortalConfig(String portalName) throws Exception;

   public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception;

   public void remove(PortalConfig config) throws Exception;

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

   public void remove(Page page) throws Exception;

   public void create(Page page) throws Exception;

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
   public List<ModelChange> save(Page page) throws Exception;

   public PageNavigation getPageNavigation(String fullId) throws Exception;

   public PageNavigation getPageNavigation(String ownerType, String id) throws Exception;

   public void save(PageNavigation navigation) throws Exception;

   public void create(PageNavigation navigation) throws Exception;

   public void remove(PageNavigation navigation) throws Exception;

   public void save(PortletPreferences portletPreferences) throws Exception;

   public <S> S load(ApplicationState<S> state) throws Exception;

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception;

   public PortletPreferences getPortletPreferences(String windowID) throws Exception;

   public <T> LazyPageList<T> find(Query<T> q) throws Exception;

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception;

   public Container getSharedLayout() throws Exception;
}