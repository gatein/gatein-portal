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
package org.exoplatform.portal.config.model;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.pom.data.RedirectConditionData;
import org.exoplatform.portal.pom.data.RedirectDevicePropertyConditionData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectCondition extends ModelObject
{

   protected String name;
   protected UserAgentConditions userAgentConditions;
   protected ArrayList<DevicePropertyCondition> devicePropertyConditions;
   
   public ArrayList<DevicePropertyCondition> getDeviceProperties()
   {
      return devicePropertyConditions;
   }

   public void setDeviceProperties(ArrayList<DevicePropertyCondition> devicePropertyConditions)
   {
      this.devicePropertyConditions = devicePropertyConditions;
   }

   public RedirectCondition()
   {
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public String getName()
   {
      return name;
   }
   
   public void setUserAgentConditions(UserAgentConditions userAgentConditions)
   {
      this.userAgentConditions = userAgentConditions;
   }
   
   public UserAgentConditions getUserAgentConditions()
   {
      return userAgentConditions;
   }
   
   @Override
   public RedirectConditionData build()
   {
      RedirectConditionData redirectConditionData = new RedirectConditionData(this.storageId, this.storageName, name);
      
      if (userAgentConditions != null)
      {
      redirectConditionData.setUserAgentConditionData(userAgentConditions.build());
      }
      
      List<RedirectDevicePropertyConditionData> devicePropertyConditionsData = redirectConditionData.getDevicePropertyConditionData();
      if (devicePropertyConditions != null && !devicePropertyConditions.isEmpty())
      {
         for (DevicePropertyCondition propertyCondition: devicePropertyConditions)
         {
            devicePropertyConditionsData.add(propertyCondition.build());
         }
      }

      return redirectConditionData;
   }

}

