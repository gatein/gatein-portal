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
package org.gatein.api.navigation;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.ApiException;
import org.gatein.api.common.i18n.Localized.Value;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.navigation.Visibility.Status;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class ObjectFactory {
    private ObjectFactory() {
    }

    public static LocalizedString createLocalizedString(Map<Locale, Described.State> descriptions) {
        if (descriptions == null)
            return null;

        Map<Locale, String> m = new HashMap<Locale, String>();
        for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet()) {
            // For some reason (UI issue possibly) an english locale can be set with no value.
            if (entry.getValue().getName() != null) {
                m.put(entry.getKey(), entry.getValue().getName());
            }
        }
        return new LocalizedString(m);
    }

    public static Map<Locale, Described.State> createDescriptions(LocalizedString string) {
        Map<Locale, Described.State> descriptions = new HashMap<Locale, Described.State>();
        for (Value<String> v : string.getLocalizedValues()) {
            descriptions.put(v.getLocale(), new Described.State(v.getValue(), null));
        }
        return descriptions;
    }

    public static org.exoplatform.portal.mop.Visibility createVisibility(Status flag) {
        switch (flag) {
            case VISIBLE:
                return org.exoplatform.portal.mop.Visibility.DISPLAYED;
            case HIDDEN:
                return org.exoplatform.portal.mop.Visibility.HIDDEN;
            case SYSTEM:
                return org.exoplatform.portal.mop.Visibility.SYSTEM;
            default:
                throw new ApiException("Unknown visibility flag " + flag);
        }
    }

    public static Visibility createVisibility(NodeState nodeState) {
        Status flag = createFlag(nodeState.getVisibility());

        if (flag == Status.PUBLICATION) {
            long start = nodeState.getStartPublicationTime();
            long end = nodeState.getEndPublicationTime();

            PublicationDate publicationDate = null;
            if (start != -1 && end != -1) {
                publicationDate = PublicationDate.between(new Date(start), new Date(end));
            } else if (start != -1) {
                publicationDate = PublicationDate.startingOn(new Date(start));
            } else if (end != -1) {
                publicationDate = PublicationDate.endingOn(new Date(end));
            }

            return new Visibility(publicationDate);
        }

        return new Visibility(flag);
    }

    private static Status createFlag(org.exoplatform.portal.mop.Visibility visibility) {
        switch (visibility) {
            case DISPLAYED:
                return Status.VISIBLE;
            case HIDDEN:
                return Status.HIDDEN;
            case SYSTEM:
                return Status.SYSTEM;
            case TEMPORAL:
                return Status.PUBLICATION;
            default:
                throw new ApiException("Unknown internal visibility '" + visibility + "'");
        }
    }
}
