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

import org.exoplatform.Constants;
import org.exoplatform.portal.application.PortalRequestContext;
import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MarkupInfo;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class ExoPortletInvocationContext extends AbstractPortletInvocationContext
{

   private HttpServletResponse response;

   private HttpServletRequest request;
   
   private String portalRequestURI;
   
   private String portletId;

   public ExoPortletInvocationContext(PortalRequestContext portalRequestContext, UIPortlet portlet)
   {
      super(new MarkupInfo(MediaType.create("text/html"), "UTF-8"));

      this.request = portalRequestContext.getRequest();
      this.response = portalRequestContext.getResponse();
      this.portalRequestURI = portalRequestContext.getRequestURI();
      this.portletId = portlet.getId();
   }

   @Override
   public HttpServletRequest getClientRequest() throws IllegalStateException
   {
      return request;
   }

   @Override
   public HttpServletResponse getClientResponse() throws IllegalStateException
   {
      return response;
   }

   public String encodeResourceURL(String url) throws IllegalArgumentException
   {
      return response.encodeURL(url);
   }

   public MarkupInfo getMarkupInfo()
   {
      return this.markupInfo;
   }

   public String renderURL(ContainerURL containerURL, URLFormat format)
   {
	   StringBuilder baseURL =
	         new StringBuilder(this.portalRequestURI).append("?").append(
	            PortalRequestContext.UI_COMPONENT_ID).append("=").append(this.portletId);
	   
      String type;
      if (containerURL instanceof RenderURL)
      {
         type = "render";
      }
      else if (containerURL instanceof ResourceURL)
      {
         type = "resource";
      }
      else if (containerURL instanceof ActionURL)
      {
         type = "action";
      }
      else
      {
         throw new Error("Unrecognized containerURL type");
      }

      baseURL.append("&portal:type=").append(type).append("&portal:isSecure=").append(request.isSecure());

      if (containerURL instanceof ActionURL)
      {
         ActionURL actionURL = (ActionURL)containerURL;

         StateString state = actionURL.getInteractionState();
         if (state != null)
         {
            String value = state.getStringValue();
            if (value != null)
            {
               baseURL.append("&").append("interactionstate").append("=").append(value);
            }
         }
      }
      else if (containerURL instanceof ResourceURL)
      {
         ResourceURL resourceURL = (ResourceURL)containerURL;

         String resourceId = resourceURL.getResourceId();
         if (resourceId != null)
         {
            baseURL.append("&").append(Constants.RESOURCE_ID_PARAMETER).append("=").append(resourceId);
         }

         CacheLevel cachability = resourceURL.getCacheability();
         if (cachability != null && cachability.name() != null)
         {
            baseURL.append("&").append(Constants.CACHELEVEL_PARAMETER).append("=").append(cachability.name());
         }

         StateString resourceState = resourceURL.getResourceState();
         if (resourceState != null)
         {
            String value = resourceState.getStringValue();
            if (value != null)
            {
               baseURL.append("&").append("resourcestate").append("=").append(value);
            }
         }
      }
      else
      {
         RenderURL renderURL = (RenderURL)containerURL;

         WindowState windowState = renderURL.getWindowState();
         if (windowState != null && windowState.toString() != null)
         {
            baseURL.append("&").append(Constants.WINDOW_STATE_PARAMETER).append("=").append(windowState.toString());
         }

         Mode mode = renderURL.getMode();
         if (mode != null && mode.toString() != null)
         {
            baseURL.append("&").append(Constants.PORTLET_MODE_PARAMETER).append("=").append(mode);
         }

         Map<String, String[]> publicNSChanges = renderURL.getPublicNavigationalStateChanges();
         if (publicNSChanges != null)
         {
            for (String key : publicNSChanges.keySet())
            {
               String[] values = publicNSChanges.get(key);
               for (String value : values)
               {
                  baseURL.append("&").append(key).append("=").append(value);
               }
            }
         }
      }

      return baseURL.toString();
   }
}
