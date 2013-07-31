/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gatein.qunit;

import static org.jboss.arquillian.graphene.Graphene.waitModel;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.graphene.enricher.findby.ByJQuery;
import org.jboss.arquillian.graphene.enricher.findby.FindBy;
import org.jboss.arquillian.qunit.api.pages.QUnitTestPage;
import org.jboss.arquillian.qunit.junit.model.QUnitAssertionImpl;
import org.jboss.arquillian.qunit.junit.model.QUnitTestImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * 
 * @author Lukas Fryc
 * @author Tolis Emmanouilidis
 * 
 */
public class QUnitPage implements QUnitTestPage {

	@FindBy(css = "#qunit-testresult .total")
	WebElement qunitTestResults;

	@FindBy(css = "#qunit-tests > li")
	List<WebElement> qunitTestsList;

	private static final String moduleNameClassSelector = "module-name";

	private static final String qunitTestNameClassSelector = "test-name";

	private static final String qunitTestRunTimeClassSelector = "runtime";

	private static final String qunitTestFailedCssSelector = "failed";

	private static String qunitTestPassedCssSelector = "passed";

	private static final String qunitTestAssertionListJquerySelector = ".qunit-assert-list > li";

	private static final String assertionSourceJquerySelector = ".test-source td pre";

	public void waitUntilTestsExecutionIsCompleted() {
		waitModel().withTimeout(5, TimeUnit.MINUTES).until()
				.element(qunitTestResults).is().present();
	}

	public int getTestsSize() {
		return qunitTestsList.size();
	}

	public QUnitTestImpl[] getTests() {
		if (!CollectionUtils.isEmpty(qunitTestsList)) {
			final QUnitTestImpl[] results = new QUnitTestImpl[qunitTestsList
					.size()];
			int qunitTestIndex = 0;
			for (WebElement qunitTest : qunitTestsList) {
				// workaround to avoid no such element exceptions
				final List<WebElement> modules = qunitTest.findElements(By
						.className(moduleNameClassSelector));
				final String moduleName = (!CollectionUtils.isEmpty(modules)) ? getTrimmedText(modules
						.get(0)) : null;

				// workaround to avoid no such element exceptions
				final List<WebElement> names = qunitTest.findElements(By
						.className(qunitTestNameClassSelector));
				final String qunitTestName = (!CollectionUtils.isEmpty(names)) ? getTrimmedText(names
						.get(0)) : null;

				// workaround to avoid no such element exceptions
				final List<WebElement> runTimes = qunitTest.findElements(By
						.className(qunitTestRunTimeClassSelector));
				final String runTime = (!CollectionUtils.isEmpty(runTimes)) ? getTrimmedText(runTimes
						.get(0)) : null;

				final int passed = Integer
						.valueOf(getTrimmedText(qunitTest.findElement(By
								.className(qunitTestPassedCssSelector))));
				final int failed = Integer
						.valueOf(getTrimmedText(qunitTest.findElement(By
								.className(qunitTestFailedCssSelector))));

				final List<WebElement> assertions = qunitTest
						.findElements(ByJQuery
								.jquerySelector(qunitTestAssertionListJquerySelector));

				final QUnitTestImpl qunitTestResult = (new QUnitTestImpl())
						.setModuleName(moduleName).setName(qunitTestName)
						.setPassed(passed).setFailed(failed)
						.setRunTime(runTime)
						.setFailed(isQunitTestFailed(failed))
						.setIndex(qunitTestIndex);

				if (!CollectionUtils.isEmpty(assertions)) {
					QUnitAssertionImpl[] qunitAssertions = new QUnitAssertionImpl[assertions
							.size()];
					int assertionIndex = 0;
					for (WebElement assertion : assertions) {
						final boolean pass = assertion
								.getAttribute("class").equalsIgnoreCase(
										"pass");
						final QUnitAssertionImpl assertionDTO = (new QUnitAssertionImpl())
								.setFailed(!pass)
								.setMessage(
										!pass ? getTrimmedText(assertion.findElement(ByJQuery
												.jquerySelector(assertionSourceJquerySelector)))
												: null);
						qunitAssertions[assertionIndex++] = assertionDTO;
					}
					qunitTestResult.setAssertions(qunitAssertions);
				}

				results[qunitTestIndex++] = qunitTestResult;
			}
			return results;
		}
		return null;
	}

	public String getFailedAssertionMessages(QUnitAssertionImpl[] assertions) {
		if (!ArrayUtils.isEmpty(assertions)) {
			StringBuilder sources = new StringBuilder();
			for (QUnitAssertionImpl assertion : assertions) {
				if (assertion.isFailed()
						&& !StringUtils.isEmpty(assertion.getMessage())) {
					sources.append(assertion.getMessage()).append(" ");
				}
			}
			return sources.toString();
		}
		return "";
	}

	private String getTrimmedText(WebElement w) {
		return w != null && w.getText() != null ? w.getText().trim() : null;
	}

	private boolean isQunitTestFailed(int failed) {
		return failed > 0;
	}
}
