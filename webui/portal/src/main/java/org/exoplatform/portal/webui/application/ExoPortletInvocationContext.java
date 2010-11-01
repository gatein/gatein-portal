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
import org.gatein.common.util.ParameterValidation;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

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

   static final String INTERACTION_STATE_PARAM_NAME = "interactionstate";
   static final String NAVIGATIONAL_STATE_PARAM_NAME = "navigationalstate";
   static final String RESOURCE_STATE_PARAM_NAME = "resourcestate";
   private static final String QMARK = "?";
   private static final String EQ = "=";
   private static final String AMP = "&";
   private static final String XMLAMP = "&amp;";

   public ExoPortletInvocationContext(PortalRequestContext portalRequestContext, UIPortlet portlet)
   {
      super(new MarkupInfo(MediaType.TEXT_HTML, "UTF-8"));

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
      boolean wantEscapeXML = false;
      if (format != null && format.getWantEscapeXML() != null)
      {
         wantEscapeXML = format.getWantEscapeXML();
      }

      // todo: shouldn't we be using URLFormat to decide on the path to use at the beginning of the URL?
      StringBuilder baseURL = new StringBuilder(this.portalRequestURI).append(QMARK)
         .append(PortalRequestContext.UI_COMPONENT_ID).append(EQ).append(this.portletId);

      String type;
      if (containerURL instanceof RenderURL)
      {
         type = Constants.PORTAL_RENDER;
      }
      else if (containerURL instanceof ResourceURL)
      {
         type = Constants.PORTAL_SERVE_RESOURCE;
      }
      else if (containerURL instanceof ActionURL)
      {
         type = Constants.PORTAL_PROCESS_ACTION;
      }
      else
      {
         throw new Error("Unrecognized containerURL type");
      }

      if (!type.equals(Constants.PORTAL_RENDER))
      {
         appendParameter(baseURL, Constants.TYPE_PARAMETER, type, wantEscapeXML);
      }

      if (format != null && format.getWantSecure() != null)
      {
         appendParameter(baseURL, Constants.SECURE_PARAMETER, format.getWantSecure().toString(), wantEscapeXML);
      }

      StateString navigationalState = containerURL.getNavigationalState();
      if (navigationalState != null && !navigationalState.getStringValue().equals(StateString.JBPNS_PREFIX))
      {
         appendParameter(baseURL, NAVIGATIONAL_STATE_PARAM_NAME, navigationalState.getStringValue(), wantEscapeXML);
      }

      WindowState windowState = containerURL.getWindowState();
      if (windowState != null)
      {
         appendParameter(baseURL, Constants.WINDOW_STATE_PARAMETER, windowState.toString(), wantEscapeXML);
      }

      Mode mode = containerURL.getMode();
      if (mode != null)
      {
         appendParameter(baseURL, Constants.PORTLET_MODE_PARAMETER, mode.toString(), wantEscapeXML);
      }

      if (containerURL instanceof ActionURL)
      {
         ActionURL actionURL = (ActionURL)containerURL;

         StateString state = actionURL.getInteractionState();
         if (state != null && !state.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            appendParameter(baseURL, INTERACTION_STATE_PARAM_NAME, state.getStringValue(), wantEscapeXML);
         }
      }
      else if (containerURL instanceof ResourceURL)
      {
         ResourceURL resourceURL = (ResourceURL)containerURL;

         appendParameter(baseURL, Constants.RESOURCE_ID_PARAMETER, resourceURL.getResourceId(), wantEscapeXML);

         CacheLevel cachability = resourceURL.getCacheability();
         if (cachability != null)
         {
            appendParameter(baseURL, Constants.CACHELEVEL_PARAMETER, cachability.name(), wantEscapeXML);
         }

         StateString resourceState = resourceURL.getResourceState();
         if (resourceState != null && !resourceState.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            appendParameter(baseURL, RESOURCE_STATE_PARAM_NAME, resourceState.getStringValue(), wantEscapeXML);
         }
      }
      else
      {
         RenderURL renderURL = (RenderURL)containerURL;

         Map<String, String[]> publicNSChanges = renderURL.getPublicNavigationalStateChanges();
         if (ParameterValidation.existsAndIsNotEmpty(publicNSChanges))
         {
            for (String key : publicNSChanges.keySet())
            {
               String[] values = publicNSChanges.get(key);
               if (values != null && values.length > 0)
               {
                  for (String value : values)
                  {
                     appendParameter(baseURL, key, value, wantEscapeXML);
                  }
               }
               else
               {
                  appendParameter(baseURL, "removePP", key, wantEscapeXML);
               }
            }
         }
      }

      return baseURL.toString();
   }

   private void appendParameter(StringBuilder builder, String name, String value, boolean wantEscapeXML)
   {
      if (value != null)
      {
         if (wantEscapeXML)
         {
            builder.append(XMLAMP).append(name).append(EQ).append(value);
         }
         else
         {
            builder.append(AMP).append(name).append(EQ).append(value);
         }
      }
   }
}
