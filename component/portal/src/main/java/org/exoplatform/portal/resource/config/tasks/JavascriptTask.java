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
package org.exoplatform.portal.resource.config.tasks;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.exoplatform.web.application.javascript.JavascriptConfigService;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class JavascriptTask
{
   
   private List<Parameter> parameters;
   
   public JavascriptTask(){
      parameters = new ArrayList<Parameter>();
   }
   
   public void execute(JavascriptConfigService service, ServletContext scontext){
      for(Parameter param : parameters){
         service.addJavascript(param.moduleName, param.scriptPath, scontext);
      }
   }
   
   public void addParam(String moduleName, String scriptPath){
      parameters.add(new Parameter(moduleName, scriptPath));
   }
   
   private class Parameter {
      
      private String moduleName;
      private String scriptPath;
      
      Parameter(String _moduleName, String _scriptPath){
         moduleName = _moduleName;
         scriptPath = _scriptPath;
      }     
   }
}
