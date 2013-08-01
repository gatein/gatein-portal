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

import java.io.File;
import java.net.URL;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PropertyConfigurator;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.xml.InitParams;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Extends the {@link org.exoplatform.container.PropertyConfigurator} to setup the virtual machine JAAS configuration
 * when it is not present (<code>java.security.auth.login.config</code> system property).
 */
public class GateInConfigurator extends PropertyConfigurator {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(GateInConfigurator.class);

    /** . */
    public static String JAAS_KEY = "java.security.auth.login.config";

    /** . */
    public static String DEFAULT_PATH = "/conf/jaas.conf";

    public GateInConfigurator(ConfigurationManager confManager) {
        this(null, confManager);
    }

    public GateInConfigurator(InitParams params, ConfigurationManager confManager) {
        super(params, confManager);

        //
        if (PropertyManager.getProperty(JAAS_KEY) == null) {

            // Determine the configuration path
            String path = null;
            File configFile = new File(new J2EEServerInfo().getServerHome() + DEFAULT_PATH);
            if (configFile.exists()) {
                // We use the explicit configuration
                path = configFile.getAbsolutePath();
            } else {
                // Use the configuration URL from the jar
                URL defConfig = Thread.currentThread().getContextClassLoader().getResource("conf/jaas.conf");
                if (defConfig != null) {
                    path = defConfig.toString();
                }
            }

            //
            if (path != null) {
                log.info("Setting JAAS configuration to " + path);
                PropertyManager.setProperty(JAAS_KEY, path);
            }
        }
    }
}
