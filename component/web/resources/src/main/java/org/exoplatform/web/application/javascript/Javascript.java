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

package org.exoplatform.web.application.javascript;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.controller.resource.Resource;
import org.gatein.common.io.IOTools;

import javax.servlet.ServletContext;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class Javascript
{

   public static Javascript create(Resource scope, String module, String path, String contextPath, int priority)
   {
      if (path.startsWith("http://") || path.startsWith("https://"))
      {
         return new External(scope, module, contextPath, path, priority);
      }
      else
      {
         return new Simple(scope, module, contextPath, path, priority);
      }
   }

   /** . */
   protected final Resource resource;
   
   /** . */
   protected final String contextPath;

   /** . */
   protected final String module;

   /** . */
   private final int priority;
   
   private Javascript(Resource resource, String module, String contextPath, int priority)
   {
      this.resource = resource;
      this.contextPath = contextPath;
      this.module = module;
      this.priority = priority < 0 ? Integer.MAX_VALUE : priority;
   }

   public abstract String getPath();

   public Resource getResource()
   {
      return resource;
   }

   public String getModule()
   {
      return module;
   }

   public String getContextPath()
   {
      return contextPath;
   }

   public int getPriority()
   {
      return priority;
   }
   
   public abstract boolean isExternalScript();
   
   @Override
   public String toString()
   {
      return "Javascript[scope=" + resource + ", path=" + getPath() +"]";
   }

   public static class External extends Javascript
   {

      /** . */
      private final String uri;

      public External(Resource scope, String module, String contextPath, String uri, int priority)
      {
         super(scope, module, contextPath, priority);

         //
         this.uri = uri;
      }

      @Override
      public String getPath()
      {
         return uri;
      }

      @Override
      public boolean isExternalScript()
      {
         return true;
      }
   }

   public abstract static class Internal extends Javascript
   {

      protected Internal(Resource scope, String module, String contextPath, int priority)
      {
         super(scope, module, contextPath, priority);
      }
      
      protected abstract InputStream open(JavascriptConfigService context) throws IOException;
   }

   public static class Simple extends Internal
   {

      /** . */
      private final String path;

      /** . */
      private final String uri;

      public Simple(Resource scope, String module, String contextPath, String path, int priority)
      {
         super(scope, module, contextPath, priority);

         //
         this.path = path;
         this.uri = contextPath + path;
      }

      @Override
      public String getPath()
      {
         return uri;
      }

      @Override
      public boolean isExternalScript()
      {
         return false;
      }

      @Override
      protected InputStream open(JavascriptConfigService context) throws IOException
      {
         ServletContext servletContext = context.getContext(contextPath);
         return servletContext != null ? servletContext.getResourceAsStream(path) : null;
      }
   }

   public static class Composite extends Internal
   {

      final ArrayList<Javascript> compounds;

      public Composite(Resource scope, String contextPath, int priority)
      {
         super(scope, null, contextPath, priority);
         
         //
         this.compounds = new ArrayList<Javascript>();
      }

      @Override
      public String getPath()
      {
         return resource.getScope().name() + "/" + resource.getId() + "/merged.js";
      }

      @Override
      public boolean isExternalScript()
      {
         return false;
      }
      
      public Javascript getCompound(String module)
      {
         for (Javascript compound : compounds)
         {
            if (compound.getModule().equals(module))
            {
               return compound;
            }
         }
         return null;
      }

      @Override
      protected InputStream open(JavascriptConfigService context) throws IOException
      {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         for (Javascript compound : compounds)
         {
            if (compound instanceof Internal)
            {
               InputStream in = ((Internal)compound).open(context);
               if (in != null)
               {
                  IOTools.copy(in, buffer);
                  buffer.write('\n');
               }
            }
            else
            {
               // We skip it for now
            }
         }
         return new ByteArrayInputStream(buffer.toByteArray());
      }
   }
}
