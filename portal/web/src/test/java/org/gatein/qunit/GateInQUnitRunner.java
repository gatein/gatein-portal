/*
 * Copyright (C) 2013 eXo Platform SAS.
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

package org.gatein.qunit;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;

import org.jboss.arquillian.qunit.api.model.TestSuite;
import org.jboss.arquillian.qunit.junit.model.TestSuiteImpl;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;

public class GateInQUnitRunner extends Suite {

    private TestSuite suite;

    private Description desc;

    public GateInQUnitRunner(Class<?> suiteClass) throws Exception {
        super(suiteClass, new LinkedList<Runner>());
        this.suite = new TestSuiteImpl(suiteClass);
    }

    @Override
    public void run(RunNotifier notifier) {
        JUnitCore core = new JUnitCore();
        GateInQUnitTestCase.notifier = notifier;
        GateInQUnitTestCase.suite = suite;
        core.run(GateInQUnitTestCase.class);
    }

    @Override
    public Description getDescription() {
        if (this.desc == null) {
            this.desc = Description.createSuiteDescription(suite.getSuiteClass().getName(), suite.getSuiteClass()
                    .getAnnotations());
        }
        return this.desc;
    }
}
