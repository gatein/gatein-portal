/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.web.redirect.api;

/**
 * Used to specify the type of redirect and the name of the redirect site.
 * 
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectKey
{  
   /**
    * The type of redirect
    */
   protected RedirectType type;
   
   /**
    * The name of the redirect site
    */
   protected String redirect;

   /**
    * 
    * Create a RedirectKey with a specified RedirectType and redirect site name 
    * 
    * @param type The redirect type
    * @param redirect The redirect site name
    */
   private RedirectKey(RedirectType type, String redirect)
   {
      this.type = type;
      this.redirect = redirect;
   }

   /**
    * Creates a NOREDIRECT redirect key
    * 
    * @return The RedirectKey to not perform a redirect
    */
   public static RedirectKey noRedirect()
   {
      return new RedirectKey(RedirectType.NOREDIRECT, null);
   }

   /**
    * Creates a REDIRECT key with a specified redirect site name
    * 
    * @param redirect The redirect site name
    * @return The redirect key
    */
   public static RedirectKey redirect(String redirect)
   {
      return new RedirectKey(RedirectType.REDIRECT, redirect);
   }

   /**
    * Creates a NEEDDEVICEINFO redirect key
    * 
    * @return A NEEDDEVICEINFO redirect key
    */
   public static RedirectKey needDeviceInfo()
   {
      return new RedirectKey(RedirectType.NEEDDEVICEINFO, null);
   }

   /**
    * Returns the name of the redirect site
    * 
    * @return the name of the redirect site
    */
   public String getRedirect()
   {
      return this.redirect;
   }

   /**
    * Returns the redirect type
    * 
    * @return The redirect type
    */
   public RedirectType getType()
   {
      return this.type;
   }
}

