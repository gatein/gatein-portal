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

package org.exoplatform.portal.pom.spi.gadget;

import org.gatein.mop.api.content.ContentType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Gadget implements Serializable
{

   /** . */
   public static final ContentType<Gadget> CONTENT_TYPE = new ContentType<Gadget>("application/gadget", Gadget.class);

   /** . */
   private String userPref;

   public String getUserPref()
   {
      return userPref;
   }

   public void setUserPref(String userPref)
   {
      this.userPref = userPref;
   }

   public void addUserPref(String addedUserPref) throws JSONException
   {
      JSONObject jsonUserPref = new JSONObject(userPref != null ? userPref : "{}");

      // Update the user Preferences with the new value. Replace the old ones if they exist.
      JSONObject addedJSONUserPref = new JSONObject(addedUserPref);
      for (Iterator<String> i = addedJSONUserPref.keys();i.hasNext();)
      {
         String key = i.next();
         jsonUserPref.put(key, addedJSONUserPref.get(key));
      }

      //
      userPref = jsonUserPref.toString();
   }
}
