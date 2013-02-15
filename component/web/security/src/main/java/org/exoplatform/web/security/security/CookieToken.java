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

/**
 * Data obejct with parsing and serializing functionality.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class CookieToken {
    private static final char DELIMITER = '.';
    private final String id;
    private final String randomString;

    public CookieToken(String token) throws TokenParseException {
        int periodPos = token.lastIndexOf(DELIMITER);
        if (periodPos < 0) {
            throw new TokenParseException("Delimiter '" + DELIMITER + "' not found in cookie token.");
        } else if (periodPos == 0) {
            throw new TokenParseException("Delimiter '" + DELIMITER + "' found at position 0 in cookie token.");
        } else if (periodPos == token.length() - 1) {
            throw new TokenParseException("Delimiter '" + DELIMITER + "' found at position token.length() - 1 in cookie token.");
        } else {
            this.id = token.substring(0, periodPos);
            this.randomString = token.substring(periodPos + 1);
        }
    }

    /**
     * @param id
     * @param randomString
     */
    public CookieToken(String id, String randomString) {
        super();
        if (id == null || id.length() == 0) {
            throw new IllegalArgumentException("user must be non-null and non-empty.");
        }
        if (randomString == null || randomString.length() == 0) {
            throw new IllegalArgumentException("randomString must be non-null and non-empty.");
        }
        if (randomString.indexOf(DELIMITER) >= 0) {
            throw new IllegalArgumentException("randomString must not contain '"+ DELIMITER +"'.");
        }
        this.id = id;
        this.randomString = randomString;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return id + DELIMITER + randomString;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((randomString == null) ? 0 : randomString.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CookieToken)) {
            return false;
        }
        CookieToken other = (CookieToken) obj;
        if (randomString == null) {
            if (other.randomString != null) {
                return false;
            }
        } else if (!randomString.equals(other.randomString)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    /**
     * @return the user
     */
    public String getId() {
        return id;
    }

    /**
     * @return the randomString
     */
    public String getRandomString() {
        return randomString;
    }

}