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
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.LoggerErrorManager;
import com.google.javascript.jscomp.Result;
import org.exoplatform.commons.utils.CharsetTextEncoder;
import org.exoplatform.commons.utils.I18N;
import org.exoplatform.commons.utils.Safe;
import org.exoplatform.commons.utils.TextEncoder;
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
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
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
   public static final QualifiedName COMPRESS_QN = QualifiedName.create("gtn", "compress");

   /** . */
   public static final QualifiedName LANG_QN = QualifiedName.create("gtn", "lang");

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
      String compressParam = context.getParameter(COMPRESS_QN);
      String lang = context.getParameter(LANG_QN);
      
      //
      Locale locale = null;
      if (lang != null && lang.length() > 0)
      {
         locale = I18N.parseTagIdentifier(lang);
      }

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
               script = service.getScript(resource, moduleParam, locale);
               sourceName = resource.getScope() + "/" + resource.getName() + "/" + moduleParam  + ".js";
            }
            else
            {
               script = service.getScript(resource, locale);
               sourceName = resource.getScope() + "/" + resource.getName() + ".js";
            }

            //
            if (script != null)
            {
               HttpServletResponse response = context.getResponse();

               // Content type + charset
               response.setContentType("text/javascript");
               response.setCharacterEncoding("UTF-8");

               // One hour caching
               // make this configurable later
               response.setHeader("Cache-Control", "max-age:3600");
               response.setDateHeader("Expires", System.currentTimeMillis() + 3600 * 1000);

               //
               if ("min".equals(compressParam))
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
                     script = new StringReader(compiler.toSource());
                  }
                  else
                  {
                     StringBuilder msg = new StringBuilder("Handle me gracefuylly JS errors\n");
                     for (JSError error : res.errors)
                     {
                        msg.append(error.sourceName).append(":").append(error.lineNumber).append(" ").append(error.description).append("\n");
                     }
                     throw new UnsupportedOperationException(msg.toString());
                  }
               }

               // Encode data
               OutputStream out = response.getOutputStream();
               try
               {
                  TextEncoder encoder = CharsetTextEncoder.getUTF8();
                  char[] buffer = new char[256];
                  for (int l = script.read(buffer);l != -1;l = script.read(buffer))
                  {
                     encoder.encode(buffer, 0, l, out);
                  }
               }
               finally
               {
                  Safe.close(out);
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
