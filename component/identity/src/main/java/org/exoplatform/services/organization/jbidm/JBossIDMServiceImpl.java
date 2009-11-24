/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.services.organization.jbidm;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.Dialect;
import org.jboss.identity.idm.api.IdentitySession;
import org.jboss.identity.idm.api.IdentitySessionFactory;
import org.jboss.identity.idm.api.cfg.IdentityConfiguration;
import org.jboss.identity.idm.common.exception.IdentityConfigurationException;
import org.jboss.identity.idm.impl.configuration.IdentityConfigurationImpl;
import org.jboss.identity.idm.impl.configuration.jaxb2.JAXB2IdentityConfiguration;
import org.jboss.identity.idm.spi.configuration.metadata.IdentityConfigurationMetaData;
import org.picocontainer.Startable;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.naming.InitialContext;

public class JBossIDMServiceImpl implements JBossIDMService, Startable
{

   private static Log log_ = ExoLogger.getLogger(JBossIDMServiceImpl.class);

   public static final String PARAM_CONFIG_OPTION = "config";

   public static final String PARAM_HIBERNATE_PROPS = "hibernate.properties";

   public static final String PARAM_HIBERNATE_MAPPINGS = "hibernate.mappings";

   public static final String PARAM_HIBERNATE_ANNOTATIONS = "hibernate.annotations";

   public static final String PARAM_JNDI_NAME_OPTION = "jndiName";

   public static final String DEFAULT_REALM_NAME_OPTION = "PortalRealm";

   // We may have several portal containers thus we need one indentitySessionFactory per portal container
   //   private static IdentitySessionFactory identitySessionFactory;
   private IdentitySessionFactory identitySessionFactory;

   private String config;

   private String defaultRealmName = "PortalRealm";

   private IdentityConfiguration identityConfiguration;

   private JBossIDMServiceImpl()
   {
   }

   public JBossIDMServiceImpl(InitParams initParams, ConfigurationManager confManager) throws Exception
   {
      ValueParam config = initParams.getValueParam(PARAM_CONFIG_OPTION);
      ValueParam jndiName = initParams.getValueParam(PARAM_JNDI_NAME_OPTION);
      ValueParam realmName = initParams.getValueParam(DEFAULT_REALM_NAME_OPTION);

      if (config == null && jndiName == null)
      {
         throw new IllegalStateException("Either '" + PARAM_CONFIG_OPTION + "' or '" + PARAM_JNDI_NAME_OPTION
            + "' parameter must " + "be specified");
      }
      if (realmName != null)
      {
         this.defaultRealmName = realmName.getValue();
      }

      SessionFactory sf = null;

      if (initParams.containsKey(PARAM_HIBERNATE_PROPS))
      {
         PropertiesParam param = initParams.getPropertiesParam(PARAM_HIBERNATE_PROPS);
         AnnotationConfiguration conf_ = new AnnotationConfiguration();
         Iterator properties = param.getPropertyIterator();
         while (properties.hasNext())
         {
            Property p = (Property)properties.next();

            //
            String name = p.getName();
            String value = p.getValue();

            // Julien: Don't remove that unless you know what you are doing
            if (name.equals("hibernate.dialect"))
            {
               Package pkg = Dialect.class.getPackage();
               String dialect = value.substring(22);
               value = pkg.getName() + "." + dialect; // 22 is the length of
               // "org.hibernate.dialect"
               log_.info("Using dialect " + dialect);
            }

            //
            conf_.setProperty(name, value);
         }

         ClassLoader cl = Thread.currentThread().getContextClassLoader();

         if (initParams.containsKey(PARAM_HIBERNATE_MAPPINGS))
         {
            ValuesParam mappings = initParams.getValuesParam(PARAM_HIBERNATE_MAPPINGS);

            List<String> paths = mappings.getValues();

            for (String path : paths)
            {
               URL url = cl.getResource(path);
               log_.info("Adding  Hibernate Mapping: " + path);
               conf_.addURL(url);
            }
         }

         if (initParams.containsKey(PARAM_HIBERNATE_ANNOTATIONS))
         {
            ValuesParam annotations = initParams.getValuesParam(PARAM_HIBERNATE_ANNOTATIONS);

            List<String> classes = annotations.getValues();

            for (String name : classes)
            {
               Class clazz = cl.loadClass(name);
               conf_.addAnnotatedClass(clazz);
            }

         }

         sf = conf_.buildSessionFactory();

      }

      if (config != null)
      {
         this.config = config.getValue();
         URL configURL = confManager.getURL(this.config);

         if (configURL == null)
         {
            throw new IllegalStateException("Cannot fine resource: " + this.config);
         }

         IdentityConfigurationMetaData configMD =
            JAXB2IdentityConfiguration.createConfigurationMetaData(confManager.getInputStream(this.config));

         identityConfiguration = new IdentityConfigurationImpl().configure(configMD);

         if (sf != null)
         {
            identityConfiguration.getIdentityConfigurationRegistry().register(sf, "hibernateSessionFactory");
         }
      }
      else
      {
         identitySessionFactory = (IdentitySessionFactory)new InitialContext().lookup(jndiName.getValue());
      }

   }

   public void start()
   {
      if (identitySessionFactory == null)
      {
         try
         {
            identitySessionFactory = identityConfiguration.buildIdentitySessionFactory();
         }
         catch (IdentityConfigurationException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public void stop()
   {
   }

   public IdentitySessionFactory getIdentitySessionFactory()
   {
      return identitySessionFactory; //To change body of implemented methods use File | Settings | File Templates.
   }

   public IdentitySession getIdentitySession() throws Exception
   {
      return getIdentitySessionFactory().getCurrentIdentitySession(defaultRealmName);
   }

   public IdentitySession getIdentitySession(String realm) throws Exception
   {
      if (realm == null)
      {
         throw new IllegalArgumentException("Realm name cannot be null");
      }
      return getIdentitySessionFactory().getCurrentIdentitySession(realm);
   }
}
