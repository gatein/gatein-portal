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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
      String metadata = fetchGagdetMetadata(url);
      metadata = metadata.substring(metadata.indexOf("[") + 1, metadata.lastIndexOf("]"));
      JSONObject jsonObj = new JSONObject(metadata);
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

   //  TODO: TanPham:Replace by getGadgetServerUrl to make server url
   //  static private String getHostBase() {
   //    String hostName = getHostName();
   //    URL url = null;
   //    try {
   //       url = new URL(hostName);
   //    } catch (Exception e) {}
   //    if(url == null) return hostName ;
   //    int index = hostName.indexOf(url.getPath()) ;
   //    if(index < 1) return hostName ;
   //    return hostName.substring(0, index) ;
   //  }
   public static String getGadgetServerUrl()
   {
      String hostName = getHostName();
      try
      {
         new URL(hostName);
      }
      catch (Exception e)
      {
         try
         {
            String newHostName = getLocalHostName() + "/" + hostName;
            new URL(newHostName);
            hostName = newHostName;
         }
         catch (Exception e2)
         {
         }
      }
      return hostName;
   }

   //  TODO: Using in gtmpl templates
   public static String getRelGadgetServerUrl()
   {
      String url = getGadgetServerUrl();
      String localHostBase = getLocalHostBase();
      int index = url.indexOf(localHostBase);
      if (index >= 0)
         return url.substring(index + localHostBase.length());
      return url;
   }

   static private String getLocalHostBase()
   {
      String hostName = getLocalHostName();
      URL url = null;
      try
      {
         url = new URL(hostName);
      }
      catch (Exception e)
      {
      }
      if (url == null)
         return hostName;
      int index = hostName.indexOf(url.getPath());
      if (index < 1)
         return hostName;
      return hostName.substring(0, index);
   }

   static private String getHostName()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService =
         (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      return gadgetService.getHostName();
   }

   static private String getLocalHostName()
   {
      PortalRequestContext pContext = Util.getPortalRequestContext();
      StringBuffer requestUrl = pContext.getRequest().getRequestURL();
      int index = requestUrl.indexOf(pContext.getRequestContextPath());
      return requestUrl.substring(0, index);
   }

}
