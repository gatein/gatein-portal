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
import java.util.Map;

import org.gatein.portal.controller.resource.script.StaticScriptResource;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestImmutableStaticScriptResourcesBuilder extends TestCase {

    /**
     * 2014-05-29 07:16 UTC
     */
    private static final long LAST_MOD_140529 = 1401347762L;
    private static final StaticScriptResource RESOURCE_1 = new StaticScriptResource("/my-app", "/dir", "/my/resource1", LAST_MOD_140529);
    private static final StaticScriptResource RESOURCE_2 = new StaticScriptResource("/my-app", "/dir", "/my/resource2", LAST_MOD_140529);

    private static Map<String, StaticScriptResource> toStaticScriptResourceMap(StaticScriptResource... resources) {
        Map<String, StaticScriptResource> result = new HashMap<String, StaticScriptResource>();
        for (int i = 0; i < resources.length; i++) {
            StaticScriptResource res = resources[i];
            result.put(res.getResourcePath(), res);
        }
        return Collections.unmodifiableMap(result);
    }

    public void testAcceptAcceptable() throws DuplicateResourceKeyException {
        Map<String, StaticScriptResource> initialMap = toStaticScriptResourceMap(RESOURCE_1);
        Collection<StaticScriptResource> toAdd = Arrays.asList(RESOURCE_2);
        Map<String, StaticScriptResource> actual = new JavascriptConfigService.ImmutableStaticScriptResourcesBuilder(
                initialMap).add(toAdd).build();
        Map<String, StaticScriptResource> expected = toStaticScriptResourceMap(RESOURCE_1, RESOURCE_2);
        assertEquals(expected, actual);
    }

    public void testAcceptInacceptable() {
        Map<String, StaticScriptResource> initialMap = toStaticScriptResourceMap(RESOURCE_1);
        Collection<StaticScriptResource> toAdd = Arrays.asList(RESOURCE_1);
        try {
            new JavascriptConfigService.ImmutableStaticScriptResourcesBuilder(
                    initialMap).add(toAdd).build();
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
    }

    public void testRemoveAvailable() throws DuplicateResourceKeyException {
        Map<String, StaticScriptResource> initialMap = toStaticScriptResourceMap(RESOURCE_1, RESOURCE_2);
        Collection<StaticScriptResource> toRemove = Arrays.asList(RESOURCE_2);
        Map<String, StaticScriptResource> actual = new JavascriptConfigService.ImmutableStaticScriptResourcesBuilder(
                initialMap).removeAll(toRemove).build();
        Map<String, StaticScriptResource> expected = toStaticScriptResourceMap(RESOURCE_1);
        assertEquals(expected, actual);
    }

    public void testRemoveUnavailable() throws DuplicateResourceKeyException {
        Map<String, StaticScriptResource> initialMap = toStaticScriptResourceMap(RESOURCE_1);
        Collection<StaticScriptResource> toRemove = Arrays.asList(RESOURCE_2);
        Map<String, StaticScriptResource> actual = new JavascriptConfigService.ImmutableStaticScriptResourcesBuilder(
                initialMap).removeAll(toRemove).build();
        Map<String, StaticScriptResource> expected = toStaticScriptResourceMap(RESOURCE_1);
        assertEquals(expected, actual);
    }

    public void testBuildUnmodifiable() throws DuplicateResourceKeyException {
        Map<String, StaticScriptResource> initialMap = toStaticScriptResourceMap(RESOURCE_1);
        Map<String, StaticScriptResource> actual = new JavascriptConfigService.ImmutableStaticScriptResourcesBuilder(initialMap).build();
        try {
            actual.put(RESOURCE_2.getResourcePath(), RESOURCE_2);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException expected) {
        }
    }

}
