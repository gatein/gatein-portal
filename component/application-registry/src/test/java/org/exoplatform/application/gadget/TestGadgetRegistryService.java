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

package org.exoplatform.application.gadget;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.test.BasicTestCase;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Jul 11, 2008  
 */
public class TestGadgetRegistryService extends BasicTestCase
{

   private GadgetRegistryService service_;

   public void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      service_ = (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
   }

   public void testAddGadget() throws Exception
   {
      Gadget g1 = new Gadget();
      g1.setName("weather");
      g1.setUrl("http://www.labpixies.com/campaigns/weather/weather.xml");
      Gadget g2 = new Gadget();
      g2.setName("map");
      g2.setUrl("http://www.labpixies.com/campaigns/maps/maps.xml");
      service_.saveGadget(g1);
      service_.saveGadget(g2);
      assertEquals(2, service_.getAllGadgets().size());
      Gadget g3 = service_.getGadget(g1.getName());
      assertNotNull(g3);
      assertEquals("weather", g3.getName());
      assertEquals("http://www.labpixies.com/campaigns/weather/weather.xml", g3.getUrl());

      Gadget g4 = service_.getGadget(g2.getName());
      assertNotNull(g4);
      assertEquals("map", g4.getName());
      assertEquals("http://www.labpixies.com/campaigns/maps/maps.xml", g4.getUrl());

   }

   public void testGetGadget() throws Exception
   {
      Gadget gadget = new Gadget();
      gadget.setName("weather");
      gadget.setUrl("http://www.labpixies.com/campaigns/weather/weather.xml");
      service_.saveGadget(gadget);
      Gadget g = service_.getGadget(gadget.getName());
      assertNotNull(g);
      assertEquals(1, service_.getAllGadgets().size());
      assertEquals("weather", g.getName());
      assertEquals("http://www.labpixies.com/campaigns/weather/weather.xml", g.getUrl());
   }

   public void testGetAllGadgets()
   {

   }

   public void testRemoveGadget()
   {

   }

   public void tearDown() throws Exception
   {
      List<Gadget> gadgets = service_.getAllGadgets();
      for (Gadget ele : gadgets)
      {
         service_.removeGadget(ele.getName());
      }
   }

}
