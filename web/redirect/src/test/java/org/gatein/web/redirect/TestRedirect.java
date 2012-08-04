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

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.gatein.web.redirect.api.RedirectKey;
import org.gatein.web.redirect.api.RedirectType;
import org.gatein.web.redirect.api.SiteRedirectService;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestRedirect extends TestConfig
{
   
   String userAgentA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/500.0 (KHTML, like Gecko) TestBrowser/1.2.3 Safari/500.0";
   String userAgentB = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/500.0 (KHTML, like Gecko) TestBrowser/1.2.3 Safari/500.";  //One character difference
   
   //Hack since there is a memory leak somewhere with bootstrap.dispose and we are using an old
   //version of junit which doesn't support @beforeClass and @afterClass annotations
   public void testALL()
   {
      atestNoRedirects();
      atestEmptyConditions();
      atestAcceptAllUserAgents();
      atestAcceptAllUserAgentsDisabled();
      atestSimpleUserAgentRedirects();
      atestSimpleUserAgentRedirectsDisabled();
      atestUserAgentRedirects();
      atestSimpleDevicePropertyRedirect();
      atestDevicePropertyRedirects();
   }
   
   public void atestNoRedirects()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite("noRedirects", userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      
      RequestLifeCycle.end();
   }
   
   public void atestEmptyConditions()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite("emptyConditions", userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }

   public void atestAcceptAllUserAgents()
   {
      String originName = "userAgentRedirectAll";
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);

      RedirectKey redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, userAgentB, null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, "", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, null, null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, "123 sadf as fa sd", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());

      RequestLifeCycle.end();
   }
   
   public void atestAcceptAllUserAgentsDisabled()
   {
      String originName = "userAgentRedirectAllDisabled";
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);

      RedirectKey redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, userAgentB, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, "", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, null, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());

      redirectKey = redirectService.getRedirectSite(originName, "123 sadf as fa sd", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());


      RequestLifeCycle.end();
   }
   
   public void atestSimpleUserAgentRedirects()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite("simpleUserAgentRedirect", userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite("simpleUserAgentRedirect", userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite("simpleUserAgentRedirect", "asdf asdf as df FOO asdf asdf a sdf", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite("simpleUserAgentRedirect", "asdf asdf as df foo asdf asdf a sdf", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }
   
   public void atestSimpleUserAgentRedirectsDisabled()
   {
      String originName = "simpleUserAgentRedirectDisabled";
      
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "asdf asdf as df FOO asdf asdf a sdf", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "asdf asdf as df foo asdf asdf a sdf", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }
   
   public void atestUserAgentRedirects()
   {
      String originName = "userAgentRedirect";
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite(originName, "sdf sdf asd foo asdf asf", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "sdf sdf asd Foo asdf asf", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectB", redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "sdf sdf helloasd foo asdf asf", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectB", redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "sdf abc helloasd foo bar asdf asf", null);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectB", redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, "sdf abc world helloasd foo bar asdf asf", null);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }
   
   public void atestSimpleDevicePropertyRedirect()
   {
      String originName = "simpleDevicePropertyRedirect";
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.NEEDDEVICEINFO, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, new HashMap<String, String>());
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, createSimplePropertyMap());
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, createPropertyMapA());
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }
   
   public void atestDevicePropertyRedirects()
   {
      String originName = "devicePropertyRedirect";
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      RedirectKey redirectKey = redirectService.getRedirectSite(originName, userAgentA, null);
      assertEquals(RedirectType.NEEDDEVICEINFO, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, new HashMap<String, String>());
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      Map<String, String> deviceProperties = new HashMap<String, String>();
      deviceProperties.put("foo", "bar");
      
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectA", redirectKey.getRedirect());
      
      deviceProperties.remove("foo");
      deviceProperties.put("number", "12");
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectB", redirectKey.getRedirect());
      
      deviceProperties.put("number", "9");
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      deviceProperties.put("number", "26.5");
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.NOREDIRECT, redirectKey.getType());
      assertNull(redirectKey.getRedirect());
      
      deviceProperties.remove("number");
      deviceProperties.put("hello", "world");
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectC", redirectKey.getRedirect());
      
      deviceProperties.put("hello", "WORLD");
      redirectKey = redirectService.getRedirectSite(originName, userAgentA, deviceProperties);
      assertEquals(RedirectType.REDIRECT, redirectKey.getType());
      assertEquals("redirectC", redirectKey.getRedirect());
      
      RequestLifeCycle.end();
   }
   
   public Map<String, String> createEmptyPropertyMap()
   {
      return new HashMap<String, String>();
   }
   
   public Map<String, String> createSimplePropertyMap()
   {
      Map<String, String> simpleMap = new HashMap<String, String>();
      simpleMap.put("foo", "bar");
      
      return simpleMap;
   }
   
   public Map<String, String> createPropertyMapA()
   {
      Map<String, String> propertyMapA = new HashMap<String, String>();
      propertyMapA.put("hello", "world");
      propertyMapA.put(null, null);
      propertyMapA.put("", "");
      propertyMapA.put("ABC", "123");
      
      return propertyMapA;
   }
   
   public Map<String, String> createPropertyMapB()
   {
      Map<String, String> propertyMapB = new HashMap<String, String>();
      propertyMapB.put("hello", "world");
      propertyMapB.put(null, null);
      propertyMapB.put("", "");
      propertyMapB.put("ABC", "123");
      propertyMapB.put("foo", "BAR");
      
      return propertyMapB;
   }
 
   public PortalContainer getContainer()
   {
      String configurationFile="org/exoplatform/portal/config/TestRedirects-configuration.xml";
      String origin = "testRedirects";
      return getContainer(configurationFile, origin);
   }
}

