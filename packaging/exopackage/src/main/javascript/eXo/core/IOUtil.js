function IOUtil() {
}

IOUtil.prototype.shift = function(args) {
  if(args.length == 0) return args ;
  var newargs =  new Array() ;
  for(var i = 0; i < args.length - 1; i++)  newargs[i] = args[i + 1] ;
  return newargs ;
}


IOUtil.prototype.emptyFolder = function(folder) {
  if(typeof(folder) == 'string') {
    this.emptyFolder(new java.io.File(folder)) ;
  } else {
    if(folder.exists() &&  folder.isDirectory()) {
      var child =  folder.listFiles();
      for(var i = 0; i < child.length; i++) {
        var  file =  child[i] 
        if(file.isDirectory()) this.emptyFolder(file) ;
        var method = file.getClass().getMethod('delete', null);
        var result = method.invoke(file, null);
        if(result) {
          eXo.System.vinfo("[DELETE] " + file.getAbsolutePath());
        } else {
          print("[ERROR]  Cannot delete " + file.getAbsolutePath());
        }
      }
    }
  }
}

IOUtil.prototype.remove = function(path) {
  var file = path ;
  if(typeof(path) == 'string') file = new java.io.File(path) ;

  if(file.exists())  {
    this.emptyFolder(file) ;
    var method = file.getClass().getMethod('delete', null);
    var result = method.invoke(file, null);
    if(result) {
      eXo.System.vinfo("[DELETE] " + file.getAbsolutePath());
    } else {
      print("[ERROR]  Cannot delete " + file.getAbsolutePath());
    }
  } else {
    print("[ERROR]  Cannot  find " + path);
  }
}

IOUtil.prototype.createByteArray = function(size)  { // creates an array of 2^size bytes
  var  buff = new java.io.ByteArrayOutputStream(size) ;
  buff.write(1) ;
  for(var i = 0; i < size; i++) {
   var innerBuff = buff.toByteArray() ;
   buff.write(innerBuff, 0, innerBuff.length) ;
  }
  var bytes =  buff.toByteArray() ;
  return bytes ;
}

IOUtil.prototype.cp = function(src, dest) {
  var srcFolder = new java.io.File(src) ;
  if(!srcFolder.exists()) {
    throw(src + " does not exist") ;
  } else if(srcFolder.isFile()) {
    var destFolder = new java.io.File(dest);
    if (destFolder.isFile()) {
    	dest = destFolder.getParent();
    }
    if (destFolder.exists()) {
      dest = dest + "/" + srcFolder.getName();
    }
    var input = new java.io.FileInputStream(srcFolder) ;
    var output = new java.io.FileOutputStream(dest) ;
    var buff = this.createByteArray(12) ;
    var len = 0 ;
    while ((len = input.read(buff)) > 0) {
      output.write(buff, 0, len);
    }
    input.close();  
    output.close(); 
    this.chmod(srcFolder,dest)
    eXo.System.vinfo("COPY", "Copy file " + src) ;
  } else {
    var destFolder = new java.io.File(dest) ;
    if(!destFolder.exists()) {
      destFolder.mkdirs() ;
      eXo.System.vinfo("MKDIR", "Create a directory " + dest) ;
    }
    var child =  srcFolder.listFiles();
    for(var i = 0; i < child.length; i++) {
      var file =  child[i] ;
      if(file.isFile())  {
        this.cp(file.getAbsolutePath(), 
                destFolder.getAbsolutePath() + "/" +  file.getName());
      } else {
        this.cp(file.getAbsolutePath(), 
                destFolder.getAbsolutePath() + "/" + file.getName());
      }
    }
  }
}

IOUtil.prototype.createFile = function(path, content) {
  var tmp = new java.lang.String(content) ;
  var bytes = tmp.getBytes() ;
  var out = new java.io.FileOutputStream(path);
  out.write(bytes, 0, bytes.length);
  out.close();
  eXo.System.vinfo("NEW", "Create file " +  path) ;
}

IOUtil.prototype.createFolder = function(path) {
  var folder = new java.io.File(path);
  if(!folder.exists()) {
    folder.mkdirs();
    eXo.System.vinfo("MKDIR", "Create a directory " + path) ;
  } else {
    eXo.System.vinfo("INFO", "Directory is exists" ) ;
  }
}

