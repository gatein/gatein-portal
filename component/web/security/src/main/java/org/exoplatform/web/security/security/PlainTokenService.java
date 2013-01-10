/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.web.security.security;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.Token;

/**
 * An abstraction layer adding {@link #getAllTokens()}. It was necessary to create this because {@link #getAllTokens()} needed
 * to be removed from {@link CookieTokenService} as it has no significance when the tokens are salt-hashed.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public abstract class PlainTokenService<T extends Token, K> extends AbstractTokenService<T, K> {
    public void cleanExpiredTokens() {
        K[] ids = getAllTokens();
        for (K id : ids) {
            T token = getToken(id);
            if (token != null && token.isExpired()) {
                deleteToken(id);
            }
        }
    }

    /**
     * @param initParams
     * @throws TokenServiceInitializationException
     */
    public PlainTokenService(InitParams initParams) throws TokenServiceInitializationException {
        super(initParams);
    }

    public abstract K[] getAllTokens();

}
