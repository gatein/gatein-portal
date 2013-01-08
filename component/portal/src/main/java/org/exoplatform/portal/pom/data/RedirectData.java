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

import java.util.List;


/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectData extends ComponentData {
    private String redirectSiteName;
    private String redirectName;
    private boolean enabled;
    private List<RedirectConditionData> conditions;
    private RedirectMappingsData mappings;

    public RedirectData(String storageId, String redirectSiteName, String redirectName, boolean enabled,
            List<RedirectConditionData> conditions, RedirectMappingsData mappings) {
        super(storageId, null);
        this.redirectSiteName = redirectSiteName;
        this.redirectName = redirectName;
        this.enabled = enabled;
        this.conditions = conditions;
        this.mappings = mappings;
    }

    public void setRedirectSiteName(String redirectSiteName) {
        this.redirectSiteName = redirectSiteName;
    }

    public String getRedirectSiteName() {
        return redirectSiteName;
    }

    public void setRedirectName(String redirectName) {
        this.redirectName = redirectName;
    }

    public String getRedirectName() {
        return redirectName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<RedirectConditionData> getConditions() {
        return conditions;
    }

    public void setConditions(List<RedirectConditionData> conditions) {
        this.conditions = conditions;
    }

    public RedirectMappingsData getMappings() {
        return mappings;
    }

    public void setMappings(RedirectMappingsData mappings) {
        this.mappings = mappings;
    }
}