IOUtil.prototype.patchWar = function(warFile, properties, templateEntry, destEntry, mentries) {
  print("[PATCH WAR]" + warFile.getName()); 
  if (this.getJarEntryContent(warFile, templateEntry) != null) {
    var configTmpl = this.getJarEntryAsText(warFile, templateEntry);      
	eXo.System.info("CONF", "Filtering war entry " + templateEntry + " > " + destEntry);      
    var config = eXo.core.Util.modifyText(configTmpl, properties) ;    
    mentries.put(destEntry, config.getBytes()) ;    
  } else {
	eXo.System.info("CONF", "Failed to filer war entry " + templateEntry);
	java.lang.System.exit(1);
  }
  return mentries;
}

IOUtil.prototype.getJarEntryContent = function(fileName, entryName) {
  var file = new java.io.File(fileName);
  if (!file.exists()) {
    eXo.System.info("IO", "" + fileName + " file not found" ) ;
    return null;
  }
  eXo.System.info("IO", "Opening " + file);
  var jar = new java.util.jar.JarFile(file) ;
  var entries = jar.entries() ;
  while(entries.hasMoreElements()) {
    var entry = entries.nextElement() ;
    if(entry.getName() == entryName) {
      var entryStream = jar.getInputStream(entry);
      var buffer = this.createByteArray(12) ;
      var bytesRead;
      var out = new java.io.ByteArrayOutputStream();
      while ((bytesRead = entryStream.read(buffer)) != -1) {
        out.write(buffer, 0, bytesRead);
      }
      entryStream.close();
      jar.close() ;
      return out.toByteArray() ;
    }
  }
  jar.close() ;
  return null;
}

IOUtil.prototype.getJarEntryAsText = function(fileName, entryName) {
  return new java.lang.String(this.getJarEntryContent(fileName, entryName));
}

IOUtil.prototype.modifyJarEntry = function(moduleFile, configEntry, properties) {
  var  file = new java.io.File(moduleFile) ;
  if(!file.exists())  return ;
  var content = this.getJarEntryAsText(moduleFile, configEntry) ;
  var i = properties.entrySet().iterator();
  while(i.hasNext()) {
    var entry = i.next() ;
    content = content.replace(entry.getKey(), entry.getValue()) ;
  }

  mentries = new java.util.HashMap() ;
  mentries.put(configEntry, content.getBytes("UTF-8")) ;
  eXo.core.IOUtil.modifyJar(moduleFile, mentries, null);
}

/**
 * This method allow you to modify the content of one/multiple entries and the manifiest of 
 * a jar file.
 * 
 * fileName: The absolute path to the jar file that you want to modify 
 * entries:  A java.util.Map  object. The key should be the entry name  that you want to
 *           modify, and the value should be a java  byte array (byte[])  
 */
IOUtil.prototype.modifyJar = function(fileName, mentries, mattrs) {
  print("[MODIFY JAR]" + fileName); 
  var file = new java.io.File(fileName); 
  var jar = new java.util.jar.JarFile(fileName) ;
  var mf = jar.getManifest() ;
  if(mattrs != null) {
    var i = mattrs.entrySet().iterator();
    while(i.hasNext()) {
      var entry = i.next();
      mf.getMainAttributes().putValue(entry.getKey(), entry.getValue()) ;
    }
  }
  var tmpFile = new java.io.File(fileName + ".tmp") ;
  var jos = new java.util.jar.JarOutputStream(new java.io.FileOutputStream(tmpFile), mf) ;
  var entries = jar.entries() ;
  var buffer = this.createByteArray(12) ;
  var bytesRead;
  while(entries.hasMoreElements()) {
    var entry = entries.nextElement() ;
    var entryName = entry.getName() ; 
    if(entryName.match("MANIFEST.MF")) {
    } else if(mentries != null && mentries.containsKey(entryName)) {
      entry = new java.util.jar.JarEntry(entryName) ;
      jos.putNextEntry(entry) ;
      var content = mentries.get(entryName) ;
      jos.write(content, 0, content.length) ;
      mentries.remove(entryName) ;
    } else  {
      var entryStream = jar.getInputStream(entry);
      jos.putNextEntry(entry) ;
      while ((bytesRead = entryStream.read(buffer)) != -1) jos.write(buffer, 0, bytesRead);
    }
  }
  jar.close() ;
  jos.close() ;
  this.remove(file) ;
  tmpFile.renameTo(file) ;
}

