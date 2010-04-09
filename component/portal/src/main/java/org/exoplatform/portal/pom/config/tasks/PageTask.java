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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.PageData;

import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.WorkspaceCustomizationContext;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;

import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageTask
{

   /** . */
   protected final String ownerType;

   /** . */
   protected final String ownerId;

   /** . */
   protected final String name;

   /** . */
   protected final PageKey key;

   /** . */
   protected final ObjectType<? extends Site> siteType;

   protected PageTask(PageKey key)
   {
      this.key = key;
      this.ownerType = key.getType();
      this.ownerId = key.getId();
      this.name = key.getName();
      this.siteType = Mapper.parseSiteType(ownerType);
   }

   public static class Clone extends PageTask implements POMTask<PageData>
   {

      /** . */
      private final ObjectType<? extends Site> cloneSiteType;

      /** . */
      private final String cloneOwnerType;

      /** . */
      private final String cloneOwnerId;

      /** . */
      private final String cloneName;

      /** . */
      private boolean deep;

      public Clone(PageKey key, PageKey cloneKey, boolean deep)
      {
         super(key);

         //
         this.cloneOwnerType = cloneKey.getType();
         this.cloneOwnerId = cloneKey.getId();
         this.cloneName = cloneKey.getName();
         this.deep = deep;
         this.cloneSiteType = Mapper.parseSiteType(cloneOwnerType);
      }

      public PageData run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();

         //
         org.gatein.mop.api.workspace.Page srcPage;
         Site srcSite = workspace.getSite(siteType, ownerId);
         if (srcSite == null)
         {
            throw new NoSuchDataException("Could not clone  page " + name + "from non existing site of type "
               + ownerType + " with id " + ownerId);
         }
         else
         {
            org.gatein.mop.api.workspace.Page root = srcSite.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            srcPage = pages.getChild(name);
         }

         //
         if (srcPage == null)
         {
            throw new NoSuchDataException("Could not clone non existing page " + name + " from site of type "
               + ownerType + " with id " + ownerId);
         }

         //
         Site dstSite = workspace.getSite(cloneSiteType, cloneOwnerId);
         if (dstSite == null)
         {
            throw new IllegalArgumentException("Could not clone page " + name + "to non existing site of type "
               + ownerType + " with id " + ownerId);
         }

         //
         org.gatein.mop.api.workspace.Page dstRoot = srcSite.getRootPage();
         org.gatein.mop.api.workspace.Page dstPages = dstRoot.getChild("pages");
         if (dstPages.getChild(cloneName) != null)
         {
            throw new IllegalArgumentException("Cloned page already exist");
         }

         //
         org.gatein.mop.api.workspace.Page dstPage = dstPages.addChild(cloneName);

         // Copy all attributes
         Attributes srcAttrs = srcPage.getAttributes();
         Attributes dstAttrs = dstPage.getAttributes();
         for (String key : srcAttrs.getKeys())
         {
            Object value = srcAttrs.getObject(key);
            dstAttrs.setObject(key, value);
         }

         // Copy described
         Described srcDescribed = srcPage.adapt(Described.class);
         Described dstDescribed = dstPage.adapt(Described.class);
         dstDescribed.setName(srcDescribed.getName());
         dstDescribed.setDescription(srcDescribed.getDescription());

         // Copy src permissions to dst permission
         PageData srcPageData = new Mapper(session).load(srcPage);
         ProtectedResource pr = dstPage.adapt(ProtectedResource.class);
         pr.setAccessPermissions(srcPageData.getAccessPermissions());
         pr.setEditPermission(srcPageData.getEditPermission());

         copy(srcPage, dstPage, srcPage.getRootComponent(), dstPage.getRootComponent());

         //
         return new Mapper(session).load(dstPage);
      }

      private void copy(org.gatein.mop.api.workspace.Page srcPage, org.gatein.mop.api.workspace.Page dstPage,
         UIContainer src, UIContainer dst)
      {
         for (UIComponent srcChild : src.getComponents())
         {
            UIComponent dstChild = dst.add(srcChild.getObjectType(), srcChild.getObjectId());

            //
            if (srcChild.isAdapted(Described.class))
            {
               Described srcDescribed = srcChild.adapt(Described.class);
               Described dstDescribed = dstChild.adapt(Described.class);
               dstDescribed.setName(srcDescribed.getName());
               dstDescribed.setDescription(srcDescribed.getDescription());
            }

            //
            if (srcChild.isAdapted(ProtectedResource.class))
            {
               ProtectedResource srcPR = srcChild.adapt(ProtectedResource.class);
               ProtectedResource dstPR = dstChild.adapt(ProtectedResource.class);
               dstPR.setAccessPermissions(srcPR.getAccessPermissions());
               dstPR.setEditPermission(srcPR.getEditPermission());
            }

            //
            Attributes srcAttrs = srcChild.getAttributes();
            Attributes dstAttrs = dstChild.getAttributes();
            for (String key : srcAttrs.getKeys())
            {
               Object value = srcAttrs.getObject(key);
               dstAttrs.setObject(key, value);
            }

            //
            if (srcChild instanceof UIWindow)
            {
               UIWindow srcWindow = (UIWindow)srcChild;
               UIWindow dstWindow = (UIWindow)dstChild;
               Customization<?> customization = srcWindow.getCustomization();
               ContentType contentType = customization.getType();
               String contentId = customization.getContentId();
               Customization parent = customization.getParent();
               Customization dstParent = null;
               if (parent != null)
               {
                  WorkspaceCustomizationContext parentCtx = (WorkspaceCustomizationContext)parent.getContext();
                  String name = parentCtx.nameOf(parent);
                  if (parentCtx == srcPage)
                  {
                     dstParent = dstPage.getCustomizationContext().getCustomization(name);
                     if (dstParent == null)
                     {
                        Object state = parent.getVirtualState();
                        dstParent = dstPage.getCustomizationContext().customize(name, contentType, contentId, state);
                     }
                  }
                  if (dstParent != null)
                  {
                     Object state = customization.getState();
                     Customization dstCustomization = dstWindow.customize(dstParent);
                     dstCustomization.setState(state);
                  }
                  else
                  {
                     Object state = customization.getVirtualState();
                     dstWindow.customize(contentType, contentId, state);
                  }
               }
               else
               {
                  Object state = customization.getVirtualState();
                  dstWindow.customize(contentType, contentId, state);
               }
            }
            else if (srcChild instanceof UIContainer)
            {
               UIContainer srcContainer = (UIContainer)srcChild;
               UIContainer dstContainer = (UIContainer)dstChild;
               copy(srcPage, dstPage, srcContainer, dstContainer);
            }
         }
      }

      @Override
      public String toString()
      {
         return "PageTask.Clone[srcOwnerType=" + ownerType + ",srcOwnerId=" + ownerId + "srcName," + name +
            "dstOwnerType=" + cloneOwnerType + ",dstOwnerId=" + cloneOwnerId + "dstName," + cloneName + "]";
      }
   }

   public static class Remove extends PageTask implements CacheableDataTask<PageKey, Void>
   {

      public Remove(PageData page)
      {
         super(page.getKey());
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.DESTROY;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public PageKey getKey()
      {
         return key;
      }

      public Void run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site == null)
         {
            throw new NoSuchDataException("Could not remove page " + name + "of non existing site of type "
               + ownerType + " with id " + ownerId);
         }
         else
         {
            org.gatein.mop.api.workspace.Page root = site.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            org.gatein.mop.api.workspace.Page page = pages.getChild(name);
            if (page == null)
            {
               throw new NoSuchDataException("Could not remove non existing page " + name + " of site of type "
                  + ownerType + " with id " + ownerId);
            }
            page.destroy();
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PageTask.Remove[ownerType=" + ownerType + ",ownerId=" + ownerId + "name," + name + "]";
      }
   }

   public static class Save extends PageTask implements CacheableDataTask<PageKey, Void>
   {

      /** . */
      private final PageData page;

      /** . */
      private List<ModelChange> changes;

      public Save(PageData page)
      {
         super(page.getKey());

         //
         this.page = page;
      }

      public DataAccessMode getAccessMode()
      {
         return page.getStorageId() != null ? DataAccessMode.WRITE : DataAccessMode.CREATE;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public PageKey getKey()
      {
         return key;
      }

      public Void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot insert page " + page + " as the corresponding portal "
               + ownerId + " with type " + siteType + " does not exist");
         }

         //
         Mapper mapper = new Mapper(session);
         changes = mapper.save(this.page, site, name);

         //
         return null;
      }

      public List<ModelChange> getChanges()
      {
         return changes;
      }

      @Override
      public String toString()
      {
         return "PageTask.Save[ownerType=" + ownerType + ",ownerId=" + ownerId + "name," + name + "]";
      }
   }

   public static class Load extends PageTask implements CacheableDataTask<PageKey, PageData>
   {

      public Load(PageKey key)
      {
         super(key);
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public PageKey getKey()
      {
         return key;
      }

      public Class<PageData> getValueType()
      {
         return PageData.class;
      }

      public PageData run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site != null)
         {
            org.gatein.mop.api.workspace.Page root = site.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            org.gatein.mop.api.workspace.Page page = pages.getChild(name);
            if (page != null)
            {
               return new Mapper(session).load(page);
            }
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PageTask.Load[ownerType=" + ownerType + ",ownerId=" + ownerId + "name," + name + "]";
      }
   }
}
