/**
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.portal.gadget.server;

import org.apache.commons.io.IOUtils;
import org.apache.shindig.common.PropertiesModule;
import org.apache.shindig.common.cache.ehcache.EhCacheModule;
import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.servlet.AuthenticationModule;
import org.apache.shindig.gadgets.servlet.ConcatProxyServlet;
import org.apache.shindig.gadgets.servlet.GadgetRenderingServlet;
import org.apache.shindig.gadgets.servlet.JsServlet;
import org.apache.shindig.gadgets.servlet.MakeRequestServlet;
import org.exoplatform.portal.gadget.core.ExoModule;
import org.exoplatform.portal.gadget.core.ExoOAuthModule;
import org.exoplatform.portal.gadget.core.GateInGuiceServletContextListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.resource.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

/**
 * Suite for running the end-to-end tests. The suite is responsible for starting up and shutting
 * down the server.
 *
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
public class EndToEndServer
{
   private static final int JETTY_PORT = 9003;

   private static final String GADGET_BASE = "/eXoGadgetServer/gadgets/ifr";

   private static final String CONCAT_BASE = "/eXoGadgetServer/gadgets/concat";

   private static final String JS_BASE = "/eXoGadgetServer/gadgets/js/*";

   private static final String MAKE_REQUEST_BASE = "/eXoGadgetServer/gadgets/makeRequest";

   public static final String SERVER_URL = "http://localhost:" + JETTY_PORT;

   public static final String GADGET_BASEURL = SERVER_URL + GADGET_BASE;

   private final Server server;

   /** Fake error code for data service servlet request */
   protected int errorCode;

   /** Fake error message for data service servlet request */
   protected String errorMessage;

   public EndToEndServer() throws Exception
   {
      server = createServer(JETTY_PORT);
   }

   public void start() throws Exception
   {
      server.start();
   }

   public void stop() throws Exception
   {
      server.stop();
   }

   public void clearDataServiceError()
   {
      errorCode = 0;
   }

   public void setDataServiceError(int errorCode, String errorMessage)
   {
      this.errorCode = errorCode;
      this.errorMessage = errorMessage;
   }

   /**
    * Create the server for end-to-end tests.
    */
   private Server createServer(int port) throws Exception
   {
      System.setProperty("shindig.port", String.valueOf(port));
      System.setProperty("jetty.port", String.valueOf(port));
      System.setProperty("gatein.gadgets.securitytokenkeyfile", "src/test/resources/conf/gadgets/key.txt");

      Server newServer = new Server(port);

      // Attach the test resources in /endtoend as static content for the test
      ResourceHandler resources = new ResourceHandler();
      URL resource = EndToEndTest.class.getResource("/endtoend");
      resources.setBaseResource(Resource.newResource(resource));
      newServer.addHandler(resources);

      Context context = new Context(newServer, "/", Context.SESSIONS);
      context.addEventListener(new GateInGuiceServletContextListener());

      Map<String, String> initParams = Maps.newHashMap();
      String modules =
         Joiner.on(":").join(ExoModule.class.getName(), ExoOAuthModule.class.getName(),
            DefaultGuiceModule.class.getName(), AuthenticationModule.class.getName(), PropertiesModule.class.getName(),
            EhCacheModule.class.getName());

      initParams.put(GateInGuiceServletContextListener.MODULES_ATTRIBUTE, modules);
      context.setInitParams(initParams);

      // Attach the gadget rendering servlet
      ServletHolder gadgetServletHolder = new ServletHolder(new GadgetRenderingServlet());
      context.addServlet(gadgetServletHolder, GADGET_BASE);

      // Attach the ConcatProxyServlet - needed for rewritten JS
      ServletHolder concatHolder = new ServletHolder(new ConcatProxyServlet());
      context.addServlet(concatHolder, CONCAT_BASE);

      // Attach the JsServlet - needed for rewritten JS
      ServletHolder jsHolder = new ServletHolder(new JsServlet());
      context.addServlet(jsHolder, JS_BASE);

      // Attach MakeRequestServlet
      ServletHolder makeRequestHolder = new ServletHolder(new MakeRequestServlet());
      context.addServlet(makeRequestHolder, MAKE_REQUEST_BASE);

      // Attach an EchoServlet, used to test proxied rendering
      ServletHolder echoHolder = new ServletHolder(new EchoServlet());
      context.addServlet(echoHolder, "/echo");

      return newServer;
   }

   static private class EchoServlet extends HttpServlet
   {

      @Override
      protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
      {
         req.setCharacterEncoding("UTF-8");
         resp.setContentType(req.getContentType());

         IOUtils.copy(req.getReader(), resp.getWriter());
      }
   }
}
