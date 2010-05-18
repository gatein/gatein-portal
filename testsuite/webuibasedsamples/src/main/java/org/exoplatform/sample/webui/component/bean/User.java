package org.exoplatform.sample.webui.component.bean;

import java.util.Date;

public  class User
{
   private String userName;

   private String password;

   private String favoriteColor;

   private String position;

   private boolean receiveEmail;

   private String gender;

   private Date dateOfBirth;

   private String description;

   public User(String userName, String favoriteColor, String position, Date dateOfBirth)
   {
      super();
      this.userName = userName;
      this.favoriteColor = favoriteColor;
      this.position = position;
      this.dateOfBirth = dateOfBirth;
   }

   public String getUserName()
   {
      return userName;
   }

   public void setUserName(String userName)
   {
      this.userName = userName;
   }

   public String getFavoriteColor()
   {
      return favoriteColor;
   }

   public void setFavoriteColor(String favoriteColor)
   {
      this.favoriteColor = favoriteColor;
   }

   public String getPosition()
   {
      return position;
   }

   public void setPosition(String position)
   {
      this.position = position;
   }

   public Date getDateOfBirth()
   {
      return dateOfBirth;
   }

   public void setDateOfBirth(Date dateOfBirth)
   {
      this.dateOfBirth = dateOfBirth;
   }
}