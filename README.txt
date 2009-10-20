====
    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.
====

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

* edit ${basedir}/profiles.xml and replace the values mentionned so that:
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
** Create a Tomcat delivery in pkg/target/tomcat/ and archives are stored in target

* mvn install -Ppkg-jbossas
** Create a JBossAS delivery in pkg/target/jboss/ and archives are stored in target

*****************
* STARTING:
*****************
* On Tomcat: go to the tomcat directory (or unzip the archive in your favorite location) and execute 'bin/gatein.sh start' ('bin/gatein.bat start' on Windows)
* On JBoss: go to the jboss directory (or unzip the archive in your favorite location) and execute 'bin/run.sh start' ('bin/grun.bat start' on Windows)

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
