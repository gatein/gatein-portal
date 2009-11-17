eXo.require("eXo.server.Tomcat");
eXo.require("eXo.server.Jboss");
eXo.require("eXo.server.JbossEar");
eXo.require("eXo.server.Ear");
eXo.require("eXo.server.Jonas");
eXo.require("eXo.server.Database");
eXo.require("eXo.server.WorkflowConfig");
eXo.require("eXo.core.TaskDescriptor");
eXo.require("eXo.command.maven");
eXo.require("eXo.command.svn");
eXo.require("eXo.core.IOUtil");
eXo.require("eXo.projects.Workflow");
eXo.require("eXo.projects.WorkflowBackup");
eXo.require("eXo.projects.Product");

// initialize possible database setups
var databaseMap = new java.util.HashMap();
databaseMap.put("hsqldb", eXo.server.Database.HsqlDB("hsqldb"));
databaseMap.put("mysql", eXo.server.Database.MysqlDB("mysql"));
databaseMap.put("oracle", eXo.server.Database.OracleDB("oracle"));
databaseMap.put("postgresql", eXo.server.Database.PostgresDB("postgresql"));
databaseMap.put("db2", eXo.server.Database.DB2ExpressDB("db2"));
databaseMap.put("db2v8", eXo.server.Database.DB2V8DB("db2v8"));
databaseMap.put("derby", eXo.server.Database.DerbyDB("derby"));
databaseMap.put("sqlserver", eXo.server.Database.SqlServerDB("sqlserver"));

// initialize possible server setups
var serverMap = new java.util.HashMap();
serverMap.put("tomcat", new Tomcat(eXo.env.workingDir + "/tomcat"));
serverMap.put("jboss", new Jboss(eXo.env.workingDir + "/jboss"));
serverMap.put("jbossear", new JbossEar(eXo.env.workingDir + "/jboss"));
serverMap.put("jonas", new Jonas(eXo.env.workingDir + "/jonas"));
serverMap.put("ear", new Ear(eXo.env.workingDir + "/ear"));

// TODO autodetect !!
var modules = [ "all", "pc", "jcr", "ws", "tools", "ecm", "cs", "ks", "portal", "liveroom", "dms", "workflow" ];
// TODO autodetect !!
var products = [ "cs", "ks", "ecm", "portal", "ultimate", "wcm", "webos", "liveroom", "dms", "workflow" ];

function exobuildInstructions() {
  print("\n" + "Use of the exobuild command: \n\n" + "  exobuild --product=name\n" + "           [--version=version]\n"
      + "           [--update]\n" + "           [--build]\n" + "           [--exclude=modules]\n"
      + "           [--deploy[=server]]\n" + "           [--release[=server]]\n"
      + "           [--workflow[=jbpm|bonita]]\n" + "           [--enable-workflow[=bonita|jbpm]]\n"
      + "           [--clean-mvn-repo]\n" + "           [--database[=dialect]]\n" + "           [--dbsetup=option]\n"
      + "\n\n" + "Options: \n" + "  * --product=name     Name of the product you want to build.\n"
      + "                       The possible names are "
      + products
      + ", ...\n"
      + "                       Default is portal.\n"
      + "  * --version=number   Allows to specify which version of the product\n"
      + "                       to build such as trunk, tags/2.0, branches/2.0,.... \n"
      + "                       Default is trunk.\n"
      + "  * --update           Run a svn update before it builds.\n"
      + "  * --build            Compile and install the sub projects of the product,\n"
      + "  * --exclude          Exclude the given modules (comma separated) from compilation and fetch jars from repository\n"
      + "                       You can specify any module name in "
      + modules
      + ".\n"
      + "                       Use this to avoid full build or when a module breaks the build\n"
      + "  * --deploy=server    Deploy to a given application server. Possible values are: 'all', "
      + serverMap.keySet()
      + ".\n"
      + "                       Default is tomcat.\n"
      + "  * --release=server   Release for the target application server. Produce a zip named after the current SVN revision.\n"
      + "                       Possible values are: 'all', "
      + serverMap.keySet()
      + ".\n"
      + "                       Default is tomcat\n"
      + "  * --clean-mvn-repo   Clean your local repository of eXo artifacts before building.\n"
      + "  * --database=dialect Specify target database dialect. The possible values are "
      + databaseMap.keySet()
      + ".\n"
      + "                       This will configure the appropriate JCR dialects and deploy the JDBC driver.\n"
      + "                       Used with --dbsetup=file option, exobuild tries to get database settings in a file named\n"
      + "                       database-configuration.{dialect}.xml\n"
      + "                       Default is hsqldb.\n"
      + "  * --dbsetup=option   Use this option with --database option to specify the database setup behaviour.\n"
      + "                       dbsetup=file will use the database and jcr files you provided.\n"
      + "                       dbsetup=ask allow you to enter the connection url , username and password of the database server.\n"
      + "                       dbsetup=defaults is the default option if dbsetup is not specified and will override settings by those defined in Database.js\n"
      + "  * --workflow=engine  (For the old verions of ECM from 2.0 to 2.2.x) Specify the workflow engine to bundle with the product. The possible values are bonita or jbpm.\n"
      + "                       This option is only used for products that use workflow. Default engine is jbpm\n"
      + "  * --enable-workflow=engine + .\n"
      + "                       (Since DMS 2.3) Specify the workflow engine to bundle with the product. The possible values are bonita or jbpm.\n"
      + "                       This option is only used for products that use workflow. Default engine is bonita\n"
      + "  * --help             To print this help. Also you can use option: '-help' or 'help' or '?' \n");
}

