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

package org.exoplatform.web.security.security;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.*;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.web.login.InitiateLoginServlet;
import org.exoplatform.web.security.Token;
import org.exoplatform.web.security.TokenStore;
import org.gatein.wci.security.Credentials;
import org.picocontainer.Startable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5,
 * 2009
 *
 * todo julien :
 * - make delay configuration from init param and @Managed setter
 * - start/stop expiration daemon
 * - manually invoke the daemon via @Managed
 *
 * @param <T> the token type
 * @param <K> the token key type
 */
@Managed
@ManagedDescription("Token Store Service")
@NameTemplate({
   @Property(key = "service", value = "TokenStore"),
   @Property(key = "name", value = "{Name}")})
public abstract class AbstractTokenService<T extends Token, K> implements Startable, TokenStore
{

   protected static final String SERVICE_CONFIG = "service.configuration";

   protected static final int DELAY_TIME = 600;

   protected final Random random = new Random();

   protected String name;

   protected long validityMillis;

   private ScheduledExecutorService executor;

   @SuppressWarnings("unchecked")
   public AbstractTokenService(InitParams initParams)
   {
      List<String> params = initParams.getValuesParam(SERVICE_CONFIG).getValues();
      this.name = params.get(0);
      long configValue = new Long(params.get(1));
      this.validityMillis = TimeoutEnum.valueOf(params.get(2)).toMilisecond(configValue);
   }

   public void start()
   {
      // start a thread, garbage expired cookie token every [DELAY_TIME]
      final AbstractTokenService service = this;
      executor = Executors.newSingleThreadScheduledExecutor();
      executor.scheduleWithFixedDelay(new Runnable()
      {
         public void run()
         {
            service.cleanExpiredTokens();
         }
      }, 0, DELAY_TIME, TimeUnit.SECONDS);

   }

   public void stop()
   {
      executor.shutdown();
   }

   public static <T extends AbstractTokenService> T getInstance(Class<T> classType)
   {
      PortalContainer container = PortalContainer.getInstance();
      return classType.cast(container.getComponentInstanceOfType(classType));
   }

   public Credentials validateToken(String stringKey, boolean remove)
   {
      if (stringKey == null)
      {
         throw new NullPointerException();
      }

      //
      K tokenKey = decodeKey(stringKey);

      T token;
      try
      {
         if (remove)
         {
            token = this.deleteToken(tokenKey);
         }
         else
         {
            token = this.getToken(tokenKey);
         }

         if (token != null)
         {
            boolean valid = token.getExpirationTimeMillis() > System.currentTimeMillis();
            if (valid)
            {
               return token.getPayload();
            }
            else if (!remove)
            {
               this.deleteToken(tokenKey);
            }
         }
      }
      catch (Exception e)
      {
      }

      return null;
   }

   @Managed
   @ManagedDescription("Clean all tokens are expired")
   @Impact(ImpactType.IDEMPOTENT_WRITE)
   public void cleanExpiredTokens()
   {
      K[] ids = getAllTokens();
      for (K id : ids)
      {
         T token = getToken(id);
         if (token.isExpired())
         {
            deleteToken(id);
         }
      }
   }

   @Managed
   @ManagedDescription("Get time for token expiration in seconds")
   public long getValidityTime()
   {
      return validityMillis / 1000;
   }

   @Managed
   @ManagedDescription("The expiration daemon period time in seconds")
   public long getPeriodTime()
   {
      return DELAY_TIME;
   }

   @Managed
   @ManagedDescription("The token service name")
   public String getName()
   {
      return name;
   }

   public abstract T getToken(K id);

   public abstract T deleteToken(K id);

   public abstract K[] getAllTokens();

   /**
    * Decode a key from its string representation.
    *
    * @param stringKey the key a s a string
    * @return the typed key
    */
   protected abstract K decodeKey(String stringKey);

   // We don't make it a property as retrieving the value can be an expensive operation
   @Managed
   @ManagedDescription("The number of tokens")
   @Impact(ImpactType.READ)
   public abstract long size() throws Exception;

   private enum TimeoutEnum {
      SECOND(1000), MINUTE(1000 * 60), HOUR(1000 * 60 * 60), DAY(1000 * 60 * 60 * 24);

      private long multiply;

      private TimeoutEnum(long multiply)
      {
         this.multiply = multiply;
      }

      public long toMilisecond(long configValue)
      {
         return configValue * multiply;
      }
   }

   protected String nextTokenId()
   {
      return InitiateLoginServlet.COOKIE_NAME + random.nextInt();
   }
}
