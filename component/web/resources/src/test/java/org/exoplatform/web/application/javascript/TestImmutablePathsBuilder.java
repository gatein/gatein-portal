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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestImmutablePathsBuilder extends TestCase {

    private static final String PREFIX_1 = "/my/prefix1";
    private static final String TARGET_PATH_1 = "//my-fast-cdn.com/my/prefix1/1.2.3";
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

    public void testAcceptAcceptable() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        Map<String, List<String>> toAdd = toPathMap(PREFIX_2, TARGET_PATH_2);
        Map<String, List<String>> actual = new JavascriptConfigService.ImmutablePathsBuilder(
                initialMap).add(toAdd).build();
        Map<String, List<String>> expected = toPathMap(PREFIX_1, TARGET_PATH_1, PREFIX_2, TARGET_PATH_2);
        assertEquals(expected, actual);
    }

    public void testAcceptInacceptable() {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        Map<String, List<String>> toAdd = toPathMap(PREFIX_1, TARGET_PATH_1);
        try {
            new JavascriptConfigService.ImmutablePathsBuilder(
                    initialMap).add(toAdd).build();
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
    }

    public void testRemoveAvailable() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        Collection<String> toRemove = Arrays.asList(PREFIX_2);
        Map<String, List<String>> actual = new JavascriptConfigService.ImmutablePathsBuilder(
                initialMap).removeAll(toRemove).build();
        Map<String, List<String>> expected = toPathMap(PREFIX_1, TARGET_PATH_1);
        assertEquals(expected, actual);
    }

    public void testRemoveUnavailable() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        Collection<String> toRemove = Arrays.asList(PREFIX_2);
        Map<String, List<String>> actual = new JavascriptConfigService.ImmutablePathsBuilder(
                initialMap).removeAll(toRemove).build();
        Map<String, List<String>> expected = toPathMap(PREFIX_1, TARGET_PATH_1);
        assertEquals(expected, actual);
    }

    public void testBuildUnmodifiable() throws DuplicateResourceKeyException {
        Map<String, List<String>> initialMap = toPathMap(PREFIX_1, TARGET_PATH_1);
        Map<String, List<String>> actual = new JavascriptConfigService.ImmutablePathsBuilder(initialMap).build();
        try {
            actual.put(PREFIX_2, Arrays.asList(TARGET_PATH_2));
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException expected) {
        }
    }

}
