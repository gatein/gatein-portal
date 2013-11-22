/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.appzu;

import java.util.ArrayList;

import juzu.impl.common.Name;
import org.gatein.portal.content.ContentDescription;
import org.gatein.portal.content.ContentProvider;
import org.gatein.portal.content.ContentType;
import org.gatein.portal.content.WindowContent;

/**
 * @author Julien Viet
 */
public class AppZuContentProvider implements ContentProvider {

    @Override
    public ContentType getContentType() {
        return new AppZuContentType();
    }

    @Override
    public WindowContent getContent(String id) {
        Name name = Name.parse(id);
        return ApplicationRepository.instance.getApplication(name);
    }

    @Override
    public Iterable<ContentDescription> findContents(String filter, int offset, int limit) {
        ApplicationRepository repo = ApplicationRepository.instance;
        ArrayList<ContentDescription> list = new ArrayList<ContentDescription>();
        for (AppContent app : repo.getApplications()) {
            list.add(new ContentDescription(
                    app.getName().toString(),
                    "Application " + app.getName(),
                    "<img alt=\"\" src=\"/portal/juzu-icon.jpg\"/>\n" +
                    "<p>" + app.getName() + "</p>"
            ));
        }
        return list;
    }
}
