/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.description;

import org.exoplatform.portal.mop.Described;

import java.util.Locale;
import java.util.Map;

/**
 * The description service provides configuration and runtime interaction of described objects.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface DescriptionService
{

   /**
    * <p>Resolve a description with the <code>locale</code> argument.</p>
    *
    * @param id the object id
    * @param locale the locale to resolve
    * @return the description
    * @throws NullPointerException if the <code>id</code> or the <code>locale</code> argument is null
    */
   Described.State resolveDescription(String id, Locale locale) throws NullPointerException;

   /**
    * <p>Resolve a description, the <code>locale1</code> argument specifies which locale is relevant for retrieval,
    * the <code>locale2</code> specifies which locale should be defaulted to when the <code>locale1</code>
    * cannot provide any relevant match. The <code>locale2</code> argument is optional.</p>
    *
    * <p>The resolution follows those rules:
    * <ul>
    *    <li>The resolution is performed against the locale1.</li>
    *    <li>When the locale1 does not resolve and a locale2 is provided, a resolution is performed with locale2.</li>
    *    <li>Otherwise null is returned.<li>
    * </ul>
    * </p>
    *
    * @param id the object id
    * @param locale2 the first locale
    * @param locale1 the second locale
    * @return the description
    * @throws NullPointerException if the <code>id</code> or the <code>locale1</code> argument is null
    */
   Described.State resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException;

   /**
    * Returns the default description or null if it does not exist.
    *
    * @param id the object id
    * @return the description
    * @throws NullPointerException if the id argument is null
    */
   Described.State getDescription(String id) throws NullPointerException;

   /**
    * Update the default description to the new description or remove it if the description argument is null.
    *
    * @param id the object id
    * @param description the new description
    * @throws NullPointerException if the id argument is null
    */
   void setDescription(String id, Described.State description) throws NullPointerException;

   /**
    * Returns a description for the specified locale argument or null if it does not exist.
    *
    * @param id the object id
    * @param locale the locale
    * @return the description
    * @throws NullPointerException if the id or locale argument is null
    */
   Described.State getDescription(String id, Locale locale) throws NullPointerException;

   /**
    * Update the description for the specified locale to the new description or remove it if the description
    * argument is null.
    *
    * @param id the object id
    * @param locale the locale
    * @param description the new description
    * @throws NullPointerException if the id or locale argument is null
    * @throws IllegalArgumentException if the locale is not valid
    */
   void setDescription(String id, Locale locale, Described.State description) throws NullPointerException, IllegalArgumentException;

   /**
    * Returns a map containing all the descriptions of an object or null if the object is not internationalized.
    *
    * @param id the object id
    * @return the map the description map
    * @throws NullPointerException if the id is null
    */
   Map<Locale, Described.State> getDescriptions(String id) throws NullPointerException;

   /**
    * Updates the description of the specified object or remove the internationalized characteristic of
    * the object if the description map is null.
    *
    * @param id the object id
    * @param descriptions the new descriptions
    * @throws NullPointerException if the id is null
    * @throws IllegalArgumentException if the map contains an invalid locale
    */
   void setDescriptions(String id, Map<Locale, Described.State> descriptions) throws NullPointerException, IllegalArgumentException;

}
