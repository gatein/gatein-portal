eXo.require("eXo.core.TaskDescriptor");

function Product() {
  this.name = null;
  this.portalwar = null;
  this.codeRepo = null;
  this.dependenciesHolder = new java.util.HashMap();
  this.serverPatches = new java.util.HashMap();

  this.module = null;
  this.useWorkflow = false;
  this.useContentValidation = false;
  this.dependencyModule = null;
  this.serverPluginVersion = "trunk";
}

Product.prototype.getVersion = function() {

};

Product.GetProduct = function(name, version) {
  // Try to load the product descriptor corresponding to the specified name
  // and version

  var srcName = name + ".packaging.product.js";
  var srcConf = eXo.env.eXoConfDir;

  // eXo.System.info("Opening " + src);
  print("Loading: " + srcConf + "/" + srcName);
  eXo.load(srcName, srcConf);

//  try {
    // The function getProduct() is defined in the loaded product decriptor
    var product = getProduct(version);

    // Set the version on the product
    product.version = version;

    //
    return product;
//  } catch (error) {
//    eXo.System.error("ERROR while loading product descriptor (name=\"" + name + "\").");
//    eXo.System.error(error);
//    throw error;
//  }
}

Product.prototype.addServerPatch = function(serverName, project) {
  eXo.System.info("DEBUG", "Product.addServerPatch serverName = " + serverName);
  var holders = this.serverPatches.get(serverName);
  if (holders == null) {
    holders = new java.util.ArrayList(3);
    this.serverPatches.put(serverName, holders);
  }
  holders.add(project);
}

Product.prototype.getServerPatches = function(serverName) {
  return this.serverPatches.get(serverName);
}

Product.prototype.addDependencies = function(project) {
  try {
    this.dependenciesHolder.put(project.relativePath, project);
    if (project.hasDependency()) {
      var list = project.dependencies;
      for ( var i = 0; i < list.size(); i++) {
        this.addDependencies(list.get(i));
      }
    }
  } catch (error) {
    print("Error while adding dependencies for project " + project);
    throw error;
  }
}

Product.prototype.getDependencies = function() {
  return this.dependenciesHolder.values();
}

Product.prototype.getDependency = function(project) {
  return this.dependenciesHolder.get(project.relativePath);
}

Product.prototype.getDependencyById = function(depId) {
  var arrDep = this.getDependencies().toArray();
  for ( var i = 0; i < arrDep.length; i++) {
    var project = arrDep[i];
    if (project.artifactId == depId) {
      return project;
    }
  }
  return null;
}

Product.prototype.hasDependency = function(project) {
  return this.dependenciesHolder.containsKey(project.relativePath);
}

Product.prototype.removeDependency = function(project) {
  eXo.System.info("DELETE", "Remove dependency " + project.artifactId);
  this.dependenciesHolder.remove(project.relativePath);
}

Product.prototype.removeDependencyById = function(projectId) {
  var project = this.getDependencyById(projectId);
  if (project !== null) {
    this.removeDependency(project);
  } else {
    eXo.System.info("ERROR", "Dependency " + projectId + " doesn't exist !");
  }
}

Product.prototype.removeDependencyByGroupId = function(groupId) {
  var dependencies = new java.util.ArrayList();
  var arrDep = this.getDependencies().toArray();
  for ( var i = 0; i < arrDep.length; i++) {
    var project = arrDep[i];
    if (project.groupId == groupId) {
      this.removeDependency(project);
    }
  }
};

Product.prototype.hasDependencyModule = function(depName) {
  var hasDep = false;
  if (this.dependencyModule !== null) {
    for ( var i = 0; i < this.dependencyModule.length && !hasDep; i++)
      hasDep = (this.dependencyModule[i].name == depName);
  }
  return hasDep;
};

Product.prototype.getDependencyModule = function(depName) {
  var mod = null;
  if (this.hasDependencyModule(depName)) {
    for ( var i = 0; i < this.dependencyModule.length && mod === null; i++) {
      if (this.dependencyModule[i].name == depName)
        mod = this.dependencyModule[i];
    }
  }
  return mod;
};

/**
 * 
 */
Product.prototype.preDeploy = function() {
  // TODO : to overwrite in your product definition
  // like : product.cleanDependencies = function() { .. }
};

Product.prototype.DeployTask = function(product, server, repos) {
  patches = product.getServerPatches(server.name);
  eXo.System.info("INFO", "Add DeployTask for product '" + product.name
      + "' version '" + product.codeRepo + "' on server '" + server.name
      + "' with patches '" + patches + "'.");
  if (patches == null) {
    var msg = "The server " + server.name + " may not support this product: "
        + product.name + ". Please try to use another server";
    eXo.System.print("INFO", msg);
    return;
  }
  var descriptor = new TaskDescriptor("Deploy Product", server.serverHome);
  descriptor.execute = function() {
    eXo.System.info("DELETE", "Delete " + server.serverHome);
    eXo.core.IOUtil.remove(server.serverHome);
    if (server.name != "ear") {
      eXo.System.info("COPY", "Copy a clean server " + server.cleanServer);
      eXo.core.IOUtil.cp(eXo.env.dependenciesDir + "/" + server.cleanServer,
          server.serverHome);
    }
    server.preDeploy(product);
    product.preDeploy();
    for ( var i = 0; i < patches.size(); i++) {
      project = patches.get(i);
      var message = "Patch the server " + server.name + " with project "
          + project.artifactId + " " + project.version;
      eXo.System.info("INFO", message);
      new java.io.File(server.patchDir).mkdirs();
      project.extractTo(repos, server.patchDir, "META-INF/maven.*");
    }

    eXo.System.info("INFO", "Deploying dependencies");
    var i = product.getDependencies().iterator();
    counter = 0;
    while (i.hasNext()) {
      dep = i.next();
      dep.deployTo(repos, server);
      server.onDeploy(dep);
      counter++;
    }
    eXo.System.info("INFO", "Deployed total " + counter + " files");
    server.postDeploy(product);
  }
  return descriptor;
}
