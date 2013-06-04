package org.gatein.portal.ui.register;

import juzu.FlashScoped;

import javax.inject.Named;

@Named("flash")
@FlashScoped
public class Flash {

   private String success = "";

   private String error = "";

   private String userName = "";

   public String getSuccess() {
      return success;
   }

   public void setSuccess(String success) {
      this.success = success;
   }

   public String getError() {
      return error;
   }

   public void setError(String error) {
      this.error = error;
   }

   public String getUserName() {
      return userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

}
