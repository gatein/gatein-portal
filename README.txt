Welcome to gatein codebase:
===========================

This will explain you how to build a package of GateIn with Tomcat or JBoss.

*****************
* COMPILATION
*****************

* mvn install

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
** Creates compressed archives in packaging/pkg/target/target

* mvn install -Ppkg-jbossas
** Creates a JBossAS delivery in packaging/pkg/target/jboss/
** Creates compressed archives in packaging/pkg/target/target

*****************
* STARTING:
*****************
* On Tomcat: go to the tomcat directory (or unzip the archive in your favorite location) and execute 'bin/gatein.sh start' ('bin/gatein.bat start' on Windows)
* On JBoss: go to the jboss directory (or unzip the archive in your favorite location) and execute 'bin/run.sh start' ('bin/run.bat start' on Windows)

* Go to http://localhost:8080/portal to see the homepage of the portal. That's it. 

******************
* ADDITIONAL INFO:
******************
Portal packaging tooling:
* An embedded version of exobuild (exo packaging tooling) is pom embbed and autorun with the install goal
* http://svn.exoplatform.org/projects/utils/exopackage/ for the sources

Process:
1/ Tomcat or JBossAS packaging are done during the "mvn install" process of
the "packaging" module
2/ exopackage-x.x.x is downloaded from maven
4/ product and module for this version are stored in the current project, and
found by exopackage during the run
5/ a maven-exec-plugin calls exobuild from maven with the right parameters
6/ Deliveries is in the target directory

**************
* WHAT'S NEXT
**************
The exobuild tooling will be ported in a maven plugin with no more .js files, or replaced by a pure maven configuration.
This work will be done before GateIn final version
