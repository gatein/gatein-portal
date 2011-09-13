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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import org.exoplatform.container.ExoContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.management.gadget.mop.exportimport.client.DisplayableException;
import org.gatein.management.gadget.mop.exportimport.client.GateInService;
import org.gatein.management.gadget.mop.exportimport.client.TreeNode;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.gatein.management.gadget.mop.exportimport.server.ContainerRequestHandler.*;

/**
 * {@code GateInServiceImpl}
 * <p>
 * The {@code GateInService} remote servlet implementation.
 * </p>
 * Created on Jan 3, 2011, 12:30:45 PM
 *
 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 * @version 1.0
 */
public class GateInServiceImpl extends RemoteServiceServlet implements GateInService
{
   private static final Logger log = LoggerFactory.getLogger(GateInService.class);

   @Override
   protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName)
   {
      // Code taken from RemoteServiceServlet
      String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName("/exportimport/" + strongName);

      // Open the RPC resource file and read its contents.
      InputStream is = getServletContext().getResourceAsStream(serializationPolicyFilePath);
      try
      {
         if (is != null)
         {
            try
            {
               return SerializationPolicyLoader.loadFromStream(is, null);
            }
            catch (ParseException e)
            {
               log.error("Failed to parse the policy file '" + serializationPolicyFilePath + "'", e);
            }
            catch (IOException e)
            {
               log.error("Could not read the policy file '" + serializationPolicyFilePath + "'", e);
            }
         }
         else
         {
            String message = "ERROR: The serialization policy file '"
               + serializationPolicyFilePath
               + "' was not found; did you forget to include it in this deployment?";
            log.error(message);
         }
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               // Ignore this error
            }
         }
      }

      return null;
   }

   /**
    * Update the Tree item asynchronously
    *
    * @param containerName name of portal container
    * @param tn          The item to be updated
    * @return the updated tree node
    */
   public TreeNode updateItem(String containerName, TreeNode tn)
   {
      //TODO: Do we need this ?
      return tn;
   }

   /**
    * Retrieve asynchronously the list of root nodes
    *
    * @param containerName The portal container name
    * @return The list of the root nodes
    */
   public List<TreeNode> getRootNodes(String containerName) throws Exception
   {
      try
      {
         return doInRequest(containerName, new ContainerCallback<List<TreeNode>>()
         {
            public List<TreeNode> doInContainer(ExoContainer container) throws Exception
            {
               ManagementController controller = getComponent(container, ManagementController.class);
               List<TreeNode> nodes = new ArrayList<TreeNode>();
               nodes.add(getSiteTypeNode(controller, "portal"));
               nodes.add(getSiteTypeNode(controller, "group"));

               return nodes;
            }
         });
      }
      catch (Exception e)
      {
         log.error("Exception obtaining portal and group site names.", e);
         throw e;
      }
   }

   private TreeNode getSiteTypeNode(ManagementController controller, String siteType) throws Exception
   {
      try
      {
         ManagedRequest request = ManagedRequest.Factory.create(
            OperationNames.READ_RESOURCE,
            PathAddress.pathAddress("mop", siteType + "sites"),
            ContentType.JSON);

         ManagedResponse response = controller.execute(request);
         if (!response.getOutcome().isSuccess())
         {
            throw new Exception(response.getOutcome().getFailureDescription());
         }

         ReadResourceModel result = (ReadResourceModel) response.getResult();
         List<TreeNode> children = new ArrayList<TreeNode>(result.getChildren().size());
         for (String siteName : result.getChildren())
         {
            TreeNode siteNode = new TreeNode(siteName);
            siteNode.setExportable(true);
            siteNode.setType(siteType);
            siteNode.setSiteName(siteName);
            children.add(siteNode);
         }

         return new TreeNode(siteType, children);
      }
      catch (Exception e)
      {
         log.error("Exception getting site type " + siteType + " node.", e);
         throw e;
      }
   }
}
