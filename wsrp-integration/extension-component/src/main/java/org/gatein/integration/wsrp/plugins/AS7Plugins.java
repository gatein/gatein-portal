/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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

package org.gatein.integration.wsrp.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gatein.wsrp.api.plugins.AbstractPlugins;
import org.picocontainer.Startable;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class AS7Plugins extends AbstractPlugins implements Startable {

    private Map<String, List<String>> implementationNames = new HashMap<String, List<String>>(7);
    private Map<String, ClassLoader> implementationToClassLoader = new HashMap<String, ClassLoader>(7);

    @Override
    protected List<String> getImplementationNamesFor(String pluginClassName, String defaultImplementationClassName) {
        final List<String> names = getCreatedImplementationNames(pluginClassName, 1);

        // only add the default one if it's not already present
        if (!names.contains(defaultImplementationClassName)) {
            names.add(defaultImplementationClassName);
        }

        return names;
    }

    @Override
    protected <T> Class<? extends T> getImplementationNamed(String className, Class<T> pluginClass)
            throws ClassNotFoundException {
        final ClassLoader classLoader = implementationToClassLoader.get(className);
        if (classLoader == null) {
            throw new ClassNotFoundException("Couldn't find a class loader for " + className);
        }
        return classLoader.loadClass(className).asSubclass(pluginClass);
    }

    public void addPluginImplementations(String interfaceName, List<String> serviceImplementations) {
        if (serviceImplementations != null && !serviceImplementations.isEmpty()) {
            List<String> names = getCreatedImplementationNames(interfaceName, serviceImplementations.size());

            names.addAll(serviceImplementations);
        }
    }

    private List<String> getCreatedImplementationNames(String interfaceName, int desiredSizeIfInexistent) {
        List<String> names = implementationNames.get(interfaceName);
        if (names == null) {
            names = new ArrayList<String>(desiredSizeIfInexistent * 2);
            implementationNames.put(interfaceName, names);
        }
        return names;
    }

    public void registerClassloader(String implementation, ClassLoader classLoader) {
        implementationToClassLoader.put(implementation, classLoader);
    }

    @Override
    public void start() {
        // nothing to do
    }

    @Override
    public void stop() {
        // nothing to do
    }
}
