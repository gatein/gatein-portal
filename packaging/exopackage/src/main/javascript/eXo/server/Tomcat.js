eXo.require("eXo.core.TaskDescriptor");
eXo.require("eXo.projects.Project");

function Tomcat(tomcatHome) {
  this.runningInstance_ = null;
  this.name = "tomcat";
  this.serverHome = tomcatHome;
  this.cleanServer = eXo.env.cleanServer; 
  this.deployLibDir = this.serverHome + "/lib";
  this.deployWebappDir = this.serverHome + "/webapps";
  this.patchDir = this.serverHome;
  this.pluginVersion = "trunk";
}

Tomcat.prototype.RunTask = function() {
  var descriptor = new TaskDescriptor("Run Tomcat", this.serverHome + "/bin");
  descriptor.execute = function() {
    java.lang.System.setProperty("user.dir", descriptor.workingDir);
    java.lang.System.setProperty("catalina.base", eXo.server.Tomcat.serverHome);
    java.lang.System.setProperty("catalina.home", eXo.server.Tomcat.serverHome);
    java.lang.System.setProperty("java.io.tmpdir", eXo.server.Tomcat.serverHome + "/temp");
    java.lang.System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
    java.lang.System.setProperty("java.security.auth.login.config", eXo.server.Tomcat.serverHome + "/conf/jaas.conf");
    var sysClasspath = [ new java.net.URL("file:" + eXo.env.javaHome + "/lib/tools.jar"),
        new java.net.URL("file:" + this.serverHome + "/bin/commons-logging-api.jar") ];
    eXo.System.addSystemClasspath(sysClasspath);
    var tomcatClasspath = [ new java.net.URL("file:" + eXo.server.Tomcat.serverHome + "/bin/bootstrap.jar") ];
    var contextLoader = java.lang.Thread.currentThread().getContextClassLoader();
    var tomcatLoader = new java.net.URLClassLoader(tomcatClasspath, contextLoader);
    java.lang.Thread.currentThread().setContextClassLoader(tomcatLoader);

    var bootstrap = tomcatLoader.loadClass("org.apache.catalina.startup.Bootstrap");
    instance = bootstrap.newInstance();
    java.lang.System.gc();
    instance.init();
    instance.start();

    java.lang.Thread.currentThread().setContextClassLoader(contextLoader);
    eXo.server.Tomcat.runningInstance_ = instance;
  }
  return descriptor;
}

Tomcat.prototype.StopTask = function() {
  var descriptor = new TaskDescriptor("Stop Tomcat", this.serverHome + "/bin");
  descriptor.execute = function() {
    if (eXo.server.Tomcat.runningInstance_ != null) {
      eXo.server.Tomcat.runningInstance_.stop();
      eXo.server.Tomcat.runningInstance_ = null;
    }
  }
  return descriptor;
}

Tomcat.prototype.CleanTask = function() {
  var descriptor = new TaskDescriptor("Clean Tomcat", this.serverHome + "/bin");
  descriptor.execute = function() {
    eXo.core.IOUtil.emptyFolder(serverHome + "/logs");
    eXo.core.IOUtil.emptyFolder(serverHome + "/temp");
  }
  return descriptor;
}

Tomcat.prototype.preDeploy = function(product) {

  var version = product.version;
  if (version.indexOf("2.0") != 0 && version.indexOf("2.1") != 0 && version.indexOf("2.2") != 0
      && version.indexOf("2.5") != 0) {
    product.addDependencies(new Project("org.slf4j", "slf4j-api", "jar", "1.5.6"));
    product.addDependencies(new Project("org.slf4j", "slf4j-jdk14", "jar", "1.5.6"));
  }
  product.addDependencies(new Project("commons-logging", "commons-logging", "jar", "1.0.4"));
  product.addDependencies(new Project("commons-pool", "commons-pool", "jar", "1.2"));
  product.addDependencies(new Project("commons-dbcp", "commons-dbcp", "jar", "1.2.1"));
  product.addDependencies(new Project("org.exoplatform.portal", "exo.portal.server.tomcat.plugin", "jar", product.serverPluginVersion));
  product.addDependencies(new Project("org.exoplatform.tool", "exo.tool.webunit", "jar", "1.0.0"));
  
  //GTNPORTAL-32 No WSRP on tomcat yet
  product.removeDependency(new Project("org.exoplatform.portal", "exo.portal.component.wsrp", "jar", product.serverPluginVersion ));
  product.removeDependencyByGroupId("org.gatein.wsrp");
  
}

Tomcat.prototype.onDeploy = function(project) {
  // if("exo-portal" == project.type) {
  // var context =
  // project.artifactId.substring(project.artifactId.lastIndexOf(".") + 1) ;
  // var dirname = this.serverHome + "/conf/Catalina/localhost/";
  // var destDir = new java.io.File(dirname);
  // if(!destDir.exists()) destDir.mkdirs() ;
  // var filename = dirname + context + ".xml";
  // eXo.System.info("TOMCAT", "Generating tomcat context" + filename);
  // var config =
  // "<Context path='/" + context+ "' docBase='" + context + "' debug='0'
  // reloadable='true' crossContext='true'> \n" +
  // //className can be org.apache.catalina.logger.FileLogger
  // " <Logger className='org.apache.catalina.logger.SystemOutLogger' \n" +
  // " prefix='localhost_" + context + "_log.' suffix='.txt'
  // timestamp='true'/> \n" +
  // " <Manager className='org.apache.catalina.session.PersistentManager'
  // saveOnRestart='false'/> \n" +
  // " <Realm className='org.apache.catalina.realm.JAASRealm' \n" +
  // " appName='exo-domain' \n" +
  // " userClassNames='org.exoplatform.services.security.jaas.UserPrincipal'
  // \n" +
  // " roleClassNames='org.exoplatform.services.security.jaas.RolePrincipal'
  // \n" +
  // " debug='0' cache='false'/> \n" +
  // " <Valve className='org.apache.catalina.authenticator.FormAuthenticator'
  // characterEncoding='UTF-8'/>" +
  // "</Context> \n";
  // eXo.core.IOUtil.createFile(filename, config) ;
  // }
}

Tomcat.prototype.postDeploy = function(product) {
  var configFileInWar = "WEB-INF/conf/configuration.xml";
  var portalwar = new java.io.File(this.deployWebappDir + "/" + product.portalwar);
  eXo.System.info("CONF", "Patching " + configFileInWar + " in " + portalwar + " : remove wsrp configuration");
  var mentries = new java.util.HashMap() ;
  var configTmpl = eXo.core.IOUtil.getJarEntryAsText(portalwar, configFileInWar);
  configTmpl = configTmpl.replaceAll("<import>war:/conf/common/wsrp-configuration.xml</import>", "");
  mentries.put(configFileInWar, configTmpl.getBytes()) ;  
  eXo.core.IOUtil.modifyJar(portalwar, mentries, null);
  //

  eXo.core.IOUtil.chmodExecutableInDir(this.serverHome + "/bin/", ".sh");
}

eXo.server.Tomcat = Tomcat.prototype.constructor;
