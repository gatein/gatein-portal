
This packaging project is a replacement for $GATEIN/portal/trunk/packaging/pkg (PKG) used with '-Ppkg-jbossas'.

The implementation generates output that is equivalent to PKG, but diverges in a few minor details that should have no real effect.

Differences are as follows:

 - MANIFEST.MF files inside *.war/META-INF don't have a Class-Path attribute
 - integration.war is stripped of reduntant source files, and pom.xml
 - concurrent-1.3.4.jar is same version but not exactly the same binary
 - stax-api-1.0.jar is same version but not exactly the same binary
 - exo.kernel.mc-* version is synchronized with the rest of exo.kernel.* artifacts
 - jcip-annotations.jar is not an exact copy of the one shipped with JBossAS, but a maven repository artifact of the same version
 - jcip-annotations-1.0.jar is removed from gatein.ear/lib as it already exists as $JBOSS_HOME/server/default/lib/jcip-annotations.jar




== Usage ==


Packaging assumes that portal has already been fully built. Therefore, cd to $GATEIN/portal/trunk directory first, and run:

mvn clean install -DskipTests

Then go to packaging/jboss-as5 to perform packaging.

There are three profiles that cover dependency download (-Pdownload), packaging as exploded (-Ppackage), and bundling as a zip (-Pbundle).

For main build use either 'download' or 'package' profile, but not both, as 'download' also includes 'package' functionality.

Profile 'package' is the one active by default. When activating any other profile, the 'package' profile is automatically turned off, so
 it may have to be explicitly activated again if packaging is required (i.e. -Ppackage,bundle).

There are two system properties that control where the root directory for JBoss AS servers is located, and what specific JBoss AS directory to use.

servers.dir=../../pkg/servers
jbossas.name=jboss-5.1.0.GA

Together they resolve into ../../pkg/servers/jboss-5.1.0.GA as the location where JBoss AS is found.

PKG build uses two equivalent properties:

exo.projects.directory.dependencies
exo.projects.app.jboss.version

which can still be used instead of servers.dir and jbossas.name.




== Examples ==


1) The simplest form uses default location for JBoss AS

mvn clean package

This is equivalent to:

mvn clean package -Ppackage -Dservers.dir=../../pkg/servers -Djbossas.name=jboss-5.1.0.GA


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
