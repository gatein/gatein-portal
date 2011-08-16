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

package org.exoplatform.application.gadget;

import org.exoplatform.application.gadget.impl.GadgetDefinition;

import java.util.Comparator;
import java.util.List;

/**
 * This service is used to register the gadget with portal. Developer uses this
 * service to manage list of gadgets.
 * <p>
 * Created by The eXo Platform SAS<br/> Jun 18, 2008<br/>
 * </p>
 */
public interface GadgetRegistryService
{

   /**
    * Deploy a set of gadgets.
    *
    * @param gadgets the gadgets to deploy
    */
   public void deploy(Iterable<GadgetImporter> gadgets);

   /**
    * Return Gadget object retrieved from database by its name.
    * 
    * @param name the name of gadget
    * @return Gadget object or null if not found
    * @throws Exception
    */
   public Gadget getGadget(String name) throws Exception;

   /**
    * Gets all of available gadgets from the database.
    * 
    * @return a list of gadgets
    * @throws Exception
    */
   public List<Gadget> getAllGadgets() throws Exception;

   /**
    * Gets all of available gadgets from the database.</br> The list of gadgets
    * are sorted.
    * 
    * @param sortComparator The comparator is used to control the order of
    *          gadgets
    * @return a list of gadgets
    * @throws Exception
    */
   public List<Gadget> getAllGadgets(Comparator<Gadget> sortComparator) throws Exception;

   /**
    * Adds the gadget to the database. If the gadget is existing, it will be
    * updated.
    * 
    * @param gadget - Gadget that is saved to database, must not be null
    * @throws Exception
    */
   public void saveGadget(Gadget gadget) throws Exception;

   /**
    * Removes the gadget from the database.
    * If can't find Gadget with that name in database, this will throw Exception
    * @param name the name of gadget
    * @throws Exception
    */
   public void removeGadget(String name) throws Exception;

   /**
    * Always return true
    * @param username
    */
   public boolean isGadgetDeveloper(String username);

   /**
    * Return Country name, it's set in xml config of GadgetRegistryService
    */
   public String getCountry();

   public String getLanguage();

   public String getModuleId();

   public String getHostName();
   
   /**
    * Get the URL of gadget from gadget definition. There are 2 kind of gadget:
    * <p>
    * <ul>
    * <li>Local gadget: Gadget definition and resource are stored in JCR workspace. This gadget content can be also accessed by WebDAV
    * <li>Remote gadget: An absolute link to gadget definition
    * </ul>
    * 
    * @param gadgetName
    * @return link to local gadget definition stored in JCR or URL to remote gadget
    */
   public String getGadgetURL(String gadgetName);
}
