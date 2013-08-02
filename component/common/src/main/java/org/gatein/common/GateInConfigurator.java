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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.commons.utils.PropertiesLoader;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.commons.utils.Tools;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.picocontainer.Startable;

/**
 * <p>The property configurator configures a set of system properties via the {@link PropertyManager}
 * static methods. It is possible to configure properties from the init params or from an external
 * file. Existing system properties are preserved and not overidden.</p>
 * <p/>
 * <p>The constructor will inspect the {@link org.exoplatform.container.xml.InitParams} params argument
 * to find a param named <code>properties</code> with an expected type of {@link PropertiesParam}. The
 * properties contained in that argument will be sourced into the property manager. When such properties
 * are loaded from an XML configuration file, the values are evaluated and property substitution occurs.</p>
 * <p/>
 * <p>When the property {@link PropertyManager#PROPERTIES_URL} is not null and points to a valid property
 * file it will loaded and sourced. Property values will be evaluated and property substitution will
 * occur. When the file name ends with the <code>.properties</code> properties are loaded using the
 * {@link java.util.Properties#load(java.io.InputStream)} method. When the file name ends with th
 * <code>.xml</code> properties are loaded using the {@link java.util.Properties#loadFromXML(java.io.InputStream)}
 * method. Suffix checks are done ignoring the case.</p>
 * <p/>
 * <p>When properties are loaded from an URL, the order of the properties declarations in the file matters.</p>
 * <p/>
 * <p>In addition this configuration also configure the JAAS login configuration to setup the virtual machine
 * JAAS configuration when it is not present (<code>java.security.auth.login.config</code> system property).</p>
 */
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

        // Resolve the property file configuration path
        String path = null;
        if (params != null) {
            PropertiesParam propertiesParam = params.getPropertiesParam("properties");
            if (propertiesParam != null) {
                log.debug("Going to initialize properties from init param");
                for (Iterator<Property> i = propertiesParam.getPropertyIterator(); i.hasNext(); ) {
                    Property property = i.next();
                    String name = property.getName();
                    String value = property.getValue();
                    log.debug("Adding property from init param " + name + " = " + value);
                    PropertyManager.setProperty(name, value);
                }
            }
            ValueParam pathParam = params.getValueParam("properties.url");
            if (pathParam != null) {
                path = pathParam.getValue();
                if (log.isDebugEnabled()) {
                    log.debug("Using file path " + path + " found from configuration");
                }
            }
        }
        String systemPath = PropertyManager.getProperty(PropertyManager.PROPERTIES_URL);
        if (systemPath != null) {
            path = systemPath;
            if (log.isDebugEnabled()) {
                log.debug("Using file path " + path + " found from system properties");
            }
        }

        //
        if (path != null) {
            log.debug("Found property file path " + path);
            configureProperties(confManager, path);
        }

        //
        if (PropertyManager.getProperty(JAAS_KEY) == null) {
            configureJAAS();
        }
    }

    private void configureProperties(ConfigurationManager confManager, String path) {
        try {
            URL url = confManager.getURL(path);
            Map<String, String> props = loadProperties(url);
            if (props != null) {
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    String propertyName = entry.getKey();
                    String propertyValue = entry.getValue();
                    log.debug("Set " + propertyName + "=" + propertyValue);
                    PropertyManager.setProperty(propertyName, propertyValue);
                }
            }
        } catch (Exception e) {
            log.error("Cannot load property file " + path, e);
        }
    }

    /**
     * Loads the properties file corresponding to the given url
     *
     * @param url              the url of the properties file
     * @return a {@link Map} of properties
     */
    public static Map<String, String> loadProperties(URL url) {
        LinkedHashMap<String, String> props = null;
        String path = null;
        InputStream in = null;
        try {
            //
            if (url != null) {
                path = url.getPath();
                in = url.openStream();
            }

            //
            if (in != null) {

                // Resolve properties file
                String fileName = url.getFile();
                if (Tools.endsWithIgnoreCase(path, ".properties")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Attempt to load property file " + path);
                    }
                    props = PropertiesLoader.load(in);
                } else if (Tools.endsWithIgnoreCase(fileName, ".xml")) {
                    if (log.isDebugEnabled()) {
                        log.debug("Attempt to load property file " + path + " with XML format");
                    }
                    props = PropertiesLoader.loadFromXML(in);
                } else if (log.isDebugEnabled()) {
                    log.debug("Will not load property file" + path + " because its format is not recognized");
                }

                // Resolve properties values
                if (props != null) {

                    // Remove existing system properties
                    props.keySet().removeAll(System.getProperties().keySet());

                    // Those properties are used for variables resolution
                    final Map<String, Object> currentProps = new HashMap<String, Object>();

                    // Resolve each value
                    for (Map.Entry<String, String> entry : props.entrySet()) {
                        String propertyName = entry.getKey();
                        String propertyValue = entry.getValue();
                        propertyValue = Deserializer.resolveVariables(propertyValue, currentProps);
                        props.put(propertyName, propertyValue);
                        currentProps.put(propertyName, propertyValue);
                    }
                }
            } else {
                log.error("Could not load property file " + path);
            }
        } catch (Exception e) {
            log.error("Cannot load property file " + path, e);
        } finally {
            Safe.close(in);
        }
        return props;
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
