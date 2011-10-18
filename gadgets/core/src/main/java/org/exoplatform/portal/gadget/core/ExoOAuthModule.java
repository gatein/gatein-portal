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

package org.exoplatform.portal.gadget.core;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.oauth.BasicOAuthStore;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret.KeyType;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthModule;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Jan 9, 2009
 * Time: 10:45:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExoOAuthModule extends OAuthModule
{
   public static final String SIGNING_KEY_FILE = "gadgets.signingKeyFile";

   private static final String SIGNING_KEY_NAME = "gadgets.signingKeyName";

   private static final String CALLBACK_URL = "gadgets.signing.global-callback-url";
   
   private static final String OAUTH_CONFIG = "config/oauth.json";
   
   private static final Log log = ExoLogger.getLogger(OAuthModule.class);;

   @Override
   protected void configure()
   {
      // Used for encrypting client-side OAuth state.
      bind(BlobCrypter.class).annotatedWith(Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER)).toProvider(
         OAuthCrypterProvider.class);

      // Used for persistent storage of OAuth access tokens.
      bind(OAuthStore.class).toProvider(ExoOAuthStoreProvider.class);
      bind(OAuthRequest.class).toProvider(OAuthRequestProvider.class);

      // TODO: tung.dang add some missing implement
      bind(Boolean.class).annotatedWith(Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED)).toInstance(
         Boolean.TRUE);
   }

   @Singleton
   public static class ExoOAuthStoreProvider implements Provider<OAuthStore>
   {
     
     private final ExoOAuthStore store;
     
     @Inject
      public ExoOAuthStoreProvider(ContainerConfig config)
      {
         store = new ExoOAuthStore();
         
         String signingKeyFile = config.getString(ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_FILE);
         String signingKeyName = config.getString(ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_NAME);
         loadDefaultKey(signingKeyFile, signingKeyName);
         
         String defaultCallbackUrl = config.getString(ContainerConfig.DEFAULT_CONTAINER,CALLBACK_URL);
         store.setDefaultCallbackUrl(defaultCallbackUrl);
         loadConsumers();
      }
      


      private void loadDefaultKey(String signingKeyFile, String signingKeyName) {
        BasicOAuthStoreConsumerKeyAndSecret key = null;
        if (!StringUtils.isBlank(signingKeyFile)) {
          try {
            log.info("Loading OAuth signing key from " + signingKeyFile);
            String privateKey = IOUtils.toString(ResourceLoader.open(signingKeyFile), "UTF-8");
            privateKey = BasicOAuthStore.convertFromOpenSsl(privateKey);
            key = new BasicOAuthStoreConsumerKeyAndSecret(null, privateKey, KeyType.RSA_PRIVATE,
                signingKeyName, null);
          } catch (Throwable t) {
            log.warn("Couldn't load key file " + signingKeyFile);
          }
        }
        if (key != null) {
          store.setDefaultKey(key);
        } else {
          log.warn("Couldn't load OAuth signing key.  To create a key, run:\n" +
              "  openssl req -newkey rsa:1024 -days 365 -nodes -x509 -keyout testkey.pem \\\n" +
              "     -out testkey.pem -subj '/CN=mytestkey'\n" +
              "  openssl pkcs8 -in testkey.pem -out oauthkey.pem -topk8 -nocrypt -outform PEM\n" +
              '\n' +
              "Then edit gadgets.properties and add these lines:\n" +
              SIGNING_KEY_FILE + "=<path-to-oauthkey.pem>\n");
        }
      }

      private void loadConsumers() {
        try {
          String oauthConfigString = ResourceLoader.getContent(OAUTH_CONFIG);
          store.initFromConfigString(oauthConfigString);
        } catch (Throwable t) {
          log.warn("Failed to initialize OAuth consumers from " + OAUTH_CONFIG, t);
        }
      }

      public OAuthStore get() {
        return store;
      }
    }
}
