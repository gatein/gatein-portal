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

import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.web.security.GateInToken;
import org.gatein.wci.security.Credentials;

import java.util.Date;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "tkn:tokenentry")
public abstract class TokenEntry
{

   @Name
   public abstract String getId();

   @Property(name = "username")
   public abstract String getUserName();

   public abstract void setUserName(String userName);

   @Property(name = "password")
   public abstract String getPassword();

   public abstract void setPassword(String password);

   @Property(name = "expiration")
   public abstract Date getExpirationTime();

   public abstract void setExpirationTime(Date expirationTime);

   @Destroy
   public abstract void remove();

   public GateInToken getToken()
   {
      return new GateInToken(
         getExpirationTime().getTime(),
         new Credentials(getUserName(), getPassword()));
   }

}
