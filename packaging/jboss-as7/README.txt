
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

- JBoss AS 7.0.2.Final and higher are supported. It does not work with JBoss AS 7.0.0.Final.
- WSRP is not yet supported
- <distributable/> is not yet supported
- Sample ears have been repackaged as their current default packaging is not supported




Building
========

Currently some patched up gatein dependencies are required to build JBoss AS7 support for GateIn. As soon as the patches are included and patched versions released this step will not be required any more. Until then, we have to build the patched versions ourselves.

Checkout the dependencies sources from sandbox:

svn co http://anonsvn.jboss.org/repos/gatein/sandbox/as7_support/tags/AS7-Beta01
cd AS7-Beta01

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


Set CONTAINERS_DIR env variable to point to a directory containing your application servers (i.e. export CONTAINERS_DIR=$HOME/devel/containers). If you already have ‘jboss-as-7.0.2.Final’ in your CONTAINERS_DIR, then remove ‘,download’ from the next command:

cd packaging/jboss-as7
mvn clean install -Ppkg-jbossas7,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR



Now that we successfully built GateIn including JBoss AS7 support, let’s run it:

cd pkg/target/jboss-as-7.0.2.Final/bin
./standalone.sh




Help
====

File JIRA issues in JBoss JIRA: https://issues.jboss.org/browse/GTNPORTAL

Ask questions, and provide suggestions and feedback on forums: http://community.jboss.org/en/gatein?view=discussions




Have a nice day :)


