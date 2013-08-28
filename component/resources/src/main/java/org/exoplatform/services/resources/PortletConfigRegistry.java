package org.exoplatform.services.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletConfig;


/**
 * A workaround for GTNPORTAL-2700. Provides a way to store {@link PortletConfig} references per portlet name.
 * {@link PortletConfig}s can then be used to retrieve a the proper {@link ResourceBundle} using
 * {@link PortletConfig#getResourceBundle(java.util.Locale)}.
 *
 * {@link PortletConfigRegistry} is designed to be instantiated per Portal Container so that it cannot happen that
 * {@link #putPortletConfig(String, PortletConfig)} is called twice for the same {@code portletName}.
 *
 * @author Peter Palaga
 *
 */
public class PortletConfigRegistry {

    private final Map<String, PortletConfig> configs = new HashMap<String, PortletConfig>(32);

    /**
     * Returns a {@link PortletConfig} stored for the given {@code portletName} or {@code null}.
     *
     * @param portletName
     * @return A {@link PortletConfig} stored for the given {@code portletName} or {@code null}.
     */
    public PortletConfig getPortletConfig(String portletName) {
        synchronized (configs) {
            return configs.get(portletName);
        }
    }

    /**
     * Stores the given {@code config} to be able to retrieve it later using {@code portletName}.
     *
     * @param portletName
     * @param config
     *
     * @throws IllegalStateException when the given {@code portletName} is already available in the {@link #configs} {@link Map}
     *         .
     */
    public void putPortletConfig(String portletName, PortletConfig config) {
        synchronized (configs) {
            if (configs.containsKey(portletName)) {
                throw new IllegalStateException("Key '" + portletName + "' already available in "
                        + PortletConfigRegistry.class.getName());
            }
            configs.put(portletName, config);
        }
    }

    /**
     * Remove the PortletConfig associated with given portletName
     *
     * @param portletName
     */
    public void removePortletConfig(String portletName) {
        synchronized (configs) {
            configs.remove(portletName);
        }
    }
}
