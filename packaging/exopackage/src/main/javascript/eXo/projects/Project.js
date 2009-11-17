eXo.require("eXo.core.IOUtil")  ;

function Project(gid, id, ptype, version) {

  // Uncomment for debugging
  // print("Project.js: new Project(gid: " + gid + " id: " + id + " ptype: "+ ptype + " version: "+ version + " )");
  
  this.groupId =  gid ;
  this.artifactId = id ;
  this.version = version ;
  this.type = ptype ;
  this.deloyName = null ;

  this.extension = ptype ;
  if(ptype == "exo-portlet" || ptype == "exo-portal") this.extension = "war" ;
  if(ptype == "exo-ear-jar") this.extension = "jar";
  if(ptype == "exopc-war") this.extension = "exopc-war";
  if(ptype == "exo-ear-rar") this.extension = "rar";
  this.relativePath = gid.replace(/\./g, '/') + "/" + id + "/" + version + "/" + 
                      id + "-" + version + "." + this.extension ;
  this.artifact = gid + ":" + id + ":" + version + ":" + this.extension;
  this.tomcatDependency =  true ;
  this.jbossDependency =  true ;
  this.jbossearDependency =  true ;
  this.earDependency =  true ;
  this.jonasDependency =  true ;
  
  this.dependencies = null ;
}

Project.prototype.setServerDependency = function (name, b) {
  if("tomcat" == name) this.tomcatDependency = b ;
  else if("jboss" == name) this.jbossDependency = b ;
  else if("jbossear" == name) this.jbossearDependency = b ;
  else if("ear" == name) this.earDependency = b ;
  else if("jonas" == name) this.jonasDependency = b ;
}

Project.prototype.addDependency =  function(project) {
  if(this.dependencies == null) this.dependencies = new java.util.ArrayList() ;
  this.dependencies.add(project) ;
  return this ;
}

Project.prototype.hasDependency = function() {return this.dependencies != null ;}

Project.prototype.extractTo = function(repository, dir, ignore) {
  for( var i = 0; i < repository.length; i++) {
    try {
      var surl = repository[i] + "/" + this.relativePath;
      var url = new java.net.URL(surl);      
      eXo.System.info("PATCH", "Fetching patch at " + repository[i] + "/" + this.relativePath);
      var is = new java.util.jar.JarInputStream(url.openStream()) ;
      var entry = is.getNextEntry() ;
      while(entry != null) {
        if(!entry.isDirectory()) {
          var name = entry.getName() ;
          if(ignore == null || !name.matches(ignore)) {
            var file = new java.io.File(dir + "/" + name);
            var parentFolder = new java.io.File(file.getParent()) ;
            if(!parentFolder.exists()) parentFolder.mkdirs() ;
            var out = new java.io.FileOutputStream(file) ;
            var buf = new eXo.core.IOUtil.createByteArray(14) ;
            var read =  is.read(buf);
            while(read != -1) {
              out.write(buf, 0, read) ;
              read =  is.read(buf);
            }
            out.close();
            if (file.getCanonicalPath().endsWith(".sh"))
              IOUtil.prototype.chmodExecutable(file);
            eXo.System.info("PATCH", dir + "/" + entry.getName()) ;
          }
        }
        entry = is.getNextEntry() ;
      }
      is.close() ;
      return ;
    } catch(err) {
      eXo.System.info(err.message);
      if(i < (repository.length - 1)) eXo.System.info("Trying to download from the repo : " + repository[i+1]);
    }

  }
  throw("Error while extracting the project : " + this.relativePath) ;
}

Project.prototype.deployTo = function(repository, server) {
  for(var i = 0; i < repository.length; i++) {
    try {
      var surl = repository[i] + "/" + this.relativePath;
      var url = new java.net.URL(surl);
      //eXo.System.info("[DEPS] " + this.artifact);
      
      var warName = null, fileName = null ;
      if(this.deployName != null) {
      	warName = this.deployName;
      } else {
        warName = this.artifactId ;
        var temp = warName.substring(warName.length - 7);
        if(temp.match(".webapp")) {
          warName = warName.substring(0, warName.lastIndexOf(".")) ;
        }
        warName = warName.substring(warName.lastIndexOf(".") + 1) ;
      }
      
      if(this.extension == "war") {
        fileName = server.deployWebappDir + "/" + warName + ".war"  ;
     	} else if(this.type == "exo-ear-jar") {
        fileName = server.deployWebappDir + "/" + this.artifactId + ".jar" ;
      } else if(this.type == "exo-ear-rar") {
        fileName = server.deployWebappDir + "/" + this.artifactId + ".rar" ;
      } else if(this.type == "ear") {
        if (server.deployEarDir == null){
          print("NO EAR DEPLOY DIR, by passing " + this.deployName);
          return ;
        }
        fileName = server.deployEarDir + "/" + this.artifactId + ".ear" ;
      } else if(this.type == "exopc-war") {
        fileName = server.deployWebappDir + "/" + warName + ".war" ;
      } else {
        fileName = server.deployLibDir + "/" + this.artifactId + "-" +this.version + "." + this.type ;
      }
      
			var file = new java.io.File(fileName);
			var parentFolder = new java.io.File(file.getParent()) ;
      if(!parentFolder.exists()) parentFolder.mkdirs() ;
      var out = new java.io.FileOutputStream(file) ;

      //Check locally, download from maven if needed
      var is = null ;
      try {
        is = url.openStream();
      } catch (err1) {
        eXo.System.info("Artifact " + this.artifact + " not found locally, searching in maven repos");
        var mvnArgs = ["dependency:get", "-Dartifact=" + this.artifact, "-Dmaven.artifact.threads=1"] ;
        maven.MavenTask(eXo.env.currentDir, mvnArgs).execute() ;
      }
            
      //Fetch the binary on the server
      var is = url.openStream() ;
      eXo.System.vprintIndentation() ;                        
      eXo.System.vprint("[") ;
      var buf = new eXo.core.IOUtil.createByteArray(14) ;
      var read =  0, totalRead = 0, chunkOf100k = 0, chunkCount = 0 ;
      while(read != -1) {
        read =  is.read(buf);
        if(read > 0) {
          out.write(buf, 0, read) ;
          chunkOf100k += read ;
          totalRead += read ;
          if(chunkOf100k > 100000) {
            chunkOf100k = chunkOf100k - 100000 ; 
            chunkCount++ ;
            eXo.System.vprint(".");  
          }
        }
      }
      for(i = chunkCount; i < 60; i++) eXo.System.vprint(" ") ;
      eXo.System.vprint("] " + totalRead/1024 + "kb\n");
      out.close();
      is.close() ;
	    eXo.System.info("DEPLOY", fileName);      
      return ;
    } catch(err) {
      eXo.System.info(err.message);
      if(i < (repository.length - 1)) {
        eXo.System.info("Trying to download from the repo : " + repository[i+1]);
      }
    }
  }
  throw("Error while deploying the project : " + this.relativePath) ;
}
eXo.projects.Project = Project.prototype.constructor ;
