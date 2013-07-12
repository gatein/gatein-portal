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

public class GateInConfigurator extends PropertyConfigurator {

    public static String JAAS_KEY = "java.security.auth.login.config";

    public static String DEFAULT_PATH = "/conf/jaas.conf";

    public GateInConfigurator(ConfigurationManager confManager) {
        this(null, confManager);
    }

    public GateInConfigurator(InitParams params, ConfigurationManager confManager) {
        super(params, confManager);

        String serverHome = new J2EEServerInfo().getServerHome();
        if (PropertyManager.getProperty(JAAS_KEY) == null) {
            // Find in default location
            String path = serverHome + DEFAULT_PATH;

            File configFile = new File(path);
            if (configFile.exists()) {
                PropertyManager.setProperty(JAAS_KEY, path);
            } else {
                // extract default config in jar
                URL defConfig = Thread.currentThread().getContextClassLoader().getResource("conf/jaas.conf");
                if (defConfig != null) {
                    PropertyManager.setProperty(JAAS_KEY, defConfig.toString());
                }
            }
        }
    }
}
