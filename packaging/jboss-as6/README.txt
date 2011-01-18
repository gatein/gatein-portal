
This project packages GateIn with JBoss AS 6.x.x.

It is based on jboss-as5 packaging project with minor changes.

Differences are as follows:

 - JBoss AS download script is adapted to download JBossAS-6.0.0.Final (instead of JBossAS-5.1.0.GA)
 - shared libraries are placed into $JBOSS_HOME/server/default/lib instead of $JBOSS_HOME/server/default/deploy/gatein.ear/lib
 - hibernate-core-3.3.2.GA is not included as it already comes with JBossAS-6.0.0.Final
 - gatein-sample-portal.ear, gatein-sample-extension.ear, and gatein-sample-skin.war are not included (they will be once the deployment descriptors are fixed)
 - wsrp-* modules are commented out in gatein.ear/META-INF/application.xml
 - Resource file for patching JBoss AS are based on JBossAS-6.0.0.Final



== Usage ==


Packaging assumes that portal has already been fully built. Therefore, cd to $GATEIN/portal/trunk directory first, and run:

mvn clean install -DskipTests

Then go to packaging/jboss-as6 to perform packaging.

There are three profiles that cover dependency download (-Pdownload), packaging as exploded (-Ppackage), and bundling as a zip (-Pbundle).

For main build use either 'download' or 'package' profile, but not both, as 'download' also includes 'package' functionality.

Profile 'package' is the one active by default. When activating any other profile, the 'package' profile is automatically turned off, so
 it may have to be explicitly activated again if packaging is required (i.e. -Ppackage,bundle).

There are two system properties that control where the root directory for JBoss AS servers is located, and what specific JBoss AS directory to use.

servers.dir=../../pkg/servers
jbossas.name=jboss-6.0.0.Final

Together they resolve into ../../pkg/servers/jboss-6.0.0.Final as the location where JBoss AS is found.

PKG build uses two equivalent properties:

exo.projects.directory.dependencies
exo.projects.app.jboss.version

which can still be used instead of servers.dir and jbossas.name.




== Examples ==


1) The simplest form uses default location for JBoss AS

mvn clean package

This is equivalent to:

mvn clean package -Ppackage -Dservers.dir=../../pkg/servers -Djbossas.name=jboss-6.0.0.Final


2) If JBoss AS is located somewhere else adjust servers.dir and jbossas.name accordingly

mvn clean package -Dservers.dir=SERVERS_DIR -Djbossas.name=JBOSSAS_NAME


3) If JBoss AS is not yet present on the system it can be automatically downloaded and extracted into SERVERS_DIR

mvn clean package -Pdownload

If SERVERS_DIR is not at default location explicitly set it:

mvn clean package -Pdownload -Dservers.dir=SERVERS_DIR

You shouldn't set jbossas.name property when using -Pdownload. 


4) Zip bundle may be produced by activating 'bundle' profile

mvn clean package -Pdownload,bundle

or

mvn clean package -Ppackage,bundle

or just

mvn package -Pbundle
