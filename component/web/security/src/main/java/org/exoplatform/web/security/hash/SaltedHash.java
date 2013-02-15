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

package org.exoplatform.web.security.hash;

import java.util.Arrays;

/**
 * An ancillary data object representing a salted hash. Use {@link SaltedHashCodec} to get a representation suitable for storing
 * in a database.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class SaltedHash {

    private final String algorithm;
    private final int iterationCount;
    private final byte[] salt;
    private final byte[] hash;

    /**
     * @param algorithm
     * @param iterationCount
     * @param salt
     * @param hash
     */
    public SaltedHash(String algorithm, int iterationCount, byte[] salt, byte[] hash) {
        super();
        if (algorithm == null) {
            throw new NullPointerException("algorithm cannot be null.");
        }
        if (salt == null) {
            throw new NullPointerException("salt cannot be null.");
        }
        if (hash == null) {
            throw new NullPointerException("hash cannot be null.");
        }
        this.algorithm = algorithm;
        this.iterationCount = iterationCount;
        this.salt = salt;
        this.hash = hash;
    }

    /**
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @return the iterationCount
     */
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * @return the salt
     */
    public byte[] getSalt() {
        return salt;
    }

    /**
     * @return the hash
     */
    public byte[] getHash() {
        return hash;
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
        result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
        result = prime * result + Arrays.hashCode(hash);
        result = prime * result + iterationCount;
        result = prime * result + Arrays.hashCode(salt);
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
        if (!(obj instanceof SaltedHash)) {
            return false;
        }
        SaltedHash other = (SaltedHash) obj;
        if (algorithm == null) {
            if (other.algorithm != null) {
                return false;
            }
        } else if (!algorithm.equals(other.algorithm)) {
            return false;
        }
        if (!Arrays.equals(hash, other.hash)) {
            return false;
        }
        if (iterationCount != other.iterationCount) {
            return false;
        }
        if (!Arrays.equals(salt, other.salt)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SaltedHash [algorithm=" + algorithm + ", iterationCount=" + iterationCount + ", salt=" + Arrays.toString(salt)
                + ", hash=" + Arrays.toString(hash) + "]";
    }

}
