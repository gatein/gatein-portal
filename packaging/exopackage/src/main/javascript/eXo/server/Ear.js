eXo.require("eXo.core.TaskDescriptor");
eXo.require("eXo.core.IOUtil");
eXo.require("eXo.server.ServerUtil");
eXo.require("eXo.projects.Project");

function Ear(earHome) {
  this.runningInstance_ = null;
  this.name = "ear";
  this.serverHome = earHome;
  // using "[product.name]-[version].ear" in the EarTask of exobuild now;
  this.earFile = eXo.env.workingDir + "/gatein.ear";
  // TODO use WEBSPHERE_HOME
  this.cleanServer = java.lang.System.getProperty("clean.server");
  if (this.cleanServer == null || this.cleanServer.equals("") || !this.cleanServer.startsWith("ear"))
    this.cleanServer = "ear";
  this.deployLibDir = this.serverHome;
  this.deployWebappDir = this.serverHome;
  this.deployEarDir = this.serverHome;  
  this.patchDir = this.serverHome;
}

Ear.prototype.RunTask = function() {
  var descriptor = new TaskDescriptor("Run Ear", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {

  }
  return descriptor;
};

Ear.prototype.StopTask = function() {
  var descriptor = new TaskDescriptor("Stop Ear", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {

  }
  return descriptor;
};

Ear.prototype.CleanTask = function() {
  var descriptor = new TaskDescriptor("Clean Ear", this.serverHome + "/bin");
  descriptor.server = this;
  descriptor.execute = function() {
    eXo.core.IOUtil.emptyFolder(this.server.serverHome + "/temp");
  }
  return descriptor;
}

Ear.prototype.preDeploy = function(product) {
  product.addDependencies(new Project("commons-pool", "commons-pool", "jar", "1.2"));
  product.addDependencies(new Project("commons-dbcp", "commons-dbcp", "jar", "1.2.1"));
  product.addDependencies(new Project("org.exoplatform.portal", "exo.portal.server.websphere.plugin", "jar",
      this.pluginVersion));
}

Ear.prototype.onDeploy = function(project) {
}

Ear.prototype.postDeploy = function(product) {
  ServerUtil = eXo.server.ServerUtil;
  ServerUtil.createWebsphereEarApplicationXml(this.deployWebappDir, product);
  ServerUtil.addClasspathForWar(this.deployLibDir);
  ServerUtil.patchWebspherePortalWebXml(this.deployWebappDir, product);
}

eXo.server.Ear = Ear.prototype.constructor;
