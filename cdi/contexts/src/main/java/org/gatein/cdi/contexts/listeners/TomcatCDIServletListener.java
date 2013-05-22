/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.cdi.contexts.listeners;

import org.gatein.cdi.CDIPortletContextExtension;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Retrieves the {@link CDIPortletContextExtension} instance through reflection as
 * injection into servlet listeners is not supported in Tomcat.
 *
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class TomcatCDIServletListener extends AbstractCDIServletListener {

    private final Logger log = LoggerFactory.getLogger(TomcatCDIServletListener.class);

    private volatile CDIPortletContextExtension extension;

    @Override
    protected CDIPortletContextExtension getExtension() {
        if (null == extension) {
            synchronized (this) {
                if (null == extension) {
                    // Retrieve extension instance from Weld.
                    try {
                        ClassLoader cl = Thread.currentThread().getContextClassLoader();

                        Class<?> containerClass = cl.loadClass("org.jboss.weld.Container");

                        Object containerInstance = containerClass.getMethod("instance").invoke(containerClass);

                        Map beanArchives = (Map) containerClass.getMethod("beanDeploymentArchives").invoke(containerInstance);

                        for (Object key : beanArchives.keySet()) {
                            Object beanManagerImpl = beanArchives.get(key);

                            Object instance = beanManagerImpl.getClass().getMethod("instance").invoke(beanManagerImpl);

                            instance = instance.getClass().getMethod("select", Class.class, Annotation[].class).invoke(instance, CDIPortletContextExtension.class, null);

                            Object extension = instance.getClass().getMethod("get").invoke(instance);

                            if (extension instanceof CDIPortletContextExtension) {
                                this.extension = (CDIPortletContextExtension) extension;
                                break;
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return extension;
    }
}
