/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
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
package org.gatein.version;

import org.gatein.integration.jboss.as7.Attribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

/**
 * Common GateIn version.
 *
 * @author Honza Fnukal
 */
public class Version {
   public static final String productName;
   public static final String productVersion;
   public static final String implementationVersion;
   public static final String prettyVersion;

   static {
      InputStream stream = Version.class.getClassLoader().getResourceAsStream("META-INF/MANIFEST.MF");
      Manifest manifest = null;
      if (stream != null) {
         try {
            manifest = new Manifest(stream);
         } catch (IOException e) {
            // manifest is null
         }
      }

      if (manifest != null) {
         productName = manifest.getMainAttributes().getValue("JBoss-Product-Release-Name");
         productVersion = manifest.getMainAttributes().getValue("JBoss-Product-Release-Version");
         implementationVersion = manifest.getMainAttributes().getValue("Implementation-Version");
      } else {
         productName = null;
         productVersion = "Unknown";
         implementationVersion = "Unknown";
      }
      String iVersion = implementationVersion==null?"Unknown":implementationVersion;
      String version = productVersion==null?iVersion:productVersion;
      if(productName==null) {
         prettyVersion = String.format("GateIn Portal %s", iVersion);
      } else {
         prettyVersion = String.format("%s %s (GateIn Portal %s)", productName, version, iVersion);
      }
   }
}
