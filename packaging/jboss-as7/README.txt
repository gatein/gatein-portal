
JBoss AS7 Support
=================


This module provides JBoss AS7 support for GateIn.

There is a new packaging module called 'jboss-as7'. It contains three sub-modules: 

- extension
-----------

Provides JBoss AS7 'gatein' subsystem implementation in the form of 'extension'. Extensions are a mechanism to extend JBoss AS7 functionality. GateIn extension plugs interceptors into JBoss AS7 deployment, management, services engine to provide proper startup order of GateIn deployment archives, and proper class visibility.


- modules
---------

Provides GateIn libraries as JBoss AS7 modules - a format for modular packaging of classes and resources in order to provide fine grained classloader isolation.


- pkg
------

Provides packaging for the build, with automated JBoss AS7 download support.



Known Issues
============

- JBoss AS 7 versions 7.1.1.Final, 7.1.3.Final, and JBoss EAP 6.1.0.Beta are supported at the moment.



Building with JBoss AS 7
========================

By default JBoss AS 7.1.1.Final is the version used.

For general build instructions see ../../README.txt


If you want to use JBoss AS 7.1.3.Final instead of default version, issue the following command:

mvn clean install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas713

This will look for jboss-as-7.1.3.Final directory inside your $SERVERS_DIR.


The packaged GateIn is available in packaging/jboss-as7/pkg/target/jboss-as-7.x.x.

To start it, go to jboss directory, and run 'bin/standalone.sh' ('bin\standalone.bat' on Windows).

Access the portal at: http://localhost:8080/portal



Building with JBoss EAP 6.1.0.Beta
==================================

Open: http://www.jboss.org/jbossas/downloads/

Download 6.1.0 Beta - EAP beta build from AS7, and EAP Maven Repository.


Create a directory jboss-eap-6.1.0.Beta under your $SERVERS_DIR:

mkdir $SERVERS_DIR/jboss-eap-6.1.0.Beta

Unzip the contents of jboss-eap-6.1.0.Beta.zip into this new directory, so that you get a subdirectory called jboss-eap-6.1

Unzip the contents of jboss-eap-6.1.0.Beta-maven-repository.zip into your $HOME/.m2/repository overwriting existing directories.

You’re set to build and package GateIn:

mvn clean
mvn install -pl build-config
cd component
mvn install -DskipTests
cd ..
mvn install -Dservers.dir=$SERVERS_DIR/jboss-eap-6.1.0.Beta -Dversion.jboss.as=7.2.0.Final-redhat-4 -Dgatein.dev=eap -Dmaven.test.skip


Once you have successfully built GateIn it’s enough to only use the last command in order to rebuild.



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
   This directory does not support hot re-deploy, and should not be used for ordinary portlet archives, that only provide portlets. Regular AS7 deployment mechanisms like JBOSS_HOME/standalone/deployments directory, CLI admin client, web admin console, or maven jboss-as:deploy are the appropriate mechanisms to use in that case.


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




Clustered mode
==============

There is a simple 'cluster' build profile available for JBoss AS 7.1.3.Final packaging.

Here's how to build it, and run two nodes on the same host in order to play with it:


mvn clean install -Dgatein.dev=jbossas713 -Dservers.dir=$SERVERS_DIR -DskipTests -Dcluster

cd packaging/jboss-as7/pkg/target/

cp -r jboss ~/gatein-node1
cp -r jboss ~/gatein-node2

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

This one tells jgroups - a library used for session replication on AS7 - to bind to specific interface. It may use a wrong one by default, preventing session replication from working.


Help
====

File JIRA issues in JBoss JIRA: https://issues.jboss.org/browse/GTNPORTAL

Ask questions, and provide suggestions and feedback on forums: http://community.jboss.org/en/gatein?view=discussions




Have a nice day :)
