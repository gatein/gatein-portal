package org.gatein.common.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;

/**
 * A URLClassLoader that skips delegation of specified resources, and classes to parent.
 */
public class FilteringClassLoader extends URLClassLoader {

    private HashSet<String> filtered = new HashSet<String>();

    public FilteringClassLoader(ClassLoader parent, URL[] urls, String [] filteredNames) {
        super(urls, parent);
        for (String name: filteredNames) {
            filtered.add(name);
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        if (filtered.contains(name)) {
            Class localClass = findLoadedClass(name);
            if (localClass == null) {
                localClass = findClass(name);
            }
            if (resolve) {
                resolveClass(localClass);
            }
            return localClass;
        }

        return super.loadClass(name, resolve);
    }
}