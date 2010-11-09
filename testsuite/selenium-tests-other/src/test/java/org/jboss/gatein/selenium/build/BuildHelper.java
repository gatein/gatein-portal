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

package org.jboss.gatein.selenium.build;

import org.apache.tools.ant.taskdefs.Execute;

import java.io.File;
import java.util.List;

public class BuildHelper
{

   public static File getMavenHome()
   {

      String mavenHome = null;

      List<String> env = Execute.getProcEnvironment();
      for (String var : env)
      {
         if (var.startsWith("M2_HOME="))
         {
            mavenHome = var.substring("M2_HOME=".length());
            break;
         }
      }

      if (mavenHome != null)
      {
         File mFile = new File(mavenHome);
         if (!mFile.isDirectory())
         {
            mavenHome = System.getProperty("maven.home");
            if (mavenHome != null)
            {
               mFile = new File(mavenHome);
            }
         }
         if (mFile.isDirectory())
         {
            return mFile;
         }
      }
      return null;
   }
}