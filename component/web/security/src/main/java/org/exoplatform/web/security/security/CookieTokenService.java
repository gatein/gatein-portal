/**
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

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.ContextualTask;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.web.security.GateInToken;
import org.exoplatform.web.security.codec.AbstractCodec;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.hash.JCASaltedHashService;
import org.exoplatform.web.security.hash.SaltedHashException;
import org.exoplatform.web.security.hash.SaltedHashService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5, 2009
 * </p>
 * <p>
 * On 2013-01-02 the followig was added by ppalaga@redhat.com:
 * <ul>
 * <li>Passwords encrypted symmetrically before they are stored. The functionaliy was taken from <a
 * href="https://github.com/exoplatform/exogtn/commit/5ef8b0fa2d639f4d834444468426dfb2c8485ae9"
 * >https://github.com/exoplatform/exogtn/commit/5ef8b0fa2d639f4d834444468426dfb2c8485ae9</a> with minor modifications. See
 * {@link #codec}</li>
 * <li>The tokens are not stored in plain text, but intead only their salted hash is stored. See {@link #saltedHashService}. To
 * enable this, the following was done:
 * <ul>
 * <li>The structure of the underlying JCR store was changed from
 *
 * <pre>
 * autologin
 * |- plain-token1 user="user1" password="***" expiration="..."
 * |- plain-token2 user="user2" password="***" expiration="..."
 * `- ...
 * </pre>
 *
 * to
 *
 * <pre>
 * autologin
 * |- user1
 * |  |- plain-token1 user="user1" password="***" expiration="..."
 * |  |- plain-token2 user="user1" password="***" expiration="..."
 * |  `- ...
 * |- user2
 * |  |- plain-token3 user="user2" password="***" expiration="..."
 * |  |- plain-token4 user="user2" password="***" expiration="..."
 * |  `- ...
 * `- ...
 * </pre>
 *
 * </li>
 * <li>The value of the token was changed from {@code "rememberme" + randomString} to {@code userName + '.' + randomString}</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * It should be considered in the future if the password field can be removed altogether from {@link TokenEntry}.
 * </p>
 *
 */
public class CookieTokenService extends AbstractTokenService<GateInToken, String> {

    /** . */
    public static final String LIFECYCLE_NAME = "lifecycle-name";
    public static final String HASH_SERVICE_INIT_PARAM = "hash.service";

    /** . */
    private ChromatticLifeCycle chromatticLifeCycle;

    /** . */
    private String lifecycleName = "autologin";

    /**
     * {@link AbstractCodec} used to symmetrically encrypt passwords before storing them.
     */
    private AbstractCodec codec;

    private SaltedHashService saltedHashService;

    private final Logger log = LoggerFactory.getLogger(CookieTokenService.class);

