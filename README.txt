Welcome to GateIn:
===========================

This will explain you how to build a package of GateIn with Tomcat or JBoss.

*****************
* COMPILATION
*****************

* mvn install
For example: mvn install

Note: If you run "mvn install" twice in a row without cleaning, one test will
fail. To workaround this issue you will need to delete the test data located
here: component/portal/target/temp/

**********************
* MAVEN CONFIGURATION:
**********************

* edit packaging/profiles.xml and replace the values mentioned so that:
  * ${exo.projects.directory.dependencies} directory contains :
  ** ${exo.projects.app.tomcat.version}/ a clean Tomcat installation, to be used as packaging template
  ** ${exo.projects.app.jboss.version}/ a clean JBoss installation, to be used as packaging template

* See http://maven.apache.org/guides/introduction/introduction-to-profiles.html for more informations
  
Alternatively you can edit your local settings.xml to add the information
provided in profiles.xml. It will override what's in the provided file.

*****************
* PACKAGING:
*****************

* mvn install -Ppkg-tomcat
** Creates a Tomcat delivery in packaging/pkg/target/tomcat/ 

* mvn install -Ppkg-jbossas
** Creates a JBossAS delivery in packaging/pkg/target/jboss/

*****************
* STARTING:
*****************
* On Tomcat: go to the tomcat directory (or unzip the archive in your favorite location) and execute 'bin/gatein.sh start' ('bin/gatein.bat start' on Windows)
*            Alternatively you can use the run command to execute GateIn in the current window 'bin/gatein.sh run' ('bin/gaete.bar run' on Windows)
*            The gatein.sh script can be passed additional arguments between the gatein.sh script and the tomcat command 'bin/gatein.sh -Dfoo=bar run'
* On JBoss: go to the jboss directory (or unzip the archive in your favorite location) and execute 'bin/run.sh start' ('bin/run.bat start' on Windows)

* Go to http://localhost:8080/portal to see the homepage of the portal. That's it. 

