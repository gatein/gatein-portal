package org.gatein.portal.mop.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gatein.portal.mop.QueryResult;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * This class implements the {@link PageService} business methods.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageServiceImpl implements PageService {

    /** . */
    private final PageStore store;

    /**
     * Create an instance that uses the provided persistence.
     *
     * @param store the persistence
     * @throws NullPointerException if the persistence argument is null
     */
    public PageServiceImpl(PageStore store) throws NullPointerException {
        if (store == null) {
            throw new NullPointerException("No null persistence allowed");
        }
        this.store = store;
    }

    @Override
    public PageContext loadPage(PageKey key) {
        if (key == null) {
            throw new NullPointerException();
        }

        //
        PageData data = store.loadPage(key);
        return data != null && data != PageData.EMPTY ? new PageContext(data) : null;
    }

    /**
     * <p>
     * Load all the pages of a specific site. Note that this method can potentially raise performance issues if the number of
     * pages is very large and should be used with cautions. That's the motiviation for not having this method on the
     * {@link PageService} interface.
     * </p>
     *
     * @param siteKey the site key
     * @return the list of pages
     * @throws NullPointerException if the site key argument is null
     * @throws PageServiceException anything that would prevent the operation to succeed
     */
    public List<PageContext> loadPages(SiteKey siteKey) throws NullPointerException, PageServiceException {
        if (siteKey == null) {
            throw new NullPointerException("No null site key accepted");
        }

        //
        List<PageKey> keys = store.findPageKeys(siteKey);
        List<PageContext> list = new ArrayList<PageContext>(keys.size());
        for (PageKey key : keys) {
            list.add(loadPage(key));
        }
        return list;
    }

    @Override
    public boolean savePage(PageContext page) {
        if (page == null) {
            throw new NullPointerException();
        }

        //
        boolean created = store.savePage(page.key, page.state);
        page.data = store.loadPage(page.key);
        page.state = null;
        return created;
    }

    @Override
    public boolean destroyPage(PageKey key) {
        if (key == null) {
            throw new NullPointerException("No null page argument");
        }

        //
        return store.destroyPage(key);
    }

    @Override
    public PageContext clone(PageKey src, PageKey dst) {
        if (src == null) {
            throw new NullPointerException("No null source accepted");
        }
        if (dst == null) {
            throw new NullPointerException("No null destination accepted");
        }

        //
        PageData clone = store.clonePage(src, dst);
        return new PageContext(clone);
    }


    @Override
    public QueryResult<PageContext> findPages(
            int from,
            int to,
            SiteType siteType,
            String siteName,
            String pageName,
            String pageTitle) {
        Collection<PageData> dataSet = store.findPages(from, to, siteType, siteName, pageName, pageTitle);
        ArrayList<PageContext> pages = new ArrayList<PageContext>(dataSet.size());
        for (PageData data : dataSet) {
            pages.add(new PageContext(data));
        }
        return new QueryResult<PageContext>(from, dataSet.size(), pages);
    }

    public void clear() {
        store.clear();
    }
}
