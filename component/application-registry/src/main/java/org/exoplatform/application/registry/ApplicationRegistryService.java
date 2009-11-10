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

package org.exoplatform.application.registry;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.config.model.ApplicationType;

import java.util.Comparator;
import java.util.List;

/**
 * Created y the eXo platform team
 * User: Tuan Nguyen
 * Date: 20 april 2007
 */
public interface ApplicationRegistryService
{

   public List<ApplicationCategory> getApplicationCategories(String accessUser, ApplicationType<?>... appTypes) throws Exception;

   public void initListener(ComponentPlugin com) throws Exception;

   public List<ApplicationCategory> getApplicationCategories() throws Exception;

   public List<ApplicationCategory> getApplicationCategories(Comparator<ApplicationCategory> sortComparator)
      throws Exception;

   public ApplicationCategory getApplicationCategory(String name) throws Exception;

   public void save(ApplicationCategory category) throws Exception;

   public void remove(ApplicationCategory category) throws Exception;

   public List<Application> getApplications(ApplicationCategory category, ApplicationType<?>... appTypes) throws Exception;

   public List<Application> getApplications(ApplicationCategory category, Comparator<Application> sortComparator,
      ApplicationType<?>... appTypes) throws Exception;

   public List<Application> getAllApplications() throws Exception;

   public Application getApplication(String id) throws Exception;

   public Application getApplication(String category, String name) throws Exception;

   public void save(ApplicationCategory category, Application application) throws Exception;

   public void update(Application application) throws Exception;

   public void remove(Application app) throws Exception;

   public void importAllPortlets() throws Exception;

   //TODO: dang.tung
   public void importExoGadgets() throws Exception;
}