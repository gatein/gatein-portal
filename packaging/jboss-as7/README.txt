
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

- JBoss AS 7 versions 7.1.0.Final, 7.1.1.Final, and 7.1.2.Final are supported at the moment
- Special release of JCR Kernel is used (2.3.6-GA-JBAS7) that supports classloader isolated environment
- WSRP is not yet supported
- <distributable/> is not yet supported
- Sample ears have been repackaged as their current default packaging is not supported
- Exception occurs, and is ignored when logging out (EXOJCR-1619)



Building
========

By default JBoss AS 7.1.0.Final is the version used. You can specify a different version by using -Dversion.jboss.as system property.

For general build instructions see ../../README.txt


If you want to use JBoss AS 7.1.1.Final instead of default version, issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas7 -Ddownload -Dversion.jboss.as=7.1.1.Final

This will look for jboss-as-7.1.1.Final directory inside your $SERVERS_DIR, and download the JBoss AS distribution if necessary.


The packaged GateIn is available in packaging/jboss-as7/pkg/target/jboss-as-7.x.x.x.

To start it, go to jboss directory, and run 'bin/standalone.sh' ('bin\standalone.bat' on Windows).

Access the portal at: http://localhost:8080/portal


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


