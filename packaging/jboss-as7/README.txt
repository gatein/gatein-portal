
JBoss AS7 Support
=================


This module provides JBoss AS7 support for GateIn.

There is a new packaging module called 'jboss-as7'. It contains three sub-modules: 

- extension
-----------

Provides JBoss AS7 'gatein' subsystem implementation in the form of 'extension'. Extensions are the mechanism to extend JBoss AS7 functionality. GateIn extension plugs interceptors into JBoss AS7 deployment, management, services engine to provide proper startup order of GateIn deployment archives, and proper class visibility.


- modules
---------

Provides GateIn libraries as JBoss AS7 modules - a format for modular packaging of classes and resources in order to provide fine grained classloader isolation.


- pkg
------

Provides packaging for the build, with automated JBoss AS7 download support.




Known Issues
============

- Only one JBoss AS 7 version is supported at one time. At the moment it's JBoss AS 7.1.0.Final.
- WSRP is not yet supported
- <distributable/> is not yet supported
- Sample ears have been repackaged as their current default packaging is not supported
- Exception occurs, and is ignored when logging out (EXOJCR-1619)



Building
========

Currently some patched up gatein dependencies are required to build JBoss AS7 support for GateIn. As soon as the patches are included and patched versions released this step will not be required any more. Until then, we have to build the patched versions ourselves.

Checkout the dependencies sources from sandbox:

svn co http://anonsvn.jboss.org/repos/gatein/sandbox/as7_support/tags/AS7-Beta03 gatein-sandbox-AS7-Beta03
cd gatein-sandbox-AS7-Beta03

This will checkout specific versions of wci and exo.kernel.container, and a new gatein-naming component.

Build them:

cd wci
mvn clean install -Dmaven.test.skip

cd ../exo.kernel.container
mvn clean install -Dmaven.test.skip

cd ../gatein-naming
mvn clean install -Dmaven.test.skip


Now that we have built the new dependencies let’s checkout GateIn Portal trunk:
cd ../..

svn co http://anonsvn.jboss.org/repos/gatein/portal/trunk gatein-portal
cd gatein-portal

If you have built gatein portal before, you can skip the tests by adding '-Dmaven.test.skip' to the next command:

mvn clean install


Set CONTAINERS_DIR env variable to point to a directory containing your application servers (i.e. export CONTAINERS_DIR=$HOME/devel/containers).
If you already have ‘jboss-as-7.1.0.Final’ in your CONTAINERS_DIR, then remove ‘,download’ from the next command:

cd packaging/jboss-as7
mvn clean install -Ppkg-jbossas7,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR



Now that we successfully built GateIn including JBoss AS7 support, let’s run it:

cd pkg/target/jboss-as-7.1.0.Final/bin
./standalone.sh


Access default portal at: http://localhost:8080/portal

Access sample portal at: http://localhost:8080/sample-portal



Customizing
===========

Packaging deploys a basic GateIn portal (gatein.ear), but also a sample portal extension, a sample portal site bound to a separate context, and a sample skin.

Samples archives can be removed, and custom portal extension archives can be added. Currently GateIn subsystem has to know about extensions in advance.
This is configured via JBOSS_HOME/standalone/configuration/standalone.xml which contains the following default configuration:

       <subsystem xmlns="urn:jboss:domain:gatein:1.0">
            <deployment-archives>
                <archive name="gatein.ear" main="true"/>
                <archive name="gatein-sample-extension.ear"/>
                <archive name="gatein-sample-portal.ear"/>
                <archive name="gatein-sample-skin.war"/>
            </deployment-archives>
            <portlet-war-dependencies>
                <dependency name="org.gatein.wci"/>
                <dependency name="org.gatein.pc"/>
                <dependency name="javax.portlet.api"/>
            </portlet-war-dependencies>
        </subsystem>

The <deployment-archives> section contains a list of GateIn portal extensions. These are archives constructed specifically to 'plug into' GateIn portal. To that end they contain a special configuration.

The archives in the list have to be deployed in JBOSS_HOME/standalone/deployments, otherwise GateIn portal won't start. So, if you want to remove some or all of the samples, it's not enough to just remove the archives from 'deployments' directory. Archive references also have to be removed from <deployment-archives> section of standalone.xml.

Similarly, when deploying any additional GateIn portal extension archive it has to be referenced via <archive> entry in <deployment-archives> section in standalone.xml.

On the other hand, user applications that only provide portlets, can simply be deployed as any web archive by dropping them into 'deployments' directory WITHOUT having to reference them in 'gatein' subsystem section of standalone.xml.


Portlet-war-dependency section allows customizing which modules are automatically made available to portlet archives (.war arhives containing portlet.xml), so that they can be deployed without any explicit JBoss modules configuration (i.e. Dependencies attribute in MANIFEST.MF, or jboss-deployment-structure.xml)



Help
====

File JIRA issues in JBoss JIRA: https://issues.jboss.org/browse/GTNPORTAL

Ask questions, and provide suggestions and feedback on forums: http://community.jboss.org/en/gatein?view=discussions




Have a nice day :)


