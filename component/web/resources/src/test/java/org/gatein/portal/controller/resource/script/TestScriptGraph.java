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

package org.gatein.portal.controller.resource.script;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.commons.utils.I18N;
import org.exoplatform.component.test.BaseGateInTest;
import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.DuplicateResourceKeyException;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.common.util.Tools;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;
import org.gatein.portal.controller.resource.ResourceScope;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestScriptGraph extends BaseGateInTest {
    private static final String CONTEXT_PATH_1 = "/my-app-1";
    private static final String CONTEXT_PATH_2 = "/my-app-2";
    private static final String CONTEXT_PATH_3 = "/my-app-3";
    private static final String CONTEXT_PATH_4 = "/my-app-4";
    private static final String CONTEXT_PATH_5 = "/my-app-5";
    private static final String CONTEXT_PATH_6 = "/my-app-6";
    private static final String CONTEXT_PATH_7 = "/my-app-7";

    /** . */
    private static final ResourceId A = new ResourceId(ResourceScope.SHARED, "A");

    /** . */
    private static final ResourceId B = new ResourceId(ResourceScope.SHARED, "B");

    /** . */
    private static final ResourceId C = new ResourceId(ResourceScope.SHARED, "C");

    /** . */
    private static final ResourceId D = new ResourceId(ResourceScope.PORTAL, "D");
    private static final ResourceId E = new ResourceId(ResourceScope.SHARED, "E");
    private static final ResourceId F = new ResourceId(ResourceScope.SHARED, "F");
    private static final ResourceId G = new ResourceId(ResourceScope.SHARED, "G");

    private static final String GROUP_1 = "group1";
    private static final String GROUP_2 = "group2";

    private static ResourceId id(ResourceScope scope) {
        return new ResourceId(scope, "test_"+ scope.name());
    }

    private static ScriptResourceDescriptor immediate(ResourceId rid) {
        return new ScriptResourceDescriptor(rid, FetchMode.IMMEDIATE);
    }
    private static ScriptResourceDescriptor onLoad(ResourceId rid) {
        return new ScriptResourceDescriptor(rid, FetchMode.ON_LOAD);
    }

    private static ScriptResourceDescriptor addDep(ScriptResourceDescriptor desc, ResourceId depId) {
        DependencyDescriptor dependency = new DependencyDescriptor(depId, null, null);
        desc.getDependencies().add(dependency);
        return desc;
    }

    public void testAddRemoveEmpty() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty().validate();

        for (ResourceScope scope : ResourceScope.values()) {
            Collection<ScriptResource> scopeValues = initial.getResources(scope);
            assertEquals(0, scopeValues.size());
        }

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            ScriptGraph afterAdd = initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id))).validate();

            Collection<ScriptResource> scopeValues = afterAdd.getResources(scope);
            assertEquals(1, scopeValues.size());

            assertEquals(id, afterAdd.getResource(id).getId());

            ScriptGraph afterRemoveNonExistent = afterAdd.remove(CONTEXT_PATH_2).validate();
            scopeValues = afterRemoveNonExistent.getResources(scope);
            assertEquals(1, scopeValues.size());
            assertEquals(id, afterAdd.getResource(id).getId());

            ScriptGraph afterRemove = afterAdd.remove(CONTEXT_PATH_1).validate();
            scopeValues = afterRemove.getResources(scope);
            assertEquals(0, scopeValues.size());
            assertNull(afterRemove.getResource(id));
        }
    }

    public void testSelfDependency() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty();
        ScriptGraph afterAdd = initial.add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), A)
        )).validate();
        /* assert that self-dep has no effect */
        assertEquals(0, afterAdd.getResource(A).getDependencies().size());
        assertEquals(0, afterAdd.getResource(A).getClosure().size());
    }

    public void testDetectTwoNodeCycle() {
        ScriptGraph initial = ScriptGraph.empty();
        try {
            initial.add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    addDep(immediate(B), A)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
            expected.printStackTrace();
        }
    }

    public void testDetectThreeNodeCycle() {
        ScriptGraph initial = ScriptGraph.empty();
        try {
            initial.add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    addDep(immediate(B), C),
                    addDep(immediate(C), A)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }
    }

    public void testUnavailableDependency() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(B), C),
                immediate(C)
        )).validate();

        try {
            graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                    addDep(immediate(D), A),
                    addDep(immediate(E), F)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }
    }

    public void testClosureAndDependencies() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(B), C),
                immediate(C)
        )).validate();
        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        assertEquals(Tools.toSet(B), a.getDependencies());
        assertEquals(Tools.toSet(C), b.getDependencies());
        assertEquals(Collections.emptySet(), c.getDependencies());

        assertEquals(Tools.toSet(B, C), a.getClosure());
        assertEquals(Tools.toSet(C), b.getClosure());
        assertEquals(Collections.emptySet(), c.getClosure());

        ScriptGraph afterRemove = graph.remove(CONTEXT_PATH_1).validate();
        /* C should be away from everywhere */
        assertNull(afterRemove.getResource(A));
        assertNull(afterRemove.getResource(B));
        assertNull(afterRemove.getResource(C));

    }

    public void testCrossContextClosure() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(C)
        )).validate();

        graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(B), C)
        )).validate();
        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        assertEquals(Tools.toSet(B), a.getDependencies());
        assertEquals(Tools.toSet(C), b.getDependencies());
        assertEquals(Collections.emptySet(), c.getDependencies());

        assertEquals(Tools.toSet(B, C), a.getClosure());
        assertEquals(Tools.toSet(C), b.getClosure());
        assertEquals(Collections.emptySet(), c.getClosure());

        ScriptGraph afterRemove = graph.remove(CONTEXT_PATH_1).validate();

        /* C should be still there as B's stale dependency */
        assertEquals(Tools.toSet(C), afterRemove.getResource(B).getDependencies());

        /* But C should be away from the graph and closures */
        assertNull(afterRemove.getResource(C));
        assertEquals(Tools.toSet(B), afterRemove.getResource(A).getClosure());
        assertEquals(Collections.emptySet(), afterRemove.getResource(B).getClosure());

    }

    public void testCrossContextClosureWith3Contexts() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(C)
        )).validate();

        graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                addDep(immediate(B), C)
        )).validate();

        graph = graph.add(CONTEXT_PATH_3, Arrays.asList(
                addDep(immediate(A), B)
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        assertEquals(Tools.toSet(B), a.getDependencies());
        assertEquals(Tools.toSet(C), b.getDependencies());
        assertEquals(Collections.emptySet(), c.getDependencies());

        assertEquals(Tools.toSet(B, C), a.getClosure());
        assertEquals(Tools.toSet(C), b.getClosure());
        assertEquals(Collections.emptySet(), c.getClosure());

        ScriptGraph afterRemove = graph.remove(CONTEXT_PATH_2).validate();

        /* B should be still there as A's stale dependency */
        assertEquals(Tools.toSet(B), afterRemove.getResource(A).getDependencies());

        /* But B should be away from the graph and closures */
        assertNull(afterRemove.getResource(B));
        assertEquals(Collections.emptySet(), afterRemove.getResource(A).getClosure());
        assertEquals(Collections.emptySet(), afterRemove.getResource(C).getClosure());

    }

    public void testCrossContextClosureWith3ContextsMultipathSemidirect() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(C)
        )).validate();

        graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                addDep(immediate(B), C)
        )).validate();

        ScriptResourceDescriptor aDesc = immediate(A);
        aDesc.getDependencies().add(new DependencyDescriptor(B, null, null));
        aDesc.getDependencies().add(new DependencyDescriptor(C, null, null));
        graph = graph.add(CONTEXT_PATH_3, Arrays.asList(
                aDesc
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        assertEquals(Tools.toSet(B, C), a.getDependencies());
        assertEquals(Tools.toSet(C), b.getDependencies());
        assertEquals(Collections.emptySet(), c.getDependencies());

        assertEquals(Tools.toSet(B, C), a.getClosure());
        assertEquals(Tools.toSet(C), b.getClosure());
        assertEquals(Collections.emptySet(), c.getClosure());

        ScriptGraph afterRemove = graph.remove(CONTEXT_PATH_2).validate();

        /* B should be still there as A's stale dependency */
        assertEquals(Tools.toSet(B, C), afterRemove.getResource(A).getDependencies());

        /* But B should be away from the graph and closures */
        assertNull(afterRemove.getResource(B));
        /* C should still be there as it is A's direct dependency */
        assertEquals(Tools.toSet(C), afterRemove.getResource(A).getClosure());
        assertEquals(Collections.emptySet(), afterRemove.getResource(C).getClosure());

    }


    /**
     * Let's have a graph like this where each node comes from a separate application and remove C
     * <pre><blockquote>
A -&gt; B -&gt; C -&gt; D -&gt; E
 \                              ^
  `-----&gt; F -----&gt; G ----´
</blockquote></pre>     *
     * @throws InvalidResourceException
     */
    public void testCrossContextClosureWith3ContextsMultipathIndirect() throws InvalidResourceException {

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(E)
        )).validate();

        graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                addDep(immediate(D), E)
        )).validate();

        graph = graph.add(CONTEXT_PATH_3, Arrays.asList(
                addDep(immediate(C), D)
        )).validate();

        graph = graph.add(CONTEXT_PATH_4, Arrays.asList(
                addDep(immediate(B), C)
        )).validate();


        graph = graph.add(CONTEXT_PATH_7, Arrays.asList(
                addDep(immediate(G), E)
        )).validate();

        graph = graph.add(CONTEXT_PATH_6, Arrays.asList(
                addDep(immediate(F), G)
        )).validate();

        ScriptResourceDescriptor aDesc = immediate(A);
        aDesc.getDependencies().add(new DependencyDescriptor(B, null, null));
        aDesc.getDependencies().add(new DependencyDescriptor(F, null, null));
        graph = graph.add(CONTEXT_PATH_5, Arrays.asList(
                aDesc
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);
        ScriptResource d = graph.getResource(D);
        ScriptResource e = graph.getResource(E);
        ScriptResource f = graph.getResource(F);
        ScriptResource g = graph.getResource(G);

        assertEquals(Tools.toSet(B, F), a.getDependencies());
        assertEquals(Tools.toSet(C), b.getDependencies());
        assertEquals(Tools.toSet(D), c.getDependencies());
        assertEquals(Tools.toSet(E), d.getDependencies());
        assertEquals(Collections.emptySet(), e.getDependencies());
        assertEquals(Tools.toSet(G), f.getDependencies());
        assertEquals(Tools.toSet(E), g.getDependencies());

        assertEquals(Tools.toSet(B, C, D, E, F, G), a.getClosure());
        assertEquals(Tools.toSet(C, D, E), b.getClosure());
        assertEquals(Tools.toSet(D, E), c.getClosure());
        assertEquals(Tools.toSet(E), d.getClosure());
        assertEquals(Collections.emptySet(), e.getClosure());
        assertEquals(Tools.toSet(G, E), f.getClosure());
        assertEquals(Tools.toSet(E), g.getClosure());

        /* this is the operation we test */
        graph = graph.remove(CONTEXT_PATH_3).validate();

        assertEquals(Tools.toSet(B, F), graph.getResource(A).getDependencies());
        /* C should be still there as B's stale dependency */
        assertEquals(Tools.toSet(C), graph.getResource(B).getDependencies());

        /* But C should be away from the graph and closures */
        assertNull(graph.getResource(C));

        assertEquals(Tools.toSet(E), graph.getResource(D).getDependencies());
        assertEquals(Collections.emptySet(), graph.getResource(E).getDependencies());
        assertEquals(Tools.toSet(G), graph.getResource(F).getDependencies());
        assertEquals(Tools.toSet(E), graph.getResource(G).getDependencies());


        /* Both C and D should disappear from A' closure */
        assertEquals(Tools.toSet(B, E, F, G), graph.getResource(A).getClosure());
        /* B has no non-stale deps, so it should have empty closure */
        assertEquals(Collections.emptySet(), graph.getResource(B).getClosure());

        assertEquals(Tools.toSet(E), graph.getResource(D).getClosure());
        assertEquals(Collections.emptySet(), graph.getResource(E).getClosure());
        assertEquals(Tools.toSet(G, E), graph.getResource(F).getClosure());
        assertEquals(Tools.toSet(E), graph.getResource(G).getClosure());

    }


    /**
     * Closure of any node depends on node relationships in graph but does not depend on the order of building graph nodes
     * @throws InvalidResourceException
     */
    public void testBuildingOrder() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), B),
                addDep(immediate(C), D),
                addDep(immediate(B), C),
                immediate(D)
        )).validate();
        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);
        ScriptResource d = graph.getResource(D);

        assertEquals(Tools.toSet(D), c.getClosure());

        assertEquals(Tools.toSet(C, D), b.getClosure());

        assertEquals(Tools.toSet(B, C, D), a.getClosure());

        assertEquals(Collections.emptySet(), d.getClosure());
    }

    public void testFetchMode() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), C),
                    immediate(B),
                    immediate(C)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                onLoad(A),
                addDep(immediate(B), C),
                immediate(C)
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);
        ScriptResource c = graph.getResource(C);

        Map<ScriptResource, FetchMode> resolution = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(resolution.keySet());
        assertEquals(1, resolution.size());
        assertEquals(Tools.toSet(a), resolution.keySet());

        //
        resolution = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(B, null));
        assertResultOrder(resolution.keySet());
        assertEquals(2, resolution.size());
        assertEquals(Tools.toSet(b, c), resolution.keySet());
        assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(c));

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        resolution = graph.resolve(pairs);
        assertResultOrder(resolution.keySet());
        assertEquals(3, resolution.size());
        assertEquals(Tools.toSet(a, b, c), resolution.keySet());
        assertEquals(FetchMode.ON_LOAD, resolution.get(a));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
        assertEquals(FetchMode.IMMEDIATE, resolution.get(c));
    }

    // ********

    public void testResolveDefaultOnLoadFetchMode() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(onLoad(A))).validate();
        ScriptResource a = graph.getResource(A);

        // Use default fetch mode
        Map<ScriptResource, FetchMode> test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        // Get resource with with same fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.ON_LOAD));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        // Don't get resource with other fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.IMMEDIATE));
        assertEquals(0, test.size());
    }

    public void testResolveDefaultImmediateFetchMode() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(immediate(A))).validate();
        ScriptResource a = graph.getResource(A);

        // Use default fetch mode
        Map<ScriptResource, FetchMode> test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, null));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));

        // Dont' get resource with other fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.ON_LOAD));
        assertEquals(0, test.keySet().size());

        // Get resource with the same fetch-mode
        test = graph.resolve(Collections.<ResourceId, FetchMode> singletonMap(A, FetchMode.IMMEDIATE));
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
    }

    public void testResolveDependency1() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    onLoad(B)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                immediate(A),
                onLoad(B)
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
        assertEquals(FetchMode.ON_LOAD, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        pairs.put(A, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(a));
        assertEquals(FetchMode.ON_LOAD, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(b));
    }

    public void testResolveDependency2() throws InvalidResourceException {
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), B),
                    immediate(B)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                onLoad(A),
                immediate(B)
        )).validate();

        ScriptResource a = graph.getResource(A);
        ScriptResource b = graph.getResource(B);

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(A, null);
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));
        assertEquals(FetchMode.IMMEDIATE, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        pairs.put(A, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(a, b), test.keySet());
        assertEquals(FetchMode.ON_LOAD, test.get(a));
        assertEquals(FetchMode.IMMEDIATE, test.get(b));

        //
        pairs = new LinkedHashMap<ResourceId, FetchMode>();
        pairs.put(B, null);
        test = graph.resolve(pairs);
        assertResultOrder(test.keySet());
        assertEquals(Tools.toSet(b), test.keySet());
        assertEquals(FetchMode.IMMEDIATE, test.get(b));
    }

    public void testResolveDisjointDependencies() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(A), C),
                immediate(B),
                immediate(C)
        )).validate();

        // Yes all permutations
        ResourceId[][] samples = { { A }, { A, B }, { B, A }, { A, B, C }, { A, C, B }, { B, A, C }, { B, C, A }, { C, A, B },
                { C, B, A }, };

        //
        LinkedHashMap<ResourceId, FetchMode> pairs = new LinkedHashMap<ResourceId, FetchMode>();
        for (ResourceId[] sample : samples) {
            pairs.clear();
            for (ResourceId id : sample) {
                pairs.put(id, null);
            }
            Map<ScriptResource, FetchMode> test = graph.resolve(pairs);
            assertResultOrder(test.keySet());
        }
    }

    public void testCrossDependency() {
        // Scripts and Module can't depend on each other
        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(immediate(A), B),
                    onLoad(B)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }

        try {
            ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                    addDep(onLoad(A), B),
                    immediate(B)
            )).validate();
            fail("InvalidResourceException expected");
        } catch (InvalidResourceException expected) {
        }
    }

    public void testDuplicateResource() throws InvalidResourceException {
        ScriptGraph graph = ScriptGraph.empty();
        ResourceId shared = new ResourceId(ResourceScope.SHARED, "foo");

        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(shared))).validate();
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(shared))).validate();
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }

        ResourceId portlet = new ResourceId(ResourceScope.PORTLET, "foo");
        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portlet))).validate();
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portlet))).validate();
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }

        ResourceId portal = new ResourceId(ResourceScope.PORTAL, "foo");
        graph = graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portal))).validate();
        try {
            graph.add(CONTEXT_PATH_1, Arrays.asList(immediate(portal))).validate();
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
    }

    /**
     * Similar to {@link #testDuplicateResource()}.
     */
    public void testAddDuplicate() throws InvalidResourceException {
        ScriptGraph initial = ScriptGraph.empty();

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            initial = initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id))).validate();
        }

        for (ResourceScope scope : ResourceScope.values()) {
            ResourceId id = id(scope);
            try {
                initial.add(CONTEXT_PATH_1, Arrays.asList(immediate(id))).validate();
                fail("DuplicateResourceKeyException expected");
            } catch (DuplicateResourceKeyException expected) {
            }

            /* no change in initial */
            Collection<ScriptResource> scopeValues = initial.getResources(scope);
            assertEquals(1, scopeValues.size());

            ScriptResource found = initial.getResource(id);
            assertEquals(id, found.getId());
        }
    }

    public void testAddRemoveGroup() throws InvalidResourceException {
        ScriptResourceDescriptor aDecriptor = new ScriptResourceDescriptor(A, FetchMode.IMMEDIATE, null, GROUP_1, false);
        aDecriptor.getSupportedLocales().add(Locale.GERMANY);
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                aDecriptor,
                new ScriptResourceDescriptor(B, FetchMode.IMMEDIATE, null, GROUP_1, false),
                new ScriptResourceDescriptor(C, FetchMode.IMMEDIATE)
        )).validate();

        graph = graph.add(CONTEXT_PATH_2, Arrays.asList(
                new ScriptResourceDescriptor(D, FetchMode.IMMEDIATE, null, GROUP_2, false)
        )).validate();

        assertEquals(Tools.toSet(A, B), graph.getLoadGroup(GROUP_1).getDependencies());
        ScriptResource a = graph.getResource(A);

        assertEquals(GROUP_1, a.getGroup().getId().getName());
        assertEquals(GROUP_1, graph.getResource(B).getGroup().getId().getName());
        assertNull(graph.getResource(C).getGroup());
        assertEquals(Tools.toSet(D), graph.getLoadGroup(GROUP_2).getDependencies());
        assertEquals(GROUP_2, graph.getResource(D).getGroup().getId().getName());

        graph = graph.remove(CONTEXT_PATH_1);
        assertNull(graph.getLoadGroup(GROUP_1));
        assertNull(graph.getResource(A));
        assertNull(graph.getResource(B));
        assertNull(graph.getResource(C));
        assertEquals(Tools.toSet(D), graph.getLoadGroup(GROUP_2).getDependencies());
        assertEquals(GROUP_2, graph.getResource(D).getGroup().getId().getName());

    }

    public void testLocale() throws InvalidResourceException {
        ScriptResourceDescriptor aDecriptor = new ScriptResourceDescriptor(A, FetchMode.IMMEDIATE, null, GROUP_1, false);
        aDecriptor.getSupportedLocales().add(Locale.GERMANY);
        ScriptGraph graph = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                aDecriptor,
                new ScriptResourceDescriptor(B, FetchMode.IMMEDIATE, null, GROUP_1, false),
                new ScriptResourceDescriptor(C, FetchMode.IMMEDIATE)
        )).validate();

        assertEquals(Tools.toSet(A, B), graph.getLoadGroup(GROUP_1).getDependencies());

        ScriptResource a = graph.getResource(A);

        Map<QualifiedName, String> expectedParams = BaseScriptResource.createBaseParameters(a.getId().getScope(), a.getId().getName());
        assertEquals(expectedParams, a.getParameters(false, null));
        expectedParams.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(Locale.GERMANY));
        assertEquals(expectedParams, a.getParameters(false, Locale.GERMANY));

        expectedParams.put(ResourceRequestHandler.LANG_QN, "");
        expectedParams.put(ResourceRequestHandler.COMPRESS_QN, "min");
        assertEquals(expectedParams, a.getParameters(true, null));
        expectedParams.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(Locale.GERMANY));
        assertEquals(expectedParams, a.getParameters(true, Locale.GERMANY));

        assertEquals(GROUP_1, a.getGroup().getId().getName());
        assertEquals(GROUP_1, graph.getResource(B).getGroup().getId().getName());

        Map<QualifiedName, String> expectedGroupParams = BaseScriptResource.createBaseParameters(ResourceScope.GROUP, GROUP_1);
        assertEquals(expectedGroupParams, a.getGroup().getParameters(false, null));
        expectedGroupParams.put(ResourceRequestHandler.LANG_QN, I18N.toTagIdentifier(Locale.GERMANY));
        assertEquals(expectedGroupParams, a.getGroup().getParameters(false, Locale.GERMANY));

        expectedGroupParams.put(ResourceRequestHandler.COMPRESS_QN, "min");
        assertEquals(expectedGroupParams, a.getGroup().getParameters(true, Locale.GERMANY));
        expectedGroupParams.put(ResourceRequestHandler.LANG_QN, "");
        assertEquals(expectedGroupParams, a.getGroup().getParameters(true, null));

    }

    /**
     * Test that each script of the test collection has no following script that belongs to its closure.
     *
     * @param test the test
     */
    private void assertResultOrder(Collection<ScriptResource> test) {
        ScriptResource[] array = test.toArray(new ScriptResource[test.size()]);
        for (int i = 0; i < array.length; i++) {
            ScriptResource resource = array[i];
            for (int j = i + 1; j < array.length; j++) {
                if (resource.getClosure().contains(array[j].getId()) && resource.getFetchMode().equals(array[j].getFetchMode())) {
                    failure("Was not expecting result order " + test, new Exception());
                }
            }
        }
    }
}
