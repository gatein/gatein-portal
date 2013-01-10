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

import org.gatein.common.io.IOTools;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.KeyStore;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/15/12
 */
public class JCASymmetricCodecBuilder extends AbstractCodecBuilder {
    @Override
    public final AbstractCodec build(Map<String, String> config) {
        String transformation = config.get("gatein.codec.jca.symmetric.keyalg");
        String keyFile = config.get("gatein.codec.jca.symmetric.keystore");
        String storeType = config.get("gatein.codec.jca.symmetric.storetype");
        String alias = config.get("gatein.codec.jca.symmetric.alias");
        char[] storePass = config.get("gatein.codec.jca.symmetric.storepass").toCharArray();
        char[] keyPass = config.get("gatein.codec.jca.symmetric.keypass").toCharArray();

        InputStream in = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(storeType);
            if (!keyFile.startsWith("file:") && config.get("gatein.codec.config.basedir") != null) {
                keyFile = config.get("gatein.codec.config.basedir") + "/" + keyFile;
            }
            in = new FileInputStream(keyFile);
            keyStore.load(in, storePass);

            KeyStore.Entry entry = keyStore.getEntry(alias, new KeyStore.PasswordProtection(keyPass));
            SecretKey secretKey = ((KeyStore.SecretKeyEntry) entry).getSecretKey();

            return new JCASymmetricCodec(transformation, secretKey);
        } catch (Exception ex) {
            // TODO: Finer exception handling here
            throw new UndeclaredThrowableException(ex);
        } finally {
            IOTools.safeClose(in);
        }
    }
}
