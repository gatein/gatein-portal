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
import org.gatein.mop.api.content.ContentType;

import java.util.Comparator;
import java.util.List;

/** Created y the eXo platform team User: Tuan Nguyen Date: 20 april 2007 */
public interface ApplicationRegistryService
{
   String REMOTE_DISPLAY_NAME_SUFFIX = " (remote)";

   /**
    * Return list of ApplicationCatgory (and applications in each category)
    *
    * @param accessUser
    * @param appTypes   - array of ApplicationType, used to filter applications in each application category
    */
   public List<ApplicationCategory> getApplicationCategories(String accessUser, ApplicationType<?>... appTypes) throws Exception;

   public void initListener(ComponentPlugin com) throws Exception;

   /** Return list of all current application categories (unsorted, all Application in all ApplicationType) */
   public List<ApplicationCategory> getApplicationCategories() throws Exception;

   /**
    * Return list of all current application categories (sorted, all applications in all types)
    *
    * @param sortComparator - Comparator used to sort the returned list
    */
   public List<ApplicationCategory> getApplicationCategories(Comparator<ApplicationCategory> sortComparator)
      throws Exception;

   /**
    * Return ApplicationCategory with name provided <br/>
    * if not found, return null
    *
    * @param name - ApplicationCategory's name
    */
   public ApplicationCategory getApplicationCategory(String name) throws Exception;

   /**
    * Save an ApplicationCategory to database <br/>
    * If it doesn't exist, a new one will be created, if not, it will be updated
    *
    * @param category - ApplicationCategory object that will be saved
    */
   public void save(ApplicationCategory category) throws Exception;

   /**
    * Remove application category (and all application in it) from database <br/>
    * If it doesn't exist, it will be ignored
    *
    * @param category - ApplicationCategory object that will be removed
    */
   public void remove(ApplicationCategory category) throws Exception;

   /**
    * Return list of applications (unsorted) in specific category and have specific type
    *
    * @param category - ApplicationCategory that you want to list applications
    * @param appTypes - array of application type
    */
   public List<Application> getApplications(ApplicationCategory category, ApplicationType<?>... appTypes) throws Exception;

   /**
    * Return list of applications (sorted) in specific category and have specific type
    *
    * @param category       - ApplicationCategory that you want to list applications
    * @param sortComparator - comparator used to sort application list
    * @param appTypes       - array of application type
    */
   public List<Application> getApplications(ApplicationCategory category, Comparator<Application> sortComparator,
                                            ApplicationType<?>... appTypes) throws Exception;

   /**
    * Return list of all Application in database (unsorted) <br/>
    * If there are not any Application in database, return an empty list
    */
   public List<Application> getAllApplications() throws Exception;

   /**
    * Return Application with id provided
    *
    * @param id - must be valid applicationId (catgoryname/applicationName), if not, this will throw exception
    */
   public Application getApplication(String id) throws Exception;

   /**
    * Return Application in specific category and have name provided in param <br/>
    * If it can't be found, return null
    *
    * @param category - name of application category
    * @param name     - name of application
    */
   public Application getApplication(String category, String name) throws Exception;

   /**
    * Save Application in an ApplicationCategory <br/>
    * If ApplicationCategory or Application don't exist, they'll be created <br/>
    * If Application has been already existed, it will be updated <br/>
    *
    * @param category    - ApplicationCategory that your application'll be saved to
    * @param application - Application that will be saved
    */
   public void save(ApplicationCategory category, Application application) throws Exception;

   /**
    * Update an Application <br/>
    * It must be existed in database, if not, this will throw an IllegalStateException
    *
    * @param application - Application that you want to update
    */
   public void update(Application application) throws Exception;

   /**
    * Remove an Application from database <br/>
    * If it can't be found, it will be ignored (no exception)
    *
    * @param app - Application that you want to remove, must not be null
    */
   public void remove(Application app) throws Exception;

   /**
    * Get all deployed portlet, add to portlet's ApplicationCategory <br/>
    * If ApplicationCategory currently doesn't exist, it'll be created  <br/>
    * If Application've already existed, it'll be ignored
    */
   public void importAllPortlets() throws Exception;

   //TODO: dang.tung

   /**
    * Get all Gadget, add to eXoGadgets application category <br/>
    * When first added, it's access permission will be Everyone <br/>
    * If ApplicationCategory currently doesn't exist, it'll be created <br/>
    * Gadget that has been imported will be ignored
    */
   public void importExoGadgets() throws Exception;

   Application createOrUpdateApplication(String categoryName, String definitionName, ContentType<?> contentType, String contentId, String displayName, String description, List<String> permissions);

   Application createApplicationFrom(org.gatein.pc.api.Portlet portlet);
}