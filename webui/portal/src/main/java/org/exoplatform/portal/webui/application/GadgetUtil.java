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

package org.exoplatform.portal.webui.application;

import org.apache.commons.io.IOUtils;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.gadget.core.SecurityTokenGenerator;
import org.exoplatform.portal.webui.util.Util;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by The eXo Platform SAS Author : Pham Thanh Tung
 * thanhtungty@gmail.com Oct 2, 2008
 */
public class GadgetUtil
{
   static public Gadget toGadget(String name, String path, boolean isLocal) throws Exception
   {
      Gadget gadget = new Gadget();
      gadget.setName(name);
      gadget.setUrl(path);
      gadget.setLocal(isLocal);
      Map<String, String> metaData = getMapMetadata(reproduceUrl(path, isLocal));
      if (metaData.containsKey("errors"))
         throw new Exception("error on the server: " + metaData.get("errors"));
      String title = metaData.get("directoryTitle");
      if (title == null || title.trim().length() < 1)
         title = metaData.get("title");
      if (title == null || title.trim().length() < 1)
         title = gadget.getName();
      gadget.setTitle(title);
      gadget.setDescription(metaData.get("description"));
      gadget.setReferenceUrl(metaData.get("titleUrl"));
      gadget.setThumbnail(metaData.get("thumbnail"));
      return gadget;
   }

   /**
    * Fetchs Metatada of gadget application, create the connection to shindig
    * server to get the metadata TODO cache the informations for better
    * performance
    * 
    * @return the string represents metadata of gadget application
    */
   @Deprecated
   public static String fetchGagdetMetadata(String urlStr)
   {
      String result = null;

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService =
         (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      try
      {
         String data =
            "{\"context\":{\"country\":\"" + gadgetService.getCountry() + "\",\"language\":\""
               + gadgetService.getLanguage() + "\"},\"gadgets\":[" + "{\"moduleId\":" + gadgetService.getModuleId()
               + ",\"url\":\"" + urlStr + "\",\"prefs\":[]}]}";

         // Send data
         String gadgetServer = getGadgetServerUrl();
         URL url = new URL(gadgetServer + (gadgetServer.endsWith("/") ? "" : "/") + "metadata");
         URLConnection conn = url.openConnection();
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
         // Get the response
         result = IOUtils.toString(conn.getInputStream(), "UTF-8");
         wr.close();
      }
      catch (IOException ioexc)
      {
         ioexc.printStackTrace();
         return "{}";
      }
      return result;
   }

   /**
    * Fetchs Metatada of gadget application, create the connection to shindig
    * server to get the metadata TODO cache the informations for better
    * performance
    *
    * @return the string represents metadata of gadget application
    */
   public static String fetchGagdetRpcMetadata(String urlStr)
   {
      String result = null;

      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService =
         (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      try
      {
         String data = "[{method:\"gadgets.metadata\", id:\"test\", params: {ids:[\""
        	   + urlStr + "\"], container:\"default\", language:\""
        	   + gadgetService.getLanguage() + "\", country:\"" + gadgetService.getCountry() + "\", view:\"home\"}}]";

         // Send data
         String gadgetServer = getGadgetServerUrl();
         URL url = new URL(gadgetServer + (gadgetServer.endsWith("/") ? "" : "/") + "api/rpc");
         URLConnection conn = url.openConnection();
         conn.setRequestProperty("Content-Type", "application/json");
         conn.setDoOutput(true);
         OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
         wr.write(data);
         wr.flush();
         // Get the response
         result = IOUtils.toString(conn.getInputStream(), "UTF-8");
         wr.close();
      }
      catch (IOException ioexc)
      {
         ioexc.printStackTrace();
         return "{}";
      }
      return result;
   }

   public static String createToken(String gadgetURL, Long moduleId)
   {
      SecurityTokenGenerator tokenGenerator =
         (SecurityTokenGenerator)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(
            SecurityTokenGenerator.class);
      return tokenGenerator.createToken(gadgetURL, moduleId);
   }

   /**
    * Gets map metadata of gadget application
    * 
    * @return map metadata of gadget application so can get value of metadata by
    *         it's key such as title, url
    * @throws JSONException if can't create jsonObject from metadata
    */
   @SuppressWarnings("unchecked")
   static public Map<String, String> getMapMetadata(String url) throws JSONException
   {
      Map<String, String> mapMetaData = new HashMap<String, String>();
      String metadata = fetchGagdetRpcMetadata(url);
      metadata = metadata.substring(metadata.indexOf("[") + 1, metadata.lastIndexOf("]"));
      JSONObject jsonObj = new JSONObject(metadata).getJSONObject(UIGadget.RPC_RESULT).getJSONObject(url).getJSONObject(UIGadget.METADATA_MODULEPREFS);
      Iterator<String> iter = jsonObj.keys();
      while (iter.hasNext())
      {
         String element = iter.next();
         mapMetaData.put(element, jsonObj.get(element).toString());
      }
      return mapMetaData;
   }

   static public String reproduceUrl(String path, boolean isLocal)
   {
      if (isLocal)
      {
         return getViewPath(path);
      }
      return path;
   }

   static public String getViewPath(String uri)
   {
      return getLocalHostBase() + "/" + PortalContainer.getCurrentRestContextName() + "/" + uri;
   }

   static public String getEditPath(String uri)
   {
      return getLocalHostBase() + "/" + PortalContainer.getCurrentRestContextName() + "/private/" + uri;
   }

   /**
    * Make full URL of gadget server
    * @return URL String
    */
   public static String getGadgetServerUrl()
   {
      String uriString = getGadgetServerURI();
      try
      {
         new URL(uriString);
      }
      catch (Exception e)
      {
         return getLocalHostBase() + (uriString.startsWith("/") ? uriString : ("/" + uriString));
      }
      return uriString;
   }

   /**
    * See getGadgetServerUrl()
    * @return URL String
    */
   @Deprecated
   public static String getRelGadgetServerUrl()
   {
      return getGadgetServerUrl();
   }

   static private String getGadgetServerURI()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService =
         (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      return gadgetService.getHostName();
   }

   static private String getLocalHostBase()
   {
      HttpServletRequest request = Util.getPortalRequestContext().getRequest();
      return request.getScheme() + "://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
   }

}
