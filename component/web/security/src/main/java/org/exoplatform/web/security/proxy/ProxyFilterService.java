/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.web.security.proxy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * The proxy filter service is used for filtering http access when it is performed by GateIn. The following rules applies to the
 * filtering:
 *
 * <ul>
 * <li>Same host URI grants access</li>
 * <li>A black list match of the host denies access</li>
 * <li>A white list match of the host grants access</li>
 * <li>Access is denied</li>
 * </ul>
 *
 * The service is configured by
 * <ul>
 * <li>a <code>white-list</code> parameter that specifies a list of white list rules</li>
 * <li>a <code>black-list</code> parameter that specifies a list of black list rules</li>
 * </ul>
 *
 * Rules are trimmed and the wildcard character can be used to match any number of character.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ProxyFilterService {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(ProxyFilterService.class);

    /** . */
    private final List<Pattern> whiteList;

    /** . */
    private final List<Pattern> blackList;

    public ProxyFilterService(InitParams params) {
        this.whiteList = createList(params.getValuesParam("white-list"));
        this.blackList = createList(params.getValuesParam("black-list"));

        // A bit of logging
        log.debug("Proxy filter service white list " + whiteList);
        log.debug("Proxy filter service black list " + blackList);
    }

    private List<Pattern> createList(ValuesParam values) {
        if (values != null) {
            ArrayList<Pattern> patterns = new ArrayList<Pattern>();
            for (Object value : values.getValues()) {
                String s = ((String) value).trim();
                StringBuilder sb = new StringBuilder("^");
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    switch (c) {
                        case '*':
                            sb.append(".*");
                            break;
                        case '[':
                        case '\\':
                        case '^':
                        case '$':
                        case '.':
                        case '|':
                        case '?':
                        case '+':
                        case '(':
                        case ')':
                            sb.append("\\");
                            sb.append(c);
                            break;
                        default:
                            sb.append(c);
                            break;
                    }
                }
                sb.append("$");
                Pattern pattern = Pattern.compile(sb.toString());
                patterns.add(pattern);
            }
            return Collections.unmodifiableList(patterns);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns true if access to a remote URI should be granted.
     *
     * @param request the servlet request doing the request
     * @param container the portal container associated with the request
     * @param remoteURI the remote URI to check
     * @return the access to the remote URI
     */
    public boolean accept(HttpServletRequest request, PortalContainer container, URI remoteURI) {
        boolean trace = log.isTraceEnabled();

        //
        String remoteHost = remoteURI.getHost();

        // Filter based on same server name
        String remoteServerName = request.getServerName();
        if (remoteHost.equals(remoteServerName)) {
            if (trace) {
                log.trace("Same host matching for URI " + remoteURI);
            }
            return true;
        }

        // Otherwise go through black list first
        for (int i = 0; i < blackList.size(); i++) {
            Pattern pattern = blackList.get(i);
            boolean rejected = pattern.matcher(remoteHost).matches();
            if (trace) {
                log.trace("Black list " + pattern + (rejected ? " matched URI " : " did not match URI") + remoteURI);
            }
            if (rejected) {
                log.info("URL for the gadget is blacklisted (pattern: "+pattern+"). Access to its contents has been blocked. URL: " + remoteURI);
                return false;
            }
        }

        // Finally go through white list first
        for (int i = 0; i < whiteList.size(); i++) {
            Pattern pattern = whiteList.get(i);
            boolean accepted = pattern.matcher(remoteHost).matches();
            if (trace) {
                log.trace("White list " + pattern + (accepted ? " matched URI " : " did not match URI") + remoteURI);
            }
            if (accepted) {
                return true;
            }
        }

        //
        log.info("URL for the gadget is not white-listed. Access to its contents has been blocked. URL: " + remoteURI);

        //
        return false;
    }
}
