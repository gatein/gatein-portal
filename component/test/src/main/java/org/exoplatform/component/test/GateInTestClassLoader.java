/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.component.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

/**
 * The GateIn test classloader overrides the <code>getResources(String)</code> method to filter the resources
 * returned by the parent classloader in the following manner:
 * <ul>
 * <li>The loading of the <code>conf/configuration.xml</code> resource is replaced by the the configuration units
 * scoped at {@link org.exoplatform.component.test.ContainerScope#ROOT}.</li>
 * <li>The loading of the <code>conf/portal/configuration.xml</code> resource is replaced by the the configuration units
 * scoped at {@link org.exoplatform.component.test.ContainerScope#PORTAL}.</li>
 * </ul>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class GateInTestClassLoader extends ClassLoader
{

   /** . */
   private final Set<String> rootConfigPaths;

   /** . */
   private final Set<String> portalConfigPaths;

   public GateInTestClassLoader(ClassLoader parent, Set<String> rootConfigPaths, Set<String> portalConfigPaths)
   {
      super(parent);

      //
      this.rootConfigPaths = rootConfigPaths;
      this.portalConfigPaths = portalConfigPaths;
   }

   @Override
   public Enumeration<URL> getResources(String name) throws IOException
   {
      System.out.println("name = " + name);
      if ("conf/configuration.xml".equals(name))
      {
         return getURLs(rootConfigPaths);
      }
      else if ("conf/portal/configuration.xml".equals(name))
      {
         return getURLs(portalConfigPaths);
      }
      else if ("conf/portal/test-configuration.xml".equals(name))
      {
         return new Vector<URL>().elements();
      }
      else
      {
         return super.getResources(name);
      }
   }

   private Enumeration<URL> getURLs(Set<String> paths) throws IOException
   {
      ArrayList<URL> urls = new ArrayList<URL>();
      for (String path : paths)
      {
         urls.addAll(Collections.list(super.getResources(path)));
      }
      return Collections.enumeration(urls);
   }
}
