/**
 * Copyright (C) 2013 eXo Platform SAS.
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

package org.gatein.common;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picocontainer.Startable;

import java.io.File;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;

public class GateInConfigurator implements Startable {

    /**
     * .
     */
    private static final Logger log = LoggerFactory.getLogger(GateInConfigurator.class);

    /**
     * .
     */
    public static String JAAS_KEY = "java.security.auth.login.config";

    /**
     * .
     */
    public static String DEFAULT_PATH = "/conf/jaas.conf";

    public GateInConfigurator(InitParams params, ConfigurationManager confManager) {
        Config config = null;
        if (params != null) {
            ValueParam pathParam = params.getValueParam("application.conf.file");
            if (pathParam != null) {
                String path = pathParam.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("Using file path " + path + " found from configuration");
                }

                try {
                    URL url = confManager.getURL(path);
                    Config appConfig = ConfigFactory.parseURL(url);
                    config = ConfigFactory.load(appConfig);
                } catch (Exception e) {
                    log.error("Cannot load configuration file " + path, e);
                }
            }
        }

        if (config == null) {
            // Loads a default configuration, equivalent to load("application") in most cases.
            config = ConfigFactory.load();
        }

        // Propagate to system properties via PropertyManager
        Set<Entry<String, ConfigValue>> entrySet = config.entrySet();
        for (Entry<String, ConfigValue> entry : entrySet) {
            String name = entry.getKey();
            String value = config.getString(name);
            PropertyManager.setProperty(name, value);
        }

        //
        if (PropertyManager.getProperty(JAAS_KEY) == null) {
            configureJAAS();
        }
    }

    private void configureJAAS() {

        // Determine the configuration jaas path
        String jaasPath = null;
        File configFile = new File(new J2EEServerInfo().getServerHome() + DEFAULT_PATH);
        if (configFile.exists()) {
            // We use the explicit configuration
            jaasPath = configFile.getAbsolutePath();
        } else {
            // Use the configuration URL from the jar
            URL defConfig = Thread.currentThread().getContextClassLoader().getResource("conf/jaas.conf");
            if (defConfig != null) {
                jaasPath = defConfig.toString();
            }
        }

        //
        if (jaasPath != null) {
            log.info("Setting JAAS configuration to " + jaasPath);
            PropertyManager.setProperty(JAAS_KEY, jaasPath);
        }
    }

    public void start() {
    }

    public void stop() {
    }
}
