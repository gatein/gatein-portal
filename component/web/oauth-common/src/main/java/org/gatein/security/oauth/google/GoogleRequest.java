/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.security.oauth.google;

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpResponseException;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.exception.OAuthException;

/**
 * Wrap Google operation within block of code to handle errors (and possibly restore access token and invoke operation again)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
abstract class GoogleRequest<T> {

    private static Logger log = LoggerFactory.getLogger(GoogleRequest.class);

    protected abstract T invokeRequest(GoogleAccessTokenContext accessTokenContext) throws IOException;

    protected abstract OAuthException createException(IOException cause);

    public T executeRequest(GoogleAccessTokenContext accessTokenContext, GoogleProcessor googleProcessor) {
        GoogleTokenResponse tokenData = accessTokenContext.getTokenData();
        try {
            return invokeRequest(accessTokenContext);
        } catch (IOException ioe) {
            if (ioe instanceof HttpResponseException) {
                HttpResponseException googleException = (HttpResponseException)ioe;
                if (googleException.getStatusCode() == 400 && tokenData.getRefreshToken() != null) {
                    try {
                        // Refresh token and retry revocation with refreshed token
                        googleProcessor.refreshToken(accessTokenContext);
                        return invokeRequest(accessTokenContext);
                    } catch (OAuthException refreshException) {
                        // Log this one with trace level. We will rethrow original exception
                        if (log.isTraceEnabled()) {
                            log.trace("Refreshing token failed", refreshException);
                        }
                    } catch (IOException ioe2) {
                        ioe = ioe2;
                    }
                }
            }
            log.warn("Error when calling Google operation. Details: " + ioe.getMessage());
            throw createException(ioe);
        }
    }
}
