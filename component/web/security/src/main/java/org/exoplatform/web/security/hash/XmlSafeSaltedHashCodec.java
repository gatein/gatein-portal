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

import java.util.StringTokenizer;

import org.gatein.common.util.Base64.EncodingOption;

/**
 * A codec able to transform a {@link SaltedHash} to and from {@link String}.
 *
 * The string representations produced by {@link XmlSafeSaltedHashCodec#encode(SaltedHash)} are granted to be valid XML element
 * names if the length of byte arrays returned by {@link SaltedHash#getSalt()} and {@link SaltedHash#getHash()} is divisible by
 * 12. This important because {@link XmlSafeSaltedHashCodec#encode(SaltedHash)} uses the URL safe variant of Byte64 encoding
 * (see {@link org.gatein.common.util.Base64}) and for byte array lengths other than 12, the string encoded by Byte64 would have
 * to be padded with character {@code '*'} which would make the result an invalid XML name.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class XmlSafeSaltedHashCodec implements SaltedHashCodec {

    public static final SaltedHashCodec INSTANCE = new XmlSafeSaltedHashCodec();

    /**
     * Delimiter used to separate the fields of a {@link SaltedHash} in its string representation.
     */
    private final char delimiter = '.';

    /**
     *
     */
    public XmlSafeSaltedHashCodec() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashCodec#encode(org.exoplatform.web.security.hash.SaltedHash)
     */
    @Override
    public String encode(SaltedHash saltedHash) {
        if (saltedHash == null) {
            return null;
        } else {
            String salt = toString(saltedHash.getSalt());
            String hash = toString(saltedHash.getHash());
            String algorithm = saltedHash.getAlgorithm();
            int iterationCount = saltedHash.getIterationCount();

            /* initialize the buffer with exact size */
            StringBuilder buffer = new StringBuilder(algorithm.length() + 8 // iterationCount digit count estimate
                    + salt.length() + hash.length() + 3 // delimiter count
            );
            return buffer.append(algorithm).append(delimiter).append(iterationCount).append(delimiter).append(salt)
                    .append(delimiter).append(hash).toString();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashCodec#decode(java.lang.String)
     */
    @Override
    public SaltedHash decode(String encodedSaltedHash) throws SaltedHashEncodingException {
        StringTokenizer st = new StringTokenizer(encodedSaltedHash, String.valueOf(delimiter));
        if (st.hasMoreTokens()) {
            String algorithm = st.nextToken();
            try {
                if (st.hasMoreTokens()) {
                    int iterationCount = Integer.parseInt(st.nextToken());
                    if (st.hasMoreTokens()) {
                        String saltString = st.nextToken();
                        byte[] saltBytes = toBytes(saltString);
                        if (st.hasMoreTokens()) {
                            String hashString = st.nextToken();
                            byte[] hashBytes = toBytes(hashString);
                            return new SaltedHash(algorithm, iterationCount, saltBytes, hashBytes);
                        }
                    }
                }
            } catch (NumberFormatException e) {
                throw new SaltedHashEncodingException("Could not decode salted hash '" + encodedSaltedHash + "'.", e);
            }
        }
        throw new SaltedHashEncodingException("Encoded salted hash '" + encodedSaltedHash + "' too short.");
    }

    protected String toString(byte[] bytes) {
        return org.gatein.common.util.Base64.encodeBytes(bytes, EncodingOption.USEURLSAFEENCODING);
    }

    protected byte[] toBytes(String str) {
        return org.gatein.common.util.Base64.decode(str, EncodingOption.USEURLSAFEENCODING);
    }

}
