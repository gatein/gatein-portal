/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.gatein.selenium;

import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import static org.jboss.gatein.selenium.build.BuildHelper.getMavenHome;
import static org.jboss.gatein.selenium.common.CommonHelper.copyAndClose;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class AbstractSingleTestWithAnt extends AbstractSingleTest
{
   private String workDirRoot = "target/";
   private String mavenProps = "project.properties";
   private String mavenActiveProps = "active-profile.properties";

   private boolean fileInited;
   private File antBuildFile;
   private Project project;

   protected String getTestName()
   {
      String className = getClass().getName();
      return className.substring(className.lastIndexOf('.') + 1);
   }

   protected Properties getActiveMavenProperties() throws IOException
   {
      Properties props = new Properties();
      File propsFile = new File(workDirRoot, mavenProps);
      if (!propsFile.isFile())
      {
         log.warn("File " + propsFile + " not found. Make sure you're using properties-maven-plugin in your pom.xml. Build may not work properly: " + getAntBuildFileName());
      }
      else
      {
         props.load(new FileInputStream(propsFile));
      }

      propsFile = new File(workDirRoot, mavenActiveProps);
      if (!propsFile.isFile())
      {
         log.warn("File " + propsFile + " not found. Make sure you're using properties-maven-plugin in your pom.xml. Build may not work properly: " + getAntBuildFileName());
      }
      else
      {
         props.load(new FileInputStream(propsFile));
      }

      return props;
   }

   protected File getTestWorkDir()
   {
      return new File(workDirRoot, getTestName());
   }

   protected String getAntBuildFileName()
   {
      return getTestName() + "-build.xml";
   }

   protected File findAntBuildFileForTest() throws IOException
   {
      if (fileInited)
         return antBuildFile;

      fileInited = true;
      String fileName = getAntBuildFileName();
      InputStream is = getClass().getResourceAsStream(fileName);

      if (is == null)
         return null;

      File outDir = getTestWorkDir();
      File outFile = new File(outDir, fileName);
      outDir.mkdirs();
      copyAndClose(is, new FileOutputStream(outFile));

      antBuildFile = outFile;
      return outFile;
   }

   protected boolean expectAntBuildFile() throws IOException
   {
      File buildFile = findAntBuildFileForTest();

      if (buildFile == null)
      {
         log.warn("Test specific ant build file not found: " + getAntBuildFileName() + ".");
         return false;
      }
      return true;
   }

   protected Project prepareAntBuild() throws IOException
   {
      /*
       * Example of how to embed ant:
       * http://svn.apache.org/viewvc/maven/plugins/tags/maven-antrun-plugin-1.6/src/main/java/org/apache/maven/plugin/antrun/AntRunMojo.java?revision=1005612&view=markup
       *
       */
      File mavenHome = getMavenHome();
      if (mavenHome != null)
         System.setProperty("maven.home", mavenHome.getAbsolutePath());

      Project project = new Project();
      File buildFile = findAntBuildFileForTest();
      project.setUserProperty("ant.file", buildFile.getAbsolutePath());

      Properties props = getActiveMavenProperties();
      for (Map.Entry ent: props.entrySet())
      {
         String key = (String) ent.getKey();
         // build.xml properties override pom.xml properties
         if (project.getProperty(key) == null)
         {
            project.setInheritedProperty(key, (String) ent.getValue());
         }
      }

      project.init();
      ProjectHelper.configureProject(project, buildFile);
      
      DefaultLogger antLogger = new DefaultLogger();
      antLogger.setOutputPrintStream( System.out );
      antLogger.setErrorPrintStream( System.err );

      String loggingLevel = project.getProperty("ant.logging.level");
      antLogger.setMessageOutputLevel(getAntDebugLevel(loggingLevel));
      project.addBuildListener( antLogger );

      this.project = project;
      return project;
   }

   protected int getAntDebugLevel(String loggingLevel)
   {
      loggingLevel = loggingLevel.toUpperCase();
      if ("VERBOSE".equalsIgnoreCase(loggingLevel))
         return Project.MSG_VERBOSE;
      else if ("DEBUG".equals(loggingLevel))
         return Project.MSG_DEBUG;
      else if ("WARN".equals(loggingLevel))
         return Project.MSG_WARN;
      else if ("ERROR".equals(loggingLevel))
         return Project.MSG_ERR;
      else
         return Project.MSG_INFO;
   }

   protected void executeAntBuild()
   {
      if (project == null)
         throw new RuntimeException("Project not initialized. Call prepareAntBuild() first.");

      String target = project.getDefaultTarget();
      if (target == null)
         throw new RuntimeException("No default target specified in ant build file: " + antBuildFile);

      project.executeTarget(target);
   }

   protected void prepareAndExecuteAntBuild() throws IOException
   {
      prepareAntBuild();
      executeAntBuild();
   }
}
