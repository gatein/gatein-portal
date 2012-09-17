Welcome to GateIn
==================

This document explains how to build and package GateIn with Tomcat, JBoss, or Jetty.


Prerequisites
=============

- Java Development Kit 1.6
- Recent Git client
- Recent Maven 3 (might work with Maven 2.2.1 as well)


Build configuration
===================

1) Profile configuration

GateIn build uses a system property called 'gatein.dev' to configure the target server to use for packaging.

When gatein.dev property is not set it will build *everything* in the project: development modules, the documentation,
the server packages, the examples, ... and package all the servers.

When the gatein.dev property is set it will build and package one or several servers thereby reducing to the minimum the build time.

The various values for gatein.dev are:

- tomcat     : Tomcat 6 and Tomcat 7
- tomcat6    : Tomcat 6
- tomcat7    : Tomcat 7
- jbossas    : JBoss AS 5 and JBoss AS 6
- jbossas5   : JBoss AS 5
- jbosass6   : JBoss AS 6
- jbossas711 : JBoss AS 7.1.1
- jbossas712 : JBoss AS 7.1.2
- jetty      : Jetty


2) Database configuration

By default the build uses a HSQLDB database. However, it is possible to use MySQL5 by using the 'mysql5' profile.


Build instructions
==================

1) Clone GateIn Portal
--------------------------

git clone git://github.com/gatein/gatein-portal.git
cd gatein-portal



2) Prepare containers to use for packaging
------------------------------------------

Create a directory on your disk that will contain specific versions of JBoss AS, Tomcat, Jetty, or some other container, used as a packaging server.

Let’s refer to this directory as SERVERS_DIR.


3) Build and package gatein-portal
----------------------------------

You can build gatein-portal without packaging it by using the following command:

mvn clean install -Dgatein.dev -DskipTests


But that's only usable for development since in order to be able to run GateIn you have to package it.

GateIn can be packaged with different web / application servers. The specific server to use is selected by using an appropriate profile.

Profile is selected by using -Dgatein.dev= with one of the supported values as described later.

There is one parameter (-Dservers.dir=$SERVERS_DIR) that has to be specified every time in order to specify the location on the disk where you want to keep you application servers used for packaging.

For each server packaging there is a default server version that will be used.

You can override the name of the directory that contains your server (under $SERVERS_DIR) by using -Dserver.name=$NAME parameter.

There are server specific equivalents that have to be used when packaging several servers in one build run. Those are described later.



  Packaging with JBoss-AS-5.1.0.GA
  --------------------------------

If you don’t have an existing JBoss AS distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas5 -Ddownload


If you have an existing JBoss-AS-5.1.0.GA distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/jboss-5.1.0.GA directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas5


The packaged GateIn is available in packaging/jboss-as5/pkg/target/jboss.

