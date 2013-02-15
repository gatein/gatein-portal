/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.api;

import java.util.Comparator;

import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.api.common.Sorting;
import org.gatein.api.page.Page;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteImpl;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class Comparators {
    public static Comparator<PortalConfig> site(final Sorting<Site> sorting) {
        if (sorting == null) {
            return null;
        } else if (sorting.getComparator() == null) {
            return new Comparator<PortalConfig>() {
                @Override
                public int compare(PortalConfig o1, PortalConfig o2) {
                    Site site = new SiteImpl(o1);
                    Site other = new SiteImpl(o2);
                    if (sorting.getOrder() == Sorting.Order.descending) {
                        Site tmp = site;
                        site = other;
                        other = tmp;
                    }

                    return site.compareTo(other);
                }
            };
        } else {
            return new ComparatorWrapper<Site, PortalConfig>(sorting.getComparator()) {

                @Override
                protected Site from(PortalConfig entity) {
                    return new SiteImpl(entity);
                }
            };
        }
    }

    public static Comparator<Page> page(final Sorting<Page> sorting) {
        if (sorting == null) {
            return null;
        } else if (sorting.getComparator() == null) {
            return new Comparator<Page>() {
                @Override
                public int compare(Page page, Page other) {
                    if (sorting.getOrder() == Sorting.Order.descending) {
                        Page tmp = page;
                        page = other;
                        other = tmp;
                    }

                    return page.compareTo(other);
                }
            };
        } else {
            return sorting.getComparator();
        }
    }

    private abstract static class ComparatorWrapper<T, F> implements Comparator<F> {
        private final Comparator<T> comparator;

        public ComparatorWrapper(Comparator<T> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(F o1, F o2) {
            return comparator.compare(from(o1), from(o2));
        }

        protected abstract T from(F entity);
    }
}
