/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.portal.api.impl.portal;

import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.gatein.api.id.Id;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Site;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.common.NotYetImplemented;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.IdentifiableImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class PageImpl extends IdentifiableImpl<Page> implements Page
{
   private final Id<? extends Site> site;
   private PageData pageData;

   public PageImpl(PageData pageData, Id<? extends Site> parent, GateInImpl gateIn)
   {
      super(gateIn.pageId(parent, pageData.getName()), pageData.getName(), gateIn);
      this.site = parent;
      this.pageData = pageData;
   }

   public Site getSite()
   {
      return getGateIn().get(site);
   }

   public String getTitle()
   {
      return pageData.getTitle();
   }

   @Override
   public String toString()
   {
      return "'" + getName() + "' Page titled '" + getTitle() + "' id " + getId();
   }

   public void setTitle(String title)
   {
      try
      {
         getGateInImpl().begin();
         final ModelDataStorage dataStorage = getGateInImpl().getDataStorage();

         // recreate page with the new title
         final PageData newPageData = new PageData(pageData.getStorageId(), pageData.getId(), pageData.getName(), pageData.getIcon(),
            pageData.getTemplate(), pageData.getFactoryId(), title, pageData.getDescription(), pageData.getWidth(), pageData.getHeight(),
            pageData.getAccessPermissions(), pageData.getChildren(), pageData.getOwnerType(), pageData.getOwnerId(),
            pageData.getEditPermission(), pageData.isShowMaxWindow());

         // save new page
         dataStorage.save(newPageData);

         // remove previous data
         dataStorage.remove(pageData);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         getGateInImpl().end();
      }


   }

   public IterableIdentifiableCollection<Navigation> getInboundNavigations()
   {
      throw new NotYetImplemented(); // todo
   }

   public Navigation createInboundNavigationIn(Site site, Navigation parent)
   {
      throw new NotYetImplemented(); // todo
   }
}
