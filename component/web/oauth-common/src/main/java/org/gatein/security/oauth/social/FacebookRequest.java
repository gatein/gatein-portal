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

package org.gatein.security.oauth.social;

import java.io.IOException;
import java.net.URL;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.security.oauth.exception.OAuthException;
import org.gatein.security.oauth.exception.OAuthExceptionCode;
import org.gatein.security.oauth.utils.HttpResponseContext;
import org.gatein.security.oauth.utils.OAuthUtils;
import org.json.JSONException;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class FacebookRequest<T> {

    private static Logger log = LoggerFactory.getLogger(FacebookRequest.class);

    protected abstract URL createURL(String accessToken) throws IOException;

    protected abstract T parseResponse(String httpResponse) throws JSONException;

    public T executeRequest(String accessToken) {
        try {
            URL url = createURL(accessToken);
            HttpResponseContext httpResponse = OAuthUtils.readUrlContent(url.openConnection());
            if (httpResponse.getResponseCode() == 200) {
                return parseResponse(httpResponse.getResponse());
            } else if (httpResponse.getResponseCode() == 400) {
                String errorMessage = "Error when obtaining content from Facebook. Error details: " + httpResponse.getResponse();
                log.warn(errorMessage);
                throw new OAuthException(OAuthExceptionCode.EXCEPTION_CODE_ACCESS_TOKEN_ERROR, errorMessage);
            } else {
                String errorMessage = "Unspecified IO error. Http response code: " + httpResponse.getResponseCode() + ", details: " + httpResponse.getResponse();
                log.warn(errorMessage);
                throw new OAuthException(OAuthExceptionCode.EXCEPTION_CODE_UNSPECIFIED_IO_ERROR, errorMessage);
            }
        } catch (JSONException e) {
            throw new OAuthException(OAuthExceptionCode.EXCEPTION_CODE_UNSPECIFIED_IO_ERROR, e);
        } catch (IOException e) {
            throw new OAuthException(OAuthExceptionCode.EXCEPTION_CODE_UNSPECIFIED_IO_ERROR, e);
        }
    }
}
