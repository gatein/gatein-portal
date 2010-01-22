**************
To run the Selenium tests for GateIn:
**************

* Fresh install a server (Tomcat or JBoss)
* Start the server
* Launch the tests:
** mvn install -Pselenese to run the html recorded Scripts
** mvn install -Pjunit to run the Test_ALL suite (which currently represents the whole recorded tests)


**************
Informations:
**************

* suite/ contains the recorded tests
** They can be edited using the Selenium IDE
* src/java/test/ contains generated tests in Java
** One Test per Selenium test
** One Test_all.java test which is a all in one (because currently the tests execution order is important) -- should not be the case

****************************
Running the tests in Eclipse
****************************
* Install SeleniumRC from http://seleniumhq.org/download/
** Probably http://release.seleniumhq.org/selenium-remote-control/1.0.1/selenium-remote-control-1.0.1-dist.zip
* Start a Selenium Server
** java -jar selenium-server.jar -interactive
* In Eclipse run you tests like classical JUnit tests