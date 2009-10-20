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

package org.exoplatform.portal.pc;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.resources.ResourceBundleService;
import org.gatein.common.i18n.ResourceBundleFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * <p>Integrates the portal resource bundle at the application level making thus reusable the same resource bundles
 * by any portlet application.</p>
 *
 * <p>This class integrates with the portlet container and is created at deployment time.</p>
 *
 * <p>The loading responsiblity is to focus only on the resource bundle located via the explicit bundle
 * defined in the portlet.xml decriptor, the portlet containe takes care of adding later the inline bundle entries
 * declared in the portlet.xml deployment descriptor.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoResourceBundleFactory implements ResourceBundleFactory
{

   /** . */
   private final ClassLoader classLoader;

   /** . */
   private final String baseName;

   public ExoResourceBundleFactory(ClassLoader classLoader, String baseName)
   {
      this.classLoader = classLoader;
      this.baseName = baseName;
   }

   public ResourceBundle getBundle(Locale locale) throws IllegalArgumentException
   {
      if (locale == null)
      {
         throw new IllegalArgumentException();
      }

      //
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(classLoader);
         PortalContainer portalContainer = PortalContainer.getInstance();
         if (portalContainer != null)
         {
            ResourceBundleService resourceBundleService =
               (ResourceBundleService)portalContainer.getComponentInstance(ResourceBundleService.class);
            if (resourceBundleService != null)
            {
               String[] portalBundles = resourceBundleService.getSharedResourceBundleNames();
               if (baseName != null)
               {
                  String[] bundles = new String[portalBundles.length + 1];
                  System.arraycopy(portalBundles, 0, bundles, 0, portalBundles.length);
                  bundles[portalBundles.length] = baseName;
                  portalBundles = bundles;
               }
               return resourceBundleService.getResourceBundle(portalBundles, locale);
            }
         }

         //
         if (baseName != null)
         {
            return ResourceBundle.getBundle(baseName, locale, classLoader);
         }
      }
      catch (Exception ignore)
      {
         // Perhaps log that as trace
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }

      //
      return null;
   }
}
