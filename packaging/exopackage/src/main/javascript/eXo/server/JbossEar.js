eXo.require("eXo.core.TaskDescriptor");
eXo.require("eXo.core.IOUtil");
eXo.require("eXo.server.ServerUtil");
eXo.require("eXo.projects.Project");

function JbossEar(jbossHome) {
  this.exoJBoss5 = true;
  this.runningInstance_ = null;
  this.name = "jbossear";
  this.serverHome = jbossHome;
  this.cleanServer = java.lang.System.getProperty("clean.server");
  if (this.cleanServer == null || this.cleanServer.equals("") || !this.cleanServer.startsWith("jboss"))
    this.cleanServer = "jboss-5.1.0.GA";
  this.deployLibDir = this.serverHome + "/server/default/deploy/gatein.ear";
  this.deployWebappDir = this.serverHome + "/server/default/deploy/gatein.ear";
  this.deployEarDir = this.serverHome + "/server/default/deploy/";
  this.patchDir = this.serverHome;// + "/server/default"; //because we have to
  // patch bin/ directory
}

JbossEar.prototype.RunTask = function() {
  var descriptor = new TaskDescriptor("Run JbossEar", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {
    var javaHome = eXo.env.javaHome;
    java.lang.System.setProperty("user.dir", descriptor.workingDir);
    java.lang.System.setProperty("program.name", "run.sh");
    java.lang.System.setProperty("java.io.tmpdir", this.server.serverHome + "/temp");
    var sysClasspath = [ new java.net.URL("file:" + this.server.javaHome + "/lib/tools.jar"),
        new java.net.URL("file:" + this.server.serverHome + "/bin/run.jar") ];
    eXo.System.addSystemClasspath(sysClasspath);

    var contextLoader = java.lang.Thread.currentThread().getContextClassLoader();
    var jbossLoader = new java.net.URLClassLoader(new URL[0], contextLoader);
    java.lang.Thread.currentThread().setContextClassLoader(jbossLoader);
    var args = new java.lang.String[0];
    jboss = new org.jboss.Main();
    jboss.boot(args);
    runningInstance_ = jboss;
    java.lang.Thread.currentThread().setContextClassLoader(contextLoader);
  }
  return descriptor;
};

JbossEar.prototype.StopTask = function() {
  var descriptor = new TaskDescriptor("Stop JbossEar", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {
    var sysClasspath = [ new java.net.URL("file:" + this.server.serverHome + "/bin/shutdown.jar"),
        new java.net.URL("file:" + this.server.serverHome + "/client/jbossall-client.jar") ];
    var contextLoader = java.lang.Thread.currentThread().getContextClassLoader();
    var jbossLoader = new java.net.URLClassLoader(sysClasspath, contextLoader);
    java.lang.Thread.currentThread().setContextClassLoader(jbossLoader);
    var args = [ "-S" ];
    org.jboss.Shutdown.main(args);
    runningInstance_ = null;
    java.lang.Thread.currentThread().setContextClassLoader(contextLoader);
  }
  return descriptor;
};

JbossEar.prototype.CleanTask = function() {
  var descriptor = new TaskDescriptor("Clean JbossEar", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {
    eXo.core.IOUtil.emptyFolder(this.server.serverHome + "/temp");
  }
  return descriptor;
}

JbossEar.prototype.preDeploy = function(product) {
  product.addDependencies(new Project("commons-pool", "commons-pool", "jar", "1.2"));
  product.addDependencies(new Project("commons-dbcp", "commons-dbcp", "jar", "1.2.1"));
  product.addDependencies(new Project("org.exoplatform.portal", "exo.portal.server.jboss.plugin", "jar",
      product.serverPluginVersion));
  // product.removeDependency(new Project("quartz", "quartz", "jar",
  // "1.5.0-RC2"));

  var version = product.version;
  if (version.indexOf("2.0") != 0 && version.indexOf("2.1") != 0 && version.indexOf("2.2") != 0
      && version.indexOf("2.5") != 0) {
    product.addDependencies(new Project("org.slf4j", "slf4j-api", "jar", "1.5.6"));
    product.addDependencies(new Project("org.slf4j", "slf4j-log4j12", "jar", "1.5.6"));
  }

  // Above 2.5 we don't bundle JOTM anymore
  var version = product.version;
  if (version.indexOf("2.0") != 0 && version.indexOf("2.1") != 0 && version.indexOf("2.2") != 0) {
    product.removeDependency(new Project("jotm", "jotm_jrmp_stubs", "jar", "2.0.10"));
    product.removeDependency(new Project("jotm", "jotm", "jar", "2.0.10"));
  }

  // Remove hibernate libs for JBoss AS5
  if (this.exoJBoss5) {
    print("====================== JBOSS5 AS 5 ====================== ");
    product.removeDependency(new Project("org.jboss", "jbossxb", "jar", "2.0.0.GA"));
    product.removeDependency(new Project("org.jboss.logging", "jboss-logging-spi", "jar", "2.0.5.GA"));
    product.removeDependency(new Project("org.jboss", "jboss-common-core", "jar", "2.2.9.GA"));
  }
}

JbossEar.prototype.onDeploy = function(project) {
}

JbossEar.prototype.postDeploy = function(product) {
  ServerUtil = eXo.server.ServerUtil;
  ServerUtil.createEarApplicationXmlForJboss(this.deployWebappDir, product);
  ServerUtil.addClasspathForWar(this.deployLibDir);

  // Use jboss PrefixSorter deployer
  var eXoResourcesFile = new java.io.File(this.deployWebappDir + "/eXoResources.war");
  var neweXoResourcesFile = new java.io.File(this.deployWebappDir + "/01eXoResources.war");
  eXoResourcesFile.renameTo(neweXoResourcesFile);

  var portalFile = new java.io.File(this.deployWebappDir + "/" + product.portalwar);
  var newPortalFile = new java.io.File(this.deployWebappDir + "/02portal.war");
  portalFile.renameTo(newPortalFile);
  product.portalwar = "02portal.war";

  //Move all jars in /lib
  var earDir = new java.io.File(this.deployWebappDir);
  var libDir = new java.io.File(this.deployWebappDir, "lib");
  libDir.mkdir();
  var files = earDir.listFiles();
  for ( var i = 0; i < files.length; i++) {
    var file = files[i];
    var filepath = file.getAbsolutePath();
    if (filepath.endsWith(".jar")){
      eXo.core.IOUtil.cp(filepath, libDir.getAbsolutePath());
      eXo.core.IOUtil.remove(file);
    }
  }
  
  eXo.core.IOUtil.chmodExecutableInDir(this.serverHome + "/bin/", ".sh");
}

eXo.server.JbossEar = JbossEar.prototype.constructor;