    public CookieTokenService(InitParams initParams, ChromatticManager chromatticManager, CodecInitializer codecInitializer)
            throws TokenServiceInitializationException {
        super(initParams);

        List<?> serviceConfig = initParams.getValuesParam(SERVICE_CONFIG).getValues();
        if (serviceConfig.size() > 3) {
            lifecycleName = (String) serviceConfig.get(3);
        }
        this.chromatticLifeCycle = chromatticManager.getLifeCycle(lifecycleName);

        ObjectParameter hashServiceParam = initParams.getObjectParam(HASH_SERVICE_INIT_PARAM);
        if (hashServiceParam == null || hashServiceParam.getObject() == null) {
            /* the default */
            saltedHashService = new JCASaltedHashService();
        } else {
            saltedHashService = (SaltedHashService) hashServiceParam.getObject();
        }
        this.codec = codecInitializer.getCodec();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.security.AbstractTokenService#start()
     */
    @Override
    public void start() {
        /* clean the legacy tokens */
        new TokenTask<Void>() {
            @Override
            protected Void execute(SessionContext context) {
                ChromatticSession session = context.getSession();
                TokenContainer container = session.findByPath(TokenContainer.class, lifecycleName);
                if (container != null) {
                    /* if the container does not exist, it makes no sense to clean the legacy tokens */
                    container.cleanLegacyTokens();
                } else {
                    session.insert(TokenContainer.class, lifecycleName);
                }
                return null;
            }

        }.executeWith(chromatticLifeCycle);
        super.start();
    }

    public String createToken(final Credentials credentials) {
        if (validityMillis < 0) {
            throw new IllegalArgumentException();
        }
        if (credentials == null) {
            throw new NullPointerException();
        }
        return new TokenTask<String>() {
            @Override
            protected String execute(SessionContext context) {
                String cookieTokenString = null;
                TokenContainer tokenContainer = getTokenContainer();
                while (cookieTokenString == null) {
                    String randomString = nextTokenId();
                    String id = nextRandom();
                    cookieTokenString = new CookieToken(id, randomString).toString();

                    String hashedRandomString = hashToken(randomString);
                    long expirationTimeMillis = System.currentTimeMillis() + validityMillis;

                    /* the symmetric encryption happens here */
                    String encryptedPassword = codec.encode(credentials.getPassword());
                    Credentials encodedCredentials = new Credentials(credentials.getUsername(), encryptedPassword);

                    try {
                        tokenContainer.saveToken(context.getSession(), id, hashedRandomString, encodedCredentials, new Date(expirationTimeMillis));
                    } catch (TokenExistsException e) {
                        cookieTokenString = null;
                    }
                }
                return cookieTokenString;
            }

        }.executeWith(chromatticLifeCycle);
    }

    @Override
    protected String nextTokenId() {
        return nextRandom();
    }

    @Override
    public GateInToken getToken(String cookieTokenString) {
        CookieToken token = null;
        try {
            token = new CookieToken(cookieTokenString);
            return new RemovableGetTokenTask(token, false).executeWith(chromatticLifeCycle);
        } catch (TokenParseException e) {
            log.warn("Could not parse cookie token:"+ e.getMessage());
        }
        return null;
    }

    @Override
    public GateInToken deleteToken(String cookieTokenString) {
        CookieToken token = null;
        try {
            token = new CookieToken(cookieTokenString);
            return new RemovableGetTokenTask(token, true).executeWith(chromatticLifeCycle);
        } catch (TokenParseException e) {
            log.warn("Could not parse cookie token:"+ e.getMessage());
        }
        return null;
    }

    /**
     * The UI should offer a way to delete all existing tokens of the current user.
     *
     * @param user
     */
    public void deleteTokensOfUser(final String user) {
        new TokenTask<Void>() {
            @Override
            protected Void execute(SessionContext context) {
                QueryResult<TokenEntry> result = findTokensOfUser(user);
                while (result.hasNext()) {
                    TokenEntry en = result.next();
                    en.remove();
                }
                return null;
            }

        }.executeWith(chromatticLifeCycle);
    }

    /**
     * Removes all tokens stored in the {@link TokenContainer}.
     */
    public void deleteAll() {
        new TokenTask<Void>() {
            @Override
            protected Void execute(SessionContext context) {
                getTokenContainer().removeAll();
                return null;
            }

        }.executeWith(chromatticLifeCycle);
    }

    @Override
    public void cleanExpiredTokens() {
        new TokenTask<Void>() {
            @Override
            protected Void execute(SessionContext context) {
                getTokenContainer().cleanExpiredTokens();
                return null;
            }
        }.executeWith(chromatticLifeCycle);
    }

    @Override
    public long size() {
        return new TokenTask<Long>() {
            @Override
            protected Long execute(SessionContext context) {
                return (long) getTokenContainer().size();
            }
        }.executeWith(chromatticLifeCycle);
    }

    @Override
    protected String decodeKey(String stringKey) {
        return stringKey;
    }

    private String hashToken(String tokenId) {
        if (saltedHashService != null) {
            try {
                return saltedHashService.getSaltedHash(tokenId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            /* no hash if saltedHashService is null */
            return tokenId;
        }
    }

    /**
     * Wraps token store logic conveniently.
     *
     * @param <V> the return type
     */
    private abstract class TokenTask<V> extends ContextualTask<V> {

        protected final TokenContainer getTokenContainer() {
            SessionContext ctx = chromatticLifeCycle.getContext();
            ChromatticSession session = ctx.getSession();
            return session.findByPath(TokenContainer.class, lifecycleName);
        }

        protected final <A> A getMixin(Object o, Class<A> type) {
            SessionContext ctx = chromatticLifeCycle.getContext();
            ChromatticSession session = ctx.getSession();
            return session.getEmbedded(o, type);
        }

        protected final QueryResult<TokenEntry> findTokensOfUser(String user) {
            SessionContext ctx = chromatticLifeCycle.getContext();
            ChromatticSession session = ctx.getSession();
            TokenContainer tokenContainer = getTokenContainer();

            String statement = new StringBuilder(128).append("jcr:path LIKE '").append(session.getPath(tokenContainer))
                    .append("/%'").append(" AND username='").append(Utils.queryEscape(user)).append("'").toString();
            return session.createQueryBuilder(TokenEntry.class).where(statement).get().objects();
        }

    }

    private class RemovableGetTokenTask extends TokenTask<GateInToken> {
        private final CookieToken token;
        private final boolean remove;

        /**
         * @param token
         */
        public RemovableGetTokenTask(CookieToken token, boolean remove) {
            super();
            this.token = token;
            this.remove = remove;
        }

        @Override
        protected GateInToken execute(SessionContext context) {
            TokenEntry en = getTokenContainer().getTokens().get(token.getId());
            if (en != null) {
                HashedToken hashedToken = getMixin(en, HashedToken.class);
                if (hashedToken != null && hashedToken.getHashedToken() != null) {
                    try {
                        if (saltedHashService.validate(token.getRandomString(), hashedToken.getHashedToken())) {
                            GateInToken encryptedToken = en.getToken();
                            Credentials encryptedCredentials = encryptedToken.getPayload();
                            Credentials decryptedCredentials = new Credentials(encryptedCredentials.getUsername(),

                            codec.decode(encryptedCredentials.getPassword()));
                            if (remove) {
                                en.remove();
                            }
                            return new GateInToken(encryptedToken.getExpirationTimeMillis(), decryptedCredentials);
                        }
                    } catch (SaltedHashException e) {
                        log.warn("Could not validate cookie token against its salted hash.", e);
                    }
                }
            }
            return null;
        }
    }
}
