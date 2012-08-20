package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class DataCache
{

   protected abstract PageData getPage(POMSession session, PageKey key);

   protected abstract void removePage(POMSession session, PageKey key);

   protected abstract void putPage(PageData data);

   protected abstract void clear();

   final PageData getPageData(POMSession session, PageKey key)
   {
      PageData data;
      if (session.isModified())
      {
         data = loadPage(session, key);
      }
      else
      {
         data = getPage(session, key);
      }

      //
      return data;
   }

   protected final PageData loadPage(POMSession session, PageKey key)
   {
      Workspace workspace = session.getWorkspace();
      ObjectType<Site> objectType = Utils.objectType(key.getSite().getType());
      Site site = workspace.getSite(objectType, key.getSite().getName());
      if (site != null)
      {
         org.gatein.mop.api.workspace.Page root = site.getRootPage();
         org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
         org.gatein.mop.api.workspace.Page page = pages.getChild(key.getName());
         if (page != null)
         {
            return new PageData(page);
         }
         else
         {
            return PageData.EMPTY;
         }
      }
      else
      {
         return PageData.EMPTY;
      }
   }
}
