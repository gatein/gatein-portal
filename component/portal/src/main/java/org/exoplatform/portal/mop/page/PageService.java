package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteType;

/**
 * <p>The page service manages the page objects in GateIn, it focus on the page entities and does not
 * provide access to the underlying page layout associated with the page.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface PageService
{

   /**
    * Find and returns a page, if no such page exist, null is returned.
    *
    * @param key the page key
    * @return the matching page
    * @throws NullPointerException if the key is null
    * @throws PageServiceException anything that would prevent the operation to succeed
    */
   PageContext loadPage(PageKey key) throws NullPointerException, PageServiceException;

   /**
    * Create, update a page. When the page state is not null, the page
    * will be created or updated depending on whether or not the page already exists.
    *
    * @param page the page
    * @throws NullPointerException if the key is null
    * @throws PageServiceException anything that would prevent the operation to succeed
    */
   boolean savePage(PageContext page) throws NullPointerException, PageServiceException;

   /**
    * Destroy a page.
    *
    * @param key the page key
    * @return true when the page was destroyed
    * @throws NullPointerException if the page key is null
    * @throws PageServiceException anything that would prevent the operation to succeed
    */
   boolean destroyPage(PageKey key) throws PageServiceException;

   PageContext clone(PageKey src, PageKey dst) throws PageServiceException;

   QueryResult<PageContext> findPages(
      int offset,
      int limit,
      SiteType siteType,
      String siteName,
      String pageName,
      String title) throws PageServiceException;

}
