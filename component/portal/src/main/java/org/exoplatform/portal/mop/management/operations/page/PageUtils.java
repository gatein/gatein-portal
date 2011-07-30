package org.exoplatform.portal.mop.management.operations.page;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.gatein.management.api.exceptions.OperationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageUtils
{
   private PageUtils(){}

   public static Page getPage(DataStorage dataStorage, PageKey pageKey, String operationName)
   {
      try
      {
         return dataStorage.getPage(pageKey.getPageId());
      }
      catch (Exception e)
      {
         throw new OperationException(operationName, "Operation failed getting page for " + pageKey, e);
      }
   }

   public static  Page.PageSet getAllPages(DataStorage dataStorage, SiteKey siteKey, String operationName)
   {
      Query<Page> query = new Query<Page>(siteKey.getTypeName(), siteKey.getName(), Page.class);
      try
      {
         List<Page> pageList = dataStorage.find(query).getAll();
         Page.PageSet pages = new Page.PageSet();
         pages.setPages(new ArrayList<Page>(pageList));

         return pages;
      }
      catch (Exception e)
      {
         throw new OperationException(operationName, "Could not retrieve pages for site " + siteKey);
      }
   }
}
