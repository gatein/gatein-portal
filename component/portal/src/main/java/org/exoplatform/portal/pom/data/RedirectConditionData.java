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
package org.exoplatform.portal.pom.data;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.RedirectCondition;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectConditionData extends ComponentData {

    protected String redirectName;
    protected RedirectUserAgentConditionData userAgentConditionData;
    protected List<RedirectDevicePropertyConditionData> devicePropertyConditionData;

    public RedirectConditionData(String storageId, String storageName, String redirectName) {
        super(storageId, null);
        this.redirectName = redirectName;
    }

    public String getRedirectName() {
        return redirectName;
    }

    public void setRedirectName(String redirectName) {
        this.redirectName = redirectName;
    }

    public RedirectUserAgentConditionData getUserAgentConditionData() {
        return userAgentConditionData;
    }

    public void setUserAgentConditionData(RedirectUserAgentConditionData userAgentConditionData) {
        this.userAgentConditionData = userAgentConditionData;
    }

    public List<RedirectDevicePropertyConditionData> getDevicePropertyConditionData() {
        if (devicePropertyConditionData == null) {
            devicePropertyConditionData = new ArrayList<RedirectDevicePropertyConditionData>();
        }
        return devicePropertyConditionData;
    }

    public RedirectCondition build() {
        RedirectCondition redirectCondition = new RedirectCondition();
        redirectCondition.setName(redirectName);
        redirectCondition.setStorageName(getStorageName());

        if (devicePropertyConditionData != null) {
            ArrayList<DevicePropertyCondition> devicePropertyConditions = new ArrayList<DevicePropertyCondition>();
            for (RedirectDevicePropertyConditionData conditionData : devicePropertyConditionData) {
                devicePropertyConditions.add(conditionData.build());
            }
            redirectCondition.setDeviceProperties(devicePropertyConditions);
        }

        if (userAgentConditionData != null) {
            redirectCondition.setUserAgentConditions(userAgentConditionData.build());
        }
        return redirectCondition;
    }
}
