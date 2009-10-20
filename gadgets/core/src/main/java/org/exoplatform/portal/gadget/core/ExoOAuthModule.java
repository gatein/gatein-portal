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
import com.google.inject.name.Names;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthModule;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;

/**
 * Created by IntelliJ IDEA.
 * User: jeremi
 * Date: Jan 9, 2009
 * Time: 10:45:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class ExoOAuthModule extends OAuthModule
{
   private static final String SIGNING_KEY_FILE = "gadgets.signingKeyFile";

   private static final String SIGNING_KEY_NAME = "gadgets.signingKeyName";

   private static final String CALLBACK_URL = "gadgets.signing.global-callback-url";

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

   public static class ExoOAuthStoreProvider extends OAuthStoreProvider
   {
      @Inject
      public ExoOAuthStoreProvider(ContainerConfig config)
      {
         //super(config.getString(ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_FILE), config.getString(ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_NAME));
         super(config.getString(ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_FILE), config.getString(
            ContainerConfig.DEFAULT_CONTAINER, SIGNING_KEY_NAME), config.getString(ContainerConfig.DEFAULT_CONTAINER,
            CALLBACK_URL));
      }
   }

   public static class ExoOAuthRequestProvider extends OAuthRequestProvider
   {
      private final HttpFetcher fetcher;

      private final OAuthFetcherConfig config;

      @Inject
      public ExoOAuthRequestProvider(HttpFetcher fetcher, OAuthFetcherConfig config)
      {
         super(fetcher, config);
         this.fetcher = fetcher;
         this.config = config;
      }

      public OAuthRequest get()
      {
         return new OAuthRequest(config, fetcher);
      }
   }
}
