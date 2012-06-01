/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.integration.wsrp.wss;

import org.gatein.wsrp.wss.WebServiceSecurityFactory;
import org.gatein.wsrp.wss.credentials.CredentialsAccessor;
import org.picocontainer.Startable;
import org.wsrp.wss.jboss5.handlers.consumer.JBWSSecurityHandlerWrapper;
import org.wsrp.wss.jboss5.handlers.consumer.WSSecurityCredentialHandler;
import org.wsrp.wss.jboss5.handlers.consumer.JBWSCustomizePortListener;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class JBoss5WSSServiceIntegration implements Startable
{
   private final WebServiceSecurityFactory wssFactory;
   
   private final JBWSCustomizePortListener JBWS_CUSTOMIZE_PORT_LISTENER = new JBWSCustomizePortListener(); 
   
   public JBoss5WSSServiceIntegration(CredentialsAccessor credentialsAccessor)
   {
      wssFactory = WebServiceSecurityFactory.getInstance();
      wssFactory.setCredentialsAccessor(credentialsAccessor);
   }
   
   public void start()
   {
      startConsumer();
   }

   public void stop()
   {
      stopConsumer();
   }
   
   protected void startConsumer()
   {      
      wssFactory.addCustomizePortListener(JBWS_CUSTOMIZE_PORT_LISTENER);
   }

   protected void stopConsumer()
   {
      wssFactory.removeCustomizePortListener(JBWS_CUSTOMIZE_PORT_LISTENER);
   }
}

