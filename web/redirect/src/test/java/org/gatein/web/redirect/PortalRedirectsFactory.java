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

import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.config.model.UserAgentConditions;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class PortalRedirectsFactory
{

   public static PortalRedirect createSimpleUASRedirect(String[] contains, String[] doesNotContain)
   {
      PortalRedirect portalRedirect = createSimpleRedirect();
      
      RedirectCondition redirectCondition = createRedirectCondition("conditionA");

      redirectCondition.setUserAgentConditions(createUserAgentConditionContains(contains, doesNotContain));
      
      ArrayList<RedirectCondition> conditions = createRedirectConditions(redirectCondition);
      portalRedirect.setConditions(conditions);
      
      return portalRedirect;
   }
   
   public static PortalRedirect createSimpleContainsRedirect(String redirectSiteName, String contains)
   {
      PortalRedirect portalRedirect = createPortalRedirect("Redirect For " + redirectSiteName, redirectSiteName, true);
      RedirectCondition redirectCondition = createRedirectCondition("conditionA");
      
      String[] uasContains = {contains};
      redirectCondition.setUserAgentConditions(createUserAgentConditionContains(uasContains, null));
      
      ArrayList<RedirectCondition> conditions = createRedirectConditions(redirectCondition);
      portalRedirect.setConditions(conditions);
      
      return portalRedirect;
   }
   
   public static PortalRedirect createSimpleRedirectAcceptAllUAS()
   {
      String[] contains = {".*"};
      return createSimpleUASRedirect(contains, null);
   }
   
   public static PortalRedirect createSimpleRedirectAcceptNoUASA()
   {
      String[] contains = null;
      String[] doesNotContain = {".*"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectAcceptNoUASB()
   {
      String[] contains = {".*"};
      String[] doesNotContain = {".*"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectAcceptNoUASC()
   {
      String[] contains = {"foo", "TestBrowser", "abc"};
      String[] doesNotContain = {".*"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectAcceptNoUASD()
   {
      String[] contains = {"foo", "TestBrowser", "abc", ".*"};
      String[] doesNotContain = {".*"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectRejectTestBrowserUAS()
   {
      String[] contains = {"foo", "TestBrowser", "abc", ".*"};
      String[] doesNotContain = {"TestBrowser"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectRejectTestBrowserExactUAS()
   {
      String[] contains = {"foo", "TestBrowser", "abc", ".*"};
      
      //note the escaped brackets
      String[] doesNotContain = {"^Mozilla/5.0 \\(X11; Linux x86_64\\) AppleWebKit/500.0 \\(KHTML, like Gecko\\) TestBrowser/1.2.3 Safari/500.0$"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   
   public static PortalRedirect createSimpleRedirectAcceptTestBrowserUAS()
   {
      String[] contains = {"foo", "TestBrowser", "abc"};
      String[] doesNotContain = {"bar", "hello", "world"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   public static PortalRedirect createSimpleRedirectAcceptTestBrowserExactUAS()
   {
      //note the escaped brackets
      String[] contains = {"foo","abc", "^Mozilla/5.0 \\(X11; Linux x86_64\\) AppleWebKit/500.0 \\(KHTML, like Gecko\\) TestBrowser/1.2.3 Safari/500.0$"};
      String[] doesNotContain = {"bar", "hello", "world"};
      return createSimpleUASRedirect(contains, doesNotContain);
   }
   
   
   public static PortalRedirect createSimpleDeviceRedirect(String propertyName, String equals, String matches, Float greaterThan, Float lessThan)
   {
      DevicePropertyCondition deviceProperty = createDevicePropertyCondition(propertyName, equals, matches, greaterThan, lessThan);
      return createSimpleDevicePropertiesRedirect(deviceProperty);
   }
  
   public static PortalRedirect createSimpleNullDeviceRedirect()
   {
      return createSimpleDevicePropertiesRedirect(null);
   }
   
   public static PortalRedirect createSimpleDevicePropertiesRedirect(DevicePropertyCondition... devicePropertyConditions)
   {
      PortalRedirect portalRedirect = createSimpleRedirect();
      
      RedirectCondition redirectCondition = createRedirectCondition("conditionA");
      String[] contains = {".*"};
      redirectCondition.setUserAgentConditions(createUserAgentConditionContains(contains, null));
      
      ArrayList<DevicePropertyCondition> devicePropertyConditionsList = null;
      if (devicePropertyConditions != null)
      {
         devicePropertyConditionsList = new ArrayList<DevicePropertyCondition>();
         for (DevicePropertyCondition deviceProperty: devicePropertyConditions)
         {
            devicePropertyConditionsList.add(deviceProperty);
         }
      }
      redirectCondition.setDeviceProperties(devicePropertyConditionsList);
      
      ArrayList<RedirectCondition> conditions = createRedirectConditions(redirectCondition);
      portalRedirect.setConditions(conditions);
      
      return portalRedirect;
   }
   
   public static PortalRedirect createSimpleRedirect()
   {
      PortalRedirect portalRedirect = createPortalRedirect("acceptAllUASPortalRedirect", "redirectSite", true);
      return portalRedirect;
   }
   
   public static ArrayList<PortalRedirect> createPortalRedirects(PortalRedirect... portalRedirectValues)
   {
      if (portalRedirectValues != null)
      {
         ArrayList<PortalRedirect> portalRedirects = new ArrayList<PortalRedirect>();
         for (PortalRedirect portalRedirect : portalRedirectValues)
         {
            portalRedirects.add(portalRedirect);
         }
         return portalRedirects;
      }
      else
      {
         return null;
      }
   }
   
   public static PortalRedirect createPortalRedirect(String name, String redirectSite, boolean enabled)
   {
      PortalRedirect portalRedirect = new PortalRedirect();
      
      portalRedirect.setName(name);
      portalRedirect.setRedirectSite(redirectSite);
      portalRedirect.setEnabled(enabled);
      portalRedirect.setMappings(null);
      portalRedirect.setConditions(null);
      
      return portalRedirect;
   }
   
   public static ArrayList<RedirectCondition> createRedirectConditions(RedirectCondition... redirectConditionValues)
   {
      if (redirectConditionValues != null)
      {
         ArrayList<RedirectCondition> redirectConditions = new ArrayList<RedirectCondition>();
         for (RedirectCondition redirectCondition : redirectConditionValues)
         {
            redirectConditions.add(redirectCondition);
         }
         return redirectConditions;
      }
      else
      {
         return null;
      }
   }
   
   public static RedirectCondition createRedirectCondition(String name)
   {
      RedirectCondition redirectCondition = new RedirectCondition();
      
      redirectCondition.setName(name);
      redirectCondition.setDeviceProperties(null);
      redirectCondition.setUserAgentConditions(null);
      
      return redirectCondition;
   }
   
   public static UserAgentConditions createUserAgentConditionContains(String[] contains, String[] doesNotContain)
   {
      UserAgentConditions userAgentCondition = new UserAgentConditions();
      
      if (contains != null)
      {
         userAgentCondition.setContains(new ArrayList<String>());
         for (String value : contains)
         { 
            userAgentCondition.getContains().add(value);
         }
      }
      
      if (doesNotContain != null)
      {
         userAgentCondition.setDoesNotContain(new ArrayList<String>());
         for (String value : doesNotContain)
         { 
            userAgentCondition.getDoesNotContain().add(value);
         }
      }
      
      return userAgentCondition;
   }

   public static DevicePropertyCondition createDevicePropertyCondition(String propertyName, String equals, String matches, Float greaterThan, Float lessThan)
   {
     DevicePropertyCondition devicePropertyCondition = new DevicePropertyCondition();
   
     if (propertyName != null)
     {
        devicePropertyCondition.setPropertyName(propertyName);
     }
   
     if (equals != null)
     {
        devicePropertyCondition.setEquals(equals);
     }
   
     if (matches != null)
     {
        devicePropertyCondition.setMatches(matches);
     }
   
     if (greaterThan != null)
     {
        devicePropertyCondition.setGreaterThan(greaterThan);
     }
   
     if (lessThan != null)
     {
        devicePropertyCondition.setLessThan(lessThan);
     }
   
     return devicePropertyCondition;
   }

}

