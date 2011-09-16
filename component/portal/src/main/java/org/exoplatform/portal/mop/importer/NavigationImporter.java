/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.pom.config.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class NavigationImporter
{

   /** . */
   private final Locale portalLocale;

   /** . */
   private final PageNavigation src;

   /** . */
   private final NavigationService service;

   /** . */
   private final ImportMode mode;

   /** . */
   private final DescriptionService descriptionService;
   
   public NavigationImporter(
      Locale portalLocale,
      ImportMode mode,
      PageNavigation src,
      NavigationService service,
      DescriptionService descriptionService)
   {
      this.portalLocale = portalLocale;
      this.mode = mode;
      this.src = src;
      this.service = service;
      this.descriptionService = descriptionService;
   }

   public void perform()
   {

      //
      SiteKey key = new SiteKey(src.getOwnerType(), src.getOwnerId());

      //
      NavigationContext dst = service.loadNavigation(key);

      //
      switch (mode)
      {
         case CONSERVE:
            if (dst == null)
            {
               dst = new NavigationContext(key, new NavigationState(src.getPriority()));
               service.saveNavigation(dst);
            }
            else
            {
               dst = null;
            }
            break;
         case MERGE:
         case INSERT:
            if (dst == null)
            {
               dst = new NavigationContext(key, new NavigationState(src.getPriority()));
               service.saveNavigation(dst);
            }
            break;
         case OVERWRITE:
            if (dst == null)
            {
               dst = new NavigationContext(key, new NavigationState(src.getPriority()));
               service.saveNavigation(dst);
            }
            break;
         default:
            throw new AssertionError();
      }

      //
      if (dst != null)
      {
         ArrayList<NavigationFragment> fragments = src.getFragments();
         if (fragments != null && fragments.size() > 0)
         {
            for (NavigationFragment fragment : fragments)
            {
               String parentURI = fragment.getParentURI();

               // Find something better than that for building the path
               List<String> path;
               if (parentURI != null)
               {
                  path = new ArrayList<String>();
                  String[] names = Utils.split("/", parentURI);
                  for (String name : names)
                  {
                     if (name.length() > 0)
                     {
                        path.add(name);
                     }
                  }
               }
               else
               {
                  path = Collections.emptyList();
               }

               //
               NavigationFragmentImporter fragmentImporter = new NavigationFragmentImporter(
                  path.toArray(new String[path.size()]),
                  service,
                  dst.getKey(),
                  portalLocale,
                  descriptionService,
                  fragment,
                  mode.config);

               //
               fragmentImporter.perform();
            }
         }
      }
   }
}
