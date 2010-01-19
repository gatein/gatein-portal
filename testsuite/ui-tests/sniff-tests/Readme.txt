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

