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

import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.ContextualTask;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.security.AbstractTokenService;
import org.gatein.wci.security.Credentials;

import java.util.Collection;

public class GadgetTokenInfoService extends AbstractTokenService<GadgetToken, BasicOAuthStoreTokenIndex>
{

   private ChromatticLifeCycle chromatticLifeCycle;

   public GadgetTokenInfoService(InitParams initParams, ChromatticManager chromatticManager)
   {
      super(initParams);
      chromatticLifeCycle = chromatticManager.getLifeCycle("gadgettokens");
   }

   public GadgetToken createToken(final BasicOAuthStoreTokenIndex key, final TokenInfo tokenInfo)
   {
      return new TokenTask<GadgetToken>()
      {
         @Override
         protected GadgetToken execute()
         {
            GadgetTokenContainer container = getGadgetTokenContainer();
            return container.saveToken(key, tokenInfo);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GadgetToken getToken(final BasicOAuthStoreTokenIndex key)
   {
      return new TokenTask<GadgetToken>()
      {
         @Override
         protected GadgetToken execute()
         {
            return getGadgetTokenContainer().getToken(key);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GadgetToken deleteToken(final BasicOAuthStoreTokenIndex key)
   {
      return new TokenTask<GadgetToken>()
      {
         @Override
         protected GadgetToken execute()
         {
            return getGadgetTokenContainer().removeToken(key);
         }
      }.executeWith(chromatticLifeCycle);
   }
   
   @Override
   public BasicOAuthStoreTokenIndex[] getAllTokens()
   {
      return new TokenTask<BasicOAuthStoreTokenIndex[]>()
      {
         @Override
         protected BasicOAuthStoreTokenIndex[] execute()
         {
            GadgetTokenContainer container = getGadgetTokenContainer();
            Collection<GadgetTokenEntry> tokens = container.getGadgetTokens().values();
            BasicOAuthStoreTokenIndex[] gadgetTokens = new BasicOAuthStoreTokenIndex[9];
            int count = 0;
            for(GadgetTokenEntry tokenEntry : tokens) {
               gadgetTokens[count++] = tokenEntry.getKey();
            }
            return gadgetTokens;
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public long size() throws Exception
   {
      return new TokenTask<Long>()
      {
         @Override
         protected Long execute()
         {
            GadgetTokenContainer container = getGadgetTokenContainer();
            Collection<GadgetTokenEntry> tokens = container.getGadgetTokens().values();
            return (long)tokens.size();
         }
      }.executeWith(chromatticLifeCycle);
   }

   public String createToken(Credentials credentials) throws IllegalArgumentException, NullPointerException
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected BasicOAuthStoreTokenIndex decodeKey(String stringKey)
   {
      throw new UnsupportedOperationException();
   }

   /**
    * Wraps token store logic conveniently.
    *
    * @param <V> the return type
    */
   private abstract class TokenTask<V> extends ContextualTask<V>
   {

      /** . */
      private SessionContext context;

      protected final GadgetTokenContainer getGadgetTokenContainer()
      {
         ChromatticSession session = context.getSession();
         GadgetTokenContainer container = session.findByPath(GadgetTokenContainer.class, "gadgettokens");
         if (container == null)
         {
            container = session.insert(GadgetTokenContainer.class, "gadgettokens");
         }
         return container;
      }

      @Override
      protected V execute(SessionContext context)
      {
         this.context = context;

         //
         try
         {
            return execute();
         }
         finally
         {
            this.context = null;
         }
      }

      protected abstract V execute();

   }
}
