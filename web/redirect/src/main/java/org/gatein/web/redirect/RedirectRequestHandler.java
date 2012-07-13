/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.web.redirect;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.application.PortalRequestHandler;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.url.PortalURLContext;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebRequestHandler;
import org.exoplatform.web.url.URLFactoryService;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.redirect.api.SiteRedirectService;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectHandler;
import org.gatein.web.redirect.api.RedirectType;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class RedirectRequestHandler extends WebRequestHandler implements Startable
{
   protected static Logger log = LoggerFactory.getLogger(RedirectRequestHandler.class);

   //The handler name to use
   public static final String HANDLER_NAME = "siteRedirect";
   
   // The path to the device detection page
   protected String browserDetectionPath;
   
   // Redirection cookie settings
   protected Integer cookieMaxAge;
   protected String cookieComment;
   protected String cookiePath = "/portal";
   protected Boolean cookieSecure;
   
   // The service to perform the redirection logic
   protected SiteRedirectService deviceRedirectionService;
   
   protected URLFactoryService urlFactory;

   //Flag if we have already tried to detect capabilities of the browser
   public static final String DEVICE_DETECTION_ATTEMPTED = "gtn.device.detectionAttempted";

   //The initial URI that was requested
   public static final String INITIAL_URI = "gtn.redirect.initialURI";
   
   //TODO: the flag shouldn't exist here, it needs to exist somewhere else in the 'api' section
   public static final String REDIRECT_FLAG = "gtn_redirect";

   public RedirectRequestHandler(InitParams params, SiteRedirectService service, URLFactoryService urlFactory)
   {
      this.deviceRedirectionService = service;
      this.urlFactory = urlFactory;
      
      ValueParam browserDectionectUrl = params.getValueParam("browser.detection.path");

      if (browserDectionectUrl != null)
      {
         browserDetectionPath = browserDectionectUrl.getValue();
      }
      
      ValueParam cookieMaxAgeValueParam = params.getValueParam("redirect.cookie.maxage");
      if (cookieMaxAgeValueParam != null)
      {
         this.cookieMaxAge = Integer.parseInt(cookieMaxAgeValueParam.getValue());
      }
      
      ValueParam cookieCommentValueParam = params.getValueParam("redirect.cookie.comment");
      if (cookieCommentValueParam != null)
      {
         this.cookieComment = cookieCommentValueParam.getValue();
      }
      
      ValueParam cookiePathValueParam = params.getValueParam("redirect.cookie.path");
      if (cookiePathValueParam != null)
      {
         this.cookiePath = cookiePathValueParam.getValue();
      }
      
      ValueParam cookieSecureValueParam = params.getValueParam("redirect.cookie.secure");
      if (cookieSecureValueParam != null)
      {
         this.cookieSecure = Boolean.parseBoolean(cookieSecureValueParam.getValue());
      }
   }

   @Override
   public String getHandlerName()
   {
      return HANDLER_NAME;
   }

   @Override
   public boolean execute(ControllerContext context) throws Exception
   {
      RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
      try
      {
      HttpServletRequest request = context.getRequest();
      HttpServletResponse response = context.getResponse();
      
      String originRequestPath = context.getParameter(PortalRequestHandler.REQUEST_PATH);
      SiteKey originSite = getOriginSiteKey(context);
      
      if (originRequestPath != null && originRequestPath.equalsIgnoreCase("null"))
      {
         originRequestPath = null;
      }

      log.debug("Site Redirect being checked on [" + originSite.getName() + "], with type [" + originSite.getTypeName() 
            + "], and request path [" + originRequestPath + "]");
      
      String redirectFlagValue = request.getParameter(REDIRECT_FLAG);
      if (redirectFlagValue != null && !redirectFlagValue.isEmpty())
      {
         SiteKey redirectSiteKey = new SiteKey(originSite.getType(),redirectFlagValue);
         RedirectKey redirectKey =  RedirectKey.redirect(redirectSiteKey.getName());
         return performRedirect(originSite, redirectKey, originRequestPath, context, true);
      }
      
      String referer = request.getHeader("Referer");
      String siteURL = request.getRequestURL().substring(0, request.getRequestURL().length() - request.getServletPath().length());
      if (referer != null && referer.startsWith(siteURL) && (context.getRequest().getSession(true).getAttribute(DEVICE_DETECTION_ATTEMPTED) == null))
      {
         return false;
      }
      
      

      RedirectKey redirectSite = getRedirect(originSite, request);
      if (redirectSite != null) // a redirect has already been set, use it
      {
         // do the redirect
         return performRedirect(originSite, redirectSite, originRequestPath, context, false);
      }
      else
      // no redirect set yet, we need to check if a redirect is requested or not
      {
         Map<String, String> deviceProperties = null;

         String userAgentString = request.getHeader("User-Agent");
         log.debug("Found user-agent string : " + userAgentString);

         //we only care if this exists or not, no need to set it to anything other than Object
         Object attemptedDeviceDetection = context.getRequest().getSession(true).getAttribute(DEVICE_DETECTION_ATTEMPTED);
         if (attemptedDeviceDetection != null)
         {
            deviceProperties = getDeviceProperties(request);
            context.getRequest().getSession().removeAttribute(DEVICE_DETECTION_ATTEMPTED);
            log.debug("Found device properties : " + deviceProperties);
         }

         redirectSite = deviceRedirectionService.getRedirectSite(originSite.getName(), userAgentString, deviceProperties);

         if (redirectSite == null || redirectSite.getType() == RedirectType.NOREDIRECT)
         {
            log.debug("Redirect returned is null or NO_REDIRECT_DETECTED. Setting NO_REDIRECT for this user");
            setRedirect(originSite, RedirectKey.noRedirect(), response);
            return false;
         }
         else if (redirectSite.getType() == RedirectType.NEEDDEVICEINFO)
         {
            if (attemptedDeviceDetection == null)
            {
               log.debug("Need browser properties detection. Redirecting to BrowserDetectionPage : "
                     + browserDetectionPath);
               request.getSession().setAttribute(DEVICE_DETECTION_ATTEMPTED, true);
               performRedirectToDeviceDetector(request, response);
               return true;
            }
            else
            {
               log.warn("DeviceDetectionService retruned NEED_BROWSER_DETECTION but the browser has already attempted dection. Setting no redirect.");
               setRedirect(originSite, RedirectKey.noRedirect(), response);
               return false;
            }
         }
         else
         // the service gave us a redirection site to use, use it.
         {
            log.debug("Redirect for origin site " + originSite.getName() + " is being set to : " + redirectSite);
            return performRedirect(originSite, redirectSite, originRequestPath, context, false);
         }
      }
      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   protected RedirectKey getRedirect(SiteKey origin, HttpServletRequest request)
   {
         return RedirectCookie.getRedirect(origin, request);
   }
   
   protected void setRedirect(SiteKey origin, RedirectKey redirect, HttpServletResponse response)
   {    
        RedirectCookie redirectCookie = new RedirectCookie(origin.getName(), redirect);
        
        if (cookieMaxAge != null)
        {
           redirectCookie.setMaxAge(cookieMaxAge);
        }
        
        if (cookieComment != null)
        {
           redirectCookie.setComment(cookieComment);
        }
        
        if (cookiePath != null)
        {
           redirectCookie.setPath(cookiePath);
        }
        
        response.addCookie(redirectCookie.toCookie());
        
        if (redirect.getType() != RedirectType.NOREDIRECT && redirect.getRedirect() != null)
        {
           RedirectCookie removeRedirectCookie = new RedirectCookie(redirect.getRedirect(), RedirectKey.noRedirect());
           
           if (cookieMaxAge != null)
           {
              removeRedirectCookie.setMaxAge(cookieMaxAge);
           }
           
           if (cookieComment != null)
           {
              removeRedirectCookie.setComment(cookieComment);
           }
           
           if (cookiePath != null)
           {
              removeRedirectCookie.setPath(cookiePath);
           }
           
           response.addCookie(removeRedirectCookie.toCookie());
        }
   }

   protected Map<String, String> getDeviceProperties(HttpServletRequest request)
   {
      Map<String, String> parameterMap = request.getParameterMap();
      if (parameterMap != null)
      {
         Map<String, String> deviceProperties = new HashMap<String, String>();

         for (String key : parameterMap.keySet())
         {
            if (key.startsWith("gtn.device."))
            {
               deviceProperties.put(key.substring("gtn.device.".length()), request.getParameter(key));
            }
         }
         return deviceProperties;
      }
      else
      {
         return null;
      }
   }

   protected boolean performRedirect(SiteKey origin, RedirectKey redirect, String requestPath,
         ControllerContext context, boolean forceRedirect) throws IOException
   {
      // If we have a no-redirect type, don't do anything and return null
      if (redirect.getType() == RedirectType.NOREDIRECT)
      {
         log.debug("Using NoRedirect for site " + redirect + " with request path :" + requestPath);
         return false;
      }
      else
      {
         log.debug("Attempting redirect to site " + redirect + " with request path :" + requestPath);
         String redirectLocation = deviceRedirectionService.getRedirectPath(origin.getName(), redirect.getRedirect(), requestPath);
         
         if (forceRedirect && redirectLocation == null)
         {
            redirectLocation = requestPath;
         }
         
         if (redirectLocation != null)
         {
            log.debug("RedirectPath set to : " + redirectLocation);

            setRedirect(origin, redirect, context.getResponse());

            //create the new redirect url
            SiteKey siteKey = new SiteKey(SiteType.PORTAL, redirect.getRedirect());
            PortalURLContext urlContext = new PortalURLContext(context, siteKey);
            NodeURL url = urlFactory.newURL(NodeURL.TYPE, urlContext);
            String s = url.setResource(new NavigationResource(SiteType.PORTAL, redirect.getRedirect(), redirectLocation)).toString();
            
            HttpServletResponse response = context.getResponse();
            HttpServletRequest request = context.getRequest();
            
            //Add in the query string, if any.
            String queryString = request.getQueryString();
            if (request.getQueryString() != null)
            {
               //remove the redirect flag from the query string
               if (queryString.contains(REDIRECT_FLAG + "="))
               {
                  queryString = queryString.substring(0, queryString.lastIndexOf(REDIRECT_FLAG));
               }
               
               if (s.endsWith("/"))
               {
                  s = s.substring(0, s.length() - 1);
               }
               
               if (queryString != null && !queryString.isEmpty())
               {
                  s += "?" + queryString;
               }
            }
            
            //set the redirect
            response.sendRedirect(response.encodeRedirectURL(s));
            return true;
         }
         else
         {
            log.debug("Did not get a node match for redirecting to site [" + redirect + "] with requestPath ["
                  + requestPath + "]. Cannot perform redirect.");
            return false;
         }
      }
   }
   
   protected void performRedirectToDeviceDetector(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      String initialURI = request.getRequestURI();
      // add back in any query strings to the initialURI
      if (request.getQueryString() != null)
      {
         if (initialURI.endsWith("/"))
         {
            initialURI = initialURI.substring(0, initialURI.length() - 1);
         }
         initialURI += "?" + request.getQueryString();
      }

      request.getSession().setAttribute(INITIAL_URI, initialURI);
      request.setAttribute(INITIAL_URI, initialURI);
      request.getRequestDispatcher(browserDetectionPath).forward(request, response);
   }
   
   protected SiteKey getOriginSiteKey(ControllerContext context)
   {
      String originSiteName = context.getParameter(PortalRequestHandler.REQUEST_SITE_NAME);
      String originSiteTypeString = context.getParameter(PortalRequestHandler.REQUEST_SITE_TYPE);
      
      SiteType originSiteType;
      if (originSiteTypeString.equals(SiteType.GROUP.getName()))
      {
         originSiteType = SiteType.GROUP;
      }
      else if (originSiteTypeString.equals(SiteType.USER.getName()))
      {
         originSiteType = SiteType.USER;
      }
      else
      {
         originSiteType = SiteType.PORTAL;
      }
      
      return new SiteKey(originSiteType, originSiteName);
   }

   @Override
   protected boolean getRequiresLifeCycle()
   {
      // Do nothing for now
      return false;
   }

   @Override
   public void start()
   {
      //required because of eXo kernel
   }

   @Override
   public void stop()
   {
      //required because of eXo kernel
   }

}
