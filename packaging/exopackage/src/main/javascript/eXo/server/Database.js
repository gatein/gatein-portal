eXo.require("eXo.core.TaskDescriptor")  ;
eXo.require("eXo.projects.Project")  ;

function DBInstance() {
  this.name = null ;
  this.drivers = [] ;
  this.conectionURL = null ;
  this.driverClass = null ;
  this.username = null ;
  this.password = null;
  this.dialect = null ;
}

DBInstance.prototype.DeployTask = function(product, server, repos) {
  var task = new TaskDescriptor("Deploy Database Driver", null) ;
  task.description = "Deploy the database driver for database "  + this.name;
//  task.product = product ;
//  task.server = server ;
  task.repos = repos ;
  task.dbinstance = this ;

  task.execute = function() {
    var driver = this.dbinstance.drivers ;
    for(var i = 0; i < driver.length ; i++) {
      var project = driver[i] ;
      project.deployTo(repos, server) ;
      server.onDeploy(project) ;
    }
  }
  return task ;
}

DBInstance.prototype.GetConfigTask = function() {
  var descriptor = new TaskDescriptor("Configure Database", null) ;
  descriptor.description = "Configure the database environment for " + this.name;
	descriptor.dbinstance = this ;
	
  descriptor.execute = function() {
    var input = null ;
    input = eXo.System.readInput("Enter the connection url[" + this.dbinstance.conectionURL + "]") ;
    if(input != null && input.length() > 0) this.dbinstance.conectionURL = input ;
    input = eXo.System.readInput("Enter the username[" + this.dbinstance.username + "]") ;
    if(input != null && input.length() > 0) this.dbinstance.username = input ;
    input = eXo.System.readInput("Enter the password[" + this.dbinstance.password + "]") ;
    if(input != null && input.length() > 0) this.dbinstance.password = input ;       
  }
  return descriptor;
}


DBInstance.prototype.ConfigureTask = function(product, server, dbsetup) {
  var descriptor =  new TaskDescriptor("Configure Database", null) ;
  descriptor.description = "Configure the database environment for " + this.name;
  descriptor.dbinstance = this ;
 
  descriptor.execute =function () {
    
    if (!product.portalwar) {
      eXo.System.info("Server have not include portal to deploy database.");
      return 1;
    }
    var IOUtil =  eXo.core.IOUtil ;
    var jarFile =  server.deployWebappDir + "/" + product.portalwar ;
    var mentries = new java.util.HashMap() ;
	
    if (dbsetup=="file") {
	  var dbconfigSource = "WEB-INF/conf/database/database-configuration."+this.dbinstance.name+".xml";  
      var dbconfigDest = "WEB-INF/conf/database/database-configuration.xml";	    
      if (IOUtil.getJarEntryContent(jarFile, dbconfigSource) != null) {
        var configTmpl = IOUtil.getJarEntryAsText(jarFile, dbconfigSource);
        mentries.put(dbconfigDest, configTmpl.getBytes()) ;    
        eXo.System.info("CONF", "Replaced " + dbconfigDest + " by " + dbconfigSource);
      } else {
        eXo.System.info("CONF", dbconfigSource + " entry not found, using " + dbconfigDest );        
      }
    } else {
	  var properties = new java.util.HashMap() ;
      properties.put("${dialect}", this.dbinstance.dialect) ;
      properties.put("${driverClass}", this.dbinstance.driverClass) ;
      properties.put("${connectionUrl}", this.dbinstance.conectionURL) ;
      properties.put("${username}", this.dbinstance.username) ;
      properties.put("${password}", this.dbinstance.password) ;
    
      var propmsg = "Database settings: " + properties.entrySet();
      eXo.System.info("CONF", propmsg);
    
      var templateEntry = "WEB-INF/conf/database/database-configuration.tmpl.xml";      
      var dbconfigEntry = "WEB-INF/conf/database/database-configuration.xml";
      mentries = IOUtil.patchWar(jarFile, properties, templateEntry, dbconfigEntry, mentries)

    }
    
    var properties = new java.util.HashMap() ;
	properties.put("${dialect}", this.dbinstance.name);
    eXo.System.info("CONF", "JCR settings: " + properties.entrySet());	
  
  	mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/jcr/repository-configuration.tmpl.xml", 
  	"WEB-INF/conf/jcr/repository-configuration.xml", mentries)
    
    mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/jcr/jcr-configuration.tmpl.xml", 
  	"WEB-INF/conf/jcr/jcr-configuration.xml", mentries)
    

    var portalwar = server.deployWebappDir + "/" + product.portalwar;
    eXo.System.info("CONF", "Patching database config in " + portalwar + ": \n\t" + mentries.keySet());
    IOUtil.modifyJar(portalwar, mentries, null) ;
  }
  return descriptor;
}

