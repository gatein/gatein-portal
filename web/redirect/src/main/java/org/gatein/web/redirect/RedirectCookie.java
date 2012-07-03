/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.web.redirect;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.portal.mop.SiteKey;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectType;

/**
 * Handles the creation and management of cookies which store the redirect preferences.
 * 
 * TODO:
 * - store the cookie values in a hash instead of a easily readable and modifiable format.
 * - include some sort of incremental identifier which can be used to invalidate older cookies.
 * - store multiple redirects within one cookie instead of a cookie per site redirect.
 * 
 * - Write tests for this class
 * 
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectCookie
{  
   private static final String COOKIE_PREFIX = "gtn.web";
   private static final String COOKIE_ORIGIN_PREFIX = COOKIE_PREFIX + ".origin.";
   private static final String COOKIE_REDIRECT_PREFIX = COOKIE_PREFIX + ".redirect.";
   private static final String COOKIE_NOREDIRECT_PREFIX = COOKIE_PREFIX + ".noredirect";
   
   protected String originSite;
   protected RedirectKey redirect;
   
   //Default to Integer.MAX_VALUE
   private int maxAge = Integer.MAX_VALUE;
   
   
   //Default cookie comment
   //TODO: i18n this ?
   private String comment = "Cookie to store site preference.";
   
   private String path;
   
   private Boolean secure;
   
   public RedirectCookie(String originSite, RedirectKey redirect)
   {
      if (originSite != null && redirect != null)
      {
         
         this.originSite = originSite;
         this.redirect = redirect;
      }
      else
      {
         throw new IllegalArgumentException("RedirectCookie requires that both the origin site [" + originSite + "] and the redirect site [" + redirect + "] be not null.");
      }
   }
   
   public void setMaxAge(int maxAge)
   {
      this.maxAge = maxAge;
   }
   
   public int getMaxAge()
   {
      return maxAge;
   }
   
   public void setComment(String comment)
   {
      this.comment = comment;
   }
   
   public String getComment()
   {
      return comment;
   }
   
   public void setPath(String path)
   {
      this.path = path;
   }
   
   public String getCookiePath()
   {
      return path;
   }
   
   public void setSecure(Boolean secure)
   {
      this.secure = secure;
   }
   
   public Boolean getSecure()
   {
      return secure;
   }
   
   public Cookie toCookie ()
   {     
         String originName = COOKIE_ORIGIN_PREFIX + originSite;
         
         String redirectValue;
         if (redirect.getType() == RedirectType.REDIRECT)
         {
            redirectValue = COOKIE_REDIRECT_PREFIX  + redirect.getRedirect();
         }
         else
         {
            redirectValue = COOKIE_NOREDIRECT_PREFIX;
         }
         
         Cookie cookie = new Cookie(originName, redirectValue);
         
         if (comment != null)
         {
            cookie.setComment(comment);
         }

         if (path != null)
         {
            cookie.setPath(path);
         }
         
         if (secure != null)
         {
            cookie.setSecure(secure);
         }
         
         cookie.setMaxAge(maxAge);

         return cookie;
   }
   
   public static RedirectKey getRedirect(SiteKey origin, HttpServletRequest request)
   {
      if (request.getCookies() != null)
      {
         String cookieName = COOKIE_ORIGIN_PREFIX + origin.getName();

         for (Cookie cookie : request.getCookies())
         {
            if (cookie.getName().equals(cookieName))
            {
               String cookieValue = cookie.getValue();
               if (cookieValue.startsWith(COOKIE_PREFIX))
               {
                  if (cookieValue.startsWith(COOKIE_REDIRECT_PREFIX))
                  {
                     String redirectSiteName = cookieValue.substring((COOKIE_REDIRECT_PREFIX).length());
                     if (redirectSiteName != null && !redirectSiteName.isEmpty())
                     {
                        return RedirectKey.redirect(redirectSiteName);
                     }
                  }
                  else
                  {
                     return RedirectKey.noRedirect();
                  }
               }
               break;
            }
         }
      }
      return null;
   }

}

