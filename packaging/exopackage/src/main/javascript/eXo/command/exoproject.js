eXo.require("eXo.server") ;
eXo.require("eXo.server.Tomcat") ;
eXo.require("eXo.command.maven") ;

function exoproject() {
  server = new Tomcat(eXo.env.workingDir + "/tomcat") ; 
};

exoproject.prototype.ModuleDeploy = function() {
  var maven = new eXo.command.maven() ;
  var mvnArgs = ["clean","install"] ;
  maven.MavenTask(eXo.env.currentDir, mvnArgs).execute() ;
   
  src = new java.io.File("target") ;
  var child =  src.listFiles();
  for(i = 0; i < child.length; i++) {
    var file =  child[i] ;
    if(file.getName().endsWith(".jar")) {
      eXo.System.info("COPY", file.getName() + " to " + server.deployLibDir) ;
      eXo.core.IOUtil.cp(file.getAbsolutePath(), server.deployLibDir + "/"  + file.getName()) ;
    } else if(file.getName().endsWith(".war")) {
      eXo.System.info("COPY", file.getName() + " to " + server.deployWebappDir) ;
      eXo.core.IOUtil.cp(file.getAbsolutePath(), server.deployWebappDir + "/"  + file.getName()) ;
    }
  }
}

exoproject.prototype.QuickWarDeploy = function() {
  var maven = new eXo.command.maven() ;
  var mvnArgs = ["compile", "jar:jar"] ;
  maven.MavenTask(eXo.env.currentDir, mvnArgs).execute() ;
   
  var dest = new java.io.File("src/main/webapp/WEB-INF/lib") ;
  if(!dest.exists())  dest.mkdir() ;
  src = new java.io.File("target") ;
  var child =  src.listFiles();
  for(var i = 0; i < child.length; i++) {
    var file =  child[i] ;
    if(file.getName().endsWith(".jar")) {
      eXo.System.info("COPY", file.getName() + " to " + server.deployLibDir) ;
      eXo.core.IOUtil.cp(file.getAbsolutePath(), "src/main/webapp/WEB-INF/lib/"  + file.getName()) ;
    } else if(file.getName().endsWith(".war")) {
		  var jarDir = new java.io.File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - 4) + "/WEB-INF/lib") ;
		  if(jarDir.exists()) {
		    var jarChild =  jarDir.listFiles();
		    for(var j = 0; j < jarChild.length; j++) {
				  var jarFile =  jarChild[j] ;
		    	eXo.System.info("COPY", jarFile.getName() + " to " + server.deployLibDir) ;
				  eXo.core.IOUtil.cp(jarFile.getAbsolutePath(), "src/main/webapp/WEB-INF/lib/"  + jarFile.getName()) ;
				}
		  }
		}
  }
}

exoproject.prototype.ContextDeploy = function() {
  var folder = new java.io.File("src/main/resources/tomcat") ;
  var child =  folder.listFiles();
  var destination = server.serverHome + "/conf/Catalina/localhost" ;
  for(var i = 0; i < child.length; i++) {
    var file =  child[i] ;
    if(file.getName().endsWith(".xml")) {
      eXo.System.info("COPY", file.getName() + " to " + destination) ;
      eXo.core.IOUtil.cp(file.getAbsolutePath(), destination + "/"  + file.getName()) ;
    }
  }
}

function printInstructions() {
    print(
      "\n\n" +
      "Usage of exoproject command: \n\n" +
      "  exoproject --deploy=[context,module,quickwar] [--server=tomcat,jboss,jonas,ear,jbossear] \n\n" +
      "Options: \n" +
      "  * --deploy             is mandatory. Enables to deploy either a context, a module (jar) or a war.\n" +
      "  * --server             is optional. Enables to specify the target application server.\n"
    );
  }

var args = arguments;

if(args.length == 0) {
  printInstructions() ;
  java.lang.System.exit(1);
}

exoproject = new exoproject() ;
for(var i = 0; i < args.length; i++) {
  var arg = args[i] ;
  if("--server=jonas".equals(arg)) {
    exoproject.server = Jonas(eXo.env.workingDir + "/jonas") ; 
  } else if("--server=jboss".equals(arg)) {
    exoproject.server = Jboss(eXo.env.workingDir + "/jboss") ; 
  } else if("--server=jbossear".equals(arg)) {
    exoproject.server = JbossEar(eXo.env.workingDir + "/jboss") ; 
  } else if("--server=ear".equals(arg)) {
    exoproject.server = Ear(eXo.env.workingDir + "/ear") ; 
  } else if("--server=tomcat".equals(arg)) {
    exoproject.server = Tomcat(eXo.env.workingDir + "/tomcat") ; 
  } else if("--deploy=context".equals(arg)) {
    exoproject.ContextDeploy() ;
  } else if("--deploy=module".equals(arg)) {
    exoproject.ModuleDeploy() ;
  } else if("--deploy=quickwar".equals(arg)) {
    exoproject.QuickWarDeploy() ;
  }
}
