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

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Subclass of {@link SecureRandom} which is able to reseed itself every {@code reseedingPeriod} milliseconds. All methods
 * inherited from {@link SecureRandom} are passed to {@link #delegate}. When reseeding the {@link #delegate} is recreated. The
 * reseeding happens in a new {@link Thread} not to block the {@link Thread} in which any of the {@code next*} methods is
 * called.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class AutoReseedRandom extends SecureRandom implements Runnable {

    private static final long serialVersionUID = 8754191896768407628L;

    /**
     * Default expiration time in milliseconds, an equivalent of 24 hours.
     */
    public static final long DEFAULT_RESEEDING_PERIOD = 1000 * 60 * 60 * 24;

    /**
     * Default random algorithm {@value #DEFAULT_RANDOM_ALGORITHM}.
     */
    public static final String DEFAULT_RANDOM_ALGORITHM = "SHA1PRNG";

    /**
     * Default random algorithm provider {@value #DEFAULT_RANDOM_ALGORITHM_PROVIDER}.
     */
    public static final String DEFAULT_RANDOM_ALGORITHM_PROVIDER = null;

    /**
     * Default seed length {@value #DEFAULT_SEED_LENGTH}.
     */
    public static final int DEFAULT_SEED_LENGTH = 32;

    /**
     * Name of the reseeding thread {@value #RESEEDING_THREAD_NAME}.
     */
    private static final String RESEEDING_THREAD_NAME = AutoReseedRandom.class.getSimpleName() + " reseeding";

    /**
     * Marked as volatile to avoid instruction reordering on initialization.
     */
    private volatile SecureRandom delegate;

    private volatile long nextReseed = 0;

    /**
     * Time in milliseconds after which the {@link #delegate} gets reseeded by {@link #resetRandom()}.
     */
    private final long reseedingPeriod;

    private final Logger log = LoggerFactory.getLogger(AutoReseedRandom.class);

    /**
     * See {@link SecureRandom#getInstance(String, String)}.
     */
    private final String algorithm;

    /**
     * See {@link SecureRandom#getInstance(String, String)}. {@link #DEFAULT_RANDOM_ALGORITHM_PROVIDER} (
     * {@value #DEFAULT_RANDOM_ALGORITHM_PROVIDER}) is used in the default constructor. You may want to consider using
     * {@code "NativePRNG"} on some platforms if you require stronger cryptography.
     */
    private final String algorithmProvider;

    /**
     * Number of Bytes, see {@link SecureRandom#generateSeed(int)}.
     */
    private final int seedLength;

    public AutoReseedRandom() {
        this(DEFAULT_RANDOM_ALGORITHM, DEFAULT_RANDOM_ALGORITHM_PROVIDER, DEFAULT_SEED_LENGTH, DEFAULT_RESEEDING_PERIOD);
    }

    /**
     * @param algorithm See {@link SecureRandom#getInstance(String, String)}.
     * @param algorithmProvider See {@link SecureRandom#getInstance(String, String)}. In most cases you will want to use
     *        {@link #DEFAULT_RANDOM_ALGORITHM_PROVIDER} ({@value #DEFAULT_RANDOM_ALGORITHM_PROVIDER}) which will lead to using
     *        {@link SecureRandom#getInstance(String)} instead of {@link SecureRandom#getInstance(String, String)}. You may want
     *        to consider using {@code "NativePRNG"}, {@code "SUN"} or other providers if you have special requirements.
     * @param seedLength number of Bytes, see {@link SecureRandom#generateSeed(int)}.
     * @param reseedingPeriod Time in milliseconds after which the {@link #delegate} gets reseeded by {@link #resetRandom()}
     */
    public AutoReseedRandom(String algorithm, String algorithmProvider, int seedLength, long reseedingPeriod) {
        super();
        this.algorithm = algorithm;
        this.algorithmProvider = algorithmProvider;
        this.seedLength = seedLength;
        this.reseedingPeriod = reseedingPeriod;
        nextReseed = System.currentTimeMillis() + reseedingPeriod;
        resetRandom();
    }

    /**
     * Forks the reseeding {@link Thread} if necessary.
     */
    private void checkReseed() {
        boolean reseed = false;
        synchronized (this) {
            /* only one thread can read or write nextReseed */
            if (System.currentTimeMillis() > nextReseed) {
                /*
                 * we move the nextReseed further to the future already here if we did it in the forked thread we could start
                 * several concurrent forks.
                 */
                nextReseed = System.currentTimeMillis() + reseedingPeriod;
                reseed = true;
            }
        }
        if (reseed) {
            new Thread(this, RESEEDING_THREAD_NAME).start();
        }
    }

    /**
     * Called from the reseeding {@link Thread} and on initialization.
     */
    private void resetRandom() {
        SecureRandom newRandom = null;
        try {
            if (algorithmProvider == null) {
                newRandom = SecureRandom.getInstance(algorithm);
            } else {
                newRandom = SecureRandom.getInstance(algorithm, algorithmProvider);
            }

            /* ensure the SecureRandom gets seeded */
            long before = log.isDebugEnabled() ? 0 : System.currentTimeMillis();
            newRandom.setSeed(newRandom.generateSeed(seedLength));
            if (log.isDebugEnabled()) {
                long now = System.currentTimeMillis();
                log.debug("secureRandom.setSeed took " + ((now - before) / 1000.0) +" seconds.");
            }
            delegate = newRandom;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            log.info("Falling back to default random algorithm provider because '" + algorithmProvider + "' is not available.", e);
            /* use the default provider */
            try {
                newRandom = SecureRandom.getInstance(algorithm);
                /* ensure the SecureRandom gets seeded */
                newRandom.setSeed(newRandom.generateSeed(seedLength));
                delegate = newRandom;
            } catch (NoSuchAlgorithmException e1) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Called from the reseeding {@link Thread}.
     *
     * @see #checkReseed()
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            resetRandom();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.SecureRandom#getAlgorithm()
     */
    @Override
    public String getAlgorithm() {
        return delegate.getAlgorithm();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.SecureRandom#setSeed(byte[])
     */
    @Override
    public synchronized void setSeed(byte[] seed) {
        delegate.setSeed(seed);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.SecureRandom#setSeed(long)
     */
    @Override
    public void setSeed(long seed) {
        if (delegate != null) {
            delegate.setSeed(seed);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.SecureRandom#nextBytes(byte[])
     */
    @Override
    public synchronized void nextBytes(byte[] bytes) {
        delegate.nextBytes(bytes);
        checkReseed();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.security.SecureRandom#generateSeed(int)
     */
    @Override
    public byte[] generateSeed(int numBytes) {
        return delegate.generateSeed(numBytes);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextInt()
     */
    @Override
    public int nextInt() {
        int result = delegate.nextInt();
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextInt(int)
     */
    @Override
    public int nextInt(int n) {
        int result = delegate.nextInt(n);
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextLong()
     */
    @Override
    public long nextLong() {
        long result = delegate.nextLong();
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextBoolean()
     */
    @Override
    public boolean nextBoolean() {
        boolean result = delegate.nextBoolean();
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextFloat()
     */
    @Override
    public float nextFloat() {
        float result = delegate.nextFloat();
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextDouble()
     */
    @Override
    public double nextDouble() {
        double result = delegate.nextDouble();
        checkReseed();
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Random#nextGaussian()
     */
    @Override
    public synchronized double nextGaussian() {
        double result = delegate.nextGaussian();
        checkReseed();
        return result;
    }

}
