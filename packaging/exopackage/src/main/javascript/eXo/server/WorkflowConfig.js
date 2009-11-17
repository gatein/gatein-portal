eXo.require("eXo.core.TaskDescriptor");
eXo.require("eXo.projects.Project");
eXo.require("eXo.core.IOUtil");

function WorkflowConfig() {
 
}

WorkflowConfig.prototype.patchWarWorkflow = function(server,product) {
  var descriptor =  new TaskDescriptor("Configure workflow", null) ;
  var workflowName = java.lang.System.getProperty("workflow");
  if (workflowName == null || workflowName == "") workflowName = "bonita";
  descriptor.description = "Configure to use workflow with " + workflowName;
  descriptor.execute =function () {
    var jarFile =  server.deployWebappDir + "/" + product.portalwar;
    var IOUtil =  eXo.core.IOUtil;
    var mentries = new java.util.HashMap();
    var properties = new java.util.HashMap();
    properties.put("${workflow}", workflowName);
    if (product.name == "eXoWorkflow") {
      eXo.System.info("CONF", "JCR settings: " + properties.entrySet());	
	    mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/configuration.tmpl.xml", 
	                  "WEB-INF/conf/configuration.xml", mentries);
	    var portalwar = server.deployWebappDir + "/" + product.portalwar;
	    eXo.System.info("CONF", "Patching workflow config in " + portalwar + ": \n\t" + mentries.keySet());
	    IOUtil.modifyJar(portalwar, mentries, null);
    }
    if (product.name == "eXoDMS") {
	    var jarFile =  server.deployWebappDir + "/" + product.portalwar;
	    var IOUtil =  eXo.core.IOUtil;
	    var mentries = new java.util.HashMap();
	    var properties = new java.util.HashMap();
	    eXo.System.info("CONF", "Enable Workflow settings");
	    mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/portal/group/organization/management/executive-board/navigation.workflow.xml", 
	                  "WEB-INF/conf/portal/group/organization/management/executive-board/navigation.xml", mentries);
	    var portalwar = server.deployWebappDir + "/" + product.portalwar;
	    eXo.System.info("CONF", "Patching workflow config in " + portalwar + ": \n\t" + mentries.keySet());
	    IOUtil.modifyJar(portalwar, mentries, null);
	    	    
	    mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/portal/group/platform/administrators/navigation.workflow.xml", 
	                  "WEB-INF/conf/portal/group/platform/administrators/navigation.xml", mentries);
	    var portalwar = server.deployWebappDir + "/" + product.portalwar;
	    eXo.System.info("CONF", "Patching workflow config in " + portalwar + ": \n\t" + mentries.keySet());
	    IOUtil.modifyJar(portalwar, mentries, null);
	    
	    mentries = IOUtil.patchWar(jarFile, properties, "WEB-INF/conf/portal/group/platform/users/navigation.workflow.xml", 
	                  "WEB-INF/conf/portal/group/platform/users/navigation.xml", mentries);
	    var portalwar = server.deployWebappDir + "/" + product.portalwar;
	    eXo.System.info("CONF", "Patching workflow config in " + portalwar + ": \n\t" + mentries.keySet());
	    IOUtil.modifyJar(portalwar, mentries, null);
    }
	}
	return descriptor;
}

eXo.server.WorkflowConfig = new WorkflowConfig();
