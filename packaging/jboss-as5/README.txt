
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

There are three profiles that cover dependency download (-Pdownload), packaging as exploded (default), and bundling as a zip (-Pbundle).

For main build use either 'download' profile or no profile (default). Both of these automatically perform 'clean' to ensure consistent results.

There are two system properties that control where the root directory for JBoss AS servers is located, and what specific JBoss AS directory to use.
They are preset to default location:

servers.dir=${project.basedir}/../../servers
jbossas.name=jboss-5.1.0.GA

Together they resolve into $GATEIN/portal/trunk/packaging/servers/jboss-5.1.0.GA as the location where JBoss AS is found.

The default goal is set to 'package' so there is no need to specify it.





== Some examples ==


1) The simplest form uses default location for JBoss AS

mvn

This is equivalent to:

mvn clean package -Dservers.dir=$GATEIN/portal/trunk/packaging/servers -Djbossas.name=jboss-5.1.0.GA


2) If JBoss AS is located somewhere else adjust servers.dir and jbossas.name accordingly

mvn -Dservers.dir=SERVERS_DIR -Djbossas.name=JBOSSAS_NAME


3) If JBoss AS is not yet present on the system it can be automatically downloaded and extracted into SERVERS_DIR

mvn -Pdownload

If SERVERS_DIR is not at default location, explicitly set it:

mvn -Pdownload -Dservers.dir=SERVERS_DIR

There's no need to set 'jbossas.name' property when using -Pdownload (if set, its value should be 'jboss-5.1.0.GA').


4) Zip bundle may be produced by activating 'bundle' profile

mvn -Pdownload,bundle

or

mvn -Pbundle





== Compatibility with previous packaging (PKG) ==


The previous packaging uses two different properties to specify 'servers.dir' and 'jbossas.name':

 - exo.projects.directory.dependencies  (equivalent to 'servers.dir')
 - exo.projects.app.jboss5.version  (equivalent to 'jbossas.name')


Compatibility mode can be activated by using -Ppkg-jbossas5.





== Build integration ==


Packaging can be run separately from portal build as described before:

# build portal first
cd $GATEIN/portal/trunk
mvn clean install -DskipTests

# then package
cd packaging/jboss-as5
mvn


Or it can be run together with the portal build, by using -Ppkg-jbossas5 compatibility mode:

# build and package all at once
cd $GATEIN/portal/trunk
mvn clean install -Ppkg-jbossas5,download -DskipTests -Dexo.projects.directory.dependencies=$GATEIN/portal/trunk/packaging/servers

(Notice how we also activate 'download' profile in this example - that's ideal for a new user who's building GateIn for the first time)





== Troubleshoot ==


1) 'Checksum validation failed!'

When using -Pdownload it may happen that the download is interrupted, and the resulting file corrupt.
The solution is to manually delete the target file from the filesystem, and run 'mvn -Pdownload' again.

2) 'DIRECTORY does not exist.' when using -Pdownload.

When using -Pdownload, and specifying -Djbossas.name=NAME at the same time (something you're adviced not to practice),
the download part of the build will completely ignore 'jbossas.name' property, but the packaging part will use it, and look for
JBoss AS instance in the specified directory.
The solution is to not specify -Djbossas.name=NAME at all, or to set its value to 'jboss-5.1.0.GA'.

3) 'Destination JBossAS directory exists already: DIRECTORY'

When using -Pdownload the downloaded JBoss AS distribution is unpacked into a local directory. If the directory exists already
the build will abort, to avoid corrupting a possibly carefully prepared specially configured JBoss AS instance.
One solution is to not use -Pdownload in this case since the appropriate JBoss AS is already present. Or, you can move the
directory out of the way, or delete it.

