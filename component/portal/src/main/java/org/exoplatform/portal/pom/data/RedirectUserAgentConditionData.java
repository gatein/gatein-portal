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

import org.exoplatform.portal.config.model.UserAgentConditions;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectUserAgentConditionData extends ComponentData {
    protected List<String> userAgentContains;
    protected List<String> userAgentDoesNotContain;

    public RedirectUserAgentConditionData(String storageId, String storageName) {
        super(storageId, storageName);
    }

    public List<String> getUserAgentContains() {
        if (userAgentContains == null) {
            userAgentContains = new ArrayList<String>();
        }
        return userAgentContains;
    }

    public List<String> getUserAgentDoesNotContain() {
        if (userAgentDoesNotContain == null) {
            userAgentDoesNotContain = new ArrayList<String>();
        }
        return userAgentDoesNotContain;
    }

    public UserAgentConditions build() {
        UserAgentConditions userAgentCondition = new UserAgentConditions();
        userAgentCondition.setStorageName(this.getStorageName());
        userAgentCondition.setContains((ArrayList) this.userAgentContains);
        userAgentCondition.setDoesNotContain((ArrayList) this.userAgentDoesNotContain);
        return userAgentCondition;
    }

}
