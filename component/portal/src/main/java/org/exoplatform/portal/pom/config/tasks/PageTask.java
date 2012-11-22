/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.pom.config.tasks;

import java.util.List;

import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageTask {

    /** . */
    protected final String ownerType;

    /** . */
    protected final String ownerId;

    /** . */
    protected final String name;

    /** . */
    protected final PageKey key;

    /** . */
    protected final ObjectType<? extends Site> siteType;

    protected PageTask(PageKey key) {
        this.key = key;
        this.ownerType = key.getType();
        this.ownerId = key.getId();
        this.name = key.getName();
        this.siteType = Mapper.parseSiteType(ownerType);
    }

    public static class Save extends PageTask implements CacheableDataTask<PageKey, Void> {

        /** . */
        private final PageData page;

        /** . */
        private List<ModelChange> changes;

        public Save(PageData page) {
            super(page.getKey());

            //
            this.page = page;
        }

        public DataAccessMode getAccessMode() {
            return page.getStorageId() != null ? DataAccessMode.WRITE : DataAccessMode.CREATE;
        }

        public Class<Void> getValueType() {
            return Void.class;
        }

        public PageKey getKey() {
            return key;
        }

        public Void run(POMSession session) {
            Workspace workspace = session.getWorkspace();
            Site site = workspace.getSite(siteType, ownerId);
            if (site == null) {
                throw new IllegalArgumentException("Cannot insert page " + page + " as the corresponding portal " + ownerId
                        + " with type " + siteType + " does not exist");
            }

            //
            Mapper mapper = new Mapper(session);
            changes = mapper.save(this.page, site, name);

            //
            return null;
        }

        public List<ModelChange> getChanges() {
            return changes;
        }

        @Override
        public String toString() {
            return "PageTask.Save[ownerType=" + ownerType + ",ownerId=" + ownerId + "name," + name + "]";
        }
    }

    public static class Load extends PageTask implements CacheableDataTask<PageKey, PageData> {

        public Load(PageKey key) {
            super(key);
        }

        public DataAccessMode getAccessMode() {
            return DataAccessMode.READ;
        }

        public PageKey getKey() {
            return key;
        }

        public Class<PageData> getValueType() {
            return PageData.class;
        }

        public PageData run(POMSession session) {
            Workspace workspace = session.getWorkspace();
            Site site = workspace.getSite(siteType, ownerId);
            if (site != null) {
                org.gatein.mop.api.workspace.Page root = site.getRootPage();
                org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
                org.gatein.mop.api.workspace.Page page = pages.getChild(name);
                if (page != null) {
                    return new Mapper(session).load(page);
                }
            }

            //
            return null;
        }

        @Override
        public String toString() {
            return "PageTask.Load[ownerType=" + ownerType + ",ownerId=" + ownerId + "name," + name + "]";
        }
    }
}
