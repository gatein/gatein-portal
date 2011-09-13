/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.gatein.management.gadget.mop.exportimport.server;

import org.exoplatform.container.ExoContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.gatein.management.gadget.mop.exportimport.server.ContainerRequestHandler.*;

/**
 * {@code FileDownloadServlet}
 * <p>
 * The file download servlet. Used for export sites.
 * </p>
 * Created on Feb 3, 2011, 3:49:16 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class FileDownloadServlet extends HttpServlet
{

   private static final Logger log = LoggerFactory.getLogger(FileDownloadServlet.class);
   private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      String portalContainerName = request.getParameter("pc");
      final String type = request.getParameter("ownerType");
      final String name = request.getParameter("ownerId");
      String safeName = name.replaceAll("/", "-");
      if (safeName.startsWith("-"))
      {
         safeName = safeName.substring(1);
      }
      String filename = new StringBuilder(type).append("_").append(safeName).append("_").append(getTimestamp()).append(".zip").toString();

      response.setContentType("application/octet-stream; charset=UTF-8");
      response.setHeader("Content-disposition", "attachment; filename=\"" + filename + "\"");

      final OutputStream os = response.getOutputStream();
      try
      {
         doInRequest(portalContainerName, new ContainerCallback<Void>()
         {

            public Void doInContainer(ExoContainer container) throws Exception
            {
               ManagementController controller = getComponent(container, ManagementController.class);
               PathAddress address = PathAddress.pathAddress("mop", type+"sites", name);

               ManagedRequest request = ManagedRequest.Factory.create(OperationNames.EXPORT_RESOURCE, address, ContentType.ZIP);
               ManagedResponse response = controller.execute(request);
               if (response.getOutcome().isSuccess())
               {
                  response.writeResult(os);
               }
               else
               {
                  throw new Exception(response.getOutcome().getFailureDescription());
               }

               return null;
            }
         });
         os.flush();
      }
      catch (Exception e)
      {
         log.error("Error during download", e);
      }
      finally
      {
         if (os != null)
         {
            os.close();
         }
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      doGet(request, response);
   }

   private String getTimestamp()
   {
      return SDF.format(new Date());
   }
}
