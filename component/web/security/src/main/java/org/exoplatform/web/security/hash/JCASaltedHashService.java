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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * A {@link SaltedHashService} implementation using a {@link javax.crypto.SecretKeyFactory} for salted hashing.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class JCASaltedHashService implements SaltedHashService {

    public static final String PBKDF2_WITH_HMAC_SHA1 = "PBKDF2WithHmacSHA1";
    public static final int DEFAULT_SALT_BYTE_LENGTH = 9;
    public static final int DEFAULT_HASH_BYTE_LENGTH = 9;
    public static final int DEFAULT_ITERATION_COUNT = 1000;

    /**
     * The number of iterations submitted to {@link SecretKeyFactory#generateSecret(java.security.spec.KeySpec)}.
     * <p>
     * From <a href="http://en.wikipedia.org/wiki/PBKDF2">http://en.wikipedia.org/wiki/PBKDF2</a> (retrieved 2013-01-08):
     * <cite>When the
     * standard was written in 2000, the recommended minimum number of iterations was 1000, but the parameter is intended to be
     * increased over time as CPU speeds increase.
     * [...] Apple's iOS 3 uses 2,000 iterations and iOS 4 uses 10,000.
     * </cite>
     */
    private int iterationCount;

    /**
     * Salt length in bytes. Use values divisible by 3 to get salted hash strings without Base64 padding characters.
     */
    private int saltByteLength;

    /**
     * Hash length in bytes. Use values divisible by 3 to get salted hash strings without Base64 padding characters.
     */
    private int hashByteLength;

    /**
     * Name of the hashing algorithm which is submitted to {@link SecretKeyFactory#getInstance(String)}.
     */
    private String algorithm;

    /**
     * Pluggable codec for serialization and deserialization of {@link SaltedHash} objects.
     */
    private SaltedHashCodec codec;

    /**
     *
     */
    private final Logger log = LoggerFactory.getLogger(JCASaltedHashService.class);

    /**
     * @param algorithm
     * @param iterationCount
     * @param saltLength
     * @param hashLength
     * @param codec
     */
    public JCASaltedHashService(String algorithm, int iterationCount, int saltLength, int hashLength, SaltedHashCodec codec) {
        super();
        this.algorithm = algorithm;
        this.iterationCount = iterationCount;
        this.saltByteLength = saltLength;
        this.hashByteLength = hashLength;
        this.codec = codec;
    }

    /**
     *
     */
    public JCASaltedHashService() {
        this(PBKDF2_WITH_HMAC_SHA1, DEFAULT_ITERATION_COUNT, DEFAULT_SALT_BYTE_LENGTH, DEFAULT_HASH_BYTE_LENGTH,
                XmlSafeSaltedHashCodec.INSTANCE);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashService#getSaltedHash(java.lang.String, java.security.SecureRandom)
     */
    @Override
    public String getSaltedHash(String password, SecureRandom random) throws SaltedHashException {
        try {
            byte[] salt = new byte[saltByteLength];
            random.nextBytes(salt);
            SaltedHash saltedHash = new SaltedHash(algorithm, iterationCount, salt, hash(this.algorithm, password, salt,
                    iterationCount, hashByteLength));
            return codec.encode(saltedHash);
        } catch (InvalidKeySpecException e) {
            throw new SaltedHashException("Could not create salted hash from password.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new SaltedHashException("Could not create salted hash from password.", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashService#validate(java.lang.String, java.lang.String)
     */
    @Override
    public boolean validate(String password, String encodedSaltedHash) throws SaltedHashException {
        try {
            SaltedHash saltedHash = codec.decode(encodedSaltedHash);
            byte[] expectedHash = hash(saltedHash.getAlgorithm(), password, saltedHash.getSalt(),
                    saltedHash.getIterationCount(), saltedHash.getHash().length);
            if (log.isDebugEnabled()) {
                log.debug("About to validate submitted hash " + Arrays.toString(expectedHash) + " against stored hash "
                        + Arrays.toString(saltedHash.getHash()));
            }
            return Arrays.equals(expectedHash, saltedHash.getHash());
        } catch (NoSuchAlgorithmException e) {
            throw new SaltedHashException("Could not validate password against salted hash.", e);
        } catch (InvalidKeySpecException e) {
            throw new SaltedHashException("Could not validate password against salted hash.", e);
        }
    }

    /**
     * Computes the hash as a byte array.
     *
     * @param algorithm
     * @param password
     * @param salt
     * @param iterationCount
     * @param hashLength
     * @return
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private static byte[] hash(String algorithm, String password, byte[] salt, int iterationCount, int hashLength)
            throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKey key = SecretKeyFactory.getInstance(algorithm).generateSecret(
                new PBEKeySpec(password.toCharArray(), salt, iterationCount, hashLength * 8));
        return key.getEncoded();
    }

    /**
     * @see #iterationCount
     * @return the iterationCount
     */
    public int getIterationCount() {
        return iterationCount;
    }

    /**
     * @see #iterationCount
     * @param iterationCount the iterationCount to set
     */
    public void setIterationCount(int iterationCount) {
        this.iterationCount = iterationCount;
    }

    /**
     * @see #saltByteLength
     * @return the saltLength
     */
    public int getSaltByteLength() {
        return saltByteLength;
    }

    /**
     * @see #saltByteLength
     * @param saltLength the saltLength to set
     */
    public void setSaltLength(int saltLength) {
        this.saltByteLength = saltLength;
    }

    /**
     * @see #hashByteLength
     * @return the hashLength
     */
    public int getHashByteLength() {
        return hashByteLength;
    }

    /**
     * @see #hashByteLength
     * @param hashLength the hashLength to set
     */
    public void setHashLength(int hashLength) {
        this.hashByteLength = hashLength;
    }

    /**
     * @see #algorithm
     * @return the algorithm
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * @see #algorithm
     * @param algorithm the algorithm to set
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * @see #codec
     * @return the codec
     */
    public SaltedHashCodec getCodec() {
        return codec;
    }

    /**
     * @see #codec
     * @param codec the codec to set
     */
    public void setCodec(SaltedHashCodec codec) {
        this.codec = codec;
    }

}
