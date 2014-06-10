/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.web.application.javascript;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.script.FetchMode;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptResourceDescriptor {

    /** . */
    final ResourceId id;

    /** . */
    final String group;

    /** . */
    final String alias;

    /** . */
    final List<Locale> supportedLocales;

    /** . */
    final List<Javascript> modules;

    /** . */
    final List<DependencyDescriptor> dependencies;

    /** . */
    FetchMode fetchMode;

    /** {@code true} if this is an AMD script resource */
    final boolean nativeAmd;

    public ScriptResourceDescriptor(ResourceId id, FetchMode fetchMode) {
        this(id, fetchMode, null, null, false);
    }

    public ScriptResourceDescriptor(ResourceId id, FetchMode fetchMode, String alias, String group, boolean amd) {
        this.id = id;
        this.modules = new ArrayList<Javascript>();
        this.dependencies = new ArrayList<DependencyDescriptor>();
        this.supportedLocales = new ArrayList<Locale>();
        this.fetchMode = fetchMode;
        this.alias = alias;
        this.group = group;
        this.nativeAmd = amd;
    }

    public ResourceId getId() {
        return id;
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public List<Javascript> getModules() {
        return modules;
    }

    public List<DependencyDescriptor> getDependencies() {
        return dependencies;
    }

    public String getAlias() {
        return alias;
    }

    public String getGroup() {
        return group;
    }

    public FetchMode getFetchMode() {
        return fetchMode;
    }

    /**
     * Returns {@code true} if this is an AMD resource. See the invocations of {@link #isNativeAmd()} in
     * {@link JavascriptConfigService#getScript(ResourceId, Locale)} to learn more about the purpose
     * of this method.
     *
     * @return
     */
    public boolean isNativeAmd() {
        return nativeAmd;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((alias == null) ? 0 : alias.hashCode());
        result = prime * result + (nativeAmd ? 1231 : 1237);
        result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
        result = prime * result + ((fetchMode == null) ? 0 : fetchMode.hashCode());
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((modules == null) ? 0 : modules.hashCode());
        result = prime * result + ((supportedLocales == null) ? 0 : supportedLocales.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ScriptResourceDescriptor)) {
            return false;
        }
        ScriptResourceDescriptor other = (ScriptResourceDescriptor) obj;
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        if (nativeAmd != other.nativeAmd) {
            return false;
        }
        if (dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!dependencies.equals(other.dependencies)) {
            return false;
        }
        if (fetchMode != other.fetchMode) {
            return false;
        }
        if (group == null) {
            if (other.group != null) {
                return false;
            }
        } else if (!group.equals(other.group)) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (modules == null) {
            if (other.modules != null) {
                return false;
            }
        } else if (!modules.equals(other.modules)) {
            return false;
        }
        if (supportedLocales == null) {
            if (other.supportedLocales != null) {
                return false;
            }
        } else if (!supportedLocales.equals(other.supportedLocales)) {
            return false;
        }
        return true;
    }

}
