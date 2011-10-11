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
import org.exoplatform.commons.utils.I18N;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.web.url.MimeType;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.url.ComponentURL;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
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

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class ExoPortletInvocationContext extends AbstractPortletInvocationContext
{

   static final String INTERACTION_STATE_PARAM_NAME = "interactionstate";
   static final String NAVIGATIONAL_STATE_PARAM_NAME = "navigationalstate";
   static final String RESOURCE_STATE_PARAM_NAME = "resourcestate";

   /** . */
   private static final Logger log = LoggerFactory.getLogger(ExoPortletInvocationContext.class);

   /** . */
   private final HttpServletResponse response;

   /** . */
   private final HttpServletRequest request;

   /** . */
   private final PortalURL<UIComponent, ComponentURL> url;

   public ExoPortletInvocationContext(PortalRequestContext portalRequestContext, UIPortlet portlet)
   {
      super(new MarkupInfo(MediaType.TEXT_HTML, "UTF-8"));

      //
      ComponentURL url = portalRequestContext.createURL(ComponentURL.TYPE, portlet);
      String path = portalRequestContext.getNodePath();
      url.setPath(path);

      //
      this.request = portalRequestContext.getRequest();
      this.response = portalRequestContext.getResponse();
      this.url = url;
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
/*
      // todo: shouldn't we be using URLFormat to decide on the path to use at the beginning of the URL?
      StringBuilder baseURL = new StringBuilder(this.portalRequestURI).append("?")
         .append(PortalRequestContext.UI_COMPONENT_ID).append("=").append(this.portletId);
*/
      
      //Clear URL parameters
      Map<String, String[]> queryParameters = url.getQueryParameters();
      if (queryParameters != null)
      {
         queryParameters.clear();
      }

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
         url.setQueryParameterValue(Constants.TYPE_PARAMETER, type);
      }

      if (format != null && format.getWantSecure() != null)
      {
         url.setQueryParameterValue(Constants.SECURE_PARAMETER, format.getWantSecure().toString());
      }

      if (containerURL instanceof ActionURL)
      {
         ActionURL actionURL = (ActionURL)containerURL;

         StateString state = actionURL.getInteractionState();
         if (state != null && !state.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            url.setQueryParameterValue(INTERACTION_STATE_PARAM_NAME, state.getStringValue());
         }

         state = actionURL.getNavigationalState();
         if (state != null && !state.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            url.setQueryParameterValue(NAVIGATIONAL_STATE_PARAM_NAME, state.getStringValue());
         }

         WindowState windowState = actionURL.getWindowState();
         if (windowState != null)
         {
            url.setQueryParameterValue(Constants.WINDOW_STATE_PARAMETER, windowState.toString());
         }

         Mode mode = actionURL.getMode();
         if (mode != null)
         {
            url.setQueryParameterValue(Constants.PORTLET_MODE_PARAMETER, mode.toString());
         }
      }
      else if (containerURL instanceof ResourceURL)
      {
         ResourceURL resourceURL = (ResourceURL)containerURL;

         url.setQueryParameterValue(Constants.RESOURCE_ID_PARAMETER, resourceURL.getResourceId());

         CacheLevel cachability = resourceURL.getCacheability();
         if (cachability != null)
         {
            url.setQueryParameterValue(Constants.CACHELEVEL_PARAMETER, cachability.name());
         }

         StateString resourceState = resourceURL.getResourceState();
         if (resourceState != null && !resourceState.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            url.setQueryParameterValue(RESOURCE_STATE_PARAM_NAME, resourceState.getStringValue());
         }

         resourceState = resourceURL.getNavigationalState();
         if (resourceState != null && !resourceState.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            url.setQueryParameterValue(NAVIGATIONAL_STATE_PARAM_NAME, resourceState.getStringValue());
         }

         WindowState windowState = resourceURL.getWindowState();
         if (windowState != null)
         {
            url.setQueryParameterValue(Constants.WINDOW_STATE_PARAMETER, windowState.toString());
         }

         Mode mode = resourceURL.getMode();
         if (mode != null)
         {
            url.setQueryParameterValue(Constants.PORTLET_MODE_PARAMETER, mode.toString());
         }
      }
      else
      {
         RenderURL renderURL = (RenderURL)containerURL;

         WindowState windowState = renderURL.getWindowState();
         if (windowState != null)//&& !windowState.equals(WindowState.NORMAL))
         {
            url.setQueryParameterValue(Constants.WINDOW_STATE_PARAMETER, windowState.toString());
         }

         Mode mode = renderURL.getMode();
         if (mode != null)
         {
            url.setQueryParameterValue(Constants.PORTLET_MODE_PARAMETER, mode.toString());
         }

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
                     url.setQueryParameterValue(key, value);
                  }
               }
               else
               {
                  url.setQueryParameterValue("removePP", key);
               }
            }
         }

         StateString state = renderURL.getNavigationalState();
         if (state != null && !state.getStringValue().equals(StateString.JBPNS_PREFIX))
         {
            url.setQueryParameterValue(NAVIGATIONAL_STATE_PARAM_NAME, state.getStringValue());
         }
      }

      //
      Map<String, String> props = containerURL.getProperties();
      String lang = props.get("gtn:lang");
      if (lang != null)
      {
         if (lang.length() == 0)
         {
            url.setLocale(null);
         }
         else
         {
            try
            {
               Locale locale = I18N.parseJavaIdentifier(lang);
               url.setLocale(locale);
            }
            catch (IllegalArgumentException e)
            {
               log.debug("Unparsable locale string: " + lang, e);
            }
         }
      }

      // Ajax support
      url.setAjax("true".equals(props.get("gtn:ajax")));

      // Confirm messsage
      url.setConfirm(props.get("gtn:confirm"));

      //
      MimeType mimeType = Boolean.TRUE.equals(format.getWantEscapeXML()) ? MimeType.XHTML : MimeType.PLAIN;
      url.setMimeType(mimeType);

      //
      return url.toString();
   }
}
