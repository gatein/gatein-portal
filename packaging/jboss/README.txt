
JBoss AS Support
=================


This module provides support for GateIn running on JBoss AS-based servers.

There is a packaging module called 'jboss'. It contains three sub-modules: 

- extension
-----------

Provides a 'gatein' subsystem implementation in the form of 'extension' for JBoss AS-based servers. Extensions are a mechanism to extend JBoss AS functionality. GateIn extension plugs interceptors into JBoss AS deployment, management, services engine to provide proper startup order of GateIn deployment archives, and proper class visibility.


- modules
---------

Provides GateIn libraries as JBoss AS modules - a format for modular packaging of classes and resources in order to provide fine grained classloader isolation.


- pkg
------

Provides packaging for the build, with automated JBoss AS7 download support.



Known Issues
============

- JBoss EAP 6.3.0.Alpha1 is the only version supported at the moment. 



Building with JBoss EAP 6.3.0.Alpha1
========================

By default JBoss EAP 6.3.0.Alpha1 is the version used.

For general build instructions see ../../README.txt

The packaged GateIn is available in packaging/jboss/pkg/target/jboss-eap.

To start it, go to the JBoss AS directory, and run 'bin/standalone.sh' ('bin\standalone.bat' on Windows).

Access the portal at: http://localhost:8080/portal



Building with JBoss EAP 6.3.0.Alpha1
====================================

Open: http://www.jboss.org/jbossas/downloads/

Download 6.3.0.Alpha1 - EAP built from AS 7.4, and EAP Maven Repository.

Create a directory jboss-eap-6.3.0.Alpha1 under your $SERVERS_DIR:

mkdir $SERVERS_DIR/jboss-eap-6.3.0.Alpha1

Unzip the contents of jboss-eap-6.3.0.Alpha.zip into this new directory, so that you get a subdirectory called jboss-eap-6.3

Unzip the contents of jboss-eap-6.3.0.Alpha-maven-repository.zip into your $HOME/.m2/repository overwriting existing directories.

You’re set to build and package GateIn:

mvn clean
mvn install -pl build-config
cd component
mvn install -DskipTests
cd ..
mvn install -Dservers.dir=$SERVERS_DIR/jboss-eap-6.3.0.Alpha1 -Dgatein.dev=eap630 -Dmaven.test.skip

Once you have successfully built GateIn it’s enough to only use the last command in order to rebuild.

The packaged GateIn is available in packaging/jboss/pkg/target/jboss-eap.


Customizing
===========

Packaging deploys a basic GateIn portal (gatein.ear), but also a sample portal extension, a sample portal site bound to a separate context, and a sample skin.

GateIn deployment archives are located in JBOSS_HOME/gatein containing the following:

 - gatein.ear

   This is the application archive containing GateIn portal. The libraries are available as modules under JBOSS_HOME/modules, and are not part of the deployment archive.

 - extensions/

   This is a directory that contains all the portal extensions to be installed when GateIn starts.
   Any archives that perform integration with GateIn kernel using configuration.xml files, or take part in GateIn's extension mechanism in order to override the default portal behaviour should be placed in this directory.
   At boot time GateIn will scan the directory and deploy containing archives.
   This directory does not support hot re-deploy, and should not be used for ordinary portlet archives, that only provide portlets. Regular JBoss AS deployment mechanisms like JBOSS_HOME/standalone/deployments directory, CLI admin client, web admin console, or maven jboss-as:deploy are the appropriate mechanisms to use in that case.


Portlet archives are web applications that contain portlet.xml file as per Portlet Specification. GateIn automatically provides some libraries to these applications so that they can properly integrate with the portal.
The list of libraries provided to portlet archives by default can be configured via JBOSS_HOME/standalone/configuration/standalone.xml which contains the following default configuration:

       <subsystem xmlns="urn:jboss:domain:gatein:1.0">
            <portlet-war-dependencies>
                <dependency name="org.gatein.wci"/>
                <dependency name="org.gatein.pc"/>
                <dependency name="javax.portlet.api"/>
            </portlet-war-dependencies>
        </subsystem>

JBoss AS7 provides mechanisms for configuring which modules are visible to a specific deployment archive (i.e.: Dependencies attribute in MANIFEST.MF, or jboss-deployment-structure.xml).



