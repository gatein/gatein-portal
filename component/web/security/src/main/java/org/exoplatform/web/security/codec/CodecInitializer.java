/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.gatein.common.io.IOTools;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Initialize codec from configuration.properties. Use default values if properties not provided
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CodecInitializer {

    private final Logger log = LoggerFactory.getLogger(CodecInitializer.class);

    private final String confDir;

    private volatile AbstractCodec codec;

    public CodecInitializer(InitParams initParams) {
        ValueParam gateinConfParam = initParams.getValueParam("gatein.conf.dir");

        this.confDir = gateinConfParam != null ? gateinConfParam.getValue() : null;
    }

    /**
     * @return shared instance of codec. If shared instance is not available, then new instance will be initialized
     * @throws TokenServiceInitializationException if some error happen during codec initialization
     */
    public AbstractCodec getCodec() throws TokenServiceInitializationException {
        AbstractCodec helper = codec;
        if (helper == null) {
            synchronized(this) {
                helper = codec;
                if (helper == null) {
                    this.codec = helper = initCodec();
                }
            }
        }
        return helper;
    }


    /**
     * Codec initialization. Method is protected, so it could be overriden if needed
     * @return Initialized codec
     * @throws TokenServiceInitializationException if some error happen during initialization
     */
    protected AbstractCodec initCodec() throws TokenServiceInitializationException {
        String builderType = PropertyManager.getProperty("gatein.codec.builderclass");
        Map<String, String> config = new HashMap<String, String>();

        if (builderType != null) {
            // If there is config for codec in configuration.properties, we read the config parameters from config file
            // referenced in configuration.properties
            String configFile = PropertyManager.getProperty("gatein.codec.config");
            InputStream in = null;
            try {
                File f = new File(configFile);
                in = new FileInputStream(f);
                Properties properties = new Properties();
                properties.load(in);
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    config.put((String) entry.getKey(), (String) entry.getValue());
                }
                config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
            } catch (IOException e) {
                throw new TokenServiceInitializationException("Failed to read the config parameters from file '" + configFile
                        + "'.", e);
            } finally {
                IOTools.safeClose(in);
            }
        } else {
            // If there is no config for codec in configuration.properties, we generate key if it does not exist and setup the
            // default config
            builderType = "org.exoplatform.web.security.codec.JCASymmetricCodecBuilder";

            String gtnConfDir = null;
            if (confDir != null) {
                ConfigurationManager confManager = (ConfigurationManager) RootContainer.getInstance().getComponentInstanceOfType(ConfigurationManager.class);
                try {
                    gtnConfDir = confManager.getResource(confDir).getPath();
                } catch (Exception ex) {
                    log.error("Failed to process the path to gateinConfDir", ex);
                }
            }

            if (gtnConfDir == null) {
                gtnConfDir = PropertyManager.getProperty("gatein.conf.dir");
                if (gtnConfDir == null || gtnConfDir.length() == 0) {
                    throw new TokenServiceInitializationException("'gatein.conf.dir' property must be set.");
                }
            }

            File f = new File(gtnConfDir + "/codec/codeckey.txt");
            if (!f.exists()) {
                File codecDir = f.getParentFile();
                if (!codecDir.exists()) {
                    codecDir.mkdir();
                }
                OutputStream out = null;
                try {
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(128);
                    SecretKey key = keyGen.generateKey();
                    KeyStore store = KeyStore.getInstance("JCEKS");
                    store.load(null, "gtnStorePass".toCharArray());
                    store.setEntry("gtnKey", new KeyStore.SecretKeyEntry(key),
                            new KeyStore.PasswordProtection("gtnKeyPass".toCharArray()));
                    out = new FileOutputStream(f);
                    store.store(out, "gtnStorePass".toCharArray());
                } catch (Exception e) {
                    throw new TokenServiceInitializationException(e);
                } finally {
                    IOTools.safeClose(out);
                }
            }
            config.put("gatein.codec.jca.symmetric.keyalg", "AES");
            config.put("gatein.codec.jca.symmetric.keystore", "codeckey.txt");
            config.put("gatein.codec.jca.symmetric.storetype", "JCEKS");
            config.put("gatein.codec.jca.symmetric.alias", "gtnKey");
            config.put("gatein.codec.jca.symmetric.keypass", "gtnKeyPass");
            config.put("gatein.codec.jca.symmetric.storepass", "gtnStorePass");
            config.put("gatein.codec.config.basedir", f.getParentFile().getAbsolutePath());
        }

        try {
            log.info("Initialized codec using builder " + builderType);
            return Class.forName(builderType).asSubclass(AbstractCodecBuilder.class).newInstance().build(config);
        } catch (Exception e) {
            throw new TokenServiceInitializationException("Could not initialize codec.", e);
        }
    }
}
