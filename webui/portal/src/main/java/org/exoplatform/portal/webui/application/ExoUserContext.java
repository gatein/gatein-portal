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

package org.exoplatform.portal.webui.application;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.organization.UserProfile;
import org.gatein.pc.api.invocation.resolver.AttributeResolver;
import org.gatein.pc.api.invocation.resolver.PrincipalAttributeResolver;
import org.gatein.pc.api.spi.UserContext;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision$
 */
//TODO: needs to be tested
class ExoUserContext implements UserContext
{
   /** . */
   private static final Map<String, String> EMPTY_STRING_TO_STRING_MAP = Collections.emptyMap();

   /** . */
   private static final List<Locale> EMPTY_LOCALE_LIST = Collections.emptyList();

   /** . */
   private final String id;

   /** . */
   private final HttpServletRequest clientRequest;

   /** . */
   private final AttributeResolver attributeResolver;

   /** . */
   private final UserProfile userProfile;

   /** Names from PortletRequest.P3PUserInfos */
   private static final Set<String> JSR_168_P3P;

   static
   {
      Set<String> keys = new HashSet<String>();

      for (PortletRequest.P3PUserInfos userInfos : PortletRequest.P3PUserInfos.values())
      {
         keys.add(userInfos.toString());
      }

      JSR_168_P3P = Collections.unmodifiableSet(keys);
   }

   public ExoUserContext(HttpServletRequest clientRequest, UserProfile userProfile) throws IllegalArgumentException
   {
      if (clientRequest == null)
      {
         throw new IllegalArgumentException("No client request provided");
      }
      this.id = clientRequest.getRemoteUser();
      this.clientRequest = clientRequest;
      this.attributeResolver = new PrincipalAttributeResolver(clientRequest);
      this.userProfile = userProfile;
   }

   /** Returns the user id or null if none was provided. */
   public String getId()
   {
      return id;
   }

   /** Returns an immutable empty map. */
   public Map<String, String> getInformations()
   {

      if (userProfile == null)
      {
         return EMPTY_STRING_TO_STRING_MAP;
      }

      Map<String, String> infoMap = userProfile.getUserInfoMap();
      Map<String, String> filteredMap = new HashMap<String, String>();

      for (String key : infoMap.keySet())
      {
         if (JSR_168_P3P.contains(key))
         {
            filteredMap.put(key, infoMap.get(key));
         }
      }

      return Collections.unmodifiableMap(filteredMap);
   }

   /**
    * Returns current PortalRequestContext's locale. It falls back to
    * clientRequest locale ({@link HttpServletRequest#getLocale}), or <code>Locale.ENGLISH</code>
    * if clientRequest object is not available.
    */
   public Locale getLocale()
   {
      PortalRequestContext context = PortalRequestContext.getCurrentInstance();

      if(context != null) {
         Locale loc = context.getLocale();
         if (loc != null)
            return loc;
      }

      return clientRequest != null ? clientRequest.getLocale() : Locale.ENGLISH;
   }

   /**
    * Returns the list of client request locales, making sure the first one in the List
    * is the same as what getLocale() method returns.
    */ 
   @SuppressWarnings("unchecked")
   public List<Locale> getLocales()
   {
      Locale loc = getLocale();
      if (loc == null)
      {
         return EMPTY_LOCALE_LIST;
      }
      else
      {
         LinkedList<Locale> locs = new LinkedList<Locale>();
         locs.add(loc);
         if (clientRequest != null)
         {
            Enumeration<Locale> clientLocs = (Enumeration<Locale>) clientRequest.getLocales();
            while (clientLocs.hasMoreElements())
            {
               Locale current = clientLocs.nextElement();
               if (current.getLanguage().equals(loc.getLanguage()) && current.getCountry().equals(loc.getCountry()))
                  continue;
               locs.add(current);
            }
         }
         return Collections.unmodifiableList(locs);
      }
   }

   public void setAttribute(String attrKey, Object attrValue)
   {
      attributeResolver.setAttribute(attrKey, attrValue);
   }

   public Object getAttribute(String attrKey)
   {
      return attributeResolver.getAttribute(attrKey);
   }
}
