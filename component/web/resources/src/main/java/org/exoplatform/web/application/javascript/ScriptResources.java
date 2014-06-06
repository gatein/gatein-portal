/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gatein.portal.controller.resource.script.StaticScriptResource;

/**
 * A container for script related entities that need to be registered and also unregistered from a {@link JavascriptConfigService}.
 *
 * @see JavascriptConfigService#add(ScriptResources)
 * @see {@link JavascriptConfigService#remove(ImmutableScriptResources)}
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class ScriptResources {

    /**
     * An immutable variant of {@link ScriptResources}.
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    public static final class ImmutableScriptResources extends ScriptResources {
        public ImmutableScriptResources(ScriptResources scriptResources) {
            super(scriptResources.contextPath, Collections.unmodifiableList(new ArrayList<ScriptResourceDescriptor>(
                    scriptResources.scriptResourceDescriptors)), Collections
                    .unmodifiableList(new ArrayList<StaticScriptResource>(scriptResources.staticScriptResources)),
                    Collections.unmodifiableMap(new LinkedHashMap<String, List<String>>(scriptResources.paths)));
        }

        /**
         * returns {@code this}.
         */
        public ImmutableScriptResources toImmutable() {
            return this;
        }
    }

    private final String contextPath;
    private final List<ScriptResourceDescriptor> scriptResourceDescriptors;
    private final List<StaticScriptResource> staticScriptResources;
    private final Map<String, List<String>> paths;

    /**
     * @param scriptResourceDescriptors
     * @param staticScriptResources
     * @param paths a {@link LinkedHashMap} or similar should used internally, because the order
     * of paths matters.
     */
    private ScriptResources(String contextPath, List<ScriptResourceDescriptor> scriptResourceDescriptors,
            List<StaticScriptResource> staticScriptResources, Map<String, List<String>> paths) {
        this.contextPath = contextPath;
        this.scriptResourceDescriptors = scriptResourceDescriptors;
        this.staticScriptResources = staticScriptResources;
        this.paths = paths;
    }

    public ScriptResources(String contextPath) {
        this.contextPath = contextPath;
        this.scriptResourceDescriptors = new ArrayList<ScriptResourceDescriptor>();
        this.staticScriptResources = new ArrayList<StaticScriptResource>();
        this.paths = new LinkedHashMap<String, List<String>>();
    }

    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * @return the scriptResourceDescriptors
     */
    public List<ScriptResourceDescriptor> getScriptResourceDescriptors() {
        return scriptResourceDescriptors;
    }

    /**
     * @return the staticScriptResources
     */
    public List<StaticScriptResource> getStaticScriptResources() {
        return staticScriptResources;
    }

    /**
     * A {@link LinkedHashMap} is used internally, because the order of paths matters.
     *
     * @return the paths
     */
    public Map<String, List<String>> getPaths() {
        return paths;
    }

    /**
     * Returns an {@link ImmutableScriptResources} instance based on this {@link ScriptResources}.
     *
     * @return a new {@link ImmutableScriptResources} instance.
     */
    public ImmutableScriptResources toImmutable() {
        return new ImmutableScriptResources(this);
    }

    /**
     * Returns {@code true} if all of {@link #scriptResourceDescriptors}, {@link #staticScriptResources}
     * and {@link #paths} are empty. Otherwise, returns {@code false}.
     *
     * @return see above
     */
    public boolean isEmpty() {
        return scriptResourceDescriptors.isEmpty() && staticScriptResources.isEmpty() && paths.isEmpty();
    }

}
