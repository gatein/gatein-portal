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
package org.gatein.web.redirect.implementation;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.redirect.Mapper;
import org.gatein.web.redirect.Redirector;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.SiteRedirectService;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class SiteRedirectServiceImpl implements SiteRedirectService, Startable {
    protected static Logger log = LoggerFactory.getLogger(SiteRedirectServiceImpl.class);

    // Handles which site to redirect to
    Redirector redirector;

    // Handles node mappings between sites
    Mapper mapper;

    // Used for retrieving the stored portal configuration
    DataStorage dataStorage;

    public SiteRedirectServiceImpl(DataStorage dataStorage, NavigationService navService) throws IOException {
        this.dataStorage = dataStorage;
        this.redirector = new Redirector();
        this.mapper = new Mapper(navService);
    }

    @Override
    public RedirectKey getRedirectSite(String origin, String userAgentString, Map<String, String> deviceProperties) {
        try {
            if (redirector != null && dataStorage != null) {
                PortalConfig pConfig = dataStorage.getPortalConfig(origin);
                if (pConfig != null) {
                    return redirector.getRedirectSite(pConfig.getPortalRedirects(), userAgentString, deviceProperties);
                } else {
                    log.warn("No PortalConfig found for site : " + origin + ". Site redirection cannot be peformed.");
                    return null;
                }
            } else {
                log.warn("Redirector(" + redirector + ") and DataStorage(" + dataStorage
                        + ") need to be both be set. Site redirection cannot be performed.");
                return null;
            }
        } catch (Exception e) {
            log.error("Error trying to get site redirect.", e);
            return null;
        }
    }

    @Override
    public String getRedirectPath(String origin, String redirect, String originRequestPath) {
        try {
            if (mapper != null) {
                if (originRequestPath == null || originRequestPath.isEmpty()) {
                    originRequestPath = "/";
                }
                PortalConfig pConfig = dataStorage.getPortalConfig(origin);

                if (pConfig == null) {
                    log.warn("No PortalConfig found for site : " + origin + ". Site redirection cannot be peformed.");
                    return null;
                }

                // TODO: determine how portalredirects should be properly stored in the portal config.
                // We should not be looping over all the portal redirects to find the right one, but using something
                // like a LinkedHashMap (we need order + mapability) is awkward and more difficult to setup in xml bindings
                // and jcr configuration.
                // NOTE: should be a minor issue, there should only ever really be 2-3 redirects for any portal configuration so
                // looping over all of them should not be a big problem. But its still a bad design which should be fixed.
                PortalRedirect pRedirect = null;
                for (PortalRedirect portalRedirect : pConfig.getPortalRedirects()) {
                    if (portalRedirect.getRedirectSite().equals(redirect)) {
                        pRedirect = portalRedirect;
                        break;
                    }
                }

                if (pRedirect != null) {
                    String redirectPath = mapper.getRedirectPath(origin, redirect, originRequestPath, pRedirect.getMappings());
                    if (redirectPath != null && redirectPath.equals("/")) {
                        return "";
                    } else {
                        return redirectPath;
                    }
                } else {
                    log.debug("Site '" + origin + "' does not have a redirect configured for site '" + redirect
                            + "'. Cannot perform redirect.");
                    return null;
                }
            } else {
                log.debug("Mapper not set. Cannot determine node to map to. Returning Null.");
                return null;
            }
        } catch (Exception e) {
            log.error("Error trying to get site redirect path.", e);
            return null;
        }
    }

    @Override
    public Map<String, String> getAlternativeSites(String site) {
        Map<String, String> siteKeys = new LinkedHashMap<String, String>();
        try {
            if (dataStorage != null) {
                PortalConfig portalConfig = dataStorage.getPortalConfig(site);
                if (portalConfig != null && portalConfig.getPortalRedirects() != null) {
                    for (PortalRedirect portalRedirect : portalConfig.getPortalRedirects()) {
                        siteKeys.put(portalRedirect.getName(), portalRedirect.getRedirectSite());
                    }
                }
            } else {
                log.debug("DataStorage is null. Cannot retrieve the PortalConfig object.");
            }
        } catch (Exception e) {
            log.error("Error trying to retrieve alternative sites.", e);
        }
        return siteKeys;
    }

    @Override
    public void start() {
        // only needed because exo kernel requires this method (really its the underlying picocontianer that needs it)
    }

    @Override
    public void stop() {
        // only needed because exo kernel requires this method (really its the underlying picocontianer that needs it)
    }
}
