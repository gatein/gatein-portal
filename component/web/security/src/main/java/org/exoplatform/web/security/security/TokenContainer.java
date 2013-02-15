/*
 * Copyright (C) 2009 eXo Platform SAS.
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

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.exoplatform.web.security.GateInToken;
import org.gatein.wci.security.Credentials;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "tkn:tokencontainer")
public abstract class TokenContainer {

    @Create
    protected abstract TokenEntry createToken();

    @OneToMany
    public abstract Map<String, TokenEntry> getTokens();


    public GateInToken getToken(String tokenId) {
        Map<String, TokenEntry> tokens = getTokens();
        TokenEntry entry = tokens.get(tokenId);
        return entry != null ? entry.getToken() : null;
    }

    public GateInToken removeToken(String tokenId) {
        Map<String, TokenEntry> tokens = getTokens();
        TokenEntry entry = tokens.get(tokenId);
        if (entry != null) {
            GateInToken token = entry.getToken();
            entry.remove();
            return token;
        } else {
            return null;
        }
    }

    public void saveToken(ChromatticSession session, String id, String hashedToken, Credentials credentials, Date expirationTime) throws TokenExistsException {
        Map<String, TokenEntry> tokens = getTokens();
        if (tokens.containsKey(id)) {
            throw new TokenExistsException();
        }
        TokenEntry entry = createToken();
        tokens.put(id, entry);
        entry.setUserName(credentials.getUsername());
        entry.setPassword(credentials.getPassword());
        entry.setExpirationTime(expirationTime);

        HashedToken hashedTokenMixin = session.create(HashedToken.class);
        session.setEmbedded(entry, HashedToken.class, hashedTokenMixin);
        hashedTokenMixin.setHashedToken(hashedToken);
    }

    public void cleanExpiredTokens() {
        Map<String, TokenEntry> tokens = getTokens();
        if (tokens != null) {
            for (TokenEntry en : tokens.values()) {
                GateInToken token = en.getToken();
                if (token.isExpired()) {
                    en.remove();
                }
            }
        }
    }

    /**
     * Removes all tokens in preGateIn-3.5.1 format, i.e. those ones starting with {@code "rememberme"}.
     */
    public void cleanLegacyTokens() {
        Map<String, TokenEntry> tokens = getTokens();
        if (tokens != null) {
            for (Entry<String, TokenEntry> en : tokens.entrySet()) {
                String token = en.getKey();
                if (token.startsWith("rememberme")) {
                    en.getValue().remove();
                }
            }
        }
    }

    /**
     * @return
     */
    public int size() {
        Map<String, TokenEntry> tokens = getTokens();
        return tokens != null ? tokens.size() : 0;
    }

    public void removeAll() {
        for (TokenEntry en : getTokens().values()) {
            en.remove();
        }
    }


}
