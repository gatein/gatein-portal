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
import java.util.Map;

import junit.framework.TestCase;

import org.exoplatform.web.application.javascript.JavascriptConfigService.StaticScriptResources;
import org.gatein.portal.controller.resource.script.StaticScriptResource;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestStaticScriptResources extends TestCase {

    /**
     * 2014-05-29 07:16 UTC
     */
    private static final long LAST_MOD_140529 = 1401347762L;
    private static final String CONTEXT_PATH_1 = "/my-app-1";
    private static final StaticScriptResource RESOURCE_1 = new StaticScriptResource(CONTEXT_PATH_1, "/dir", "/my/resource1", LAST_MOD_140529);
    private static final String CONTEXT_PATH_2 = "/my-app-2";
    private static final StaticScriptResource RESOURCE_2 = new StaticScriptResource(CONTEXT_PATH_2, "/dir", "/my/resource2", LAST_MOD_140529);
    private static final StaticScriptResource RESOURCE_3 = new StaticScriptResource(CONTEXT_PATH_2, "/dir", "/my/resource1", LAST_MOD_140529);

    private static Map<String, StaticScriptResource> toStaticScriptResourceMap(StaticScriptResource... resources) {
        Map<String, StaticScriptResource> result = new HashMap<String, StaticScriptResource>();
        for (int i = 0; i < resources.length; i++) {
            StaticScriptResource res = resources[i];
            result.put(res.getResourcePath(), res);
        }
        return Collections.unmodifiableMap(result);
    }

    public void testEmpty() throws DuplicateResourceKeyException {
        assertEquals(0, StaticScriptResources.empty().getEntries().size());

        /* unmodifiable? */
        try {
            StaticScriptResources.empty().getEntries().put("/whatever", RESOURCE_1);
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expected) {
        }
    }


    public void testAddToEmpty() throws DuplicateResourceKeyException {
        StaticScriptResources empty = StaticScriptResources.empty();
        assertSame(empty, empty.add(null));
        assertSame(empty, empty.add(Collections.<StaticScriptResource> emptyList()));

        StaticScriptResources actual = StaticScriptResources.empty().add(Arrays.asList(RESOURCE_1));
        assertEquals(toStaticScriptResourceMap(RESOURCE_1), actual.getEntries());

        /* unmodifiable? */
        try {
            actual.getEntries().put("/whatever", RESOURCE_1);
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expectedException) {
        }
    }


    public void testAddRemoveToNonEmpty() throws DuplicateResourceKeyException {
        StaticScriptResources initial = StaticScriptResources.empty().add(Arrays.asList(RESOURCE_1));
        StaticScriptResources actual = initial.add(Arrays.asList(RESOURCE_2));

        assertEquals(toStaticScriptResourceMap(RESOURCE_1, RESOURCE_2), actual.getEntries());

        /* unmodifiable? */
        try {
            actual.getEntries().put("/whatever", RESOURCE_1);
            fail("UnsupportedOperationException expected when adding to a presupposedly unmodifiable map");
        } catch (UnsupportedOperationException expectedException) {
        }

        StaticScriptResources ctx2Removed = actual.remove(CONTEXT_PATH_2);
        assertEquals(toStaticScriptResourceMap(RESOURCE_1), ctx2Removed.getEntries());

        StaticScriptResources bothRemoved = ctx2Removed.remove(CONTEXT_PATH_1);
        assertEquals(Collections.emptyMap(), bothRemoved.getEntries());


        /* remove nothing */
        StaticScriptResources nothingRemoved = initial.remove("/no/such/context/there");
        assertSame(initial, nothingRemoved);
        StaticScriptResources nothingRemoved2 = initial.remove(null);
        assertSame(initial, nothingRemoved2);
    }

    public void testAddInacceptableToNonEmpty() throws DuplicateResourceKeyException {
        StaticScriptResources initial = StaticScriptResources.empty().add(Arrays.asList(RESOURCE_1));

        try {
            initial.add(Arrays.asList(RESOURCE_3));
            fail("DuplicateResourceKeyException expected");
        } catch (DuplicateResourceKeyException expected) {
        }
        /* assert that there is no change */
        assertEquals(toStaticScriptResourceMap(RESOURCE_1), initial.getEntries());
    }

}
