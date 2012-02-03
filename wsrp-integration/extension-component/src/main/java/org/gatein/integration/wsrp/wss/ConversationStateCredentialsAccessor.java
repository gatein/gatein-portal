/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.integration.wsrp.wss;

import org.exoplatform.services.security.ConversationState;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wci.security.Credentials;
import org.gatein.wsrp.wss.credentials.CredentialsAccessor;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ConversationStateCredentialsAccessor implements CredentialsAccessor
{

   private static final Logger log = LoggerFactory.getLogger(ConversationStateCredentialsAccessor.class);

   /**
    * Reading credentials from @{link ConversationState} of current user.
    *
    * @return credentials
    */
   @Override
   public Credentials getCredentials()
   {
      if (ConversationState.getCurrent() == null)
      {
         log.warn("Cannot find Credentials because ConversationState not set.");
         return null;
      }
      return (Credentials)ConversationState.getCurrent().getAttribute(Credentials.CREDENTIALS);
   }
}
