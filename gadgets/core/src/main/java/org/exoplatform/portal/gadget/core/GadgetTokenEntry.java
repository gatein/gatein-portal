package org.exoplatform.portal.gadget.core;

import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

@PrimaryType(name = "tkn:gadgettoken")
public abstract class GadgetTokenEntry
{

   public BasicOAuthStoreTokenIndex getKey()
   {
      BasicOAuthStoreTokenIndex key = new BasicOAuthStoreTokenIndex();
      key.setGadgetUri(getGadgetUri());
      key.setModuleId(getModuleId());
      key.setServiceName(getServiceName());
      key.setTokenName(getTokenName());
      key.setUserId(getUserId());
      return key;
   }

   @Property(name = "userId")
   public abstract String getUserId();

   public abstract void setUserId(String userId);

   @Property(name = "gadgetUri")
   public abstract String getGadgetUri();

   public abstract void setGadgetUri(String gadgetUri);

   @Property(name = "moduleId")
   public abstract long getModuleId();

   public abstract void setModuleId(long moduleId);

   @Property(name = "tokenName")
   public abstract String getTokenName();

   public abstract void setTokenName(String tokenName);

   @Property(name = "serviceName")
   public abstract String getServiceName();

   public abstract void setServiceName(String serviceName);

   @Property(name = "accessToken")
   public abstract String getAccessToken();

   public abstract void setAccessToken(String accessToken);

   @Property(name = "tokenSecret")
   public abstract String getTokenSecret();

   public abstract void setTokenSecret(String tokenSecret);

   @Property(name = "sessionHandle")
   public abstract String getSessionHandle();

   public abstract void setSessionHandle(String sessionHandle);

   @Property(name = "tokenExpireMillis")
   public abstract long getTokenExpireMillis();

   public abstract void setTokenExpireMillis(long tokenExpireMillis);

   @Destroy
   public abstract void remove();

   public GadgetToken getToken()
   {
      return new GadgetToken(getAccessToken(), getTokenSecret(), getServiceName(), getTokenExpireMillis());
   }
}
