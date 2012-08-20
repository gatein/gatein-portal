package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

import java.util.Iterator;
import java.util.List;

/**
 * The page entity service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public interface PageService
{

   PageContext loadPage(PageKey key);

   boolean savePage(PageContext page);

   boolean destroyPage(PageKey key);

   PageContext clone(PageKey src, PageKey dst);

   QueryResult<PageContext> findPages(
      int offset,
      int limit,
      SiteType siteType,
      String siteName,
      String pageName,
      String title);

}
