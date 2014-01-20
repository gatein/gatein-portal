# "Disable_User" database migration script

The "disable user" feature implement in GTNPORTAL-3227. The GateIn's picketlink organization service implementation need legacy database to be migrated (Look at Jira issue for details)
To migrate data, just run this java class:

__org.exoplatform.services.organization.idm.DisabledUserMigrationScript__ (located in exo.portal.component.identity-xxx.jar)

# Quickstart 
Default configuration works for default GateIn configuration (hsqldb organization database, default picketlink realm). The default IDM, hibernate configuratioin files can be found at GATEIN/component/identity/src/main/resources/

__IDE__
1. Config hibernate connection url in configuration.properties
2. Right click on DisableUserMigrationScript.java --> choose Run As --> Java Application

__Command line__
__java -cp lib/*:target/exo.portal.component.identity-xxx.jar -Dhibernate.connection.url=[connection\_url] org.exoplatform.services.organization.idm.DisabledUserMigrationScript__

__Note__: 
By default, GateIn use HSQLDB, we need to stop GateIn server before running the migration script

# Custom migration script run

This is standalone java application, it can be run with-out portal container. User can run it by Eclipse: choose option "Run as Java Application" (need to modify hibernate.connection.url in configuration.properties) or use java command:

__java -cp lib/*:target/exo.portal.component.identity-xxx.jar [systemProperties] org.exoplatform.services.organization.idm.DisabledUserMigrationScript [config.properties]__

### Library: those libs should be in classpath

__lib/*__  : contains hiberate, picketlink and log4j library

	antlr-2.7.6rc1.jar 
	hsqldb-2.0.0.jar 
	picketlink-idm-common-1.4.3.Final.jar 
	dom4j-1.6.1.jar 
	javassist-3.14.0-GA.jar 
	picketlink-idm-core-1.4.3.Final.jar 
	exo.kernel.commons-2.5.0-Alpha1.jar 
	jboss-logging-3.1.2.GA.jar 
	picketlink-idm-hibernate-1.4.3.Final.jar 
	hibernate-commons-annotations-4.0.1.Final.jar 
	jta-1.1.jar 
	picketlink-idm-spi-1.4.3.Final.jar 
	hibernate-core-4.1.6.Final.jar 
	log4j-1.2.16.jar 
	slf4j-api-1.6.1.jar 
	hibernate-jpa-2.0-api-1.0.1.Final.jar 
	picketlink-idm-api-1.4.3.Final.jar 
	slf4j-simple-1.6.1.jar 

### Properties

[systemProperties] - customize the migration behaviour. All properties are optional, there is a default configuration.properties contains default configurations for the script:

__picketlink.config\_file\_path=picketlink-idm-config.xml__                   --> path to picketlink config file, this file must be in the classpath

__picketlink.realmName=idm\_realm\_portal__		                --> picketlink realm name

__hibernate.connection.driver\_class=org.hsqldb.jdbcDriver__

__hibernate.connection.url=jdbc:hsqldb:file:path/to/file__

__hibernate.connection.username=sa__

__hibernate.connection.password=password__

__hibernate.dialect=org.hibernate.dialect.HSQLDialect__

__hibernate.config\_path=hibernate.cfg.xml__		                --> DB connection infos are set by above configs, but if user want more hibernate config, just specify path to hibernate config file, this file must be in the classpath

__enable\_user\_from=0__						                --> script will start to migrate users from this index (user is arrange by name in ASCENDING order)

__batch=100__								        --> by default, it migrate for a batch 100 users in a transaction, if there is any problem, the transaction will be rolled-back for that 100 users, look at the console, there is a log to show it's fail from which index, then you can use "enable\_user\_from" optioni to run again from that index. This should be usefull for large database

If there is no [systemProperties]. Migration script will be configured by a property file, that is specify by [config.properties] argument (this properties file must be in the classpath)
The default configuration files are in the JAR file (configuration.properties, hibernate.cfg.xml, picketlink-idm-config.xml). They will be use in case there is no system properties or argument.

# Example

Let say we have custom properties file: __conf/config.properties__. This file contains those configs

__picketlink.config\_file\_path=idm-config.xml__    --> custom config for PicketlinkIDM

__picketlink.realmName=idm\_realm\_portal__

__hibernate.config\_path=hi.cfg.xml__		      --> custom config for Hibernate, this file should contains DB connection infos

__hibernate.dialect=org.hibernate.dialect.HSQLDialect__

__batch=1000__

All the config file should be in the classpath, for example, if they are in the directory "conf", run this command:

__java -cp conf:lib/*:target/exo.portal.component.identity-xxx.jar -Denable\_user\_from=100 org.exoplatform.services.organization.idm.DisabledUserMigrationScript config.properties__

--> custom config files will be loaded, and it will start to migrate users from index: 100
