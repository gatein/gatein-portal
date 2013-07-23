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

import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.spi.annotations.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.qunit.api.model.DeploymentMethod;
import org.jboss.arquillian.qunit.api.model.TestMethod;
import org.jboss.arquillian.qunit.api.model.TestSuite;
import org.jboss.arquillian.qunit.junit.model.QUnitAssertionImpl;
import org.jboss.arquillian.qunit.junit.model.QUnitTestImpl;
import org.jboss.arquillian.qunit.pages.QUnitPage;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.openqa.selenium.WebDriver;

@RunWith(Arquillian.class)
@RunAsClient
public class GateInQUnitTestCase {

    private static final String TEST_ARCHIVE_FILE_NAME = "test.war";

    @ArquillianResource
    private URL contextPath;

    @Drone @FireFox
    WebDriver driver;

    @Page
    QUnitPage qunitPage;

    public static TestSuite suite;

    public static RunNotifier notifier;

    @Deployment(testable = false)
    public static Archive<?> deployment() {
        final DeploymentMethod deploymentMethod = suite.getDeploymentMethod();
        final Archive<?> archive = deploymentMethod != null ? deploymentMethod.getArchive() : ShrinkWrap.create(
                WebArchive.class, TEST_ARCHIVE_FILE_NAME);

        ExplodedImporter resources = ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class);

        String[] paths = suite.getQUnitResources().split(",");
        for (String path : paths) {
            resources.importDirectory(path.trim());
        }

        archive.merge(resources.as(GenericArchive.class), "/", Filters.includeAll());
        return archive;
    }

    @Test
    public void executeTestCases() {
        final TestMethod[] qunitTestMethods = suite.getTestMethods();
        if (!ArrayUtils.isEmpty(qunitTestMethods)) {
            for (TestMethod method : qunitTestMethods) {
                if (!StringUtils.isEmpty(method.getQunitTestFile())) {
                    executeQunitTestFile(method);
                }
            }
        }
    }

    private void executeQunitTestFile(TestMethod testMethod) {
        driver.get((new StringBuilder()).append(contextPath.toExternalForm()).append(testMethod.getQunitTestFile()).toString());

        qunitPage.waitUntilTestsExecutionIsCompleted();
        final QUnitTestImpl[] qunitTests = qunitPage.getTests();

        if (!ArrayUtils.isEmpty(qunitTests)) {
            final Description suiteDescription = Description.createSuiteDescription(testMethod.getMethod().getDeclaringClass()
                    .getName(), testMethod.getMethod().getAnnotations());
            for (QUnitTestImpl qunitTestResult : qunitTests) {
                final Description testDescription = Description.createTestDescription(testMethod.getMethod()
                        .getDeclaringClass(), qunitTestResult.getDescriptionName());
                suiteDescription.addChild(testDescription);
                notifier.fireTestStarted(testDescription);
                if (qunitTestResult.isFailed()) {
                    notifier.fireTestFailure(new Failure(testDescription, new Exception(generateFailedMessage(qunitTestResult
                            .getAssertions()))));
                } else {
                    notifier.fireTestFinished(testDescription);
                }
            }
            suite.getDescription().addChild(suiteDescription);
        }
    }

    private String generateFailedMessage(QUnitAssertionImpl[] assertions) {
        return (new StringBuilder()).append("Failed ").append(qunitPage.getFailedAssertionMessages(assertions)).toString();
    }
}
