eXo.require("eXo.core.IOUtil");

function ServerUtil() { }

ServerUtil.prototype.createEarApplicationXmlForJboss = function(deployEarDir, product) {
  var earDir = new java.io.File(deployEarDir) ;
  var b = new java.lang.StringBuilder();
  b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  b.append("<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\" \"http://java.sun.com/dtd/application_1_3.dtd\">");
  b.append("\n<application>\n");
  b.append("  <display-name>exoplatform</display-name>\n");
  var eXoResources = "eXoResources.war";
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>01").append(eXoResources).append("</web-uri>\n");
  b.append("      <context-root>").append(eXoResources.substring(0, eXoResources.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>02").append(product.portalwar).append("</web-uri>\n");
  b.append("      <context-root>").append(product.portalwar.substring(0, product.portalwar.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  var file = earDir.list();
  for (var i = 0; i < file.length; i++) {
    if(file[i].endsWith("war") && file[i] != product.portalwar && file[i] != eXoResources) {
      var idx = file[i].indexOf('.');
      var context = file[i].substring(0, idx);
      b.append("  <module>\n");
      b.append("    <web>\n");
      b.append("      <web-uri>").append(file[i]).append("</web-uri>\n");
      b.append("      <context-root>").append(context).append("</context-root>\n");
      b.append("    </web>\n");
      b.append("  </module>\n");
//Jars moved in lib/
//    } else if(file[i].endsWith("jar")) {
//      b.append("  <module>\n").
//        append("    <ejb>").append(file[i]).append("</ejb>\n").
//        append("  </module>\n");
    } else if(file[i].endsWith("rar")) {
      b.append("  <module>\n");
      b.append("    <connector>").append(file[i]).append("</connector>\n");
      b.append("  </module>\n");
    }
  }
  b.append("</application>\n");
  eXo.core.IOUtil.createFolder(deployEarDir + "/META-INF");
  var out = 
    new java.io.FileOutputStream(deployEarDir + "/META-INF/application.xml");
  out.write(b.toString().getBytes(), 0, b.length());
  out.close();
}


ServerUtil.prototype.createEarApplicationXml = function(deployEarDir, product) {
  var earDir = new java.io.File(deployEarDir) ;
  var b = new java.lang.StringBuilder();
  b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  b.append("<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\" \"http://java.sun.com/dtd/application_1_3.dtd\">");
  b.append("\n<application>\n");
  b.append("  <display-name>exoplatform</display-name>\n");
  var eXoResources = "eXoResources.war";
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>").append(eXoResources).append("</web-uri>\n");
  b.append("      <context-root>").append(eXoResources.substring(0, eXoResources.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>").append(product.portalwar).append("</web-uri>\n");
  b.append("      <context-root>").append(product.portalwar.substring(0, product.portalwar.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  var file = earDir.list();
  for (var i = 0; i < file.length; i++) {
    if(file[i].endsWith("war") && file[i] != product.portalwar && file[i] != eXoResources) {
      var idx = file[i].indexOf('.');
      var context = file[i].substring(0, idx);
      b.append("  <module>\n");
      b.append("    <web>\n");
      b.append("      <web-uri>").append(file[i]).append("</web-uri>\n");
      b.append("      <context-root>").append(context).append("</context-root>\n");
      b.append("    </web>\n");
      b.append("  </module>\n");
    } else if(file[i].endsWith("jar")) {
      b.append("  <module>\n").
        append("    <ejb>").append(file[i]).append("</ejb>\n").
        append("  </module>\n");
    } else if(file[i].endsWith("rar")) {
      b.append("  <module>\n");
      b.append("    <connector>").append(file[i]).append("</connector>\n");
      b.append("  </module>\n");
    }
  }
  b.append("</application>\n");
  eXo.core.IOUtil.createFolder(deployEarDir + "/META-INF");
  var out = 
    new java.io.FileOutputStream(deployEarDir + "/META-INF/application.xml");
  out.write(b.toString().getBytes(), 0, b.length());
  out.close();
}

ServerUtil.prototype.createWebsphereEarApplicationXml = function(deployEarDir, product) {
  var earDir = new java.io.File(deployEarDir) ;
  var b = new java.lang.StringBuilder();
  b.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
  b.append("<!DOCTYPE application PUBLIC \"-//Sun Microsystems, Inc.//DTD J2EE Application 1.3//EN\" \"http://java.sun.com/dtd/application_1_3.dtd\">");
  b.append("\n<application>\n");
  b.append("  <display-name>exoplatform</display-name>\n");
  var eXoResources = "eXoResources.war";
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>").append(eXoResources).append("</web-uri>\n");
  b.append("      <context-root>").append(eXoResources.substring(0, eXoResources.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  b.append("  <module>\n");
  b.append("    <web>\n");
  b.append("      <web-uri>").append(product.portalwar).append("</web-uri>\n");
  b.append("      <context-root>").append(product.portalwar.substring(0, product.portalwar.indexOf('.'))).append("</context-root>\n");
  b.append("    </web>\n");
  b.append("  </module>\n");
  var file = earDir.list();
  for (var i = 0; i < file.length; i++) {
    if(file[i].endsWith("war") && file[i] != product.portalwar && file[i] != eXoResources) {
      var idx = file[i].indexOf('.');
      var context = file[i].substring(0, idx);
      b.append("  <module>\n");
      b.append("    <web>\n");
      b.append("      <web-uri>").append(file[i]).append("</web-uri>\n");
      b.append("      <context-root>").append(context).append("</context-root>\n");
      b.append("    </web>\n");
      b.append("  </module>\n");
    }
  }
  b.append("</application>\n");
  eXo.core.IOUtil.createFolder(deployEarDir + "/META-INF");
  var out = new java.io.FileOutputStream(deployEarDir + "/META-INF/application.xml");
  out.write(b.toString().getBytes(), 0, b.length());
  out.close();
}


ServerUtil.prototype.patchWebspherePortalWebXml = function(deployEarDir, product) {
  var warFile = deployEarDir + "/" + product.portalwar;
  var file = new java.io.File(warFile);
  if (!file.exists()) {
    eXo.System.info("IO", warFile + " file not found" ) ;
    return null;
  }
  var jar = new java.util.jar.JarFile(file) ;
  var webXmlEntry = "WEB-INF/web.xml";
  eXo.System.info("INFO", "---------------------------------------------------------------");
  eXo.System.info("INFO", "To be patched web.xml within " + product.portalwar + " file " + warFile + "/" + webXmlEntry);
  var webXML = eXo.core.IOUtil.getJarEntryAsText(warFile, webXmlEntry);

  var b = new java.lang.StringBuilder();
  b.append("<!-- Websphere Listener -->\n");
  b.append("  <listener>\n");                                                                                                                                                                                                       
  b.append("    <listener-class>org.exoplatform.services.organization.ext.websphere.WebsphereSessionListener</listener-class>\n");                                                                                                                      
  b.append("  </listener>\n");
  b.append("\n");
  b.append("  <listener>");
  webXML = webXML.replaceFirst("<listener>", b.toString());
  
  b = new java.lang.StringBuilder();
  b.append("<!-- Websphere filter -->\n");
  b.append("  <filter>\n");                                                                                                                                                                                                       
  b.append("    <filter-name>WebsphereFilter</filter-name>\n");                                                                                                                      
  b.append("    <filter-class>org.exoplatform.services.organization.ext.websphere.WebsphereFilter</filter-class>\n");
  b.append("  </filter>\n");
  b.append("\n");
  b.append("  <filter>");
  webXML = webXML.replaceFirst("<filter>", b.toString());
  
  b = new java.lang.StringBuilder();
  b.append("<!-- Websphere filter-mapping -->\n");
  b.append("  <filter-mapping>\n");                                                                                                                                                                                                       
  b.append("    <filter-name>WebsphereFilter</filter-name>\n");                                                                                                                      
  b.append("    <url-pattern>/public/*</url-pattern>\n");
  b.append("  </filter-mapping>\n");
  b.append("\n");
  b.append("  <filter-mapping>");
  webXML = webXML.replaceFirst("<filter-mapping>", b.toString());

 
  var replaceMap = new java.util.HashMap() ;

  replaceMap.put(webXmlEntry, webXML.getBytes()) ;   
  eXo.core.IOUtil.modifyJar(warFile, replaceMap, null) ;
}

ServerUtil.prototype.addClasspathForWar = function(earPath) {
  var earDir = new java.io.File(earPath) ;
  var files = earDir.listFiles() ;
  var b  = new java.lang.StringBuilder() ;
  for(var i = 0; i< files.length; i++) {
    var file =  files[i] ;
    if (file.getName().endsWith(".jar")) {
      b.append(file.getName()).append(' ');
    }
  }
  var classpath = b.toString() ;
  for(var i = 0; i< files.length; i++) {
    var file =  files[i] ;
    if (file.getName().endsWith(".war")) {
      manifestAttributes = new java.util.HashMap() ;
      manifestAttributes.put("Class-Path", classpath) ;
      if (file.isFile()) {  
         eXo.core.IOUtil.modifyJar(file.getAbsolutePath(), null, manifestAttributes);
      } 
    }
  }
}
  
eXo.server.ServerUtil = new ServerUtil();