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

package org.exoplatform.portal.application;

import org.exoplatform.Constants;
import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.commons.utils.PortalPrinter;
import org.exoplatform.commons.xml.DOMSerializer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.http.QueryStringParser;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class extends the abstract WebuiRequestContext which itself extends the RequestContext one
 * 
 * <p>It mainly implements the abstract methods and overide some.
 */
public class PortalRequestContext extends WebuiRequestContext
{

   protected static Log log = ExoLogger.getLogger("portal:PortalRequestContext");

   final static public int PUBLIC_ACCESS = 0;

   final static public int PRIVATE_ACCESS = 1;

   final static public String UI_COMPONENT_ACTION = "portal:action";

   final static public String UI_COMPONENT_ID = "portal:componentId";

   final static public String CACHE_LEVEL = "portal:cacheLevel";

   final static public String REQUEST_TITLE = "portal:requestTitle".intern();

   final static public String REQUEST_METADATA = "portal:requestMetadata".intern();

   private String portalOwner_;

   private String nodePath_;

   private String requestURI_;

   private String portalURI;

   private int accessPath = -1;

   private HttpServletRequest request_;

   private HttpServletResponse response_;

   private String cacheLevel_ = "cacheLevelPortlet";

   private boolean ajaxRequest_ = true;

   private boolean forceFullUpdate = false;

   private Writer writer_;

   protected JavascriptManager jsmanager_ = new JavascriptManager();

   private List<Element> extraMarkupHeaders;

   private final PortalURLBuilder urlBuilder;
   
   private Map<String, String[]> parameterMap;

   private Locale locale = Locale.ENGLISH;

   public JavascriptManager getJavascriptManager()
   {
      return jsmanager_;
   }

   public PortalRequestContext(WebuiApplication app, HttpServletRequest req, HttpServletResponse res) throws Exception
   {
      super(app);
      request_ = req;
      response_ = res;
      response_.setBufferSize(1024 * 100);
      setSessionId(req.getSession().getId());
      
      
      //The encoding needs to be set before reading any of the parameters since the parameters's encoding
      //is set at the first access.
      
      //TODO use the encoding from the locale-config.xml file
      response_.setContentType("text/html; charset=UTF-8");
      try
      {
         request_.setCharacterEncoding("UTF-8");
      }
      catch (UnsupportedEncodingException e)
      {
         log.error("Encoding not supported", e);
      }
      
      // Query parameters from the request will be set in the servlet container url encoding and not
      // necessarly in utf-8 format. So we need to directly parse the parameters from the query string.
      parameterMap = new HashMap<String, String[]>();
      parameterMap.putAll(request_.getParameterMap());
      String queryString = req.getQueryString();
      if (queryString != null)
      {
         //The QueryStringParser currently only likes & and not &amp;
         queryString = queryString.replace("&amp;", "&");
         Map<String, String[]> queryParams = QueryStringParser.getInstance().parseQueryString(queryString);
         parameterMap.putAll(queryParams);
      }
      
      ajaxRequest_ = "true".equals(req.getParameter("ajaxRequest"));
      String cache = req.getParameter(CACHE_LEVEL);
      if (cache != null)
      {
         cacheLevel_ = cache;
      }

      requestURI_ = req.getRequestURI();
      String decodedURI = URLDecoder.decode(requestURI_, "UTF-8");
      
      // req.getPathInfo will already have the encoding set from the server.
      // We need to use the UTF-8 value since this is how we store the portal name.
      // Reconstructing the getPathInfo from the non server decoded values.
      String servletPath = URLDecoder.decode(req.getServletPath(), "UTF-8");
      String contextPath = URLDecoder.decode(req.getContextPath(), "UTF-8");
      String pathInfo = "/";
      if (requestURI_.length() > servletPath.length() + contextPath.length())
         pathInfo = decodedURI.substring(servletPath.length() + contextPath.length());
      
      int colonIndex = pathInfo.indexOf("/", 1);
      if (colonIndex < 0)
      {
         colonIndex = pathInfo.length();
      }
      portalOwner_ = pathInfo.substring(1, colonIndex);
      nodePath_ = pathInfo.substring(colonIndex, pathInfo.length());

      portalURI = decodedURI.substring(0, decodedURI.lastIndexOf(nodePath_)) + "/";

      if (decodedURI.indexOf("/public/") >= 0)
      {
         accessPath = PUBLIC_ACCESS;
      }
      else if (decodedURI.indexOf("/private/") >= 0)
      {
         accessPath = PRIVATE_ACCESS;
      }

      urlBuilder = new PortalURLBuilder(requestURI_);
   }

   public void refreshResourceBundle() throws Exception
   {
      appRes_ = getApplication().getResourceBundle(getLocale());
   }

   public String getTitle() throws Exception
   {
      String title = (String)request_.getAttribute(REQUEST_TITLE);

      //
      if (title == null)
      {
         UIPortal uiportal = Util.getUIPortal();

         //
         PageNode node = uiportal.getSelectedNode();
         if (node != null)
         {
            ExoContainer container = getApplication().getApplicationServiceContainer();
            container.getComponentInstanceOfType(UserPortalConfigService.class);
            UserPortalConfigService configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
            Page page = configService.getPage(node.getPageReference(), getRemoteUser());

            //
            if (page != null)
            {
               title = page.getTitle();
               return ExpressionUtil.getExpressionValue(this.getApplicationResourceBundle(), title);
            }
            else
            {
               title = node.getResolvedLabel();
            }
         }
      }

      //
      return title;
   }

   public Orientation getOrientation()
   {
      return ((UIPortalApplication)uiApplication_).getOrientation();
   }

   public void setLocale(Locale locale)
   {
      this.locale = locale;
   }

   public Locale getLocale()
   {
      return locale;
   }

