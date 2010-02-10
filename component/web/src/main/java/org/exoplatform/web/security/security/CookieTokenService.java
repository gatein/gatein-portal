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

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.ContextualTask;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.Credentials;
import org.exoplatform.web.security.GateInToken;

import java.util.Collection;
import java.util.Date;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5,
 * 2009
 */
public class CookieTokenService extends AbstractTokenService<GateInToken, String>
{

   /** . */
   private ChromatticManager chromatticManager;

   /** . */
   private ChromatticLifeCycle chromatticLifeCycle;

   public CookieTokenService(InitParams initParams, ChromatticManager chromatticManager)
   {
      super(initParams);

      //
      this.chromatticManager = chromatticManager;
      this.chromatticLifeCycle = chromatticManager.getLifeCycle("autologin");
   }

   public String createToken(final Credentials credentials)
   {
      if (validityMillis < 0)
      {
         throw new IllegalArgumentException();
      }
      if (credentials == null)
      {
         throw new NullPointerException();
      }
      return new TokenTask<String>() {
         @Override
         protected String execute()
         {
            String tokenId = nextTokenId();
            long expirationTimeMillis = System.currentTimeMillis() + validityMillis;
            GateInToken token = new GateInToken(expirationTimeMillis, credentials);
            TokenContainer container = getTokenContainer();
            container.saveToken(tokenId, token.getPayload(), new Date(token.getExpirationTimeMillis()));
            return tokenId;
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GateInToken getToken(final String id)
   {
      return new TokenTask<GateInToken>() {
         @Override
         protected GateInToken execute()
         {
            return getTokenContainer().getToken((String)id);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public GateInToken deleteToken(final String id)
   {
      return new TokenTask<GateInToken>() {
         @Override
         protected GateInToken execute()
         {
            return getTokenContainer().removeToken((String)id);
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public String[] getAllTokens()
   {
      return new TokenTask<String[]>() {
         @Override
         protected String[] execute()
         {
            TokenContainer container = getTokenContainer();
            Collection<TokenEntry> tokens = container.getAllTokens();
            String[] ids = new String[tokens.size()];
            int count = 0;
            for (TokenEntry token : tokens)
            {
               ids[count++] = token.getId();
            }
            return ids;
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   public long size() throws Exception
   {
      return new TokenTask<Long>() {
         @Override
         protected Long execute()
         {
            TokenContainer container = getTokenContainer();
            Collection<TokenEntry> tokens = container.getAllTokens();
            return (long)tokens.size();
         }
      }.executeWith(chromatticLifeCycle);
   }

   @Override
   protected String decodeKey(String stringKey)
   {
      return stringKey;
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

      protected final TokenContainer getTokenContainer() {
         SessionContext ctx = chromatticLifeCycle.getContext();
         ChromatticSession session = ctx.getSession();
         TokenContainer container = session.findByPath(TokenContainer.class, "autologin");
         if (container == null)
         {
            container = session.insert(TokenContainer.class, "autologin");
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
