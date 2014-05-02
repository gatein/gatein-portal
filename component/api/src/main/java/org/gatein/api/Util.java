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

import org.apache.commons.lang.LocaleUtils;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageKey;
import org.gatein.api.common.Attributes;
import org.gatein.api.page.PageId;
import org.gatein.api.security.Group;
import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.api.site.SiteType;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class Util {

    public static Properties from(Attributes attributes) {
        if (attributes == null) {
            return null;
        }
        Properties properties = new Properties(attributes);
        Object showInfoBarKey = Site.AttributeKeys.SHOW_PORTLET_INFO_BAR.getName();
        String showInfoBar = properties.get(showInfoBarKey);
        if (showInfoBar == null) {
            if (properties.containsKey(showInfoBarKey)) {
                properties.remove(showInfoBarKey);
            }
        } else {
            properties.put(showInfoBar, Boolean.parseBoolean(showInfoBar) ? "1" : "0");
        }
        return properties;
    }

    public static Attributes from(Properties properties) {
        if (properties == null) {
            return new Attributes();
        }

        Attributes attributes = new Attributes();
        for (Entry<String, String> e : properties.entrySet()) {
            String key = e.getKey();
            String value = e.getValue();
            if (key.equals(Site.AttributeKeys.SHOW_PORTLET_INFO_BAR.getName())) {
                if ("0".equals(value)) {
                    attributes.put(Site.AttributeKeys.SHOW_PORTLET_INFO_BAR, Boolean.FALSE);
                } else if ("1".equals(value)) {
                    attributes.put(Site.AttributeKeys.SHOW_PORTLET_INFO_BAR, Boolean.TRUE);
                } else {
                    attributes.put(key, value);
                }
            } else {
                attributes.put(key, value);
            }
        }
        return attributes;
    }

    public static PageId from(PageKey pageKey) {
        if (pageKey == null)
            return null;

        SiteKey siteKey = pageKey.getSite();
        switch (pageKey.getSite().getType()) {
            case PORTAL:
                return new PageId(siteKey.getName(), pageKey.getName());
            case GROUP:
                return new PageId(new Group(siteKey.getName()), pageKey.getName());
            case USER:
                return new PageId(new User(siteKey.getName()), pageKey.getName());
            default:
                throw new AssertionError();
        }
    }

    public static PageKey from(PageId pageId) {
        if (pageId == null)
            return null;

        return new PageKey(from(pageId.getSiteId()), pageId.getPageName());
    }

    public static SiteId from(SiteKey siteKey) {
        if (siteKey == null)
            return null;

        switch (siteKey.getType()) {
            case PORTAL:
                return new SiteId(siteKey.getName());
            case GROUP:
                return new SiteId(new Group(siteKey.getName()));
            case USER:
                return new SiteId(new User(siteKey.getName()));
            default:
                throw new AssertionError();
        }
    }

    public static SiteKey from(SiteId siteId) {
        if (siteId == null)
            return null;

        switch (siteId.getType()) {
            case SITE:
                return SiteKey.portal(siteId.getName());
            case SPACE:
                return SiteKey.group(siteId.getName());
            case DASHBOARD:
                return SiteKey.user(siteId.getName());
            default:
                throw new AssertionError();
        }
    }

    public static org.exoplatform.portal.mop.SiteType from(SiteType siteType) {
        if (siteType == null)
            return null;

        switch (siteType) {
            case SITE:
                return org.exoplatform.portal.mop.SiteType.PORTAL;
            case SPACE:
                return org.exoplatform.portal.mop.SiteType.GROUP;
            case DASHBOARD:
                return org.exoplatform.portal.mop.SiteType.USER;
            default:
                throw new AssertionError();
        }
    }

    public static Permission from(String... permissions) {
        if (permissions == null)
            return null;
        if (permissions.length == 1 && permissions[0] == null)
            return null; // for some reason this is happening...

        return from(Arrays.asList(permissions));
    }

    public static Permission from(List<String> permissions) {
        if (permissions == null)
            return null;

        if (permissions.size() == 1 && permissions.get(0).equals("Everyone")) {
            return Permission.everyone();
        } else {
            Set<Membership> memberships = new LinkedHashSet<Membership>(permissions.size());
            for (String permission : permissions) {
                memberships.add(Membership.fromString(permission));
            }

            return new Permission(memberships);
        }
    }

    public static String[] from(Permission permission) {
        if (permission == null)
            return null;

        if (permission.isAccessibleToEveryone()) {
            return new String[] { "Everyone" };
        } else {
            String[] permissions = new String[permission.getMemberships().size()];
            Iterator<Membership> memberships = permission.getMemberships().iterator();
            for (int i = 0; i < permissions.length; i++) {
                permissions[i] = memberships.next().toString();
            }

            return permissions;
        }
    }

    public static Locale toLocale(String locale) {
        return LocaleUtils.toLocale(locale);
    }

    public static String fromLocale(Locale locale) {
        return locale.toString();
    }
}
