package org.exoplatform.portal.gadget.core;

import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;

import java.util.Map;

@PrimaryType(name = "tkn:gadgettokencontainer")
public abstract class GadgetTokenContainer
{
   @Create
   protected abstract GadgetTokenEntry createGadgetToken();

   @OneToMany
   protected abstract Map<String, GadgetTokenEntry> getGadgetTokens();

   public GadgetToken getToken(BasicOAuthStoreTokenIndex tokenKey)
   {
      Map<String, GadgetTokenEntry> tokens = getGadgetTokens();
      for (GadgetTokenEntry tokenEntry : tokens.values())
      {
         BasicOAuthStoreTokenIndex key = tokenEntry.getKey();
         if (tokenKey.equals(key)) return tokenEntry.getToken();
      }
      return null;
   }

   public GadgetToken removeToken(BasicOAuthStoreTokenIndex tokenKey)
   {
      Map<String, GadgetTokenEntry> tokens = getGadgetTokens();
      
      for (GadgetTokenEntry tokenEntry : tokens.values())
      {
         BasicOAuthStoreTokenIndex key = tokenEntry.getKey();
         if (tokenKey.equals(key))
         {
            GadgetToken token = tokenEntry.getToken();
            tokenEntry.remove();
            return token;
         }
      }
      return null;
   }

   public GadgetToken saveToken(BasicOAuthStoreTokenIndex tokenKey, TokenInfo tokenInfo, long expirationTime)
   {
      Map<String, GadgetTokenEntry> tokens = getGadgetTokens();
      GadgetTokenEntry entry = null;
      for (GadgetTokenEntry item : tokens.values())
      {
         BasicOAuthStoreTokenIndex key = item.getKey();
         if (tokenKey.equals(key))
         {
            entry = item;
         }
      }
      if (entry == null)
      {
         entry = createGadgetToken();
         tokens.put("gadgettoken" + System.currentTimeMillis(), entry);
      }
      entry.setGadgetUri(tokenKey.getGadgetUri());
      entry.setModuleId(tokenKey.getModuleId());
      entry.setServiceName(tokenKey.getServiceName());
      entry.setTokenName(tokenKey.getTokenName());
      entry.setUserId(tokenKey.getUserId());
      
      entry.setAccessToken(tokenInfo.getAccessToken());
      entry.setTokenSecret(tokenInfo.getTokenSecret());
      entry.setSessionHandle(tokenInfo.getSessionHandle() == null ? "" : tokenInfo.getSessionHandle());
      entry.setTokenExpireMillis(expirationTime);
      return entry.getToken();
   }
}