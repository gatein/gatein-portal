function Env() {

  print("******************************************");
  print("* eXo Package version embeeded in GateIn *");
  print("******************************************");

  this.eXoPackageHome = java.lang.System.getProperty("exo.package.home");
  print("* eXoPackageHome : " + this.eXoPackageHome);

  this.baseDir = java.lang.System.getProperty("exo.base.dir");
  print("* BaseDir : " + this.baseDir);

  this.workingDir = java.lang.System.getProperty("exo.working.dir");
  if (this.workingDir == null || this.workingDir.equals("")) {
    this.workingDir = this.baseDir + "/exo-working";
  }
  print("* workingDir : " + this.workingDir);

  this.eXoConfDir = java.lang.System.getProperty("exo.conf.dir");
  print("* eXoConfDir : " + this.eXoConfDir);

  this.dependenciesDir = java.lang.System.getProperty("exo.dep.dir");
  if (this.dependenciesDir == null || this.dependenciesDir.equals(""))
    this.dependenciesDir = this.baseDir + "/exo-dependencies";
  print("* dependenciesDir : " + this.dependenciesDir);

  this.eXoProjectsDir = java.lang.System.getProperty("exo.src.dir");
  if (this.eXoProjectsDir == null || this.eXoProjectsDir.equals(""))
    this.eXoProjectsDir = this.baseDir + "/eXoProjects";
  print("* eXoProjectsDir : " + this.eXoProjectsDir);

  this.javaHome = java.lang.System.getProperty("java.home");
  print("* javaHome : " + this.javaHome);
  this.currentDir = java.lang.System.getProperty("exo.current.dir");
  print("* currentDir : " + this.currentDir);

  if (this.currentDir.startsWith("/cygdrive/")) {
    this.currentDir = this.currentDir.substring("/cygdrive/".length);
    this.currentDir = this.currentDir.replaceFirst("/", ":/");
  }
  print("* currentDir : " + this.currentDir);

  this.m2Home = java.lang.System.getProperty("exo.m2.home");
  if (this.m2Home == null || this.m2Home.equals(""))
    this.m2Home = this.baseDir + "/maven2";
  print("* m2Home : " + this.m2Home);

  print("* m2Repos : " + java.lang.System.getProperty("exo.m2.repos"));
  var m2Repos = java.lang.System.getProperty("exo.m2.repos").split(",");
  this.m2Repos = new Array();
  var j = 0;
  for ( var i = 0; i < m2Repos.length; i++)
    if (m2Repos[i].trim() != "")
      this.m2Repos[j++] = m2Repos[i].trim();

  this.cleanServer = java.lang.System.getProperty("clean.server");
  print("* cleanServer : " + this.cleanServer);
  print("********************************");

}

var eXo = {
  core : {},
  projects : {},
  server : {},
  command : {},

  env :new Env(),

  require : function(module, jsLocation) {
    try {
      if (eval(module + ' != null'))
        return;
    } catch (err) {
      print("[ERROR] err  : " + module);
      java.lang.System.exit(1);
    }
    if (jsLocation == null) {
      jsLocation = eXo.env.eXoPackageHome + '/javascript/';
    }
    var path = jsLocation + module.replace(/\./g, '/') + '.js';
    try {
      load(path);
    } catch (err) {
      print("[ERROR] Cannot load the javascript module " + module + " from " + jsLocation);
      print(err);
    }
  },

  load : function(relativePath, jsLocation) {
    if (jsLocation == null) {
      jsLocation = eXo.env.eXoPackageHome + '/javascript/';
    }
    var path = jsLocation + '/' + relativePath;
    print("Loading path : " + path);
    try {
      load(path);
    } catch (err) {
      print("Cannot load the javascript module " + relativePath + " from " + jsLocation);
      print(err);
    }
  }
};

eXo.require("eXo.System");
eXo.require("eXo.core.Util");

if (arguments.length > 0) {
  var command = arguments[0];
  arguments = eXo.core.Util.shift(arguments);
  eXo.require("eXo.command." + command);
}
