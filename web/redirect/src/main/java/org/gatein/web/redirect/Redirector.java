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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.config.model.UserAgentConditions;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectType;

/**
 * Determines what site, if any, should be used for a redirect.
 * Bases this decisions on the portal's redirect conditions and the device properties available.
 * 
 * TODO: create an interface for this and configure the service using the kernel
 * TODO: update the tests for this to use portal.xml files instead of creating the configurations manually in code
 * 
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Redirector
{  
   protected static Logger log = LoggerFactory.getLogger(Redirector.class);

   public RedirectKey getRedirectSite(ArrayList<PortalRedirect> portalRedirects, String userAgentString, Map<String, String> deviceProperties)
   {
      if (userAgentString == null)
      {
         //if the uas is null, then just transform it into 'null'. This allows the admin to configure how to handle the 'null' the null case easily.
         //since the uas is user modifiable, setting it to whatever we want here will not cause any problems.
         userAgentString = "null";
      }
      
      boolean needsBrowserDetection = false;
      
      if (portalRedirects != null && !portalRedirects.isEmpty())
      {
         for (PortalRedirect redirect : portalRedirects)
         {
            if (redirect.isEnabled())
            {
               RedirectType redirectCondition = checkConditions(userAgentString, deviceProperties, redirect.getConditions());
               if (redirectCondition == RedirectType.REDIRECT)
               {
                  log.debug("Found a match with UAS " + userAgentString + " and DeviceProperties " + deviceProperties
                        + ". Seting redirect to : " + redirect.getRedirectSite());

                  return RedirectKey.redirect(redirect.getRedirectSite());
               }
               else if (redirectCondition == RedirectType.NEEDDEVICEINFO)
               {
                  needsBrowserDetection = true;
               }
            }
         }

         if (needsBrowserDetection == false)
         {
            log.debug("Could not find a match with the specifed uas and device properties. Returning NO_REDIRECT_DETECTED");
            return RedirectKey.noRedirect();
         }
         else
         {
            log.debug("Found a match with the specifed uas but it requires device properties. Returning NO_REDIRECT_DETECTED");
            return RedirectKey.needDeviceInfo();
         }
      }
      else
      {
         log.debug("No UserAgentString specified and no device properties. Returning NO_REDIRECT_DETECTED");
         return RedirectKey.noRedirect();
      }
   }

   protected RedirectType checkConditions(String userAgentString, Map<String, String> deviceProperties,
         List<RedirectCondition> conditions)
   {
      log.debug("Checking conditions for redirect with " + userAgentString + " and device properties "
            + deviceProperties);
      if (conditions != null)
      {
         for (RedirectCondition condition : conditions)
         {
            boolean userAgentStringMatch = checkUserAgentStrings(userAgentString, condition.getUserAgentConditions());
            log.debug("UserAgentStringMatch : " + userAgentStringMatch);
            if (userAgentStringMatch == true)
            {
               if (condition.getDeviceProperties() == null || condition.getDeviceProperties().isEmpty())
               {
                  log.debug("UserAgentStringMatch and no device detection has been specified. Using Redirect");
                  return RedirectType.REDIRECT;
               }
               else
               {
                  if (deviceProperties == null)
                  {
                     log.debug("Conditional device properties exists, but no deviceProperties available. Using Browser Detection");
                     return RedirectType.NEEDDEVICEINFO;
                  }
                  else
                  {
                     boolean devicePropertiesMatch = checkDeviceProperties(deviceProperties, condition.getDeviceProperties());
                     if (devicePropertiesMatch == true)
                     {
                        log.debug("UserAgentStringMatch and device properties match. Using Redirect");
                        return RedirectType.REDIRECT;
                     }
                     else
                     {
                        return RedirectType.NOREDIRECT;
                     }
                  }
               }
            }
         }
      }
      return RedirectType.NOREDIRECT;
   }

   protected boolean checkDeviceProperties(Map<String, String> deviceProperties, List<DevicePropertyCondition> conditions)
   {
      if (conditions != null && !conditions.isEmpty())
      {
         if (deviceProperties == null)
         {
            return false;
         }

         for (DevicePropertyCondition deviceProperty : conditions)
         {
            if (deviceProperties.containsKey(deviceProperty.getPropertyName()))
            {
               if (!checkProperty(deviceProperties.get(deviceProperty.getPropertyName()), deviceProperty))
               {
                  return false;
               }
            }
            else
            {
               return false;
            }
         }
      }
      return true; // if the deviceProperties are null or empty, then the condition is matched
   }

   protected boolean checkProperty(String propertyValue, DevicePropertyCondition deviceProperty)
   {
      if (deviceProperty.getGreaterThan() != null)
      {
         try
         {
            Float propertyValueFloat = Float.parseFloat(propertyValue);
            if (propertyValueFloat <= deviceProperty.getGreaterThan())
            {
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            log.debug("Encountered a NumberFormatException trying to parse a property value (" + propertyValue + ") which should be a float.");
            return false;
         }
      }

      if (deviceProperty.getLessThan() != null)
      {
         try
         {
            Float propertyValueFloat = Float.parseFloat(propertyValue);
            if (propertyValueFloat >= deviceProperty.getLessThan())
            {
               return false;
            }
         }
         catch (NumberFormatException nfe)
         {
            log.debug("Encountered a NumberFormatException trying to parse a property value (" + propertyValue + ") which should be a float.");
            return false;
         }
      }

      if (deviceProperty.getEquals() != null)
      {
         if (!propertyValue.equals(deviceProperty.getEquals()))
         {
            return false;
         }
      }

      if (deviceProperty.getMatches() != null)
      {
         try
         {
            Pattern pattern = Pattern.compile(deviceProperty.getMatches());
            if (propertyValue == null)
            {
              propertyValue = "";
            }
               Matcher matcher = pattern.matcher(propertyValue);
               if (!matcher.find())
               {
                  return false;
               }
            
         }
         catch (PatternSyntaxException pse)
         {
            log.debug("Encountered a PatternSyntaxException trying to compile a device property pattern (" + deviceProperty.getMatches() + ") which should be a valid string for a pattern.");
            return false;
         }
      }

      return true;
   }

   /**
    * Determines if the current user agent string passes the redirect conditions.
    * 
    * @param userAgentString The user agent string
    * @param condition The conditions to check against
    * @return True if the user agent string passes the conditions
    */
   protected boolean checkUserAgentStrings(String userAgentString, UserAgentConditions condition)
   {  
      // Check the black list first for user agent string matches, fail immediately if any match
      if (condition.getDoesNotContain() != null && !condition.getDoesNotContain().isEmpty())
      {
         if (userAgentContains(userAgentString, condition.getDoesNotContain()))
         {
            return false;
         }
      }

      if (condition.getContains() != null && !condition.getContains().isEmpty())
      {
         return userAgentContains(userAgentString, condition.getContains());
      }
      else
      {
         //if we have no contains or contains is empty, we can never match to this 
         return false;
      }
   }

   protected boolean userAgentContains(String userAgentString, List<String> contains)
   {
      if (userAgentString != null)
      {
         for (String contain : contains)
         {
            Pattern pattern = Pattern.compile(contain);
            Matcher matcher = pattern.matcher(userAgentString);
            if (matcher.find())
            {
               return true;
            }
         }
      }
      return false;
   }
}
