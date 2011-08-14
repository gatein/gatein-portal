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

package org.exoplatform.web.handler;

import org.exoplatform.container.ExoContainer;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.WebRequestHandler;

import java.io.Writer;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Dec 9, 2006  
 */
public class UploadHandler extends WebRequestHandler
{

   static enum UploadServiceAction {
      PROGRESS, UPLOAD, DELETE, ABORT
   }

   public String getHandlerName()
   {
      return "upload";
   }

   @Override
   public void execute(ControllerContext context) throws Exception
   {
      execute(context.getController(), context.getRequest(), context.getResponse());
   }

   public void execute(WebAppController controller, HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      String action = req.getParameter("action");
      String[] uploadIds = req.getParameterValues("uploadId");
      
      res.setHeader("Cache-Control", "no-cache");

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      UploadService service = (UploadService)container.getComponentInstanceOfType(UploadService.class);
      if (action == null || action.length() < 1)
         return;

      UploadServiceAction uploadActionService = UploadServiceAction.valueOf(action.toUpperCase());
      if (uploadActionService == UploadServiceAction.PROGRESS)
      {
         Writer writer = res.getWriter();
         if (uploadIds == null)
            return;
         StringBuilder value = new StringBuilder();
         value.append("{\n  upload : {");
         for (int i = 0; i < uploadIds.length; i++)
         {
            UploadResource upResource = service.getUploadResource(uploadIds[i]);
            if (upResource == null)
               continue;
            if (upResource.getStatus() == UploadResource.FAILED_STATUS)
            {
               
               int limitMB = service.getUploadLimitsMB().get(uploadIds[i]).intValue();
               value.append("\n    \"").append(uploadIds[i]).append("\": {");
               value.append("\n      \"status\":").append('\"').append("failed").append("\",");
               value.append("\n      \"size\":").append('\"').append(limitMB).append("\"");
               value.append("\n    }");
               continue;
            }
            double percent = 100;
            if (upResource.getStatus() == UploadResource.UPLOADING_STATUS)
            {
               percent = (upResource.getUploadedSize() * 100) / upResource.getEstimatedSize();
            }
            value.append("\n    \"").append(uploadIds[i]).append("\": {");
            value.append("\n      \"percent\":").append('\"').append((int)percent).append("\",");
            value.append("\n      \"fileName\":").append('\"').append(encodeName(upResource.getFileName()))
               .append("\"");
            value.append("\n    }");
            if (i < uploadIds.length - 1)
               value.append(',');
         }
         value.append("\n  }\n}");
         writer.append(value);
      }
      else if (uploadActionService == UploadServiceAction.UPLOAD)
      {
         service.createUploadResource(req);
      }
      else if (uploadActionService == UploadServiceAction.DELETE)
      {
         service.removeUploadResource(uploadIds[0]);
      }
      else if (uploadActionService == UploadServiceAction.ABORT)
      {
         service.removeUploadResource(uploadIds[0]);
      }
   }

   public String encodeName(String name) throws Exception
   {
      String[] arr = name.split(" ");
      String str = "";
      for (int i = 0; i < arr.length; i++)
      {
         str += " " + URLEncoder.encode(arr[i], "UTF-8");
      }
      return str;
   }

}