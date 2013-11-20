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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
* @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
* @version $Revision$
*/
public class RssReaderBean {

    private URL contentSource;
    private String authorUrlPrefix = "";
    private List<RssTitleBean> feedTitles = new ArrayList<RssTitleBean>();

    private boolean valid = true;
    private String sourceIO = "";

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean isValid) {
        this.valid = isValid;
    }

    public String getSourceIO() {
        return sourceIO;
    }

    public void setSourceIO(String sourceIO) {
        this.sourceIO = sourceIO;
    }

    public String getAuthorUrlPrefix() {
        return authorUrlPrefix;
    }

    public void setAuthorUrlPrefix(String authorUrlPrefix) {
        this.authorUrlPrefix = authorUrlPrefix;
    }

    public URL getContentSource() {
        return contentSource;
    }

    public void setContentSource(URL blogSource) {
        this.contentSource = blogSource;
    }

    public List<RssTitleBean> getFeedTitles() {
        return this.feedTitles;
    }

    public void setFeedTitles(List<RssTitleBean> feedTitles) {
        this.feedTitles = feedTitles;
    }
}
