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
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.config.model.UserAgentConditions;
import org.gatein.web.redirect.Redirector;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectType;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestRedirector extends TestCase
{
   
   /**
    * Configuration Elements
    * - String userAgent
    * - Map<String, String> deviceProperties (propertyName, propertyValue)
    * - List<PortalRedirect>
    *   - String redirectSite name
    *   - String redirectName
    *   - Boolean enabled
    *   - RedirectMappings (not used in the Redirector)
    *   - List<RedirectCondition>
    *     - String Name
    *     - UserAgentConditions
    *       - List<String> contains
    *       - List<String> does-not-contain
    *     - List<PropertyCondition>
    *       - propertyName
    *       - greaterThan
    *       - lessThan
    *       - equals
    *       - matches 
    */
   
   /**
    * What to test
    * - ordering of the redirects
    * 
    * - null and empty value situations (to make sure no NPE or other errors). The service has been designed (hopefully) to not fail or
    *   throw an exception in these cases, so needs to be a bit tricky in how we test it.
    * 
    * - enabled and disabled redirects work as expected
    * 
    * - RedirectConditions
    *   - user agent (contains/does-not-contain)
    *   - propertyconditions
    */
   
   /**
    * Test Extras
    * - test matrix of all (or almost all) possible combinations
    * - test performance and deeper integration (ie use mock objects and make sure that if disabled, no other methods are ever called)
    */
   
   String userAgentA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/500.0 (KHTML, like Gecko) TestBrowser/1.2.3 Safari/500.0";
   String userAgentB = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/500.0 (KHTML, like Gecko) TestBrowser/1.2.3 Safari/500.";  //One character difference

   public void testEnable()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptAllUAS();
      checkIsRedirect(userAgentA, null, portalRedirect);
      
      portalRedirect.setEnabled(false);
      checkIsNoRedirect(userAgentA, null, portalRedirect);
   }

   public void testSimpleConfigurationAcceptAllUAS()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptAllUAS();
      checkAcceptAllUAS(portalRedirect);
   }
   
   public void testSimpleConfigurationRejectAllUAS()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptNoUASA();
      checkRejectAllUAS(portalRedirect);

      portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptNoUASB();
      checkRejectAllUAS(portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptNoUASC();
      checkRejectAllUAS(portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptNoUASD();
      checkRejectAllUAS(portalRedirect);
   }
   
   public void testSimpleConfigurationRejectTestBrowserUAS()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleRedirectRejectTestBrowserUAS();
      checkRejectUserAgent(userAgentA, portalRedirect);
      checkRejectUserAgent(userAgentB, portalRedirect);
      checkAcceptUserAgent("", portalRedirect);
      checkAcceptUserAgent(null, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleRedirectRejectTestBrowserExactUAS();
      checkRejectUserAgent(userAgentA, portalRedirect);
      checkAcceptUserAgent(userAgentB, portalRedirect);
      checkAcceptUserAgent("", portalRedirect);
      checkAcceptUserAgent(null, portalRedirect);
   }
   
   public void testSimpleConfigurationAcceptTestBrowserUAS()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptTestBrowserUAS();
      checkAcceptUserAgent(userAgentA, portalRedirect);
      checkAcceptUserAgent(userAgentB, portalRedirect);
      checkRejectUserAgent("", portalRedirect);
      checkRejectUserAgent(null, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleRedirectAcceptTestBrowserExactUAS();
      checkAcceptUserAgent(userAgentA, portalRedirect);
      checkRejectUserAgent(userAgentB, portalRedirect);
      checkRejectUserAgent("", portalRedirect);
      checkRejectUserAgent(null, portalRedirect);
   }
   
   public void testNullPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleNullDeviceRedirect();
      checkAcceptDeviceProperty(null, portalRedirect);
      checkAcceptDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getPropertyMap(), portalRedirect);
   }
   
   public void testEmptyPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect(null, null, null, null, null); //note: this still creates a device property but with empty values
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkRejectDeviceProperty(getSimplePropertyMap(), portalRedirect);
      
      //GetPropertyMap() contains an entry with a null key, so the property map does contain the required device property
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.remove(null); // if we remove the entry with the null key, it should now fail
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }

   public void testNullPropertyPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("bar", null, null, null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkRejectDeviceProperty(getSimplePropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getPropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'bar'
   }
   
   public void testGTPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", null, null, 10F, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkRejectDeviceProperty(getSimplePropertyMap(), portalRedirect);
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect); //Note: getPropertyMap contains an entry 'ABC' with value 123
      propertyMap.remove("ABC");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "-1");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "-1.2");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "0.0");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "1.5");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "10");
      checkRejectDeviceProperty(propertyMap, portalRedirect); // should still fail since >10 not >= 10
      propertyMap.put("ABC", "10.1");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("ABC", "XYZ");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }

   public void testLTPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", null, null, null, 123.5F);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkRejectDeviceProperty(getSimplePropertyMap(), portalRedirect);
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect); //Note: getPropertyMap contains an entry 'ABC' with value 123
      propertyMap.remove("ABC");
      checkRejectDeviceProperty(propertyMap, portalRedirect); 
      propertyMap.put("ABC", "124");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "123.8");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "0.0");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "-10.5");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("ABC", "123.5");
      checkRejectDeviceProperty(propertyMap, portalRedirect); // should still fail since <123.5 not <= 123.5
      propertyMap.put("ABC", "123.49999");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("ABC", "XYZ");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testEqualsPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", "bar", null, null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getSimplePropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();  //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.remove("foo");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", "xyz");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", " bar ");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", "", null, null, null);
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testMatchesPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, "bar", null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", "BAR");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "basdfasdrf as df asdf as dfasdf");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "asdf asd fa sdf a sdfbarasdf as df");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "asdf asd fa sdf a sdfBARasdf as df");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testMatchesIgnoreCasePropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, "(?i)bar", null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", "BAR");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "basdfasdrf as df asdf as dfasdf");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "asdf asd fa sdf a sdfbarasdf as df");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "asdf asd fa sdf a sdfBARasdf as df");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testMatchesORPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, "(?i)b(ar|az)", null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", "BAR");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "baz");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "BAZ");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "BAT");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "bat");
      checkRejectDeviceProperty(propertyMap, portalRedirect);

      
      propertyMap.put("foo", "asdf as dfa sdf bazasd sadf");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, "((?i)bar)|BAZ", null, null);      
      propertyMap.put("foo", "baz");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "Baz");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "BAZ");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testExactPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, "^bar$", null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      propertyMap.put("foo", "bar.");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", " bar ");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", ".bar");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "barbarbar");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testAcceptAllPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", null, ".*", null, null);
      checkNeedInfoDeviceProperty(null, portalRedirect);
      checkRejectDeviceProperty(getEmptyPropertyMap(), portalRedirect);
      checkAcceptDeviceProperty(getSimplePropertyMap(), portalRedirect); //Note: getPropertyMap contains an entry 'foo' with value 'bar'
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", null);
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "asdf asd fa sdf asdf");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "123");
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testSimpleredirectComplexPropertyRedirect()
   {
      PortalRedirect portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("foo", "bar", ".*", null, null);
      
      Map<String, String> propertyMap = getPropertyMap();
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      propertyMap.put("foo", "Bar");
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", "123", "123", 100.23F, 150.43F);
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", "123", ".*", 100.23F, 150.43F);
      checkAcceptDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", "123.0", ".*", 100.23F, 150.43F);
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", "123", ".*", 100.23F, -150.43F);
      checkRejectDeviceProperty(propertyMap, portalRedirect);
      
      portalRedirect = PortalRedirectsFactory.createSimpleDeviceRedirect("ABC", "123", "123.0", 100.23F, 150.34F);
      checkRejectDeviceProperty(propertyMap, portalRedirect);
   }
   
   public void testComplextRedirect() //Complex -> more than one redirect site. We need to checkthe order
   {
      PortalRedirect portalRedirectFoo = PortalRedirectsFactory.createSimpleContainsRedirect("redirectFoo", "foo");
      PortalRedirect portalRedirectBar = PortalRedirectsFactory.createSimpleContainsRedirect("redirectBar", "bar");
      PortalRedirect portalRedirectBaz = PortalRedirectsFactory.createSimpleContainsRedirect("redirectBaz", "baz");
      
      checkIsRedirect("redirectFoo", "asdf asd fasd foo asdf aafsf", null, portalRedirectFoo, portalRedirectBar, portalRedirectBaz);
      checkIsRedirect("redirectBar", "asdf asd fasd bar asdf aafsf", null, portalRedirectFoo, portalRedirectBar, portalRedirectBaz);
      checkIsRedirect("redirectBaz", "asdf asd fasd baz asdf aafsf", null, portalRedirectFoo, portalRedirectBar, portalRedirectBaz);
      
      checkIsRedirect("redirectFoo", "asdf asd fasd foo bar baz aafsf", null, portalRedirectFoo, portalRedirectBar, portalRedirectBaz);
      checkIsRedirect("redirectBar","asdf asd fasd foo bar baz aafsf", null, portalRedirectBar, portalRedirectBaz, portalRedirectFoo);
      checkIsRedirect("redirectBaz","asdf asd fasd foo bar baz aafsf", null, portalRedirectBaz, portalRedirectFoo, portalRedirectBar);
      
      checkIsRedirect("redirectBaz","asdf asd fasd foo baz aafsf", null, portalRedirectBar, portalRedirectBaz, portalRedirectFoo);
   }
   
   /** ========================================================================================== **/
   
   public void checkAcceptUserAgent(String userAgent, PortalRedirect portalRedirect)
   {
      checkIsRedirect(userAgent, null, portalRedirect);
      checkIsRedirect(userAgent, getEmptyPropertyMap(), portalRedirect);
      checkIsRedirect(userAgent, getSimplePropertyMap(), portalRedirect);
      checkIsRedirect(userAgent, getPropertyMap(), portalRedirect);
   }
   
   public void checkAcceptDeviceProperty(Map<String, String> deviceProperties, PortalRedirect portalRedirect)
   {
      checkIsRedirect(userAgentA, deviceProperties, portalRedirect);
      checkIsRedirect(userAgentB, deviceProperties, portalRedirect);
      checkIsRedirect("", deviceProperties, portalRedirect);
      checkIsRedirect(null, deviceProperties, portalRedirect);
   }
   
   public void checkRejectDeviceProperty(Map<String, String> deviceProperties, PortalRedirect portalRedirect)
   {
      checkIsNoRedirect(userAgentA, deviceProperties, portalRedirect);
      checkIsNoRedirect(userAgentB, deviceProperties, portalRedirect);
      checkIsNoRedirect("", deviceProperties, portalRedirect);
      checkIsNoRedirect(null, deviceProperties, portalRedirect);
   }
   
   public void checkNeedInfoDeviceProperty(Map<String, String> deviceProperties, PortalRedirect portalRedirect)
   {
      checkIsNeedDeviceInfo(userAgentA, deviceProperties, portalRedirect);
      checkIsNeedDeviceInfo(userAgentB, deviceProperties, portalRedirect);
      checkIsNeedDeviceInfo("", deviceProperties, portalRedirect);
      checkIsNeedDeviceInfo(null, deviceProperties, portalRedirect);
   }
   
   public void checkRejectUserAgent(String userAgent, PortalRedirect portalRedirect)
   {
      checkIsNoRedirect(userAgent, null, portalRedirect);
      checkIsNoRedirect(userAgent, getEmptyPropertyMap(), portalRedirect);
      checkIsNoRedirect(userAgent, getSimplePropertyMap(), portalRedirect);
      checkIsNoRedirect(userAgent, getPropertyMap(), portalRedirect);
   }
   
   public void checkRejectAllUAS(PortalRedirect portalRedirect)
   {
      checkRejectUserAgent(userAgentA, portalRedirect);
      checkRejectUserAgent("", portalRedirect);
      checkRejectUserAgent(null, portalRedirect);
   }
   
   public void checkAcceptAllUAS(PortalRedirect portalRedirect)
   {
      checkAcceptUserAgent(userAgentA, portalRedirect);
      checkAcceptUserAgent("", portalRedirect);
      checkAcceptUserAgent(null, portalRedirect);
   }
   
   public void checkIsNoRedirect(String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      RedirectKey redirectKey = getRedirectKey(userAgent, deviceProperties, portalRedirects);
      
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertEquals(null, redirectKey.getRedirect());
   }
   
   public void checkIsRedirect(String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      checkIsRedirect("redirectSite", userAgent, deviceProperties, portalRedirects);
   }
   
   public void checkIsRedirect(String redirectSiteName,String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      RedirectKey redirectKey = getRedirectKey(userAgent, deviceProperties, portalRedirects);
      
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals(redirectSiteName, redirectKey.getRedirect());
   }
   
   public void checkIsNeedDeviceInfo(String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      RedirectKey redirectKey = getRedirectKey(userAgent, deviceProperties, portalRedirects);
      
      assertEquals(RedirectType.NEEDDEVICEINFO, redirectKey.getType());
      assertEquals(null, redirectKey.getRedirect());
   }
   
   public void checkNeedsDeviceProperties(String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      RedirectKey redirectKey = getRedirectKey(userAgent, deviceProperties, portalRedirects);
      
      assertEquals(RedirectType.NEEDDEVICEINFO, redirectKey.getType());
      assertEquals(null, redirectKey.getRedirect());
   }
   
   public RedirectKey getRedirectKey(String userAgent, Map<String, String> deviceProperties, PortalRedirect... portalRedirects)
   {
      Redirector redirector = new Redirector();
      ArrayList<PortalRedirect> portalRedirectsList = null;
      
      if (portalRedirects != null)
      {
         portalRedirectsList = new ArrayList<PortalRedirect>();
         for (PortalRedirect portalRedirect: portalRedirects)
         {
            portalRedirectsList.add(portalRedirect);
         }
      }
      
      return redirector.getRedirectSite(portalRedirectsList, userAgent, deviceProperties);
   }
   
   public Map<String, String> getEmptyPropertyMap()
   {
      Map<String, String> propertyMap = new HashMap<String, String>();
      
      return propertyMap;
   }
   
   public Map<String, String> getSimplePropertyMap()
   {
      Map<String, String> propertyMap = new HashMap<String, String>();
      propertyMap.put("foo", "bar");
      
      return propertyMap;
   }
   
   public Map<String, String> getPropertyMap()
   {
      Map<String, String> propertyMap = new HashMap<String, String>();
      propertyMap.put("hello", "world");
      propertyMap.put(null, null);
      propertyMap.put("nullValue", null);
      propertyMap.put("bar", "baz");
      propertyMap.put("foo", "bar");
      propertyMap.put("ABC", "123");
      return propertyMap;
   }

}

