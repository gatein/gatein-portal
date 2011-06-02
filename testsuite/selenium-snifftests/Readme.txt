**************
To run the Selenium tests for GateIn
**************

From mvn command line:
* Start a GateIn server (Tomcat or JBoss)
* Start the server
* Launch the tests:
** mvn install -Pselenium to run the html recorded Scripts generated in Java during the process

Changing the port (JBoss is using 4444 in default config):
** mvn install -Pselenium -Dselenium.port=6666

Changing the browser (firefox, safari, iexplorer, opera):
** mvn install -Pselenium -Dselenium.browser=safari

From Eclipse:
* Start a GateIn server (Tomcat or JBoss)
* Start the Selenium server ( GateIn server (Tomcat or JBoss)
** Main class: org.openqa.selenium.server.SeleniumServer
** Parameter: -userExtensions ${project_loc}/src/suite/user-extensions.js
** Some .launch files are available in src/eclipse
* Run any test like a unit test


**************
Informations:
**************

* suite/ contains the recorded tests
** They can be edited using the Selenium IDE
* src/java/main/java/ contains a generator to create the same tests in Java
** One Test per Selenium test
* target/generated/test contains the generated tests

******************************
Known Issues:
******************************

For Mac OSX Snow Leopard Users:
* Known Issue make FF to crash : http://jira.openqa.org/browse/SRC-743
** Details for the fix : http://jira.openqa.org/browse/SRC-743?focusedCommentId=19345&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_19345

For selenium-maven-plugin and JUnit Selenium users here is what worked for me:

* patch selenium-rc with this patch
<begining of patch>
Index: server-coreless/src/main/java/org/openqa/selenium/server/browserlaunchers/SystemUtils.java
===================================================================
--- server-coreless/src/main/java/org/openqa/selenium/server/browserlaunchers/SystemUtils.java	(revision 7242)
+++ server-coreless/src/main/java/org/openqa/selenium/server/browserlaunchers/SystemUtils.java	(working copy)
@@ -14,7 +14,7 @@
             return WindowsUtils.getExactPathEnvKey();
         }
         if (Os.isFamily("mac")) {
-            return "DYLD_LIBRARY_PATH";
+            return "XXX_DYLD_LIBRARY_PATH";
         }
         // TODO other linux?
         return "LD_LIBRARY_PATH";
<end of patch>
* checkout, patch & compile selenium-rc 1.0.2-SNAPSHOT (it requires the selenium-core to be compiled or get the SNAPSHOTs one the openqa repo)
** http://svn.openqa.org/svn/selenium-rc/trunk
** http://svn.openqa.org/svn/selenium-core/trunk
** checkout and patch the selenium-maven-plugin to use selenium-server 1.0.2-SNAPSHOT
** http://svn.codehaus.org/mojo/trunk/mojo/selenium-maven-plugin
** Change the dependency 1.0.1 to 1.0.2-SNAPSHOT

Then in this project you can force the use of those versions:
* mvn install -Pselenium -Denforcer.skip=true -Dorg.selenium.maven-plugin.version=1.1-SNAPSHOT -Dorg.selenium.server.version=1.0.2-SNAPSHOT
* mvn eclipse:eclipse -Pselenium -Denforcer.skip=true -Dorg.selenium.maven-plugin.version=1.1-SNAPSHOT -Dorg.selenium.server.version=1.0.2-SNAPSHOT

