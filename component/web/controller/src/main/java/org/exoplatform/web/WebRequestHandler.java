/**
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

package org.exoplatform.web;

import org.exoplatform.container.component.BaseComponentPlugin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS
 * Mar 21, 2007  
 * 
 * Abstract calss that one must implement if it want to provide a dedicated handler for a custom servlet path
 * 
 * In case of portal the path is /portal but you could return your own from the getPath() method and hence the 
 * WebAppController would use your own handler
 * 
 * The execute method is to be overrided and the buisness logic should be handled here
 */
abstract public class WebRequestHandler extends BaseComponentPlugin
{

   public void onInit(WebAppController controller) throws Exception
   {

   }

   abstract public String[] getPath();

   abstract public void execute(WebAppController app, HttpServletRequest req, HttpServletResponse res) throws Exception;

   public void onDestroy(WebAppController controler) throws Exception
   {

   }

}