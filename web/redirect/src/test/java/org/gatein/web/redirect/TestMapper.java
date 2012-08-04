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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.gatein.web.redirect.api.SiteRedirectService;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestMapper extends TestConfig
{
   /* Components of Mapper
    * - use-node-name-matching -> T/F
    * - unresolved nodes:
    *   - REDIRECT
    *   - NO_REDIRECT
    *   - ROOT
    *   - COMMON_ANCESTOR_NAME_MATCH
    * - the actual mappings
    * 
    * 
    * TESTS:
    * 
    * NODE NAME MATCHING:
    * 1) if using node name matching
    *    - check that we get a hit when node names match
    *    - check that we do not get a hit when node names do not match
    * 2) if not using node name matching
    *    - check that we do not get a hit when node names match
    *    - check that we do not get a hit when node names do not match
    * 
    * **TEST ABOVE ALSO WITH REDIRECT/NO_REDIRECT/ROOT/COMMON_ANCESTOR_NAME_MATCHING
    * **CHECK ALSO WITH THE ACTUAL MAP, MAKE SURE IT OVERWRITES WHAT WE SHOULD BE EXPECTING
    * 
    */

   //Hack since there is a memory leak somewhere with bootstrap.dispose and we are using an old
   //version of junit which doesn't support @beforeClass and @afterClass annotations
   public void testAll()
   {
      atestSiteA();
      atestSiteB();
      atestSiteC();
      atestSiteD();
      atestSiteE();
      atestSiteF();
      atestSiteG();
      atestSiteH();
      atestSiteI();
   }
   
   public void atestSiteA()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check that mappings are working
      String redirectPath = redirectService.getRedirectPath("origin", "redirectA", null);
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "");
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "/");
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "root");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "foo");
      assertEquals("bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "hello/world");
      assertEquals("redirect/hello/world", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "hello/world");
      assertEquals("redirect/hello/world", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "ABC/123/XYZ");
      assertEquals("123", redirectPath);
      
      //check that node name matching is working
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "hello");
      assertEquals("hello", redirectPath);
      
      //check unresolved nodes (redirectA defaults to NO_REDIRECT)
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "this-node-does-not-exist");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectA", "this/node/does/not/exist");
      assertEquals(null, redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteB()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check that mappings are working
      String redirectPath = redirectService.getRedirectPath("origin", "redirectB", null);
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "");
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "/");
      assertEquals("redirect_root", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "root");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "foo");
      assertEquals("bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "hello/world");
      assertEquals("redirect/hello/world", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "hello/world");
      assertEquals("redirect/hello/world", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "ABC/123/XYZ");
      assertEquals("123", redirectPath);
      
      //check that node name matching is _not_ working, since for redirectB its disabled
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "foo/bar");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "foo/bar/baz");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "hello");
      assertEquals(null, redirectPath);
      
      //check unresolved nodes (redirectA defaults to NO_REDIRECT)
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "this-node-does-not-exist");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectB", "this/node/does/not/exist");
      assertEquals(null, redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteC()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check that mappings are working
      String redirectPath = redirectService.getRedirectPath("origin", "redirectC", "foo");
      assertEquals("bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "hello/world");
      assertEquals("redirect/hello/world", redirectPath);
      
      //check that node name matching is working
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "hello");
      assertEquals("hello", redirectPath);
      
      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "/");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "this-node-does-not-exist");
      assertEquals("this-node-does-not-exist", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectC", "this/node/does/not/exist");
      assertEquals("this/node/does/not/exist", redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteD()
   {
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check unresolved nodes
      String redirectPath = redirectService.getRedirectPath("origin", "redirectD", "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "hello");
      assertEquals("hello", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "/");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "this-node-does-not-exist");
      assertEquals("this-node-does-not-exist", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", "redirectD", "this/node/does/not/exist");
      assertEquals("this/node/does/not/exist", redirectPath);
      
      RequestLifeCycle.end();
   }
   
   
   public void atestSiteE()
   {
      String redirectName = "redirectE";
      String redirectPath;
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check node name matching
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello");
      assertEquals("hello", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "/");
      assertEquals("", redirectPath);
      
      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this-node-does-not-exist");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this/node/does/not/exist");
      assertEquals(null, redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteF()
   {
      String redirectName = "redirectF";
      String redirectPath;
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);

      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/baz");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, null);
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "/");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this-node-does-not-exist");
      assertEquals(null, redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this/node/does/not/exist");
      assertEquals(null, redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteG()
   {
      String redirectName = "redirectG";
      String redirectPath;
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check node name matching
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello");
      assertEquals("hello", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "/");
      assertEquals("", redirectPath);
      
      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this-node-does-not-exist");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this/node/does/not/exist");
      assertEquals("", redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteH()
   {
      String redirectName = "redirectH";
      String redirectPath;
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);

      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/baz");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "/");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this-node-does-not-exist");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this/node/does/not/exist");
      assertEquals("", redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public void atestSiteI()
   {
      String redirectName = "redirectI";
      String redirectPath;
      PortalContainer container = getContainer();
      RequestLifeCycle.begin(container);
      SiteRedirectService redirectService = (SiteRedirectService)container.getComponentInstanceOfType(SiteRedirectService.class);
      assertNotNull(redirectService);
      
      //check node name matching
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/baz");
      assertEquals("foo/bar/baz", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello");
      assertEquals("hello", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, null);
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "/");
      assertEquals("", redirectPath);
      
      //check unresolved nodes
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bat");
      assertEquals("foo", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "foo/bar/bat");
      assertEquals("foo/bar", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "hello/world/123");
      assertEquals("hello/world", redirectPath);
      
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this-node-does-not-exist");
      assertEquals("", redirectPath);
      redirectPath = redirectService.getRedirectPath("origin", redirectName, "this/node/does/not/exist");
      assertEquals("", redirectPath);
      
      RequestLifeCycle.end();
   }
   
   public PortalContainer getContainer()
   {
      String configurationFile="org/exoplatform/portal/config/TestMappings-configuration.xml";
      String origin = "testMappings";
      return getContainer(configurationFile, origin);
   }
}

