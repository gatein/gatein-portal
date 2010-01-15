package org.exoplatform.portal.gadget.core;

import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;
import org.exoplatform.web.security.Token;


public class GadgetToken extends TokenInfo implements Token
{
   public GadgetToken(String accessToken, String tokenSecret, String sessionHandle,
      long tokenExpireMillis)
   {
      super(accessToken, tokenSecret, sessionHandle, tokenExpireMillis);
   }

   public boolean isExpired()
   {
      return false;
   }
}