function errExobuild(cause, value) {
  eXo.System.info("ERR", cause + ": " + value);
  exobuildInstructions();
  java.lang.System.exit(1);
}

// name is "=NAME_SERVER" or "=all" or "" as default for tomcat
// cycle is "deploy" or "release"
function storeServers(name, cycle) {
  if (name == "") {
    eXo.System.info("INFO", " add server = " + "tomcat");
    deployServers.add(serverMap.get("tomcat"));
  } else if (name.match("=")) {
    var serverName = name.substring("=".length);
    if (serverName == "all") {
      eXo.System.info("INFO", " add servers = " + serverMap.keySet());
      deployServers = serverMap.values();
    } else {
      if (serverMap.get(serverName) == null)
        errExobuild("UNKNOWN server in " + cycle + " for deploy", serverName);
      eXo.System.info("INFO", " add server = " + serverName);
      deployServers.add(serverMap.get(serverName));
    }
  } else
    errExobuild("UNKNOWN server in " + cycle, serverName);
}

function ReleaseTask(server, product, version) {
  var descriptor = new TaskDescriptor("Release Task", server.serverHome);
  descriptor.execute = function() {
    var versionInfo = "unknown";
    if (!noInternet && "trunk" == version) {
      var commands = [ "svn", "info", eXo.env.eXoProjectsDir + "/" + product.codeRepo ];
      eXo.System.info("RELEASE", "Getting product revision from SVN.");
      var result = eXo.System.run(commands);
      var line = result.split("\n");
      for ( var i = 0; i < line.length; i++) {
        if (line[i].match("vision")) {
          eXo.System.info("RELEASE", line[i]);
          versionInfo = "r" + line[i].substring(line[i].lastIndexOf(":") + 1, line[i].length()).trim();
        }
      }
    } else {
      versionInfo = version;
    }
    var zipName = "exo-" + product.name + "-" + versionInfo + "-" + server.name;
    eXo.System.info("RELEASE", "Building zip: " + zipName + ".zip" + " in " + eXo.env.workingDir);
    eXo.core.IOUtil.zip(server.serverHome, eXo.env.workingDir, zipName);
  }
  return descriptor;
}

function EarTask(server, product, version) {
  var descriptor = new TaskDescriptor("Ear Task", server.serverHome);
  descriptor.execute = function() {
    var versionInfo = "unknown";
    if (!noInternet && "trunk" == version) {
      var commands = [ "svn", "info", eXo.env.eXoProjectsDir + "/" + product.codeRepo ];
      eXo.System.info("EAR", "Getting product revision from SVN.");
      var result = eXo.System.run(commands);
      var line = result.split("\n");
      for ( var i = 0; i < line.length; i++) {
        if (line[i].match("vision")) {
          eXo.System.info("EAR", line[i]);
          versionInfo = "r" + line[i].substring(line[i].lastIndexOf(":") + 1, line[i].length()).trim();
        }
      }
    } else {
      versionInfo = version;
    }
    var earName = product.name + "-" + versionInfo;
    var dest = eXo.env.workingDir + "/" + earName + ".ear"; // =
    // server.earFile;
    eXo.System.info("EAR", "Building ear: " + dest);
    eXo.core.IOUtil.ear(server.serverHome, dest);
    eXo.core.IOUtil.remove(server.serverHome);
  }
  return descriptor;
}

