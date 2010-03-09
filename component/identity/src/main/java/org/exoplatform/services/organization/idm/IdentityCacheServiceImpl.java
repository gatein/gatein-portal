/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.services.organization.idm;

import org.picketlink.idm.cache.APICacheProvider;

import java.util.LinkedList;
import java.util.List;


/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class IdentityCacheServiceImpl implements IdentityCacheService
{

   private final List<APICacheProvider> cacheProviders = new LinkedList<APICacheProvider>();

   public IdentityCacheServiceImpl()
   {
   }

   public void register(APICacheProvider cacheProvider)
   {

      if (cacheProvider != null)
      {
         cacheProviders.add(cacheProvider);
      }

   }

   public void invalidate(String namespace)
   {
      for (APICacheProvider cacheProvider : cacheProviders)
      {
         cacheProvider.invalidate(namespace);
      }
   }

   public void invalidateAll()
   {
      for (APICacheProvider cacheProvider : cacheProviders)
      {
         cacheProvider.invalidateAll();
      }
   }
}
