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

package org.gatein.portal.controller.resource.script;

import java.util.Arrays;
import java.util.Locale;

import junit.framework.TestCase;

import org.exoplatform.portal.resource.InvalidResourceException;
import org.exoplatform.web.application.javascript.DependencyDescriptor;
import org.exoplatform.web.application.javascript.ScriptResourceDescriptor;
import org.gatein.portal.controller.resource.ResourceId;
import org.gatein.portal.controller.resource.ResourceRequestHandler;
import org.gatein.portal.controller.resource.ResourceScope;
import org.gatein.portal.controller.resource.script.Module.Local.Content;
import org.gatein.portal.controller.resource.script.ScriptResource.DepInfo;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class TestScriptResource extends TestCase {
    private static final String CONTEXT_PATH_1 = "/my-app-1";
    /** . */
    private static final ResourceId SHARED_A = new ResourceId(ResourceScope.SHARED, "SHARED_A");
    /** . */
    private static final ResourceId SHARED_B = new ResourceId(ResourceScope.SHARED, "SHARED_B");
    /** . */
    private static final ResourceId SHARED_C = new ResourceId(ResourceScope.SHARED, "SHARED_C");

    private static ScriptResourceDescriptor immediate(ResourceId rid) {
        return new ScriptResourceDescriptor(rid, FetchMode.IMMEDIATE);
    }

    private static ScriptResourceDescriptor addDep(ScriptResourceDescriptor desc, ResourceId depId) {
        DependencyDescriptor dependency = new DependencyDescriptor(depId, null, null);
        desc.getDependencies().add(dependency);
        return desc;
    }

    public void testImmutable() throws InvalidResourceException {
        ScriptResource initial = ScriptGraph.empty().add(CONTEXT_PATH_1, Arrays.asList(
                addDep(immediate(SHARED_A), SHARED_B),
                immediate(SHARED_B)
        )).getResource(SHARED_A);
        try {
            initial.getClosure().add(SHARED_C);
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getDependencies().add(SHARED_C);
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getDepInfo(SHARED_B).add(new DepInfo("foo", "bar"));
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getModules().add(new Module.Local(SHARED_C, CONTEXT_PATH_1, new Content[0], null, 0));
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getParameters(true, null).put(ResourceRequestHandler.LANG_QN, "whatever");
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getParameters(false, null).put(ResourceRequestHandler.LANG_QN, "whatever");
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getParameters(true, Locale.US).put(ResourceRequestHandler.LANG_QN, "whatever");
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
        try {
            initial.getParameters(false, Locale.US).put(ResourceRequestHandler.LANG_QN, "whatever");
            fail("UnsupportedOperationException expected when adding to a presupposedly immutable collection.");
        } catch (UnsupportedOperationException expectedException) {
        }
    }

}
