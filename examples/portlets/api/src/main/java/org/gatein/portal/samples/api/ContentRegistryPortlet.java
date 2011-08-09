/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.portal.samples.api;

import org.gatein.api.GateIn;
import org.gatein.api.content.Category;
import org.gatein.api.content.ManagedContent;
import org.gatein.api.util.IterableCollection;
import org.gatein.api.util.IterableIdentifiableCollection;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.PrintWriter;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ContentRegistryPortlet extends GenericPortlet
{
   private GateIn gateIn;

   @Override
   public void init(PortletConfig config) throws PortletException
   {
      super.init(config);
      gateIn = (GateIn)config.getPortletContext().getAttribute(GateIn.GATEIN_API);
   }

   @Override
   protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      PrintWriter writer = response.getWriter();

      writer.println("<h1>Categories</h1>");

      writer.println("<ul>");
      IterableIdentifiableCollection<Category> categories = gateIn.getDefaultPortal().getContentRegistry().getAllCategories();
      for (Category category : categories)
      {
         writer.println("<li>");
         outputCategory(category, writer);
         writer.println("</li>");
      }
      writer.println("</ul>");
   }

   private void outputCategory(Category category, PrintWriter writer) throws IOException
   {
      writer.println("<h2>" + category.getDisplayName() + "</h2>");
      writer.println("<ul>");

      final IterableCollection<ManagedContent> managedContents = category.getManagedContents();
      for (ManagedContent managedContent : managedContents)
      {
         outputManagedContent(managedContent, writer);
      }

      writer.println("</ul><br/>");
   }

   private void outputManagedContent(ManagedContent content, PrintWriter writer) throws IOException
   {
      writer.println("<h3>" + content.getDisplayName() + "</h3>");
      writer.println("<h4>Name:" + content.getName() + "</h4>");
      writer.println("Content: " + content.getContent());
      writer.println("<br/>");
   }
}
