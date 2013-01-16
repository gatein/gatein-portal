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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectType;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectCookieService {
    protected static final String DEFAULT_PREFIX = "gtn.site";
    protected static final String NAME_PREFERENCE_FLAG = ".preference";
    protected static final int DEFAULT_MAXAGE = 2600000; // Approx 1 month

    // Redirection cookie settings
    protected Integer maxAge;
    protected String comment;
    protected String path;
    protected Boolean secure;

    protected String cookiePrefix;

    public RedirectCookieService(InitParams params) {
        ValueParam cookieMaxAgeValueParam = params.getValueParam("redirect.cookie.maxage");
        if (cookieMaxAgeValueParam != null) {
            this.maxAge = Integer.parseInt(cookieMaxAgeValueParam.getValue());
        } else {
            this.maxAge = DEFAULT_MAXAGE;
        }

        ValueParam cookieCommentValueParam = params.getValueParam("redirect.cookie.comment");
        if (cookieCommentValueParam != null) {
            this.comment = cookieCommentValueParam.getValue();
        }

        ValueParam cookiePathValueParam = params.getValueParam("redirect.cookie.path");
        if (cookiePathValueParam != null) {
            this.path = cookiePathValueParam.getValue();
        }

        ValueParam cookieSecureValueParam = params.getValueParam("redirect.cookie.secure");
        if (cookieSecureValueParam != null) {
            this.secure = Boolean.parseBoolean(cookieSecureValueParam.getValue());
        }

        ValueParam cookiePrefixValueParam = params.getValueParam("redirect.cookie.prefix");
        if (cookiePrefixValueParam != null) {
            cookiePrefix = cookiePrefixValueParam.getValue();
        } else {
            cookiePrefix = DEFAULT_PREFIX;
        }
    }

    public Cookie createCookie(String originSite, RedirectKey redirect, String cookiePath) {
        if (originSite != null && redirect != null) {
            String originName = cookiePrefix + NAME_PREFERENCE_FLAG;

            String redirectValue;
            if (redirect.getType() == RedirectType.REDIRECT) {
                redirectValue = redirect.getRedirect();
            } else {
                redirectValue = originSite;
            }

            Cookie cookie = new Cookie(originName, redirectValue);

            if (comment != null) {
                cookie.setComment(comment);
            }

            if (path != null) {
                cookie.setPath(path);
            } else {
                cookie.setPath(cookiePath);
            }

            if (secure != null) {
                cookie.setSecure(secure);
            }

            cookie.setMaxAge(maxAge);

            return cookie;
        } else {
            throw new IllegalArgumentException("RedirectCookie requires that both the origin site [" + originSite
                    + "] and the redirect site [" + redirect + "] be not null.");
        }

    }

    public RedirectKey getRedirect(SiteKey origin, HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookiePrefix + NAME_PREFERENCE_FLAG)) {
                    String cookieValue = cookie.getValue();

                    if (cookieValue.equals(origin.getName())) {
                        return RedirectKey.noRedirect();
                    } else {
                        if (cookieValue != null && !cookieValue.isEmpty()) {
                            return RedirectKey.redirect(cookieValue);
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }
}
