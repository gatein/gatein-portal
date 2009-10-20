/**
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

package org.exoplatform.portal.config.model.wsrp;

import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.spi.wsrp.WSRPState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPApplication extends Application<WSRPState, WSRPId>
{

   public WSRPApplication(String storageId, WSRPId id)
   {
      super(storageId, id);
   }

   public WSRPApplication(WSRPId id)
   {
      super(id);
   }

   @Override
   public ApplicationType<WSRPState, WSRPId> getType()
   {
      return ApplicationType.WSRP_PORTLET;
   }

   @Override
   public String getApplicationType()
   {
      return org.exoplatform.web.application.Application.WSRP_TYPE;
   }
}