var build_ = false;
var update_ = false;
var ask = false;
var exclude_ = null;
var release_ = false;
var cleanMVNRepo_ = false;
var dbsetup = "defaults";
var maven = new eXo.command.maven();
var exosvn = null;
var server = null;
var deployServers = new java.util.HashSet();
var productName = "portal";
var product = null;
var dialect = "hsqldb";
var database = databaseMap.get(dialect);
var version = "trunk";
var workflow = new Workflow("bonita", version);
var workflowBackup = new WorkflowBackup("jbpm", version);
var useWorkflowPlg = false;
var enableWorkflow = false;
var tasks = new java.util.ArrayList();
var noInternet = false;

var args = arguments;

if (args.length == 0)
  errExobuild("NO ARGUMENT PASSED", "");

for ( var i = 0; i < args.length; i++) {
  var arg = args[i];
  if ("--update" == arg) {
    update_ = true;
  } else if (arg.match("--version")) {
    if (arg.match("--version="))
      version = arg.substring("--version=".length);
  } else if ("--build" == arg) {
    build_ = true;
  } else if (arg.match("--dbsetup")) {
    if (arg.match("--dbsetup="))
      dbsetup = arg.substring("--dbsetup=".length);
  } else if ("--clean-mvn-repo" == arg) {
    cleanMVNRepo_ = true;
  } else if (arg.match("--release")) {
    storeServers(arg.substring("--release".length), "release");
    release_ = true;
  } else if (arg.match("--exclude=")) {
    exclude_ = arg.substring("--exclude=".length);
  } else if (arg.match("--deploy")) {
    storeServers(arg.substring("--deploy".length), "deploy");
  } else if (arg.match("--database")) {
    if (arg.match("--database=")) {
      dialect = arg.substring("--database=".length);
      var database = databaseMap.get(dialect);
      if (database == null)
        errExobuild("UNKNOWN dialect", dialect);
    }
  } else if (arg.match("--product")) {
    if (arg.match("--product=")) {
      productName = arg.substring("--product=".length);
    }
  } else if (arg.match("--workflow")) {
    eXo.System
        .info(
            "INFO",
            "This parameter was deprecated since ECM 2.3. If you are using DMS 2.3 please use --enable-workflow to replace the old one");
    var workflowName = arg.substring("--workflow=".length);
    if (workflowName != "") {
      workflow.name = workflowName;
      workflowBackup.name = workflowName;
      java.lang.System.setProperty("workflow", workflowName);
    }
    useWorkflowPlg = true;
  } else if (arg.match("--enable-workflow")) {
    var workflowName = arg.substring("--enable-workflow=".length);
    if ((workflowName == "bonita") || (workflowName == "jbpm")) {
      workflow.name = workflowName;
      java.lang.System.setProperty("workflow", workflowName);
    } else {
      eXo.System.info("ERR", "'--enable-workflow' only accepts 'bonita' or 'jbpm'");
      java.lang.System.exit(1);
    }
    useWorkflowPlg = true;
    enableWorkflow = true;
  } else if (arg == "--nointernet") {
    noInternet = true;
  } else if (arg == "--help" || arg == "-help" || arg == "help" || arg == "?") {
    exobuildInstructions();
    java.lang.System.exit(1);
  } else {
    errExobuild("UNKNOWN ARGUMENT", arg);
  }
}

if (productName == null || productName == "") {
  errExobuild("NULL product", "");
} else {
  product = Product.GetProduct(productName, version);
  if (productName == "workflow") {
    enableWorkflow = true;
  }

}

if (deployServers != null && !deployServers.isEmpty() && dbsetup == "ask") {
  tasks.add(database.GetConfigTask());
}

