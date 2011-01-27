/*
* Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.gatein.common.NotYetImplemented;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.web.security.GateInToken;
import org.gatein.wci.security.Credentials;

/**
 * This class is only used to get validity form configuration.
 * 
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class TicketConfiguration extends AbstractTokenService<GateInToken, String>
{

   public TicketConfiguration(InitParams initParams)
   {
      super(initParams);
   }

   @Override
   public GateInToken getToken(String id)
   {
      throw new NotYetImplemented();
   }

   @Override
   public GateInToken deleteToken(String id)
   {
      throw new NotYetImplemented();
   }

   @Override
   public String[] getAllTokens()
   {
      throw new NotYetImplemented();
   }

   @Override
   protected String decodeKey(String stringKey)
   {
      throw new NotYetImplemented();
   }

   @Override
   public long size() throws Exception
   {
      throw new NotYetImplemented();
   }

   public String createToken(Credentials credentials) throws IllegalArgumentException, NullPointerException
   {
      throw new NotYetImplemented();
   }
}
