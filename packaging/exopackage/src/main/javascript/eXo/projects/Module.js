eXo.require("eXo.projects.Project");

function Module() {
  this.version = null;
  this.relativeMavenRepo = null;
  this.relativeSRCRepo = null;
  this.name = null;
}

Module.GetModule = function(path, params) {
  // Try to load the module descriptor corresponding to the specified name and
  // version
//  var srcPath = "/modules/" + path + "/";
//  var srcConf = eXo.env.eXoConfDir + srcPath;
//  var srcLoc = eXo.env.currentDir + srcPath;
//
//  // eXo.System.info("Opening " + src);
//  print("Loading module: " + srcPath);
//  if (new java.io.File(srcLoc+"module.js").exists()) {
//    // Local
//    eXo.load("module.js", srcLoc);
//  } else {
//    // Sinon configuration
//    eXo.load("module.js", srcConf);
//  }
  var srcName = path + ".packaging.module.js";
  var srcConf = eXo.env.eXoConfDir;

  // eXo.System.info("Opening " + src);
  print("Loading: " + srcName + " ( in " + srcConf + ")");
  eXo.load(srcName, srcConf);

  
  try {
    // The function getModule() is defined in the loaded module descriptor
    return getModule(params);
  } catch (error) {
    print("ERROR while loading module descriptor (name=\"" + name
        + "\", version=\"" + version + "\"). Perhaps it is missing.");
    java.lang.System.exit(1);
  }
  return null;
}
