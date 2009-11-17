eXo.require("eXo.core.TaskDescriptor");

function maven() {

}
maven.prototype.MavenTask = function(projectDir, args) {
  var descriptor = new TaskDescriptor("Maven Task", projectDir) ;
  descriptor.description = "Run mvn " + args + " against module " + projectDir ;
  descriptor.projectDir = projectDir;
  descriptor.mavenArgs = args;

  descriptor.execute = function() {
    var m2Home= eXo.env.m2Home ;
    java.lang.System.setProperty("maven.home", m2Home) ;
    java.lang.System.setProperty("classworlds.conf", m2Home + "/bin/m2.conf") ;

    java.lang.System.setProperty("user.dir", this.workingDir) ;    
    print("path: " + m2Home) ;
    var mvnClasspath = [
      new java.net.URL("file:" + m2Home + "/core/boot/classworlds-1.1.jar"),
      new java.net.URL("file:" + m2Home + "/boot/classworlds-1.1.jar")
    ] ;

    var contextLoader= java.lang.Thread.currentThread().getContextClassLoader();
    var mvnLoader = new java.net.URLClassLoader(mvnClasspath, contextLoader);
    java.lang.Thread.currentThread().setContextClassLoader(mvnLoader);
    var  type = mvnLoader.loadClass("org.codehaus.classworlds.Launcher") ;
    var exitCode = type.newInstance().mainWithExitCode(this.mavenArgs);
    java.lang.System.gc() ;
    if(exitCode != 0) {
      throw new java.lang.Exception("BUILD MODULE :" + this.workingDir + " FAILED");
    }
    java.lang.Thread.currentThread().setContextClassLoader(contextLoader); 
  }
  return descriptor ;
}

eXo.command.maven = maven.prototype.constructor ;