IOUtil.prototype.addToArchive = function(zosStream, entryPath, file) {
  eXo.System.info("ADDTOARCHIVE", entryPath + file.getName()) ;
  if (entryPath != null && entryPath != "") 
    entryPath += "/";
  
  if(file.isDirectory()) {
    var children = file.listFiles() ; 
    for(var i = 0; i < children.length; i++) {
      this.addToArchive(zosStream, entryPath + file.getName(), children[i]) ;
    }
  } else {
    zosStream.putNextEntry(new java.util.zip.ZipEntry(entryPath + file.getName()));
    if (this.log)
      eXo.System.info("DEPLOY", entryPath + file.getName()) ;
    var fis =  new java.io.FileInputStream(file);
    var buf = this.createByteArray(12) ;
    var len = -1;
    while((len = fis.read(buf)) > 0) {
      zosStream.write(buf, 0, len);
    }
    fis.close();
  }
}

IOUtil.prototype.zip = function(src, dest, zipName) {
  var srcFile = new java.io.File(src) ;  
  if(!srcFile.exists()) {
    eXo.System.vinfo("INFO", "File Not Exist " +  srcFile.getAbsolutePath()) ;
    java.lang.System.exit(1) ;
  }
  var destDir = new java.io.File(dest);
  if(!destDir.exists()) destDir.mkdirs() ;
  var destFile = new java.io.File(dest + "/" + zipName + ".zip");
  this.remove(destFile) ;
  var zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(destFile));
  if(srcFile.isDirectory()) {
    var children = srcFile.listFiles() ; 
    for(var i = 0; i < children.length; i++) {
      this.addToArchive(zos, zipName, children[i]) ;
    }
  } else {
    this.addToArchive(zos, zipName, srcFile) ;
  }
  zos.close() ;
}

IOUtil.prototype.ear = function(src, dest) {
  this.log = true;
  var srcFile = new java.io.File(src) ;  
  if(!srcFile.exists()) {
    eXo.System.vinfo("INFO", "File Not Exist " +  srcFile.getAbsolutePath()) ;
    java.lang.System.exit(1) ;
  }
  var destDir = new java.io.File(dest.substring(0, dest.lastIndexOf("/")));
  if(!destDir.exists()) destDir.mkdirs() ;
  var destFile = new java.io.File(dest);
  this.remove(destFile) ;
  var zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(destFile));
  if(srcFile.isDirectory()) {
    var children = srcFile.listFiles() ; 
    for(var i = 0; i < children.length; i++) {
      this.addToArchive(zos, "", children[i]) ;
    }
  } else {
    this.addToArchive(zos, "", srcFile) ;
  }
  zos.close() ;
  this.log = false;
}

IOUtil.prototype.chmod = function(srcFolder, dest) {
  if (!java.lang.System.getProperty("os.name").startsWith("Windows")) {
    try{
      java.lang.Runtime.getRuntime().exec("chmod --reference=" + srcFolder + " " + dest );
    } catch(error) {
    }
  }
}

IOUtil.prototype.chmodExecutable = function(dest) {
  if (!java.lang.System.getProperty("os.name").startsWith("Windows")) {
    try{
      print("chmod +x on " + dest);
      java.lang.Runtime.getRuntime().exec("chmod +x " + dest );
    } catch(error) {
    }
  }
}

IOUtil.prototype.chmodExecutableInDir = function(dirName, extension) {
  //Executable .sh
  var binDir = new java.io.File(dirName);
	if (binDir.isDirectory()){
		var files = binDir.listFiles();
		for (i = 0; i < files.length; i++) {
			var fileChild = files[i];
            if (fileChild.getName().endsWith(extension)){
                eXo.core.IOUtil.chmodExecutable(fileChild);
            }
        }
	}
  }

eXo.core.IOUtil = new IOUtil() ;

//var test = eXo.core.IOUtil.getJarEntryContent("target/exo.tool.build-2.0.jar", "linux/exobuild.sh");
//print (new java.lang.String(test));
