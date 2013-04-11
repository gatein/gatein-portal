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

import java.util.Map;

import org.exoplatform.portal.config.model.RedirectMappings;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.GenericScope;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Handles the mapping between the nodes when performing a redirect
 *
 * TODO: create an interface for this and configure the service using the kernel
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Mapper {
    protected static Logger log = LoggerFactory.getLogger(Mapper.class);

    NavigationService navService;

    public Mapper(NavigationService navService) {
        this.navService = navService;
    }

    public String getRedirectPath(String originSite, String redirectSite, String originalRequestPath,
            RedirectMappings redirectMappings) {
        return getRequestPath(originSite, redirectSite, originalRequestPath, redirectMappings);
    }

    protected String getRequestPath(String originSite, String redirectSite, String requestPath,
            RedirectMappings redirectMappings) {
        if (redirectMappings == null) {
            return null;
        }

        Map<String, String> mappings = redirectMappings.getMap();
        // first check if we have explicit mappings for this requestPath, always blindly follow any explicit mappings
        if (mappings != null) {
            String redirectRequestPath = mappings.get(requestPath);

            if (redirectRequestPath == null) {
                if (requestPath.startsWith("/")) {
                    // it has a leading / so check without the '/'
                    redirectRequestPath = mappings.get(requestPath.substring(1));
                } else {
                    // otherwise it doesn't have a leading '/' check with one.
                    redirectRequestPath = mappings.get("/" + requestPath);
                }
            }

            if (redirectRequestPath != null) {
                if (redirectRequestPath.startsWith("/")) {
                    return redirectRequestPath;
                } else {
                    return "/" + redirectRequestPath;
                }
            }
        }

        // next check if we use node name matching
        if (redirectMappings.isUseNodeNameMatching()) {
            String redirectRequestPath = getNodeIfExists(redirectSite, requestPath,
                    redirectMappings.getUnresolvedNode() == RedirectMappings.UnknownNodeMapping.COMMON_ANCESTOR_NAME_MATCH);
            {
                if (redirectRequestPath != null) {
                    if (redirectRequestPath.startsWith("/")) {
                        return redirectRequestPath;
                    } else {
                        return "/" + redirectRequestPath;
                    }
                }
            }
        }

        // if no explicit mapping, no name matching and not using common ancestor
        if (redirectMappings.getUnresolvedNode() == RedirectMappings.UnknownNodeMapping.NO_REDIRECT) {
            return null;
        } else if (redirectMappings.getUnresolvedNode() == RedirectMappings.UnknownNodeMapping.REDIRECT) {
            if (requestPath.startsWith("/")) {
                return requestPath;
            } else {
                return "/" + requestPath;
            }
        } else if (redirectMappings.getUnresolvedNode() == RedirectMappings.UnknownNodeMapping.ROOT) {
            return "/";
        } else {
            log.warn("Unknown redirect configuration option for an unknown node [" + redirectMappings.getUnresolvedNode()
                    + "]. Will not perform redirect.");
            return null;
        }
    }

    protected String getNodeIfExists(String redirectSite, String requestPath, Boolean useCommonAncestor) {
        log.info("GetNodeExits called [" + redirectSite + "] : [" + requestPath + "]");

        NavigationContext navContext = null;

        if (redirectSite == null || navService == null) {
            log.warn("Redirect site name [" + redirectSite + "] or the navigation service object [" + navService
                    + "] is null. Cannot perform redirect.");
            return null;
        } else {
            navContext = navService.loadNavigation(SiteKey.portal(redirectSite));
        }

        if (navContext == null) // if the navContext is null, the redirectSite doesn't exist and we can't redirect to it.
        {
            log.warn("Cannot preform redirect since can't retrieve navigation for site : " + redirectSite);
            return null;
        } else if (requestPath == null || requestPath.isEmpty() || requestPath.equals("/")) {
            // a nav context exists and we are checking the root node, no need to check anything else
            return "/";
        }

        String[] path = new String[0];
        if (requestPath.startsWith("/")) {
            path = requestPath.substring(1).split("/");
        } else {
            path = requestPath.split("/");
        }

        NodeContext nodeContext = navService.loadNode(NodeModel.SELF_MODEL, navContext,
                GenericScope.branchShape(path, Scope.ALL), null);

        boolean found = true;
        String lastCommonAncestor = "/";

        for (String nodeName : path) {
            nodeContext = nodeContext.get(nodeName);
            if (nodeContext == null) {
                found = false;
                break;
            } else {
                if (lastCommonAncestor.equals("/")) {
                    lastCommonAncestor += nodeContext.getName();
                } else {
                    lastCommonAncestor += "/" + nodeContext.getName();
                }
            }
        }

        if (found == true) {
            if (requestPath.startsWith("/")) {
                return requestPath;
            } else {
                return "/" + requestPath;
            }
        } else if (useCommonAncestor) {
            return lastCommonAncestor;
        } else {
            return null;
        }
    }

}
