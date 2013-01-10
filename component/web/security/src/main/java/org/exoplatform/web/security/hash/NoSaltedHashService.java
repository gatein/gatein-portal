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

import java.security.SecureRandom;

/**
 * A {@link SaltedHashService} implementation which does not do any hashing at all. It simply returns the plaintext password
 * from {@link #getSaltedHash(String, SecureRandom)} and tests the string equality of {@code password} and {@code saltedHash} in
 * {@link #validate(String, String)}.
 *
 * This class is intended to be used in tests and maybe also in some real life scenarios where backwards compatibility requires
 * storing of plaintext passwords.
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class NoSaltedHashService implements SaltedHashService {

    /**
     *
     */
    public NoSaltedHashService() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashService#getSaltedHash(java.lang.String, java.security.SecureRandom)
     */
    @Override
    public String getSaltedHash(String password, SecureRandom random) throws SaltedHashException {
        return password;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.exoplatform.web.security.hash.SaltedHashService#validate(java.lang.String, java.lang.String)
     */
    @Override
    public boolean validate(String password, String saltedHash) throws SaltedHashException {
        return password == saltedHash || (password != null && password.equals(saltedHash));
    }

}
