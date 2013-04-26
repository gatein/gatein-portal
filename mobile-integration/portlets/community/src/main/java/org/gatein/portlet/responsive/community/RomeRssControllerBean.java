/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.portlet.responsive.community;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class RomeRssControllerBean {

    private static final Logger log = LoggerFactory.getLogger(RomeRssControllerBean.class);

    public static List<RssTitleBean> getFeedTitles(URL source, int headsize) throws IOException, FeedException {
        List<RssTitleBean> rssTitleBeanList = new ArrayList<RssTitleBean>();
        XmlReader reader = null;

        try {
            reader = new XmlReader(source);
            SyndFeed feed = new SyndFeedInput().build(reader);

            for (Iterator i = feed.getEntries().iterator(); i.hasNext() && (headsize-- > 0);) {
                SyndEntry entry = (SyndEntry) i.next();

                RssTitleBean rssTitleBean = new RssTitleBean();
                rssTitleBean.setTitle(entry.getTitle());
                rssTitleBean.setLink(entry.getLink());
                rssTitleBean.setPublishedDate(entry.getPublishedDate());

                List<RssAuthorBean> rssAuthors = new ArrayList<RssAuthorBean>();

                for (SyndPerson author : (List<SyndPerson>) entry.getAuthors()) {
                    RssAuthorBean rssAuthorBean = new RssAuthorBean();
                    rssAuthorBean.setName(author.getName());
                    rssAuthorBean.setUri(author.getUri());

                    rssAuthors.add(rssAuthorBean);
                }

                rssTitleBean.setAuthors(rssAuthors);

                rssTitleBeanList.add(rssTitleBean);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return rssTitleBeanList;
    }
}
