/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.exportimport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.operation.model.ExportTask;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageExportTask extends AbstractExportTask implements ExportTask {
    public static final String FILE = "pages.xml";

    private final DataStorage dataStorage;
    private final PageService pageService;
    private final Marshaller<Page.PageSet> marshaller;
    private final List<String> pageNames;

    public PageExportTask(SiteKey siteKey, DataStorage dataStorage, PageService pageService, Marshaller<Page.PageSet> marshaller) {
        super(siteKey);
        this.dataStorage = dataStorage;
        this.pageService = pageService;
        this.marshaller = marshaller;
        pageNames = new ArrayList<String>();
    }

    @Override
    public void export(OutputStream outputStream) throws IOException {
        Page.PageSet pages = new Page.PageSet();
        pages.setPages(new ArrayList<Page>(pageNames.size()));
        for (String pageName : pageNames) {
            try {
                PageKey pageKey = new PageKey(siteKey, pageName);
                pages.getPages().add(PageUtils.getPage(dataStorage, pageService, pageKey));
            } catch (Exception e) {
                throw new IOException("Could not retrieve page name " + pageName + " for site " + siteKey, e);
            }
        }

        marshaller.marshal(pages, outputStream, true);
    }

    @Override
    protected String getXmlFileName() {
        return FILE;
    }

    public void addPageName(String pageName) {
        pageNames.add(pageName);
    }

    public List<String> getPageNames() {
        return Collections.unmodifiableList(pageNames);
    }
}
