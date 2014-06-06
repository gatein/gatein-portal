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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.exoplatform.web.application.javascript.JavascriptConfigService.PathMappings;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestPathMappings extends TestCase {

    private static final String CONTEXT_PATH_1 = "/ctx1";
    private static final String PREFIX_1 = "/my/prefix1";
    private static final String TARGET_PATH_1 = "//my-fast-cdn.com/my/prefix1/1.2.3";
    private static final String CONTEXT_PATH_2 = "/ctx2";
    private static final String PREFIX_2 = "/my/prefix2";
    private static final String TARGET_PATH_2 = "https://another-cdn.com/my/prefix2/3.2.1";

    private static Map<String, List<String>> toPathMap(String... pathEntries) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (int i = 0; i < pathEntries.length; ) {
            String prefix = pathEntries[i++];
            List<String> targetPath = Collections.unmodifiableList(Arrays.asList(pathEntries[i++]));
            result.put(prefix, targetPath);
        }
        return Collections.unmodifiableMap(result);
    }

    public void testEmpty() throws DuplicateResourceKeyException {
        assertEquals(0, PathMappings.empty().getEntries().size());
        assertEquals(0, PathMappings.empty().getPrefixesToContextPaths().size());

        /* unmodifiable? */
        try {
            PathMappings.empty().getEntries().put(PREFIX_1, Collections.unmodifiableList(Arrays.asList(TARGET_PATH_1)));
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expected) {
        }
    }

    public void testAddToEmpty() throws DuplicateResourceKeyException {
        PathMappings empty = PathMappings.empty();
        assertSame(empty, empty.add(CONTEXT_PATH_1, null));
        assertSame(empty, empty.add(CONTEXT_PATH_1, Collections.<String, List<String>> emptyMap()));

        Map<String, List<String>> toAdd = toPathMap(PREFIX_2, TARGET_PATH_2);
        PathMappings actual = PathMappings.empty().add(CONTEXT_PATH_2, toAdd);
        Map<String, List<String>> expected = toPathMap(PREFIX_2, TARGET_PATH_2);
        assertEquals(expected, actual.getEntries());
        assertEquals(1, actual.getPrefixesToContextPaths().size());
        assertEquals(Collections.singleton(CONTEXT_PATH_2), actual.getPrefixesToContextPaths().get(PREFIX_2));

        /* unmodifiable? */
        try {
            actual.getEntries().put(PREFIX_1, Collections.unmodifiableList(Arrays.asList(TARGET_PATH_1)));
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expectedException) {
        }
    }

    public void testAddRemoveToNonEmpty() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        PathMappings initial = PathMappings.empty().add(CONTEXT_PATH_1, initialMap);
        Map<String, List<String>> toAdd = toPathMap(PREFIX_2, TARGET_PATH_2);
        PathMappings actual = initial.add(CONTEXT_PATH_2, toAdd);

        Map<String, List<String>> expected = toPathMap(PREFIX_1, TARGET_PATH_1, PREFIX_2, TARGET_PATH_2);
        assertEquals(expected, actual.getEntries());
        assertEquals(expected, actual.getEntries());
        assertEquals(2, actual.getPrefixesToContextPaths().size());
        assertEquals(Collections.singleton(CONTEXT_PATH_1), actual.getPrefixesToContextPaths().get(PREFIX_1));
        assertEquals(Collections.singleton(CONTEXT_PATH_2), actual.getPrefixesToContextPaths().get(PREFIX_2));

        /* unmodifiable? */
        try {
            actual.getEntries().put(PREFIX_1, Collections.unmodifiableList(Arrays.asList(TARGET_PATH_1)));
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expectedException) {
        }

        PathMappings ctx2Removed = actual.remove(CONTEXT_PATH_2);
        assertEquals(toPathMap(PREFIX_1, TARGET_PATH_1), ctx2Removed.getEntries());

        PathMappings bothRemoved = ctx2Removed.remove(CONTEXT_PATH_1);
        assertEquals(Collections.emptyMap(), bothRemoved.getEntries());


        /* remove nothing */
        PathMappings nothingRemoved = initial.remove("/no/such/context/there");
        assertSame(initial, nothingRemoved);
        PathMappings nothingRemoved2 = initial.remove(null);
        assertSame(initial, nothingRemoved2);
    }

    public void testAddInacceptableToNonEmpty() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        PathMappings initial = PathMappings.empty().add(CONTEXT_PATH_1, initialMap);

        try {
            initial.add(CONTEXT_PATH_2, toPathMap(PREFIX_1, TARGET_PATH_2));
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
        /* assert that there is no change */
        assertEquals(toPathMap(PREFIX_1, TARGET_PATH_1), initial.getEntries());
    }

    public void testAddRemoveVerySame() throws DuplicateResourceKeyException {
        PathMappings initial = PathMappings.empty().add(CONTEXT_PATH_1, toPathMap(PREFIX_1, TARGET_PATH_1));
        PathMappings actual = initial.add(CONTEXT_PATH_2, toPathMap(PREFIX_1, TARGET_PATH_1));

        Map<String, List<String>> expected = toPathMap(PREFIX_1, TARGET_PATH_1);
        assertEquals(expected, actual.getEntries());
        assertEquals(1, actual.getPrefixesToContextPaths().size());
        Set<String> contexts = actual.getPrefixesToContextPaths().get(PREFIX_1);
        assertEquals(2, contexts.size());
        assertTrue(contexts.contains(CONTEXT_PATH_1));
        assertTrue(contexts.contains(CONTEXT_PATH_2));

        /* unmodifiable? */
        try {
            actual.getEntries().put(PREFIX_1, Collections.unmodifiableList(Arrays.asList(TARGET_PATH_1)));
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expectedException) {
        }

        PathMappings ctx1Removed = actual.remove(CONTEXT_PATH_1);
        assertEquals(toPathMap(PREFIX_1, TARGET_PATH_1), ctx1Removed.getEntries());
        assertEquals(1, ctx1Removed.getPrefixesToContextPaths().size());
        assertEquals(Collections.singleton(CONTEXT_PATH_2), ctx1Removed.getPrefixesToContextPaths().get(PREFIX_1));

        PathMappings ctx2Removed = actual.remove(CONTEXT_PATH_2);
        assertEquals(toPathMap(PREFIX_1, TARGET_PATH_1), ctx2Removed.getEntries());
        assertEquals(1, ctx2Removed.getPrefixesToContextPaths().size());
        assertEquals(Collections.singleton(CONTEXT_PATH_1), ctx2Removed.getPrefixesToContextPaths().get(PREFIX_1));

        PathMappings bothRemoved = ctx2Removed.remove(CONTEXT_PATH_1);
        assertEquals(Collections.emptyMap(), bothRemoved.getEntries());
        assertEquals(0, bothRemoved.getPrefixesToContextPaths().size());

    }

}
