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

import java.util.Map;


/**
 * Provides a service to perform redirects based on device characteristics, determine the node mappings for these particular
 * redirects, and provide a means to determine what other alternative sites are available for a particular site.
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface SiteRedirectService {
    /**
     * Returns what action should be performed in terms of a redirect when a user access a portal site. The redirect is based on
     * the value of the User-Agent for the users browser and/or the any properties determined from the browser. User-Agent
     * string and deviceProperties are optional values and can pass a Null value.
     *
     * The returned RedirectKey can specify 3 possible situations based on its RedirectType: 1) RedirectType.REDIRECT: The user
     * should be redirected to the SiteKey specified. 2) RedirectType.NOREDIRECT: No redirect should be performed 3)
     * RedirectType.NEEDDEVICEINFO: A redirect cannot currently be determined, but could be determined based on device
     * properties if they were available
     *
     * @param origin The name for the original site requested.
     * @param userAgentString The value User-Agent for the current browser. Null if the User-Agent string is missing from the
     *        HTTP headers.
     * @param deviceProperties A map of device properties determined. Null if no deviceProperties determined.
     * @return The RedirectKey which specifies the redirect action to be taken.
     */
    RedirectKey getRedirectSite(String origin, String userAgentString, Map<String, String> deviceProperties);

    /**
     * Returns the redirectPath to use for the site redirection.
     *
     * If no redirection should be performed, this method will return null. For example, the service could be configured to not
     * redirect if there is no corresponding node on the redirect site.
     *
     * @param origin The name the original site requested
     * @param redirect The name of the redirect site
     * @param originRequestPath The requestPath of the original site requested
     * @return The redirectPath for the redirection, or Null if no redirect should be performed.
     */
    String getRedirectPath(String origin, String redirect, String originRequestPath);

    /**
     * Returns the list of alternative sites for the specified URL
     *
     * @param site The site
     * @return The list of alternative site names
     */
    Map<String, String> getAlternativeSites(String originSite);
}
