eXo.require("eXo.core.IOUtil") ;
eXo.require("eXo.core.TaskDescriptor") ;
eXo.require("eXo.projects.Project");

function Jonas(jonasHome) {
  this.runningInstance_ = null ;
  this.name = "jonas" ;
  this.serverHome = jonasHome ;
  this.cleanServer = java.lang.System.getProperty("clean.server") ;
  if(this.cleanServer == null || this.cleanServer.equals("") || !this.cleanServer.startsWith("JONAS")) this.cleanServer = "JONAS_4_8_6" ;
  this.deployLibDir = this.serverHome + "/lib/apps" ;
  this.deployWebappDir = this.serverHome + "/apps/autoload/gatein.ear";
  this.deployEarDir = this.serverHome + "/apps/autoload/";
  this.patchDir = this.serverHome ;
}

Jonas.prototype.RunTask = function() {
  descriptor =  new TaskDescriptor("Run Jonas", this.serverHome + "/bin") ;
  descriptor.execute = function() {
    eXo.System.info("RunTask() has not been implemented.") ;
  }
  return descriptor ;
};

Jonas.prototype.StopTask = function() {
  descriptor =  new TaskDescriptor("Stop Jonas", this.serverHome + "/bin") ;
  descriptor.execute = function() {
    eXo.System.info("StopTask() has not been implemented.") ;
  }
  return descriptor ;
};

Jonas.prototype.CleanTask = function() {
  descriptor =  new TaskDescriptor("Clean Jonas", this.serverHome + "/bin") ;
  descriptor.server = this;
  descriptor.execute = function() {
    eXo.core.IOUtil.emptyFolder(this.server.serverHome + "/logs");
    eXo.core.IOUtil.emptyFolder(this.server.serverHome + "/temp");
    eXo.core.IOUtil.emptyFolder(this.server.serverHome + "/work");
  }
  return descriptor ;
}

Jonas.prototype.preDeploy = function(product) {
  eXo.core.IOUtil.createFolder(this.deployWebappDir + "/META-INF");
  product.addDependencies(new Project("commons-dbcp", "commons-dbcp", "jar", "1.2.1")) ;
  product.addDependencies(new Project("commons-pool", "commons-pool", "jar", "1.2")) ;
  product.addDependencies(new Project("org.exoplatform.portal", "exo.portal.server.jonas.plugin", "jar", product.serverPluginVersion)) ;  //this.pluginVersion
}

Jonas.prototype.onDeploy = function(project) {
	
//if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
//	    process = serverHome + "\\bin\\nt\\post-patch.bat";
//	  }
//	  else {
//	    process = serverHome + "/bin/unix/post-patch.sh";
//	    try {
//	    	Runtime.getRuntime().exec("chmod +x " + process);
//	    } catch(Exception e) {
//	      System.err.println("[ERROR] " + e.toString());
//	    }
//	  }
//	  try {
//	    Runtime.getRuntime().exec(process);
//	  } catch(Exception e) {
//	    System.err.println("[ERROR] " + e.toString());
//	  }
//   }
	
}

Jonas.prototype.postDeploy = function(product) {
  ServerUtil = eXo.server.ServerUtil ;
  ServerUtil.createEarApplicationXml(this.deployWebappDir, product) ;
  ServerUtil.addClasspathForWar(this.deployLibDir) ;
  var workflow = java.lang.System.getProperty("workflow");
  if(product.useWorkflow && workflow == "bonita") {
  	var IOUtil =  eXo.core.IOUtil ;
  	var properties = new java.util.HashMap() ;
	  properties.put("${workflow}", "bonita") ;  
	  var jarFile =  server.deployWebappDir + "/" + product.portalwar ;
	  var mentries = new java.util.HashMap() ;
	  var configTmpl = 
	    IOUtil.getJarEntryAsText(jarFile, "WEB-INF/conf/configuration.tmpl.xml");
	  var config = eXo.core.Util.modifyText(configTmpl, properties) ;
	  mentries.put("WEB-INF/conf/configuration.xml", config.getBytes()) ;	    		    	
	  IOUtil.modifyJar(server.deployWebappDir + "/" + product.portalwar, mentries, null) ;
  }  
  eXo.core.IOUtil.chmodExecutableInDir(this.serverHome + "/bin/", ".sh");
}

eXo.server.Jonas = Jonas.prototype.constructor ;
