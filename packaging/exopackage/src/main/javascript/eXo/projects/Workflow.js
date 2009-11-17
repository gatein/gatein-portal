eXo.require("eXo.core.IOUtil") ;
eXo.require("eXo.core.TaskDescriptor") ;
eXo.require("eXo.projects.Project");

function Workflow(workflowName, workflowVersion) {  
  this.name = workflowName;
  this.version = workflowVersion;   
}

Workflow.prototype.configWorkflow = function(product) {

print("Workflow.js: Configuring workflow version: " + this.version + "   name: "+this.name);

  product.addDependencies(this.getPortlet());
	if(this.name == "jbpm") {
	print("Workflow.js: adding dependencies for jbpm");
  	product.addDependencies(new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.component.workflow.impl.jbpm.facade", "jar", this.version));
 		product.addDependencies(new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.component.workflow.impl.jbpm.engine", "jar", "3.0"));
		product.addDependencies(new Project("org.exoplatform.ecm.workflow.bp", "exo.ecm.workflow.bp.jbpm.payraise", "jar", this.version));
		product.addDependencies(new Project("org.exoplatform.ecm.workflow.bp", "exo.ecm.workflow.bp.jbpm.holiday", "jar", this.version));
		if (product.useContentvalidation) {
		  product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.jbpmconfig", "jar", product.contentvalidationVersion));
			product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation.bp", "exo.ecm.dms.ext.contentvalidation.bp.jbpm.content.publishing", "jar", product.contentvalidationVersion));
			product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.plugin", "jar", product.contentvalidationVersion));
      product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.workflowPublication", "jar", product.contentvalidationVersion));
      product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.webui", "jar", product.contentvalidationVersion));
		}
	} else if(this.name == "bonita") {
		print("Workflow.js: adding dependencies for bonita");

		product.addDependencies(new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.component.workflow.impl.bonita", "jar", this.version));
		product.addDependencies(new Project("org.exoplatform.ecm.workflow.bp", "exo.ecm.workflow.bp.bonita.holiday", "jar", this.version));
		product.addDependencies(new Project("org.exoplatform.ecm.workflow.bp", "exo.ecm.workflow.bp.bonita.payraise", "jar", this.version));
		if (product.useContentvalidation) {
		  product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.plugin", "jar", product.contentvalidationVersion));
      product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.workflowPublication", "jar", product.contentvalidationVersion));
			product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.bonitaconfig", "jar", product.contentvalidationVersion));
			product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation.bp", "exo.ecm.dms.ext.contentvalidation.bp.bonita.content-publishing", "jar", product.contentvalidationVersion));
			product.addDependencies(new Project("org.exoplatform.ecm.dms.ext.contentvalidation", "exo.ecm.dms.ext.contentvalidation.component.webui", "jar", product.contentvalidationVersion));
		}
		
		product.addDependencies(new Project("org.ow2.bonita", "bonita-api", "jar", "4.0"));
		product.addDependencies(new Project("org.ow2.bonita", "bonita-core", "jar", "4.0"));
		product.addDependencies(new Project("org.ow2.novabpm", "novaBpmIdentity", "jar", "1.0"));
		product.addDependencies(new Project("org.ow2.novabpm", "novaBpmUtil", "jar", "1.0"));
		product.addDependencies(new Project("org.jbpm", "pvm", "jar", "r2175"));
		
		//Remove duplicate ehcache from Portal
		product.removeDependency(new Project("net.sf.ehcache", "ehcache", "jar", "1.4.1"));
		
		//Add external dependencies 
		product.addDependencies(new Project("bsh", "bsh", "jar", "2.0b1"));
		product.addDependencies(new Project("net.sf.ehcache", "ehcache", "jar", "1.5.0"));
		product.addDependencies(new Project("backport-util-concurrent", "backport-util-concurrent", "jar", "3.1"));
		product.addDependencies(new Project("org.ow2.util.asm", "asm", "jar", "3.1"));
		product.addServerPatch("jbossear",new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.server.jboss.patch-ear", "jar", this.version));
		product.addServerPatch("jboss",new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.server.jboss.patch", "jar", this.version));
  	product.addServerPatch("tomcat",new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.server.tomcat.patch", "jar", this.version));
	}
}

Workflow.prototype.getPortlet = function() {
    return new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.portlet.workflow", "exo-portlet", this.version).
	    addDependency(new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.component.workflow.api", "jar", this.version)).
	    addDependency(new Project("org.exoplatform.ecm.workflow", "exo.ecm.workflow.webui.workflow", "jar", this.version)).
	    addDependency(new Project("rome", "rome", "jar", "0.9")).
	    addDependency(new Project("com.totsp.feedpod", "itunes-com-podcast", "jar", "0.2")).
	    addDependency(new Project("jdom", "jdom", "jar", "1.0")).
	    addDependency(new Project("org.apache.ws.commons", "ws-commons-util", "jar", "1.0.1")).
	    addDependency(new Project("com.sun.xml.stream", "sjsxp", "jar", "1.0"));
}

eXo.projects.Workflow = Workflow.prototype.constructor ;
