
Welcome to GateIn
==================

This document explains how to build and package GateIn with Tomcat or JBoss.


Prerequisites
=============

- Java Development Kit 1.6
- Recent Subversion client
- Recent Maven 3 (might work with Maven 2.2.1 as well)


Build instructions
==================


1) Check out GateIn Portal
--------------------------

svn co http://anonsvn.jboss.org/repos/gatein/portal/trunk gatein-portal
cd gatein-portal



2) Prepare containers to use for packaging
------------------------------------------

Create a directory on your disk that will contain specific released versions of JBoss AS, Tomcat, Jetty, or some other container, used as a template for GateIn packaging.

Let’s refer to this directory as CONTAINERS_DIR.


3) Build and package gatein-portal
----------------------------------

GateIn can be packaged with different web / application servers. The specific container to use is selected by using an appropriate profile.


  Packaging with JBoss-AS-5.1.0.GA
  --------------------------------

If you don’t have an existing JBoss AS distribution, tell the build to automatically download it for you.
Issue the following command:

mvn clean install -DskipTests -Ppkg-jbossas5,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR


If you have an existing JBoss-AS-5.1.0.GA distribution from jboss.org, unpack it into CONTAINERS_DIR directory so that you get CONTAINERS_DIR/jboss-5.1.0.GA directory.
Issue the following command:

mvn clean install -DskipTests -Ppkg-jbossas5 -Dexo.projects.directory.dependencies=$CONTAINERS_DIR

The packaged GateIn is available in packaging/jboss-as5/pkg/target/jboss.

To start, go to jboss directory, and run 'bin/run.sh' ('bin\run.bat' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with JBoss-AS-6.0.0.Final
  -----------------------------------

If you don’t have an existing JBoss-AS distribution, tell the build to automatically download it for you.
Issue the following command:

mvn clean install -DskipTests -Ppkg-jbossas6,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR


If you have an existing JBoss-AS-6.0.0.Final distribution from jboss.org, unpack it into CONTAINERS_DIR directory so that you get CONTAINERS_DIR/jboss-6.0.0.Final directory.
Issue the following command:

mvn clean install -DskipTests -Ppkg-jbossas6 -Dexo.projects.directory.dependencies=$CONTAINERS_DIR

The packaged GateIn is available in packaging/jboss-as6/pkg/target/jboss.

To start, go to jboss directory, and run 'bin/run.sh' ('bin\run.bat' on Windows)

Access the portal at: http://localhost:8080/portal


  Packaging with Tomcat 6.x.x
  ---------------------------

If you don’t have an existing Tomcat 6.x.x distribution from tomcat.apache.org, tell the build to automatically download it for you.
Issue the following command:

mvn clean install -DskipTests -Ppkg-tomcat,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR


If you have an existing Tomcat 6.x.x distribution from tomcat.apache.org, unpack it into CONTAINERS_DIR directory so that you get CONTAINERS_DIR/apache-tomcat-6.x.x directory.
Issue the following command:

mvn clean install -DskipTests -Ppkg-tomcat -Dexo.projects.directory.dependencies=$CONTAINERS_DIR -Dexo.projects.app.tomcat.version=apache-tomcat-6.x.x

(where apache-tomcat-6.x.x refers to a directory under $CONTAINERS_DIR directory - adjust appropriately to match your version)

The packaged GateIn is available in packaging/tomcat/pkg/tc6/target/tomcat6.

To start, go to tomcat6 directory, and run 'bin/gatein.sh start' ('bin\gatein.bat start' on Windows).
Alternatively you can use 'bin/gatein.sh run' ('bin\gatein.bat run' on Windows).

Access the portal at: http://localhost:8080/portal


  Packaging with Tomcat 7.x.x
  ---------------------------

If you don’t have an existing Tomcat 7.x.x distribution from tomcat.apache.org, tell the build to automatically download it for you.
Issue the following command:

mvn clean install -DskipTests -Ppkg-tomcat7,download -Dexo.projects.directory.dependencies=$CONTAINERS_DIR

If you have an existing Tomcat 7.x.x distribution from tomcat.apache.org, unpack it into CONTAINERS_DIR directory so that you get CONTAINERS_DIR/apache-tomcat-7.x.x directory.
Issue the following command:

mvn clean install -DskipTests -Ppkg-tomcat7 -Dexo.projects.directory.dependencies=$CONTAINERS_DIR -Dexo.projects.app.tomcat7.version=apache-tomcat-7.x.x

(where apache-tomcat-7.x.x refers to a directory under $CONTAINERS_DIR directory - adjust appropriately to match your version)

The packaged GateIn is available in packaging/tomcat/pkg/tc7/target/tomcat7.

To start, go to tomcat7 directory, and run 'bin/gatein.sh start' ('bin\gatein.bat start' on Windows).
Alternatively you can use 'bin/gatein.sh run' ('bin\gatein.bat run' on Windows).

Access the portal at: http://localhost:8080/portal


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


Stuck?
------

Check user forums: http://community.jboss.org/en/gatein?view=discussions


Have some ideas, suggestions, want to contribute?
-------------------------------------------------

Join the discussions on forums at www.gatein.org or at #gatein-contrib IRC channel on freenode.net


