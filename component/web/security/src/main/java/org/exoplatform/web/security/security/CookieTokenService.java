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
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.GateInToken;
import org.gatein.wci.security.Credentials;

import java.util.Collection;
import java.util.Date;

/**
 * Created by The eXo Platform SAS Author : liem.nguyen ncliam@gmail.com Jun 5,
 * 2009
 */
public class CookieTokenService extends AbstractTokenService<GateInToken, String>
{

   /** . */
   public static final String LIFECYCLE_NAME="lifecycle-name";
	
   /** . */
   private ChromatticLifeCycle chromatticLifeCycle;
   
   /** . */
   private String lifecycleName="autologin";

   //TODO: Introduce the concept of priority and store the plugins in a map structure
   private AbstractCodec codec;
   
   public CookieTokenService(InitParams initParams, ChromatticManager chromatticManager)
   {
      super(initParams);

      if (initParams.getValuesParam(SERVICE_CONFIG).getValues().size() > 3)
      {
    	  lifecycleName = (String)initParams.getValuesParam(SERVICE_CONFIG).getValues().get(3);
      }
      this.chromatticLifeCycle = chromatticManager.getLifeCycle(lifecycleName);
      
      //Set the default codec
      this.codec = new ToThrowAwayCodec();
   }

   public final void setupCodec(ComponentPlugin codecPlugin)
   {
      if(codecPlugin instanceof AbstractCodec)
      {
         this.codec = (AbstractCodec)codecPlugin;
      }
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
            
            //Save the token, password is encoded thanks to the codec
            container.encodeAndSaveToken(tokenId, token.getPayload(), new Date(expirationTimeMillis), codec);
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
            //Get the token, encoded password is decoded thanks to codec
            return getTokenContainer().getTokenAndDecode(id, codec);
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

      protected final TokenContainer getTokenContainer() {
         SessionContext ctx = chromatticLifeCycle.getContext();
         ChromatticSession session = ctx.getSession();
         TokenContainer container = session.findByPath(TokenContainer.class, lifecycleName);
         if (container == null)
         {
            container = session.insert(TokenContainer.class, lifecycleName);
         }
         return container;
      }

      @Override
      protected V execute(SessionContext context)
      {
         return execute();
      }

      protected abstract V execute();

   }
}
