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

package org.gatein.portal.samples.api;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ResourceURL;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * This portlet shows how to leverage the portlet URL <code>gtn:lang</code> property to set the language
 * for render urls.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class LocaleURLPortlet extends GenericPortlet
{

   @Override
   public void serveResource(ResourceRequest req, ResourceResponse resp) throws PortletException, IOException
   {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      String namespace = "n_" + resp.getNamespace();
      ResourceURL resourceURL = resp.createResourceURL();
      resourceURL.setCacheability(ResourceURL.PAGE);
      PortletURL renderURL = resp.createRenderURL();
      Locale current = req.getLocale();

      //
      writer.print(
         "<html>" +
         "<head>" +
         "<script type='text/javascript'>" +
         "function openLinkInParent(){" +
         "var e = document.getElementById(\"" + namespace + "\");" +
         "var url = e.options[e.selectedIndex].value;" +
         "window.open(url,'" + namespace + "_parent');" +
         "window.focus();" +
         "}\n");

      writer.print(
         "</script>" +
         "</head>" +
         "<body>");

      writer.print("<p>Selecting a language will update the main portal window with the language in URL</p>");

      writer.print("<p><form action=\"javascript:openLinkInParent()\">");
      writer.print("<select id=\"" + namespace + "\">");
      renderURL.setProperty("gtn:lang", "");
      writer.print("<option value='" + renderURL + "'>&nbsp;</option>");
      for (String lang : new String[]{"en","fr","it","vi"})
      {
         renderURL.setProperty("gtn:lang", lang);
         writer.print("<option value=\"" + renderURL + "\">" + new Locale(lang).getDisplayName(current) + "</option>");
      }
      writer.print("</select>");
      writer.print("<input type='submit' value=\"Change\"/>");
      writer.print("</form></p>");
      writer.print("</body></html>");
   }

   @Override
   protected void doView(RenderRequest req, RenderResponse resp) throws PortletException, IOException
   {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();
      Locale current = req.getLocale();
      ResourceURL resource = resp.createResourceURL();
      resource.setCacheability(ResourceURL.PAGE);
      String namespace = "n_" + resp.getNamespace();
      String remoteWindowName = namespace + "_remote";

      writer.print(
         "<script type='text/javascript'>" +
         "var " + remoteWindowName + "; function " + namespace + "_popup(url){" +
         "window.name='" + namespace + "_parent';" +
         "window.open(url, '" + remoteWindowName + "', 'width=256,height=128,scrollable=yes')" +
         "}" +
         "onload = function() {" +
         "if (typeof " + remoteWindowName + " != 'undefined') " +
         "{" + remoteWindowName + ".location.reload(true);" +
         "}" +
         "}" +
         "</script>");

      writer.print(
         "<p><a href='#' onclick=\"" + namespace + "_remote=" + namespace + "_popup('" + resource + "')\">" +
         "Language controller</a></p>");

      writer.print("<p>Current locale is " + current.getDisplayName(current) + "</p>");

      //
      writer.close();
   }
}