Domain mode support
===================


GateIn comes with a ready made domain.xml / host.xml demo configuration for a single-host domain mode setup with two different GateIn instances. It’s a scenario that allows central configuration and management of two differently configured portal instances running side-by-side via JBoss AS management infrastructure.

For more documentation about domain mode, how to set it up, and how to manage it see:

https://docs.jboss.org/author/display/AS72/Admin+Guide#AdminGuide-Managementclients
https://docs.jboss.org/author/display/AS72/Core+management+concepts
https://docs.jboss.org/author/display/AS72/Domain+Setup


Domain mode demo configuration
------------------------------

Demo domain configuration establishes two instances that listen on different ports. The one on port 8080 provides WSRP integration, and Mobile support. The other one, listening on port 8330, provides Mobile support, but does not provide WSRP integration. This way we have two different portal instances.

Setting up the demo configuration is easy. Simply run the following shell script:

    $JBOSS_HOME/bin/demo-domain-setup.sh

which is in the same directory as standalone.sh script used to run GateIn in standalone mode.

Then run GateIn in domain mode instead of standalone mode:

    $JBOSS_HOME/bin/standalone.sh

This command will start up a host controller process that in our case also serves as a domain controller. This process contains a domain management console at http://localhost:9990/console. It uses domain.xml, and host.xml configuration files from $JBOSS_HOME/domain/configuration, and based on them it spawns two Gatein instances (two JVM processes).

Console can be used to deploy a portlet application (i.e. simplest-helloworld-portlet.war) to both GateIn instances.

The demo configuration shows how a gatein.extensions.dir system property can be used to assign different extensions directories to different servers in order to allow different portals to coexist on the same host.
The first server - 'server-one' - uses the default extensions directory location ($JBOSS_HOME/gatein/extensions). For the second one - 'server-two' - we specify an alternate extensions location in host.xml.

The demo configuration also makes two copies of gatein configuration directory ($JBOSS_HOME/standalone/configuration/gatein) - one for each server.

It's possible to specify GateIn configuration directory location for each server, or for a server group by setting several system properties - i.e.:

    <property name="exo.conf.dir" value="${jboss.home.dir}/standalone/configuration/gatein"/>
    <property name="gatein.conf.dir" value="${jboss.home.dir}/standalone/configuration/gatein"/>
    <property name="gatein.portlet.config" value="${jboss.home.dir}/standalone/configuration/gatein"/>

GateIn configuration directory is then shared among different server instances.


Limitations
--------------

Only standard portlet applications can be deployed via domain management. GateIn portal extensions have to be deployed by manual copying into gatein extensions directory in order to be deployed on server restart.



Clustered mode
==============

There is a simple 'cluster' build profile available for JBoss AS 7.1.3.Final packaging.

Here's how to build it, and run two nodes on the same host in order to play with it:


mvn clean install -Dgatein.dev=eap630 -Dservers.dir=$SERVERS_DIR -DskipTests -Dcluster

cd packaging/jboss/pkg/target/

cp -r jboss-eap ~/gatein-node1
cp -r jboss-eap ~/gatein-node2

Now open three consoles, and in all of them do 'cd $HOME'.

In first console run the database:

java -cp gatein-node1/modules/com/h2database/h2/main/h2-1.3.168.jar org.h2.tools.Server


In second console run the first GateIn node:

cd gatein-node1
bin/standalone.sh --server-config=standalone-ha.xml -Djboss.node.name=node1 -Djboss.socket.binding.port-offset=100


And in third console run the second GateIn node:

cd gatein-node2
bin/standalone.sh --server-config=standalone-ha.xml -Djboss.node.name=node2 -Djboss.socket.binding.port-offset=200


Point your browser to http://localhost:8180/portal to access the first instance, and http://localhost:8280/portal for the second instance.


If there are multiple interfaces in the system another parameter may have to be used: -Djgroups.bind_addr=IP_ADDRESS

This one tells jgroups - a library used for session replication on JBoss AS - to bind to specific interface. It may use a wrong one by default, preventing session replication from working.


Help
====

File JIRA issues in JBoss JIRA: https://issues.jboss.org/browse/GTNPORTAL

Ask questions, and provide suggestions and feedback on forums: http://community.jboss.org/en/gatein?view=discussions

Have a nice day :)
