/*
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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.OneToMany;
import org.exoplatform.web.security.GateInToken;
import org.gatein.wci.security.Credentials;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "tkn:tokencontainer")
public abstract class TokenContainer
{

   @Create
   protected abstract TokenEntry createToken();

   @OneToMany
   protected abstract Map<String, TokenEntry> getTokens();

   public Collection<TokenEntry> getAllTokens()
   {
      return getTokens().values();
   }

   public GateInToken getToken(String tokenId)
   {
      Map<String, TokenEntry> tokens = getTokens();
      TokenEntry entry = tokens.get(tokenId);
      return entry != null ? entry.getToken() : null;
   }

   public GateInToken removeToken(String tokenId)
   {
      Map<String, TokenEntry> tokens = getTokens();
      TokenEntry entry = tokens.get(tokenId);
      if (entry != null)
      {
         GateInToken token = entry.getToken();
         entry.remove();
         return token;
      }
      else
      {
         return null;
      }
   }

   public GateInToken saveToken(String tokenId, Credentials credentials, Date expirationTime)
   {
      Map<String, TokenEntry> tokens = getTokens();
      TokenEntry entry = tokens.get(tokenId);
      if (entry == null)
      {
         entry = createToken();
         tokens.put(tokenId, entry);
         entry.setUserName(credentials.getUsername());
         entry.setPassword(credentials.getPassword());
      }
      entry.setExpirationTime(expirationTime);
      return entry.getToken();
   }
   
   public GateInToken encodeAndSaveToken(String tokenId, Credentials credentials, Date expirationTime, AbstractCodec codec)
   {
      Map<String, TokenEntry> tokens = getTokens();
      TokenEntry entry = tokens.get(tokenId);
      if (entry == null)
      {
         entry = createToken();
         tokens.put(tokenId, entry);
         entry.setUserName(credentials.getUsername());
         entry.setPassword(codec.encode(credentials.getPassword()));
      }
      entry.setExpirationTime(expirationTime);
      return entry.getToken();
   }
   
   public GateInToken getTokenAndDecode(String tokenId, AbstractCodec codec)
   {
      Map<String, TokenEntry> tokens = getTokens();
      TokenEntry entry = tokens.get(tokenId);
      if(entry != null)
      {
         GateInToken gateInToken = entry.getToken();
         Credentials payload = gateInToken.getPayload();
         
         //Return a cloned GateInToken
         return new GateInToken(gateInToken.getExpirationTimeMillis(), new Credentials(payload.getUsername(), codec
               .decode(payload.getPassword())));

      }
      return null;
   }

}
