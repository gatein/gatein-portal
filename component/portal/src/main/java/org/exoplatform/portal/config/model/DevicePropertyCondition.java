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

import java.util.regex.Pattern;

import org.exoplatform.portal.pom.data.RedirectDevicePropertyConditionData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class DevicePropertyCondition extends ModelObject {

    protected String propertyName;
    protected Float greaterThan;
    protected Float lessThan;
    protected String equals;
    protected String matches;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Float getGreaterThan() {
        return greaterThan;
    }

    public void setGreaterThan(Float greaterThan) {
        this.greaterThan = greaterThan;
    }

    public Float getLessThan() {
        return lessThan;
    }

    public void setLessThan(Float lessThan) {
        this.lessThan = lessThan;
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(String equals) {
        this.equals = equals;
    }

    public String getMatches() {
        return matches;
    }

    public void setMatches(String matches) {
        this.matches = matches;
    }

    @Override
    public RedirectDevicePropertyConditionData build() {
        RedirectDevicePropertyConditionData conditionData = new RedirectDevicePropertyConditionData(this.storageId,
                this.storageName, propertyName);

        conditionData.setGreaterThan(greaterThan);
        conditionData.setLessThan(lessThan);
        conditionData.setEquals(equals);

        if (matches != null) {
            conditionData.setMatches(Pattern.compile(matches));
        }

        return conditionData;
    }
}
