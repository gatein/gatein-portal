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

Welcome on GateIn Tomcat packaging

1) How to run Tomcat
 * On the Windows platform
   Open a DOS prompt command, go to tomcat/bin and type the command
      "gatein.bat run" for production
      "gatein-dev.bat run" for development 

 * On Unix/linux/cygwin/MacOSX
   Open a terminal, go to tomcat/bin and type the command:
      "./gatein.sh run" for production
      "./gatein-dev.sh run" for development 
      
   You may need to change the permission of all *.sh files in the tomcat/bin dir by using: chmod +x *.sh

 For both OS environments, you need to set the JAVA_HOME variable.

2) How to access GateIn front page:

 * Enter one of the following addresses into your browser address bar:
    http://localhost:8080/portal
    http://localhost:8080/portal/public/classic


 You can log into the portal with the following accounts: root, john, mary, demo. 
 All those accounts have the default password "gtn".

For more documentation and latest updated news, please visit our website www.gatein.org.

Thank your for using GateIn !
JBoss & eXo Platform development teams
