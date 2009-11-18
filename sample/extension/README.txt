====
    Copyright (C) 2009 eXo Platform SAS.
    
    This is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation; either version 2.1 of
    the License, or (at your option) any later version.
    
    This software is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
    Lesser General Public License for more details.
    
    You should have received a copy of the GNU Lesser General Public
    License along with this software; if not, write to the Free
    Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
    02110-1301 USA, or see the FSF site: http://www.fsf.org.
====

########################################
# HOW TO DEPLOY
########################################

########################################
# On JBoss (tested on JBoss 5.1.0.GA)
########################################

We assume that you have a clean JBoss version of GateIn: ie We assume that you have already the file gatein.ear in the deploy directory 
of jboss 

You need to:

1. Add the file sample-ext.ear from sample/extension/ear/target/ to the deploy directory of jboss 
2. Add the file starter.ear from starter/ear/target/ to the deploy directory of jboss 

WARNING: This can only work if a Unified ClassLoader has been configured on your JBoss (default behavior) and
the load order is first the exoplatform.ear then the sample-ext.ear and finally the starter.ear

########################################
# On Tomcat (tested on Tomcat 6.0.20)
########################################

We assume that you have a clean Tomcat version of GateIn: ie We assume that you have already all the jar files of GateIn and their dependencies 
into tomcat/lib and you have the related relam name "gatein-domain" defined in the file tomcat/conf/jaas.conf

1. Add the file sample-ext.war from sample/extension/war/target/ to the tomcat/webapps directory
2. Add the folder starter from starter/war/target/ to the tomcat/webapps directory 
3. Rename the directory (unzipped folder) starter to "starter.war" (for more details see the warning below)
4. Add the jar file exo.portal.sample.extension.config-X.Y.Z.jar from sample/extension/config/target/ to the tomcat/lib directory
5. Add the jar file exo.portal.sample.extension.jar-X.Y.Z.jar from sample/extension/jar/target/ to the tomcat/lib directory

WARNING: This can only work if the starter.war is the last war file to be loaded, so don't hesitate to rename it if your war files are loaded 
following to the alphabetic order

########################################
# HOW TO TEST
########################################

########################################
# On JBoss (tested on JBoss 5.1.0.GA)
########################################

You need to:

1. Go to the bin directory of jboss 
2. Launch "./run.sh" or "run.bat"
3. When jboss is ready, you can launch your web browser and access to http://localhost:8080/portal 

########################################
# On Tomcat (tested on Tomcat 6.0.20)
########################################

You need to:

1. Go to the bin directory of tomcat 
2. Launch "./gatein.sh run" or "gatein.bat run"
3. When tomcat is ready, you can launch your web browser and access to http://localhost:8080/portal 
