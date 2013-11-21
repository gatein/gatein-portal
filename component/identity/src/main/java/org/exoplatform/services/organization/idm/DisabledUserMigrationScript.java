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

package org.exoplatform.services.organization.idm;

import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Properties;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.picketlink.idm.api.Attribute;
import org.picketlink.idm.api.AttributesManager;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.IdentitySessionFactory;
import org.picketlink.idm.api.SortOrder;
import org.picketlink.idm.api.Transaction;
import org.picketlink.idm.api.User;
import org.picketlink.idm.api.cfg.IdentityConfiguration;
import org.picketlink.idm.api.query.UserQuery;
import org.picketlink.idm.api.query.UserQueryBuilder;
import org.picketlink.idm.common.exception.IdentityConfigurationException;
import org.picketlink.idm.impl.api.SimpleAttribute;
import org.picketlink.idm.impl.configuration.IdentityConfigurationImpl;
import org.picketlink.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
import org.picketlink.idm.spi.configuration.metadata.IdentityConfigurationMetaData;

public class DisabledUserMigrationScript {

    private String realmName;

    private IdentitySessionFactory identitySessionFactory;

    private SessionFactory sessionFactory_;

    private int batch;

    private static final Log log = ExoLogger.getExoLogger(DisabledUserMigrationScript.class);

    public DisabledUserMigrationScript(Properties config) throws Exception {
        // setup Hibernate
        setupHibernate(config);
        // setup PicketLink
        setupPicketlink(config);

        String b = config.getProperty("batch");
        this.batch = Integer.parseInt(b == null ? "100" : b);
    }

    public void enableAll(int first) throws Exception {
        final long startTime = System.currentTimeMillis();
        startTransaction();
        final int size = getIdentitySession().getPersistenceManager().getUserCount();

        log.info("Starting enable for {} users", size - first);
        int length = batch > size ? size : batch;

        while (length > 0) {
            try {
                log.info("enable for user from {} to {}", first, (first + length));
                startTransaction();
                UserQueryBuilder qb = getIdentitySession().createUserQueryBuilder();
                List<User> users = load(qb, first, length);
                for (User user : users) {
                    setEnabled(user.getId(), true);
                }
                getIdentitySession().save();
                endTransaction();
            } catch (Exception e) {
                log.info("fail to migrate for users from index: {}", first);
                recoverFromIDMError(e);
                break;
            }
            first = first + batch;
            length = batch + first > size ? size - first : batch;
        }

        log.info("Finish enable all users in : {}ms", (System.currentTimeMillis() - startTime));
    }

    private List<org.picketlink.idm.api.User> load(UserQueryBuilder qb, int index, int length) throws Exception {
        qb.sort(SortOrder.ASCENDING).page(index, length);
        UserQuery query = qb.createQuery();
        return getIdentitySession().list(query);
    }

    public void setEnabled(String userName, boolean enabled) throws Exception {
        Attribute[] attrs = new Attribute[] { new SimpleAttribute(UserDAOImpl.USER_ENABLED, String.valueOf(enabled)) };

        IdentitySession session = getIdentitySession();
        AttributesManager am = session.getAttributesManager();
        am.updateAttributes(userName, attrs);
    }

    public void startTransaction() throws Exception {
        if (!getIdentitySession().getTransaction().isActive()) {
            getIdentitySession().beginTransaction();
        }
    }

    public void endTransaction() throws Exception {
        if (getIdentitySession().getTransaction().isActive()) {
            getIdentitySession().getTransaction().commit();
        }
    }

    public void recoverFromIDMError(Exception e) {
        log.error(e);
        try {
            // We need to restart Hibernate transaction if it's available. First rollback old one and then start new one
            Transaction idmTransaction = getIdentitySession().getTransaction();
            if (idmTransaction.isActive()) {
                idmTransaction.rollback();
                log.info("IDM error recovery finished. Old transaction has been rolled-back");
            }
        } catch (Exception e1) {
            log.warn("Error during recovery of old error", e1);
        }
    }

    private void setupHibernate(Properties config) throws Exception {
        final Configuration conf_ = new Configuration();

        conf_.setProperty("hibernate.connection.driver_class", config.getProperty("hibernate.connection.driver_class"));
        conf_.setProperty("hibernate.connection.url", config.getProperty("hibernate.connection.url"));
        conf_.setProperty("hibernate.connection.username", config.getProperty("hibernate.connection.username"));
        conf_.setProperty("hibernate.connection.password", config.getProperty("hibernate.connection.password"));
        conf_.setProperty("hibernate.dialect", config.getProperty("hibernate.dialect"));

        String config_path = config.getProperty("hibernate.config_path");
        URL url = Thread.currentThread().getContextClassLoader().getResource(config_path);
        if (url == null) {
            log.error("hibernate config file not found: {}", config_path);
        } else {
            log.info("adding hibernate config file {}", config_path);
            conf_.addURL(url);
        }

        sessionFactory_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<SessionFactory>() {
            public SessionFactory run() {
                SessionFactory factory = conf_.configure().buildSessionFactory();
                return factory;
            }
        });
    }

    private void setupPicketlink(Properties config) throws Exception {
        String pkConfig = config.getProperty("picketlink.config_file_path");
        URL pk_config_url = Thread.currentThread().getContextClassLoader().getResource(pkConfig);
        if (pk_config_url == null) {
            throw new IllegalStateException("Cannot fine resource: " + pkConfig);
        }
        this.realmName = config.getProperty("picketlink.realmName", "idm_realm");

        IdentityConfigurationMetaData configMD = JAXB2IdentityConfiguration.createConfigurationMetaData(pk_config_url.openStream());
        IdentityConfiguration identityConfiguration = new IdentityConfigurationImpl().configure(configMD);
        identityConfiguration.getIdentityConfigurationRegistry().register(sessionFactory_, "hibernateSessionFactory");

        if (identitySessionFactory == null) {
            try {
                identitySessionFactory = identityConfiguration.buildIdentitySessionFactory();
            } catch (IdentityConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public IdentitySession getIdentitySession() throws Exception {
        return identitySessionFactory.getCurrentIdentitySession(realmName);
    }

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();

        String config_path = args.length > 0 ? args[0] : null;
        if (config_path != null) {
            URL config_url = Thread.currentThread().getContextClassLoader().getResource(config_path);
            if (config_url == null) {
                log.error("config file is not found: {}", config_path);
            } else {
                log.info("using config file {}", config_url);
                config.load(config_url.openStream());
            }
        } else {
            log.info("using default config file");
            InputStream defInput = Thread.currentThread().getContextClassLoader()
                    .getResourceAsStream("configuration.properties");
            config.load(defInput);
        }

        for (Object key : System.getProperties().keySet()) {
            config.setProperty((String) key, System.getProperty((String) key));
        }

        int first = Integer.parseInt(config.getProperty("enable_user_from", "0"));
        DisabledUserMigrationScript service = new DisabledUserMigrationScript(config);
        service.enableAll(first);
        service.sessionFactory_.close();
    }
}