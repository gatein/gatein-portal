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

import java.io.IOException;
import java.net.URL;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequestDispatcher;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @version $Revision$
 */
public class CommunityPortlet extends GenericPortlet {

    private String DEFAULT_URL = "/#";
    private String URL_RSS_BLOG = "url.rss.blog";
    private String URL_RSS_TWITTER = "url.rss.twitter";
    private String URL_CONTENT_BLOG = "url.blog";
    private String URL_CONTENT_TWITTER = "url.twitter";
    private String PFX_BLOG_AUTHOR = "pfx.url.author";
    private static final Logger log = LoggerFactory.getLogger(CommunityPortlet.class);

    @Override
    protected void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        PortletPreferences portletPreferences = request.getPreferences();

        String urlRssBlog = portletPreferences.getValue(URL_RSS_BLOG, DEFAULT_URL);
        String urlRssTwitter = portletPreferences.getValue(URL_RSS_TWITTER, DEFAULT_URL);
        String urlContentBlog = portletPreferences.getValue(URL_CONTENT_BLOG, DEFAULT_URL);
        String urlContentTwitter = portletPreferences.getValue(URL_CONTENT_TWITTER, DEFAULT_URL);
        String pfxBlogAuthor = portletPreferences.getValue(PFX_BLOG_AUTHOR, "");

        RssReaderBean gateInBlogRssReader = makeReaderBean(urlRssBlog, urlContentBlog, 2, pfxBlogAuthor);
        RssReaderBean gateInTwitterRssReader = makeReaderBean(urlRssTwitter, urlContentTwitter, 2, null);

        request.setAttribute("blogRSSBean", gateInBlogRssReader);
        request.setAttribute("twitterRSSBean", gateInTwitterRssReader);

        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/community.jsp");
        prd.include(request, response);
    }

    private RssReaderBean makeReaderBean(String urlRss, String urlContent, int head, String pfxBlogAuthor) {
        RssReaderBean rssReaderBean = new RssReaderBean();

        try {
            URL rssSourceUrl = new URL(urlRss);
            rssReaderBean.setFeedTitles(RomeRssControllerBean.getFeedTitles(rssSourceUrl, head));
            rssReaderBean.setContentSource(new URL(urlContent));
            rssReaderBean.setAuthorUrlPrefix(pfxBlogAuthor);
        } catch (IOException e) {
            rssReaderBean.setValid(false);
            rssReaderBean.setSourceIO(urlRss);
            log.error("Unable to open RSS feed url: " + e);
        }

        return rssReaderBean;
    }
}
