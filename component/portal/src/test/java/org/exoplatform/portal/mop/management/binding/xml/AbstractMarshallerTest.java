/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.binding.xml;

import junit.framework.TestCase;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.Preference;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractMarshallerTest extends TestCase
{
   protected void compareComponents(List<ModelObject> expectedComponents, List<ModelObject> actualComponents)
   {
      assertEquals(expectedComponents.size(), actualComponents.size());
      for (int i=0; i<expectedComponents.size(); i++)
      {
         ModelObject expected = expectedComponents.get(i);
         ModelObject actual = actualComponents.get(i);
         assertEquals(expected.getClass(), actual.getClass());

         if (expected instanceof Application)
         {
            compareApplication((Application) expected, (Application) actual);
         }
         else if (expected instanceof PageBody)
         {
            assertTrue(actual instanceof PageBody);
         }
         else if (expected instanceof Container)
         {
            compareContainer((Container) expected, (Container) actual);
         }
      }
   }

   protected void compareContainer(Container expected, Container actual)
   {
      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getName(), actual.getName());
      assertEquals(expected.getIcon(), actual.getIcon());
      assertEquals(expected.getTemplate(), actual.getTemplate());
      assertEquals(expected.getFactoryId(), actual.getFactoryId());
      assertEquals(expected.getTitle(), actual.getTitle());
      assertEquals(expected.getDescription(), actual.getDescription());
      assertEquals(expected.getWidth(), actual.getWidth());
      assertEquals(expected.getHeight(), actual.getHeight());
      assertEquals(Arrays.asList(expected.getAccessPermissions()), Arrays.asList(actual.getAccessPermissions()));

      compareComponents(expected.getChildren(), actual.getChildren());
   }

   protected void compareApplication(Application expected, Application actual)
   {
      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertEquals(expected.getType(), actual.getType());
      if (expected.getState() == null)
      {
         assertNull(actual.getState());
      }
      else
      {
         assertNotNull(actual.getState());
         compareApplicationState(expected.getState(), actual.getState());
      }

      assertNull(actual.getStorageId());
      assertNull(actual.getStorageName());
      assertNull(actual.getId());
      assertEquals(expected.getTitle(), actual.getTitle());
      assertEquals(expected.getIcon(), actual.getIcon());
      assertEquals(expected.getDescription(), actual.getDescription());
      assertEquals(expected.getShowInfoBar(), actual.getShowInfoBar());
      assertEquals(expected.getShowApplicationState(), actual.getShowApplicationState());
      assertEquals(expected.getShowApplicationMode(), actual.getShowApplicationMode());
      assertEquals(expected.getTheme(), actual.getTheme());
      assertEquals(expected.getWidth(), actual.getWidth());
      assertEquals(expected.getHeight(), actual.getHeight());
      assertEquals(expected.getProperties(), actual.getProperties());
      assertEquals(Arrays.asList(expected.getAccessPermissions()), Arrays.asList(actual.getAccessPermissions()));
   }

   protected void compareApplicationState(ApplicationState expected, ApplicationState actual)
   {
      assertEquals(expected.getClass(), actual.getClass());
      if (expected instanceof TransientApplicationState)
      {
         TransientApplicationState expectedTas = (TransientApplicationState) expected;
         TransientApplicationState actualTas = (TransientApplicationState) actual;
         assertEquals(expectedTas.getContentId(), actualTas.getContentId());
         assertNull(actualTas.getOwnerType());
         assertNull(actualTas.getOwnerId());
         assertNull(actualTas.getUniqueId());
         if (expectedTas.getContentState() == null)
         {
            assertNull(actualTas.getContentState());
         }
         else
         {
            assertEquals(expectedTas.getContentState().getClass(), actualTas.getContentState().getClass());
            if (expectedTas.getContentState() instanceof Portlet)
            {
               comparePortlet((Portlet) expectedTas.getContentState(), (Portlet) actualTas.getContentState());
            }
            else if (expectedTas.getContentState() instanceof Gadget)
            {
               compareGadget((Gadget) expectedTas.getContentState(), (Gadget) actualTas.getContentState());
            }
         }
      }
   }

   protected void comparePortlet(Portlet expected, Portlet actual)
   {
      for (Preference expectedPref : expected)
      {
         Preference actualPref = actual.getPreference(expectedPref.getName());
         assertNotNull(actualPref);
         assertEquals(expectedPref.getName(), actualPref.getName());
         assertEquals(expectedPref.getValues(), actualPref.getValues());
         assertEquals(expectedPref.isReadOnly(), actualPref.isReadOnly());
      }
   }

   private void compareGadget(Gadget expected, Gadget actual)
   {
      assertNotNull(expected);
      assertNotNull(actual);
      // When gadget user prefs are supported in gatein_objects, uncomment.
      //assertEquals(expected.getUserPref(), actual.getUserPref());
   }
}
