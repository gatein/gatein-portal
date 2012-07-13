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
package org.gatein.web.redirect.api;

import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface RedirectHandler
{
   /**
    * Returns a map of the names of the redirect sites and a corresponding URL to use to perform a switch to the
    * alternative site.
    * 
    * If persist is set to true, on subsequent access to the site, the redirect will be automatically performed.
    * If persist is set to false, no redirect will occur when the user access the site on subsequent requests. 
    * 
    * @param URI The URI of the current page
    * @param persist True if we want to persist the site preference
    * @return A map containing the name of the alternative site and the url to use to redirect to that site
    */
   public Map<String, String> getAlternativeRedirects(String siteName, String URI, boolean persist);
}

