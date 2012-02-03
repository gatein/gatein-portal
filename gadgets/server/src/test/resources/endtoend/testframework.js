/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

var tests;

function assertTrue(msg, value) {
  if (!value) {
    throw "assertTrue() failed: " + msg;
  }
}

function assertFalse(msg, value) {
  if (value) {
    throw "assertFalse() failed: " + msg;
  }
}

function assertEquals(msg, a, b) {
  if (a != b) {
    throw "assertEquals() failed: " + msg +
        "\nExpected \"" + a + "\", was \"" + b + "\"";
  }
}

/**
 * Signals the server code that a test successfully finished.  This
 * method must be called to verify that a test completed successfully,
 * instead of simply failing to load.
 */

function finished() {
  alert("FINISHED");
  // After finishing run the next test..
  runOneTest();
}

/** Executes the test identifed by the testMethod URL parameter */
function executeTest() {
  var params = gadgets.util.getUrlParameters();
  var testMethod = params["testMethod"];
  if (!testMethod) {
    throw "No testMethod parameter found.";
  }

  // "all": run all the tests
  if ("all" == testMethod) {
    allTests();
  } else {
    // Show an alert for the test method name, identifying what test started.
    alert(testMethod);

    var testMethodFunction = tests[testMethod];
    if (!testMethodFunction) {
      throw "Test method " + testMethod + " not found.";
    }

    // Execute the test method
    testMethodFunction();
  }
}

var testFunctions = [];

function runOneTest() {
  var t = testFunctions.pop();
  if (t) {
    alert(t);
    tests[t]();
  }
}
function allTests() {
  // Collect the test names and iterate through them serially
  for (var testMethod in tests) {
    testFunctions.push(testMethod);
  }
  runOneTest();
}

gadgets.util.registerOnLoadHandler(executeTest);
