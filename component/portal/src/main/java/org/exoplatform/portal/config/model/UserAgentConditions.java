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

import org.exoplatform.portal.pom.data.RedirectUserAgentConditionData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class UserAgentConditions extends ModelObject {
    protected ArrayList<String> contains;
    protected ArrayList<String> doesNotContain;

    public ArrayList<String> getContains() {
        return contains;
    }

    public void setContains(ArrayList<String> contains) {
        this.contains = contains;
    }

    public ArrayList<String> getDoesNotContain() {
        return doesNotContain;
    }

    public void setDoesNotContain(ArrayList<String> doesNotContain) {
        this.doesNotContain = doesNotContain;
    }

    @Override
    public RedirectUserAgentConditionData build() {
        RedirectUserAgentConditionData userAgentConditionData = new RedirectUserAgentConditionData(storageId, storageName);

        if (contains != null) {
            userAgentConditionData.getUserAgentContains().addAll(contains);
        }
        if (doesNotContain != null) {
            userAgentConditionData.getUserAgentDoesNotContain().addAll(doesNotContain);
        }

        return userAgentConditionData;
    }

}
