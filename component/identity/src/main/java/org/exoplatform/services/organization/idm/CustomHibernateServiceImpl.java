/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.services.organization.idm;

import java.security.PrivilegedAction;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.database.impl.HibernateServiceImpl;
import org.gatein.common.classloader.DelegatingClassLoader;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.BootstrapServiceRegistry;
import org.hibernate.service.BootstrapServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.internal.StandardServiceRegistryImpl;

/**
 * Custom implementation of {@link org.exoplatform.services.database.HibernateService} compatible with Hibernate4
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class CustomHibernateServiceImpl extends HibernateServiceImpl {
    private SessionFactory sessFactory;

    public CustomHibernateServiceImpl(InitParams initParams, CacheService cacheService) {
        super(initParams, cacheService);
    }

    @Override
    public SessionFactory getSessionFactory() {
        if (sessFactory == null) {
            sessFactory = SecurityHelper.doPrivilegedAction(new PrivilegedAction<SessionFactory>() {
                public SessionFactory run() {
                    return buildSessionFactory();
                }
            });
        }
        return sessFactory;
    }

    // We need custom ClassloaderService, which is able to locate our UserTransactionJtaPlatform class from tccl.
    // Default ClassLoaderServiceImpl is bootstrapped with no-arg constructor, so it uses only Hibernate Classloader
    // TODO: Remove once https://issues.jboss.org/browse/HIBERNATE-137 will be fixed (likely whole class can be removed)
    protected SessionFactory buildSessionFactory() {
        Configuration conf = getHibernateConfiguration();

        BootstrapServiceRegistry bootstrapRegistry = createHibernateBootstrapServiceRegistry();

        final ServiceRegistry serviceRegistry = new ServiceRegistryBuilder(bootstrapRegistry).applySettings(
                conf.getProperties()).buildServiceRegistry();
        conf.setSessionFactoryObserver(new SessionFactoryObserver() {
            @Override
            public void sessionFactoryCreated(SessionFactory factory) {
            }

            @Override
            public void sessionFactoryClosed(SessionFactory factory) {
                ((StandardServiceRegistryImpl) serviceRegistry).destroy();
            }
        });

        final ClassLoader old = SecurityHelper.doPrivilegedAction(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });

        try {
            SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
                public Void run() {
                    DelegatingClassLoader cl = new DelegatingClassLoader(old,
                            org.picketlink.idm.api.IdentitySessionFactory.class.getClassLoader());
                    Thread.currentThread().setContextClassLoader(cl);
                    return null;
                }
            });
            return conf.buildSessionFactory(serviceRegistry);
        } finally {
            if (old != null) {
                SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>() {
                    public Void run() {
                        Thread.currentThread().setContextClassLoader(old);
                        return null;
                    }
                });
            }
        }
    }

    /**
     * @return bootstrapServiceRegistry, which is using classloaderService, which is able to find classes from tccl, hibernate
     *         classloader and system classloader
     */
    protected BootstrapServiceRegistry createHibernateBootstrapServiceRegistry() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ClassLoader hibernateCl = BootstrapServiceRegistry.class.getClassLoader();
        return new BootstrapServiceRegistryBuilder().withApplicationClassLoader(tccl).withHibernateClassLoader(hibernateCl)
                .build();
    }

}
