/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.controller.resource;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.LoggerErrorManager;
import com.google.javascript.jscomp.Result;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.exoplatform.web.controller.QualifiedName;
import org.gatein.common.io.IOTools;

import com.google.javascript.jscomp.Compiler;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ResourceRequestHandler extends WebRequestHandler
{

   /** . */
   private static String PATH = "META-INF/maven/org.exoplatform.portal/exo.portal.component.web.resources/pom.properties";

   /** . */
   private static final Logger log = LoggerFactory.getLogger(ResourceRequestHandler.class);

   /** . */
   public static final String VERSION;

   static
   {
      // Detecting version from maven properties
      // empty value is ok
      String version = "";
      URL url = ResourceRequestHandler.class.getClassLoader().getResource(PATH);
      if (url != null)
      {
         log.debug("Loading resource serving version from " + url);
         InputStream in = null;
         try
         {
            in = url.openStream();
            Properties props = new Properties();
            props.load(in);
            version = props.getProperty("version");
         }
         catch (IOException e)
         {
            log.error("Could not read properties from " + url, e);
         }
         finally
         {
            IOTools.safeClose(in);
         }
      }

      //
      log.info("Use version \"" + version + "\" for resource serving");
      VERSION = version;
   }
   
   /** . */
   public static final QualifiedName VERSION_QN  = QualifiedName.create("gtn", "version");

   /** . */
   public static final QualifiedName RESOURCE_QN = QualifiedName.create("gtn", "resource");

   /** . */
   public static final QualifiedName SCOPE_QN = QualifiedName.create("gtn", "scope");

   /** . */
   public static final QualifiedName MODULE_QN = QualifiedName.create("gtn", "module");

   /** . */
   public static final QualifiedName MINIFIED_QN = QualifiedName.create("gtn", "minified");

   @Override
   public String getHandlerName()
   {
      return "script";
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {

      JavascriptConfigService service = (JavascriptConfigService)PortalContainer.getComponent(JavascriptConfigService.class);

      String resourceParam = context.getParameter(RESOURCE_QN);
      String scopeParam = context.getParameter(SCOPE_QN);
      String moduleParam = context.getParameter(MODULE_QN);
      String minifiedParam = context.getParameter(MINIFIED_QN);

      //
      if (scopeParam != null && resourceParam != null)
      {
         try
         {
            ResourceScope scope = ResourceScope.valueOf(ResourceScope.class, scopeParam);
            ResourceId resource = new ResourceId(scope, resourceParam);
            
            //
            Reader script;
            String sourceName;
            if (moduleParam != null)
            {
               script = service.getScript(resource, moduleParam);
               sourceName = resource.getScope() + "/" + resource.getName() + "/" + moduleParam  + ".js";
            }
            else
            {
               script = service.getScript(resource);
               sourceName = resource.getScope() + "/" + resource.getName() + ".js";
            }

            //
            if (script != null)
            {
               HttpServletResponse response = context.getResponse();
               response.setContentType("application/x-javascript");
               Writer out = response.getWriter();
               
               //
               if ("true".equals(minifiedParam))
               {
                  CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
                  CompilerOptions options = new CompilerOptions();
                  level.setDebugOptionsForCompilationLevel(options);
                  Compiler compiler = new Compiler();
                  compiler.setErrorManager(new LoggerErrorManager(java.util.logging.Logger.getLogger(ResourceRequestHandler.class.getName())));
                  StringWriter code = new StringWriter();
                  IOTools.copy(script, code);
                  JSSourceFile[] inputs = new JSSourceFile[]{
                     JSSourceFile.fromCode(sourceName, code.toString())
                  };
                  Result res = compiler.compile(new JSSourceFile[0], inputs, options);
                  if (res.success)
                  {
                     out.write(compiler.toSource());
                  }
                  else
                  {
                     throw new UnsupportedOperationException("handle me gracefuylly");
                  }
               }
               else
               {
                  IOTools.copy(script, out);
               }

               //
               return true;
            }
            else
            {
               // What should we do ?
            }
         }
         catch (IllegalArgumentException e)
         {
            // Not found
         }
      }
      
      //
      return false;
   }
   
   @Override
   protected boolean getRequiresLifeCycle()
   {
      return false;
   }
}
