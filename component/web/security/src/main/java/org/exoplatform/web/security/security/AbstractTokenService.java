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

import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.Impact;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.web.login.LoginServlet;
import org.exoplatform.web.security.Token;
import org.exoplatform.web.security.TokenStore;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.util.Base64;
import org.gatein.common.util.Base64.EncodingOption;
import org.gatein.wci.security.Credentials;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5, 2009
 *
 * todo julien : - make delay configuration from init param and @Managed setter - start/stop expiration daemon - manually invoke
 * the daemon via @Managed
 *
 * @param <T> the token type
 * @param <K> the token key type
 */
@Managed
@ManagedDescription("Token Store Service")
@NameTemplate({ @Property(key = "service", value = "TokenStore"), @Property(key = "name", value = "{Name}") })
public abstract class AbstractTokenService<T extends Token, K> implements Startable, TokenStore {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected static final String SERVICE_CONFIG = "service.configuration";

    protected static final int DELAY_TIME = 600;

    /**
     * See {@link #tokenByteLength}. 8 bytes (64 bits) would be enough, but we want to get padding-less Byte64 representation, so we take
     * the next greater number divisible by 3 which is 9. 9 bytes is equal to 72 bits.
     */
    public static final int DEFAULT_TOKEN_BYTE_LENGTH = 9;

    /**
     * The number of random bits generared by {@link #nextRandom()}. Use values divisible by 3 to produce random strings
     * consisting only of {@code 0-9}, {@code a-z}, {@code A-Z}, {@code -} and {@code _}, i.e. URL safe Byte64 without padding.
     * If a value not divisible by 3 is used the random strings will contain {@code *} in addition to the named characters.
     */
    protected final int tokenByteLength;

    protected String name;

    protected long validityMillis;

    protected final SecureRandom random;

    private ScheduledExecutorService executor;

    @SuppressWarnings("unchecked")
    public AbstractTokenService(InitParams initParams) throws TokenServiceInitializationException {
        List<String> params = initParams.getValuesParam(SERVICE_CONFIG).getValues();
        this.name = params.get(0);
        long configValue = new Long(params.get(1));
        this.validityMillis = TimeoutEnum.valueOf(params.get(2)).toMilisecond(configValue);

        this.tokenByteLength = DEFAULT_TOKEN_BYTE_LENGTH;
        this.random = new AutoReseedRandom();
    }

    public void start() {
        // start a thread, garbage expired cookie token every [DELAY_TIME]
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                    AbstractTokenService.this.cleanExpiredTokens();
                } catch (Throwable t) {
                    log.warn("Failed to clean expired tokens", t);
                }
            }
        }, 0, DELAY_TIME, TimeUnit.SECONDS);

    }

    public void stop() {
        executor.shutdown();
    }

    public static <T extends AbstractTokenService<?, ?>> T getInstance(Class<T> classType) {
        PortalContainer container = PortalContainer.getInstance();
        return classType.cast(container.getComponentInstanceOfType(classType));
    }

    public Credentials validateToken(String stringKey, boolean remove) {
        if (stringKey == null) {
            throw new NullPointerException();
        }

        //
        K tokenKey = decodeKey(stringKey);

        T token;
        try {
            if (remove) {
                token = this.deleteToken(tokenKey);
            } else {
                token = this.getToken(tokenKey);
            }

            if (token != null) {
                boolean valid = token.getExpirationTimeMillis() > System.currentTimeMillis();
                if (valid) {
                    return token.getPayload();
                } else if (!remove) {
                    this.deleteToken(tokenKey);
                }
            }
        } catch (Exception e) {
        }

        return null;
    }

    @Managed
    @ManagedDescription("Clean all tokens are expired")
    @Impact(ImpactType.IDEMPOTENT_WRITE)
    public abstract void cleanExpiredTokens();

    @Managed
    @ManagedDescription("Get time for token expiration in seconds")
    public long getValidityTime() {
        return validityMillis / 1000;
    }

    @Managed
    @ManagedDescription("The expiration daemon period time in seconds")
    public long getPeriodTime() {
        return DELAY_TIME;
    }

    @Managed
    @ManagedDescription("The token service name")
    public String getName() {
        return name;
    }

    public abstract T getToken(K id);

    public abstract T deleteToken(K id);

    /**
     * Decode a key from its string representation.
     *
     * @param stringKey the key a s a string
     * @return the typed key
     */
    protected abstract K decodeKey(String stringKey);

    // We don't make it a property as retrieving the value can be an expensive operation
    @Managed
    @ManagedDescription("The number of tokens")
    @Impact(ImpactType.READ)
    public abstract long size();

    private enum TimeoutEnum {
        SECOND(1000), MINUTE(1000 * 60), HOUR(1000 * 60 * 60), DAY(1000 * 60 * 60 * 24);

        private long multiply;

        private TimeoutEnum(long multiply) {
            this.multiply = multiply;
        }

        public long toMilisecond(long configValue) {
            return configValue * multiply;
        }
    }

    protected String nextTokenId() {
        return LoginServlet.COOKIE_NAME + nextRandom();
    }

    protected String nextRandom() {
        byte[] randomBytes = new byte[tokenByteLength];
        random.nextBytes(randomBytes);
        return Base64.encodeBytes(randomBytes, EncodingOption.USEURLSAFEENCODING);
    }

}
