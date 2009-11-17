* exopackage : scripting to build GateIn ear and to assemble the gatein distribution.

Status:
As explain in the GateIn main README file, this tooling is aimed to be replaced.
So this artifact is here for a short time to let the team work on replacing this way to package the 
Please see http://jira.jboss.org/jira/browse/GTNPORTAL-256 for the main Jira about it.

Description:
* .js files represent the scripts executed to package GateIn and its extensions.
* exobuild is the packager :
** creates the ear or not according to the target AS
** setup the DB configuration
** copy the binary to the target AS

Goal:
* Replace this scripting by a maven plugin and some maven projects (ear for jboss packaging for example)

Tasks:
* Describe the features (jboss, tomcat packaging, ...)
* Clean-up non needed code (svn update, dependency download, ...)
* Study what maven built-in functions can be used
* Write a dedicate maven plugin if needed