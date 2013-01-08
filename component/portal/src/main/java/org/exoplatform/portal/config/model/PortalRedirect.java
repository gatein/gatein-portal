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

import org.exoplatform.portal.pom.data.RedirectConditionData;
import org.exoplatform.portal.pom.data.RedirectData;
import org.exoplatform.portal.pom.data.RedirectMappingsData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class PortalRedirect extends ModelObject {
    protected String redirectSite;
    protected String name;
    protected boolean enabled;
    protected ArrayList<RedirectCondition> conditions;
    protected RedirectMappings mappings;

    public String getRedirectSite() {
        return redirectSite;
    }

    public void setRedirectSite(String redirectSite) {
        this.redirectSite = redirectSite;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ArrayList<RedirectCondition> getConditions() {
        return conditions;
    }

    public void setConditions(ArrayList<RedirectCondition> conditions) {
        this.conditions = conditions;
    }

    public RedirectMappings getMappings() {
        return mappings;
    }

    public void setMappings(RedirectMappings mappings) {
        this.mappings = mappings;
    }

    public RedirectData build() {
        RedirectData redirectData = new RedirectData(storageId, redirectSite, name, enabled,
                buildRedirectConditionData(conditions), buildRedirectMappingsData(mappings));

        return redirectData;
    }

    protected ArrayList<RedirectConditionData> buildRedirectConditionData(ArrayList<RedirectCondition> redirectConditions) {
        if (redirectConditions != null) {
            ArrayList<RedirectConditionData> redirectConditionsData = new ArrayList<RedirectConditionData>();
            for (RedirectCondition redirectCondition : redirectConditions) {
                redirectConditionsData.add(redirectCondition.build());
            }

            return redirectConditionsData;
        } else {
            return null;
        }
    }

    protected RedirectMappingsData buildRedirectMappingsData(RedirectMappings mappings) {
        if (mappings != null) {
            RedirectMappingsData mappingsData = new RedirectMappingsData(storageId, storageName);

            if (mappings.getUnresolvedNode() != null) {
                mappingsData.setUnresolvedNode(mappings.getUnresolvedNode().name());
            }

            mappingsData.setUseNodeNameMatching(mappings.isUseNodeNameMatching());

            if (mappings.getMap() != null) {
                mappingsData.getMappings().putAll(mappings.getMap());
            }

            return mappingsData;
        } else {
            return null;
        }
    }

}
