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

package org.gatein.security.oauth.twitter;

import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class TwitterInteractionState {

    public enum STATE {
        AUTH, FINISH
    }

    private final STATE state;
    private final RequestToken requestToken;
    private final AccessToken accessToken;
    private final User user;

    TwitterInteractionState(STATE state, RequestToken requestToken, AccessToken accessToken, User user) {
        this.state = state;
        this.requestToken = requestToken;
        this.accessToken = accessToken;
        this.user = user;
    }

    public STATE getState() {
        return state;
    }

    public RequestToken getRequestToken() {
        return requestToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }
}