   @SuppressWarnings("unchecked")
   public Map<String, String> getMetaInformation()
   {
      return (Map<String, String>)request_.getAttribute(REQUEST_METADATA);
   }

   public String getCacheLevel()
   {
      return cacheLevel_;
   }

   public String getRequestParameter(String name)
   {
      if (parameterMap.get(name) != null && parameterMap.get(name).length > 0)
      {
         return parameterMap.get(name)[0];
      }
      else
      {
         return null;
      }
   }

   public String[] getRequestParameterValues(String name)
   {
      return parameterMap.get(name);
   }

   public Map<String, String[]> getPortletParameters()
   {
      Map<String, String[]> unsortedParams = parameterMap;
      Map<String, String[]> sortedParams = new HashMap<String, String[]>();
      Set<String> keys = unsortedParams.keySet();
      for (String key : keys)
      {
         if (!key.startsWith(Constants.PARAMETER_ENCODER))
         {
            sortedParams.put(key, unsortedParams.get(key));
         }
      }
      return sortedParams;
   }

   final public String getRequestContextPath()
   {
      return request_.getContextPath();
   }

   @Override
   public String getPortalContextPath()
   {
      return getRequestContextPath();
   }

   public String getActionParameterName()
   {
      return PortalRequestContext.UI_COMPONENT_ACTION;
   }

   public String getUIComponentIdParameterName()
   {
      return PortalRequestContext.UI_COMPONENT_ID;
   }

   public String getPortalOwner()
   {
      return portalOwner_;
   }

   public String getNodePath()
   {
      return nodePath_;
   }

   public String getRequestURI()
   {
      return requestURI_;
   }

   public String getPortalURI()
   {
      return portalURI;
   }

   public URLBuilder<UIComponent> getURLBuilder()
   {
      return urlBuilder;
   }

   public int getAccessPath()
   {
      return accessPath;
   }

   final public String getRemoteUser()
   {
      return request_.getRemoteUser();
   }

   final public boolean isUserInRole(String roleUser)
   {
      return request_.isUserInRole(roleUser);
   }

   final public Writer getWriter() throws Exception
   {
      if (writer_ == null)
      {
         writer_ = new PortalPrinter(response_.getOutputStream(), true, 30000);
      }
      return writer_;
   }
   
   final public void setWriter(Writer writer)
   {
	   this.writer_ = writer;
   }

   final public boolean useAjax()
   {
      return ajaxRequest_;
   }

   @SuppressWarnings("unchecked")
   final public HttpServletRequest getRequest()
   {
      return request_;
   }

   @SuppressWarnings("unchecked")
   final public HttpServletResponse getResponse()
   {
      return response_;
   }

   /**
    * 
    * @see org.exoplatform.web.application.RequestContext#getFullRender()
    */
   final public boolean getFullRender()
   {
      return forceFullUpdate;
   }

   /**
    * Sets a boolean value to force whether portal will be fully rendered
    * and it is only effective to an Ajax request.<p/>
    * 
    * if the value is set to <code>true</code>, it means :<br/>
    *
    * 1) Only portal ui components are rendered <br/>
    * 2) Portlets will be fully rendered if are inner of the portal ui components being updated
    * 
    * @param forceFullUpdate
    * 
    * This method is deprecated, ignoreAJAXUpdateOnPortlets should be used instead
    */
   @Deprecated()
   final public void setFullRender(boolean forceFullUpdate)
   {
      this.forceFullUpdate = forceFullUpdate;
   }
   
   /**
    * Call to this method makes sense only in the scope of an AJAX request.
    * 
    *   Invoking ignoreAJAXUpdateOnPortlets(true) as there is need to update only UI components 
    * of portal (ie: the components outside portlet windows) are updated by AJAX. In the request
    * response, all the blocks <PortletRespond > are empty. The content displayed in portlet 
    * windows are retrieved by non-AJAX render request to associated portlet object.  
    * 
    * 
    * @param ignoreAJAXUpdateOnPortlets
    */
   final public void ignoreAJAXUpdateOnPortlets(boolean ignoreAJAXUpdateOnPortlets)
   {
      this.forceFullUpdate = ignoreAJAXUpdateOnPortlets;
   }

   final public void sendRedirect(String url) throws IOException
   {
      setResponseComplete(true);
      response_.sendRedirect(url);
   }

   public void setHeaders(Map<String, String> headers)
   {
      Set<String> keys = headers.keySet();
      for (Iterator<String> iter = keys.iterator(); iter.hasNext();)
      {
         String key = iter.next();
         response_.setHeader(key, headers.get(key));
      }
   }
   
   public List<String> getExtraMarkupHeadersAsStrings() throws Exception
   {
      List<String> markupHeaders = new ArrayList<String>();
      if (extraMarkupHeaders != null && !extraMarkupHeaders.isEmpty())
      {
         for (Element element : extraMarkupHeaders)
         {
            StringWriter sw = new StringWriter();
            DOMSerializer.serialize(element, sw);
            markupHeaders.add(sw.toString());
         }
      }
      return markupHeaders;
   }
   
   /**
    * Get the extra markup headers to add to the head of the html.
    * @return The markup to be added.
    */
   public List<Element> getExtraMarkupHeaders()
   {
	   return this.extraMarkupHeaders;
   }
   
   /**
    * Add an extra markup to the head of the html page.
    * @param element The element to add
    * @param portletWindowId The ID of portlet window contributing markup header
    */
   public void addExtraMarkupHeader(Element element, String portletWindowId)
   {
      element.setAttribute("class", "ExHead-" + portletWindowId);
	  if (this.extraMarkupHeaders == null)
	  {
		  this.extraMarkupHeaders = new ArrayList<Element>();
	  }
	  this.extraMarkupHeaders.add(element);
   }


}
