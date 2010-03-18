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

package org.exoplatform.applicationregistry.webui;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.gatein.common.i18n.LocalizedString;

import java.util.Comparator;

/** Created by The eXo Platform SAS Author : Pham Thanh Tung thanhtungty@gmail.com Sep 11, 2008 */
public class Util
{

   static public String getLocalizedStringValue(LocalizedString localizedString, String defaultValue)
   {
      if (localizedString == null || localizedString.getDefaultString() == null)
      {
         return defaultValue;
      }
      else
      {
         return localizedString.getDefaultString();
      }
   }

   static public class CategoryComparator implements Comparator<ApplicationCategory>
   {

      public int compare(ApplicationCategory cate1, ApplicationCategory cate2)
      {
         return cate1.getDisplayName().compareToIgnoreCase(cate2.getDisplayName());
      }

   }

   static public class ApplicationComparator implements Comparator<Application>
   {

      public int compare(Application app1, Application app2)
      {
         return app1.getDisplayName().compareToIgnoreCase(app2.getDisplayName());
      }

   }

   static public class GadgetComparator implements Comparator<Gadget>
   {

      public int compare(Gadget gadget1, Gadget gadget2)
      {
         return gadget1.getTitle().compareToIgnoreCase(gadget2.getTitle());
      }

   }

}