if (update_) {
  // exosvn = new eXo.command.exosvn();
  exosvn = new eXo.command.svn();
  if ("all" != exclude_) {
    for ( var i = 0; i < product.dependencyModule.length; i++) {
      var module = product.dependencyModule[i];
      if (exclude_ == null || exclude_.indexOf(module.name) < 0) {
        var moduleDir = eXo.env.eXoProjectsDir + "/" + module.relativeSRCRepo;
        var directory = new java.io.File(moduleDir);
        if (directory.exists())
          tasks.add(exosvn.UpdateTask(moduleDir));
      }
    }
  }
  tasks.add(exosvn.UpdateTask(eXo.env.eXoProjectsDir + "/" + product.codeRepo));
}

if (build_) {
  var mvnArgs;
  // When you use --enable-workflow parameter, with DMS product,
  // we need use Maven profiles function to build
  // DMS with both component DMS-core and DMS-ext(including content
  // validation)
  if (enableWorkflow && (product.name == "eXoDMS")) {
    mvnArgs = [ "clean", "install", "-P", "ext" ];
  } else {
    mvnArgs = [ "clean", "install" ];
  }
  for ( var i = 0; i < product.dependencyModule.length; i++) {
    var module = product.dependencyModule[i];
    if (cleanMVNRepo_) {
      eXo.core.IOUtil.remove(eXo.env.dependenciesDir + "/repository/" + module.relativeMavenRepo);
    }
    if ("all" != exclude_) {
      if (exclude_ == null || exclude_.indexOf(module.name) < 0) {
        var moduleDir = eXo.env.eXoProjectsDir + "/" + module.relativeSRCRepo;
        var directory = new java.io.File(moduleDir);
        if (directory.exists())
          tasks.add(maven.MavenTask(moduleDir, mvnArgs));
      }
    }
  }
  var moduleDir = eXo.env.eXoProjectsDir + "/" + product.codeRepo;
  tasks.add(maven.MavenTask(moduleDir, mvnArgs));
}

if (deployServers != null && !deployServers.isEmpty()) {
  if (product.useWorkflow || useWorkflowPlg || enableWorkflow) {
    if (enableWorkflow) {
      workflow.version = product.workflowVersion;
      workflow.configWorkflow(product);
    } else {
      workflowBackup.version = product.workflowVersion;
      workflowBackup.configWorkflow(product);
    }
  }

  var serv = deployServers.iterator();
  while (serv.hasNext()) {
    server = serv.next();
    server.pluginVersion = product.serverPluginVersion;
    tasks.add(product.DeployTask(product, server, eXo.env.m2Repos));
    if (database != null) {
      tasks.add(database.DeployTask(product, server, eXo.env.m2Repos));
      tasks.add(database.ConfigureTask(product, server, dbsetup));
    }
    if (product.name == "eXoWorkflow") {
      var patchWorkflow = eXo.server.WorkflowConfig;
      tasks.add(patchWorkflow.patchWarWorkflow(server, product));
    }
    if (product.name == "eXoDMS") {
      if (product.useWorkflow || useWorkflowPlg || enableWorkflow) {
        if (enableWorkflow) {
          var patchWorkflow = eXo.server.WorkflowConfig;
          tasks.add(patchWorkflow.patchWarWorkflow(server, product));
        }
      }
    }
    if (server.name == "ear") {
      tasks.add(EarTask(server, product, version));
    }

    if (release_)
      tasks.add(ReleaseTask(server, product, version));
  }
}
/**
 * Liveroom Deploys and configures Openfire Deploys a Red5 server automatically
 * after deploying Liveroom
 */
if ((product.hasDependencyModule("liveroom") || productName == "liveroom") && deployServers != null) {
  var liveroomModule = (product.module.name == "liveroom") ? product.module : product.getDependencyModule("liveroom");
  liveroomModule.configure(tasks, deployServers, serverMap);
}

/**
 * Deploy openfire for CS Chat application Deploys and configures Openfire
 * Deploys a Red5 server automatically after deploying Liveroom
 */
if ((product.hasDependencyModule("cs") || productName == "cs") && deployServers != null) {
  var csModule = (product.module.name == "cs") ? product.module : product.getDependencyModule("cs");
  if (csModule.eXoApplication && csModule.eXoApplication.chat) {
    csModule.configure(tasks, deployServers, serverMap);
  }
}

for ( var i = 0; i < tasks.size(); i++) {
  task = tasks.get(i);
  var start = java.lang.System.currentTimeMillis();
  task.banner();
  task.execute();
  task.executionTime = java.lang.System.currentTimeMillis() - start;
  task.report();
}
java.lang.System.exit(0);