//==========================================================================================

function Database() {
  
}

Database.prototype.HsqlDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("hsqldb", "hsqldb", "jar", "1.8.0.7") ] ;

  instance.driverClass = "org.hsqldb.jdbcDriver";
  instance.dialect = "org.hibernate.dialect.HSQLDialect" ;
  instance.conectionURL = "jdbc:hsqldb:file:../temp/data/exodb";
  instance.username = "sa" ;
  instance.password = "";
  return instance ;
}

Database.prototype.MysqlDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("mysql", "mysql-connector-java", "jar", "5.0.5")] ;
   
  instance.driverClass = "com.mysql.jdbc.Driver";
  instance.dialect = "org.hibernate.dialect.MySQLDialect" ;
  instance.conectionURL = "jdbc:mysql://localhost/webos";
  instance.username = "exoadmin" ;
  instance.password = "exo12321";
    
  return instance ;
}

Database.prototype.PostgresDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("org.postgresql", "postgresql-jdbc3", "jar", "8.2-505")] ;
   
  instance.driverClass = "org.postgresql.Driver";
  instance.dialect = "org.hibernate.dialect.PostgreSQLDialect" ;
  instance.conectionURL = "jdbc:postgresql://192.168.1.15:5432/exodb";
  instance.username = "exo" ;
  instance.password = "exo";
  
  return instance ;
}
  
Database.prototype.OracleDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("oracle", "ojdbc", "jar", "1.4")] ;
   
  instance.driverClass = "oracle.jdbc.OracleDriver";
  instance.dialect = "org.hibernate.dialect.Oracle9Dialect" ;
  instance.conectionURL = "jdbc:oracle:thin:@//192.168.1.15:1521/xe";
  instance.username = "exo" ;
  instance.password = "exo";
    
  return instance ;
}

Database.prototype.DB2ExpressDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("com.ibm.db2", "db2jcc", "jar", "9.1"),
  										 new Project("com.ibm.db2", "db2jcc_license_cu", "jar", "9.1")] ;
   
  instance.driverClass = "com.ibm.db2.jcc.DB2Driver";
  instance.dialect = "org.hibernate.dialect.DB2Dialect" ;
  instance.conectionURL = "jdbc:db2://192.168.1.15:50000/exodb";
  instance.username = "exoinst" ;
  instance.password = "exo";
    
  return instance ;
}

Database.prototype.DB2V8DB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("com.ibm.db2", "db2jcc", "jar", "8.0"),
  										 new Project("com.ibm.db2", "db2jcc_license_cu", "jar", "8.0")] ;
   
  instance.driverClass = "com.ibm.db2.jcc.DB2Driver";
  instance.dialect = "org.hibernate.dialect.DB2Dialect" ;
  instance.conectionURL = "jdbc:db2://10.21.10.11:50520/EXO";
  instance.username = "bdpdo11" ;
  instance.password = "edvgpn8ll40k";
    
  return instance ;
}

Database.prototype.DerbyDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("org.apache", "derby", "jar", "10.2")] ;
   
  instance.driverClass = "org.apache.derby.jdbc.ClientDriver";
  instance.dialect = "org.hibernate.dialect.DerbyDialect" ;
  instance.conectionURL = "jdbc:derby://192.168.1.15:1527/exodb;create=true";
  instance.username = "exo" ;
  instance.password = "exo";
    
  return instance ;
}

Database.prototype.SqlServerDB = function(name) {
  var instance = new DBInstance() ;
  instance.name = name ;
  instance.drivers = [ new Project("com.microsoft", "sqljdbc", "jar", "1.1.1501")] ;
   
  instance.driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  instance.dialect = "org.hibernate.dialect.SQLServerDialect" ;
  instance.conectionURL = "jdbc:sqlserver://192.168.1.19:1433;databaseName=exodb";
  instance.username = "exo" ;
  instance.password = "exo";
    
  return instance ;
}

//==========================================================================================

eXo.server.Database = new Database() ;
