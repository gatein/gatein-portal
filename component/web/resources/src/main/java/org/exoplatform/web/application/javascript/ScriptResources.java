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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.gatein.portal.controller.resource.script.StaticScriptResource;

/**
 * A container for script related entities that need to be registered and also unregistered from a {@link JavascriptConfigService}.
 *
 * @see JavascriptConfigService#add(ScriptResources)
 * @see {@link JavascriptConfigService#remove(ImmutableScriptResources, String)}
 *
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 */
public class ScriptResources {
    private static final Log log = ExoLogger.getLogger(ScriptResources.class);

    /**
     * An immutable variant of {@link ScriptResources}.
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    public static final class ImmutableScriptResources extends ScriptResources {
        public ImmutableScriptResources(ScriptResources scriptResources) {
            super(Collections.unmodifiableList(new ArrayList<ScriptResourceDescriptor>(
                    scriptResources.scriptResourceDescriptors)), Collections
                    .unmodifiableList(new ArrayList<StaticScriptResource>(scriptResources.staticScriptResources)),
                    new ImmutablePathsBuilder(scriptResources.paths).build());
        }

        /**
         * returns {@code this}.
         */
        public ImmutableScriptResources toImmutable() {
            return this;
        }
    }

    /**
     * A builder for producing immutable paths {@link Map}s.
     *
     * @see {@link ScriptResources#paths}
     * @see {@link JavascriptConfigService#paths}
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     */
    public static class ImmutablePathsBuilder {
        /**
         * @return an empty immutable {@link Map}.
         */
        public static Map<String, List<String>> buildEmpty() {
            return Collections.emptyMap();
        }

        /**
         * The result for {@link #build()} is collected here.
         */
        private final Map<String, List<String>> paths;

        /**
         * Creates a new builder based on the given {@code paths}. The values from {@code paths}
         * are deeply copied into a new {@link LinkedHashMap}. It is a {@link LinkedHashMap} because
         * the order of paths matters.
         *
         * @param paths the initial {@link #paths}
         */
        public ImmutablePathsBuilder(Map<String, List<String>> paths) {
            super();
            this.paths = new LinkedHashMap<String, List<String>>(paths);
            for (Map.Entry<String, List<String>> en : this.paths.entrySet()) {
                en.setValue(Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
            }
        }

        /**
         * Adds those elements of {@code pathEntries} to {@link #paths} which are not there yet and
         * removes those elements from {@code pathEntries} which are available in {@link #paths} already.
         * Hence, {@code pathEntries} parameter can be modified during the call.
         * <p>
         * To put it in other words, the status of {@code pathEntries} map on the caller will be the same
         * as before minus the entries that were already in {@link #paths}:
         *
         * @param pathEntries
         * @return see above
         */
        public ImmutablePathsBuilder accept(Map<String, List<String>> pathEntries) throws DuplicateResourceKeyException {
            for (Iterator<Map.Entry<String, List<String>>> it = pathEntries.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, List<String>> en = it.next();
                List<String> availableValue = this.paths.get(en.getKey());
                if (availableValue != null) {
                    throw new DuplicateResourceKeyException("Ignoring path entry " + en + " because the given resource path was already provided: "
                            + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding path entry " + en);
                    }
                    this.paths.put(en.getKey(), Collections.unmodifiableList(new ArrayList<String>(en.getValue())));
                }
            }
            return this;
        }

        /**
         * @return a new immutable paths {@link Map} that can be used in {@link JavascriptConfigService#paths}.
         */
        public Map<String, List<String>> build() {
            return Collections.unmodifiableMap(paths);
        }

        /**
         * Removes all keys given in {@code keysToRemove} from the underlying {@link #paths}.
         *
         * @param keysToRemove
         * @return
         */
        public ImmutablePathsBuilder removeAll(Collection<String> keysToRemove) {
            for (String prefix : keysToRemove) {
                paths.remove(prefix);
            }
            return this;
        }
    }

    /**
     * A builder for producing immutable static script resources {@link Map}s.
     *
     * @see {@link ScriptResources#staticScriptResources}
     * @see {@link JavascriptConfigService#staticScriptResources}
     *
     * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
     *
     */
    public static class ImmutableStaticScriptResourcesBuilder {
        /**
         * @return an empty immutable {@link Map}.
         */
        public static Map<String, StaticScriptResource> buildEmpty() {
            return Collections.emptyMap();
        }

        /**
         * The result for {@link #build()} is collected here.
         */
        private final Map<String, StaticScriptResource> staticScriptResources;

        /**
         * Creates a new builder based on the given {@code staticScriptResources}. The values from {@code paths}
         * are copied into a new {@link HashMap}.
         *
         * @param staticScriptResources
         */
        public ImmutableStaticScriptResourcesBuilder(Map<String, StaticScriptResource> staticScriptResources) {
            super();
            this.staticScriptResources = new HashMap<String, StaticScriptResource>(staticScriptResources);
        }

        /**
         * Adds those elements of {@code toAdd} to {@link #staticScriptResources} which are not there yet and
         * removes those elements from {@code toAdd} which are available in {@link #staticScriptResources} already.
         * Hence, {@code toAdd} can be modified during the call.
         * <p>
         * To put it in other words, the status of {@code toAdd} collection on the caller will be the same
         * as before minus the entries that were already in {@link #staticScriptResources}:
         *
         * @param toAdd entriess to add
         * @return
         */
        public ImmutableStaticScriptResourcesBuilder accept(Collection<StaticScriptResource> toAdd) throws DuplicateResourceKeyException {
            for (Iterator<StaticScriptResource> it = toAdd.iterator(); it.hasNext();) {
                StaticScriptResource staticScriptResource = it.next();
                String resourcePath = staticScriptResource.getResourcePath();
                StaticScriptResource availableValue = staticScriptResources.get(resourcePath);
                if (availableValue != null) {
                    throw new DuplicateResourceKeyException("Ignoring " + StaticScriptResource.class.getSimpleName() + " " + staticScriptResource
                            + " because the given resource path was already provided by " + availableValue);
                } else {
                    /* add only if not there already */
                    if (log.isDebugEnabled()) {
                        log.debug("Adding " + staticScriptResource);
                    }
                    staticScriptResources.put(resourcePath, staticScriptResource);
                }
            }
            return this;
        }

        /**
         * @return a new immutable staticScriptResources {@link Map} that can be used in {@link JavascriptConfigService#staticScriptResources}.
         */
        public Map<String, StaticScriptResource> build() {
            return Collections.unmodifiableMap(staticScriptResources);
        }

        /**
         * Removes all entries given in {@code toRemove} from the underlying {@link #staticScriptResources}.
         * @param toRemoveStaticScriptResources
         * @return
         */
        public ImmutableStaticScriptResourcesBuilder removeAll(Collection<StaticScriptResource> toRemove) {
            for (StaticScriptResource staticScriptResource : toRemove) {
                staticScriptResources.remove(staticScriptResource.getResourcePath());
            }
            return this;
        }
    }

    private final List<ScriptResourceDescriptor> scriptResourceDescriptors;
    private final List<StaticScriptResource> staticScriptResources;
    private final Map<String, List<String>> paths;

    /**
     * @param scriptResourceDescriptors
     * @param staticScriptResources
     * @param paths a {@link LinkedHashMap} or similar should used internally, because the order
     * of paths matters.
     */
    private ScriptResources(List<ScriptResourceDescriptor> scriptResourceDescriptors,
            List<StaticScriptResource> staticScriptResources, Map<String, List<String>> paths) {
        this.scriptResourceDescriptors = scriptResourceDescriptors;
        this.staticScriptResources = staticScriptResources;
        this.paths = paths;
    }

    public ScriptResources() {
        this.scriptResourceDescriptors = new ArrayList<ScriptResourceDescriptor>();
        this.staticScriptResources = new ArrayList<StaticScriptResource>();
        this.paths = new LinkedHashMap<String, List<String>>();
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
