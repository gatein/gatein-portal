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
package org.exoplatform.portal.i18n;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.i18n.Injector;

import org.chromattic.api.ChromatticSession;

/**
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * Apr 21, 2011
 */

@ConfiguredBy(
{@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/test-i18nframework-configuration.xml")})
public class TestI18NFramework extends AbstractKernelTest
{
   private ChromatticLifeCycle lifeCycle;

   private ChromatticManager chromatticManager;

   @Override
   protected void setUp() throws Exception
   {
/*
      PortalContainer container = PortalContainer.getInstance();
      chromatticManager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
      lifeCycle = chromatticManager.getLifeCycle("i18n");
      lifeCycle.openContext();
*/
   }

   private <E> ChromatticSession createSampleData(String nodeName)
   {
      ChromatticSession session = lifeCycle.getChromattic().openSession();
      session.addEventListener(new Injector(session));
      NavigationNode node = session.insert(NavigationNode.class, nodeName);
      Described described = session.getEmbedded(node, Described.class);
      if (described == null)
      {
         described = session.create(Described.class);
      }

      A a = session.getEmbedded(node, A.class);
      if (a == null)
      {
         a = session.create(A.class);
      }

      session.setEmbedded(node, Described.class, described);
      session.setEmbedded(node, A.class, a);

      return session;
   }

   public void testFoo()
   {

   }

/*
   public void testI18N()
   {
      String homepage_en = "Homepage";
      String homepage_vi = "Trangchu";
      String description_en = "This is the homepage";

      ChromatticSession session = createSampleData("node1");
      NavigationNode node = session.findByPath(NavigationNode.class, "node1");

      I18NFramework framework = new I18NFramework(session);
      Described describe_en = framework.putMixin(node, Described.class, "en");
      describe_en.setName(homepage_en);

      Described describe_vi = framework.putMixin(node, Described.class, "vi");
      describe_vi.setName(homepage_vi);

      Language language = session.findByPath(Language.class, "node1/gtn:languages/en");
      assertNotNull(language);
      Described describe_en_new = session.getEmbedded(language, Described.class);
      assertEquals(describe_en_new.getName(), homepage_en);

      language = session.findByPath(Language.class, "node1/gtn:languages/vi");
      assertNotNull(language);
      Described describe_vi_new = session.getEmbedded(language, Described.class);
      assertEquals(describe_vi_new.getName(), homepage_vi);

      A a_en = framework.putMixin(node, A.class, "en");
      a_en.setDescription(description_en);

      a_en = framework.getMixin(node, A.class, "en");
      assertEquals(description_en, a_en.getDescription());
      session.save();
      session.close();
   }

   public void testNotEmbedded()
   {
      String nodeTest = "testNotEmbedded";
      ChromatticSession session = this.createSampleData(nodeTest);
      NavigationNode node = session.findByPath(NavigationNode.class, nodeTest);
      I18NFramework framework = new I18NFramework(session);
      try
      {
         framework.putMixin(node, B.class, "en");
         fail();
      }
      catch (IllegalStateException e)
      {
         e.printStackTrace();
      }
      
      try
      {
         framework.getMixin(node, B.class, "en");
         fail();
      }
      catch(IllegalStateException e)
      {
         e.printStackTrace();
      }
   }
   
   public void testGetDefaultLanguage()
   {
      String nodeTest = "testGetDefaultLanguage";
      String name_en = "homepage";
      String name_vi = "trangchu";
      ChromatticSession session = this.createSampleData(nodeTest);
      NavigationNode node = session.findByPath(NavigationNode.class, nodeTest);
      Described described = session.getEmbedded(node, Described.class);
      described.setName(name_en);
      
      I18NFramework framework = new I18NFramework(session);
      described = framework.putMixin(node, Described.class, "vi");
      described.setName(name_vi);
      
      described = framework.getMixin(node, Described.class, "en");
      assertNotNull(described);
      assertEquals(described.getName(), name_en);
      
      described = framework.getMixin(node, Described.class, "vi");
      assertNotNull(described);
      assertEquals(described.getName(), name_vi);
   }
*/

   @Override
   protected void tearDown() throws Exception
   {
/*
      lifeCycle.closeContext(false);
*/
   }
}
