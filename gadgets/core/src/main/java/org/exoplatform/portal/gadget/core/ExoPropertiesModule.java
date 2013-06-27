/**
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
package org.exoplatform.portal.gadget.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.PropertiesModule;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.google.inject.CreationException;
import com.google.inject.spi.Message;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class ExoPropertiesModule extends PropertiesModule {
    private static final String GTN_SHINDIG_PROPERTIES = "shindig.properties";

    /** . */
    private Log log = ExoLogger.getLogger(ExoPropertiesModule.class);

    private final Properties properties;

    public ExoPropertiesModule() {
        super();

        // This ensures RootContainer initialized first
        // to populate properties in configuration.properties into PropertyManager
        RootContainer.getInstance();

        this.properties = readPropertyFile(GTN_SHINDIG_PROPERTIES);
    }

    protected Properties getProperties() {
        if (properties != null) {
            return properties;
        } else {
            return super.getProperties();
        }
    }

    private Properties readPropertyFile(String propertyFile) {
        Properties properties = null;
        InputStream is = null;

        // Try to load shindig.properties from the gadgets directory in precedence if it is configured.
        String gadgetDir = PropertyManager.getProperty("gatein.gadgets.dir");
        if (gadgetDir != null) {
            File file = new File(new File(gadgetDir), propertyFile);
            if (file.exists() && file.isFile()) {
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    log.debug("File " + file.getAbsolutePath() + " doesn't exist");
                }
            }
        }

        // Then try to load shindig.properties from the gadget server application war.
        if (is == null) {
            GateInContainerConfigLoader currentLoader = GateInGuiceServletContextListener.getCurrentLoader();
            is = currentLoader.loadResourceAsStream(propertyFile);
        }

        try {
            if (is != null) {
                properties = new Properties();
                properties.load(is);
            }
        } catch (IOException e) {
            throw new CreationException(Arrays.asList(new Message("Unable to load properties: " + propertyFile)));
        } finally {
            IOUtils.closeQuietly(is);
        }

        return properties;
    }
}