To start it, go to jboss directory, and run 'bin/run.sh' ('bin\run.bat' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with JBoss-AS-7.x.x
  -----------------------------------

If you don’t have an existing JBoss AS distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=$AS7_LABEL -Ddownload

Where $AS7_LABEL is one of: jbossas711, or jbossas712.


If you have an existing JBoss-AS-7.x.x distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/jboss-as-7.x.x directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=$AS7_LABEL


For more finegrained control you can specify the directory under $SERVERS_DIR that contains the jboss-as-7.x.x that you wish to use for packaging.
You do that by using either -Dserver.name=$NAME or -Djboss7.name=$NAME.

IMPORTANT: Make sure that your $AS7_LABEL matches the JBoss AS version in your specified directory.


The packaged GateIn is available in packaging/jboss-as7/pkg/target/jboss-as-7.x.x.

To start it, go to jboss directory, and run 'bin/standalone.sh' ('bin\standalone.bat' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with JBoss-AS-6.0.0.Final
  -----------------------------------

If you don’t have an existing JBoss-AS distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas6 -Pdownload


If you have an existing JBoss-AS-6.0.0.Final distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/jboss-6.0.0.Final directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jbossas6


The packaged GateIn is available in packaging/jboss-as6/pkg/target/jboss.

To start, go to jboss directory, and run 'bin/run.sh' ('bin\run.bat' on Windows)

Access the portal at: http://localhost:8080/portal


  Packaging with Tomcat 7.x.x
  ---------------------------

If you don’t have an existing Tomcat 7.x.x distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=tomcat7 -Pdownload


If you have an existing Tomcat 7.x.x distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/apache-tomcat-7.x.x directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=tomcat7 -Dserver.name=apache-tomcat-7.x.x

(fix tomcat version in 'server.name')


The packaged GateIn is available in packaging/tomcat/pkg/tc7/target/tomcat7.

To start, go to tomcat7 directory, and run 'bin/gatein.sh run' ('bin\gatein.bat run' on Windows).
Alternatively you can use 'bin/gatein.sh start' ('bin\gatein.bat start' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with Tomcat 6.x.x
  ---------------------------

If you don’t have an existing Tomcat 6.x.x distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=tomcat6 -Pdownload


If you have an existing Tomcat 6.x.x distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/apache-tomcat-6.x.x directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=tomcat6 -Dserver.name=apache-tomcat-6.x.x

(fix tomcat version in 'server.name')


The packaged GateIn is available in packaging/tomcat/pkg/tc6/target/tomcat6.

To start, go to tomcat6 directory, and run 'bin/gatein.sh run' ('bin\gatein.bat run' on Windows).
Alternatively you can use 'bin/gatein.sh start' ('bin\gatein.bat start' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with Jetty 6.x.x
  ---------------------------

If you don’t have an existing Jetty 6.x.x distribution, the build can automatically download it for you.

Issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jetty -Pdownload


If you have an existing Jetty 6.x.x distribution, unpack it into SERVERS_DIR directory so that you get SERVERS_DIR/jetty-6.x.x directory.

In this case you can issue the following command:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Dgatein.dev=jetty -Dserver.name=jetty-6.x.x

(fix jetty version in 'server.name')


The packaged GateIn is available in packaging/jetty/pkg/target/jetty.

To start, go to jetty directory, and run 'bin/gatein.sh start' ('bin\gatein.bat start' on Windows).
Alternatively you can use 'bin/gatein.sh run' ('bin\gatein.bat run' on Windows).

Access the portal at: http://localhost:8080/portal



Packaging with all containers in one go
=======================================

The simplest way to package with all the supported containers is to let the build download all the default app server versions:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Ddownload


You can also specify server names for each container - for example:

mvn install -DskipTests -Dservers.dir=$SERVERS_DIR -Djboss5.name=jboss-5.1.0 -Djboss7.name=jboss-7.1.0 -Djboss6.name=jboss-6.0.0 -Dtomcat6.name=tomcat-6.0.35 -Dtomcat7.name=tomcat-7.0.19 -Djetty.name=jetty-6.0.24


For JBoss AS7 packaging the version used in this case would be 7.1.0.Final. If you want to use a different version you have to specify it with -Dversion.jboss.as=.
Valid values are: 7.1.0.Final, 7.1.1.Final, and 7.1.2.Final


Release instructions
====================


You should execute this magic command line:

mvn release:prepare
mvn release:perform


Troubleshooting
===============


Maven dependencies issues
-------------------------

While GateIn should build without any extra maven repository configuration it may happen that the build complains about missing artifacts.

If you encounter this situation, please let us know via our forums (http://community.jboss.org/en/gatein?view=discussions).

As a quick workaround you may try setting up maven repositories as follows.

Create file settings.xml in $HOME/.m2  (%HOMEPATH%\.m2 on Windows) with the following content:

<settings>
  <profiles>
    <profile>
      <id>jboss-public-repository</id>
      <repositories>
        <repository>
          <id>jboss-public-repository-group</id>
          <name>JBoss Public Maven Repository Group</name>
          <url>https://repository.jboss.org/nexus/content/groups/public-jboss/</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>jboss-public-repository-group</id>
          <name>JBoss Public Maven Repository Group</name>
          <url>https://repository.jboss.org/nexus/content/groups/public-jboss/</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>

    <profile>
      <id>exo-public-repository</id>
      <repositories>
        <repository>
          <id>exo-public-repository-group</id>
          <name>eXo Public Maven Repository Group</name>
          <url>http://repository.exoplatform.org/content/groups/public</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>exo-public-repository-group</id>
          <name>eXo Public Maven Repository Group</name>
          <url>http://repository.exoplatform.org/content/groups/public</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
          </snapshots>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>

  <activeProfiles>
    <activeProfile>jboss-public-repository</activeProfile>
    <activeProfile>exo-public-repository</activeProfile>
  </activeProfiles>
</settings>


Normally you should not need to configure this to build GateIn.



OutOfMemoryException
--------------------

Try increasing maximum heap size used by Maven:

export MAVEN_OPTS=-Xmx256m

(on Windows try: set MAVEN_OPTS=-Xmx256m)



Stuck?
------

Check user forums: http://community.jboss.org/en/gatein?view=discussions


Have some ideas, suggestions, want to contribute?
-------------------------------------------------

Join the discussions on forums at www.gatein.org or at #gatein IRC channel on freenode.net


