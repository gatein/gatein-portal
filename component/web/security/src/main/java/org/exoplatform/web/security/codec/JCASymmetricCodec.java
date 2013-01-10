/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.web.security.codec;

import org.gatein.common.util.Base64;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Symmetric codec bases on Java Cryptography Library
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/15/12
 */
public class JCASymmetricCodec extends AbstractCodec {

    private final Cipher encrypter;

    private final Cipher decrypter;

    JCASymmetricCodec(String transformation, SecretKey key) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException {
        this.encrypter = Cipher.getInstance(transformation);
        this.decrypter = Cipher.getInstance(transformation);

        this.encrypter.init(Cipher.ENCRYPT_MODE, key);
        this.decrypter.init(Cipher.DECRYPT_MODE, key);
    }

    @Override
    public String encode(String plainInput) {
        try {
            byte[] bytes;
            synchronized (encrypter) {
                bytes = encrypter.doFinal(plainInput.getBytes());
            }
            return Base64.encodeBytes(bytes);
        } catch (Exception ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }

    @Override
    public String decode(String encodedInput) {
        try {
            byte[] bytes = Base64.decode(encodedInput);
            synchronized (decrypter) {
                bytes = decrypter.doFinal(bytes);
            }
            return new String(bytes);
        } catch (Exception ex) {
            throw new UndeclaredThrowableException(ex);
        }
    }
}
