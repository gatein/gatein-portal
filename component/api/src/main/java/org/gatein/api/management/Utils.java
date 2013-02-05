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

package org.gatein.api.management;

import org.apache.commons.lang.LocaleUtils;
import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.common.i18n.Localized;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.internal.StringJoiner;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageId;
import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.api.site.SiteId;
import org.gatein.management.api.exceptions.InvalidDataException;
import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.exceptions.ResourceExistsException;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationContext;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class Utils {
    private Utils() {
    }

    public static void verifyAccess(Site site, OperationContext context) throws NotAuthorizedException {
        if (!hasPermission(site.getAccessPermission())) {
            throw new NotAuthorizedException(context.getUser(), context.getOperationName());
        }
    }

    public static void verifyAccess(Page page, OperationContext context) throws NotAuthorizedException {
        if (!hasPermission(page.getAccessPermission())) {
            throw new NotAuthorizedException(context.getUser(), context.getOperationName());
        }
    }

    public static boolean hasPermission(Permission permission) {
        PortalRequest request = PortalRequest.getInstance();
        Portal portal = request.getPortal();
        User user = request.getUser();

        return portal.hasPermission(user, permission);
    }

    public static void populate(String fieldName, Locale locale, ModelObject model) {
        if (locale == null) {
            model.set(fieldName, (String) null);
            return;
        }

        String localeString = locale.getLanguage();
        if (localeString == null) {
            throw new RuntimeException("Language was null for locale " + locale);
        }
        String country = locale.getCountry();
        if (country != null && country.length() > 0) {
            localeString += "-" + country.toLowerCase();
        }

        model.set(fieldName, localeString);
    }

    public static void populate(String fieldName, LocalizedString string, ModelObject model) {
        if (string == null)
            return;

        ModelList list = model.get(fieldName, ModelList.class);
        if (string.isLocalized()) {
            for (Localized.Value<String> value : string.getLocalizedValues()) {
                ModelObject localizedModel = list.add().asValue(ModelObject.class);
                localizedModel.set("value", value.getValue());
                populate("lang", value.getLocale(), localizedModel);
            }
        } else {
            list.add().asValue(ModelObject.class).set("value", string.getValue());
        }
    }

    public static void populate(String fieldName, Permission permission, ModelObject model) {
        if (permission != null) {
            ModelList list = model.get(fieldName, ModelList.class);
            if (permission.isAccessibleToEveryone()) {
                list.add("Everyone");
            }

            for (Membership membership : permission.getMemberships()) {
                list.add(membership.toString());
            }
        }
    }

    public static void set(String name, Object value, ModelObject model) {
        String s = (value == null) ? null : value.toString();
        model.set(name, s);
    }

    public static void set(String name, Date value, ModelObject model) {
        String s = null;
        if (value != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(value);
            s = DatatypeConverter.printDateTime(cal);
        }

        set(name, s, model);
    }

    public static <T extends ModelValue> T get(ModelObject modelObject, Class<T> type, String...names) {
        Model model = modelObject.get(names);
        try {
            return model.asValue(type);
        } catch (IllegalArgumentException e) {
            ModelValue.ModelValueType expected;
            if (type == ModelString.class) {
                expected = ModelValue.ModelValueType.STRING;
            } else if (type == ModelNumber.class) {
                expected = ModelValue.ModelValueType.NUMBER;
            } else if (type == ModelBoolean.class) {
                expected = ModelValue.ModelValueType.BOOLEAN;
            } else if (type == ModelList.class) {
                expected = ModelValue.ModelValueType.LIST;
            } else if (type == ModelObject.class) {
                expected = ModelValue.ModelValueType.OBJECT;
            } else if (type == ModelReference.class) {
                expected = ModelValue.ModelValueType.REFERENCE;
            } else {
                expected = ModelValue.ModelValueType.UNDEFINED;
            }
            throw invalidType(model, expected, names);
        }
    }

    public static String nonNullString(ModelObject model, String... names) {
        ModelString string = get(model, ModelString.class, names);
        if (!string.isDefined()) {
            throw invalidValue(null, names);
        }
        String value = string.getValue();
        if (value == null) {
            throw invalidValue(null, names);
        }

        return value;
    }

    public static Date getDate(ModelObject model, String...names) {
        String string = get(model, ModelString.class, names).getValue();
        if (string != null) {
            try {
                return DatatypeConverter.parseDateTime(string).getTime();
            } catch (IllegalArgumentException e) {
                throw invalidValue(string, names);
            }
        }

        return null;
    }

    public static Locale getLocale(ModelObject model, String...names) {
        String string = get(model, ModelString.class, names).getValue();
        if (string != null) {
            try {
                return LocaleUtils.toLocale(string);
            } catch (IllegalArgumentException e) {
                throw invalidValue(string, names);
            }
        }

        return null;
    }

    public static Permission getPermission(ModelObject model, boolean allowNull, String...names) {
        ModelList permissionsModel = get(model, ModelList.class, names);
        if (!allowNull && !permissionsModel.isDefined()) {
            throw invalidValue(null, names);
        }
        Permission permission = null;
        for (int i=0; i<permissionsModel.size(); i++) {
            ModelValue mv = permissionsModel.get(i);
            String field = resolveField(names) + "[" + i + "]"; // Used for error reporting
            if (mv.getValueType() != ModelValue.ModelValueType.STRING) {
                throw invalidType(mv, ModelValue.ModelValueType.STRING, field);
            }
            String perm = mv.asValue(ModelString.class).getValue();
            if (perm == null) {
                throw requiredFieldWhen("permissions are defined", field);
            }

            if (perm.equals("Everyone")) {
                if (permission != null) {
                    throw invalidData("Only one value is allowed when 'Everyone' is defined for %s", field);
                }
                permission = Permission.everyone();
            } else {
                if (permission != null && permission.isAccessibleToEveryone()) {
                    throw invalidData("Only one value is allowed when 'Everyone' is defined for %s", field);
                } else if (permission != null) {
                    permission.addMembership(Membership.fromString(perm));
                } else {
                    permission = new Permission(Membership.fromString(perm));
                }
            }
        }

        return permission;
    }

    public static InvalidDataException invalidValue(String value, String...names) {
        return invalidData("Invalid value '" + value + "' for %s", names);
    }

    public static InvalidDataException invalidType(ModelValue value, ModelValue.ModelValueType type, String...names) {
        return invalidData("Invalid value type " + value.getValueType() + " for %s. Was expecting " + type, names);
    }

    public static InvalidDataException requiredField(String...names) {
        return invalidData("%s is required", names);
    }

    public static InvalidDataException requiredFieldWhen(String when, String...names) {
        return invalidData("%s is required when " + when, names);
    }

    public static InvalidDataException invalidData(String format, String... names) {
        if (names == null) {
            throw new InvalidDataException(format);
        }
        return new InvalidDataException(String.format(format, resolveField(names)));
    }

    public static String resolveField(String...names) {
        return StringJoiner.joiner(".").join(names);
    }

    public static ResourceExistsException alreadyExists(String message, SiteId id) {
        return new ResourceExistsException(message + ". Site " + id + " already exists.");
    }

    public static ResourceExistsException alreadyExists(String message, PageId id) {
        return new ResourceExistsException(message + ". Page " + id.getPageName() + " already exists for site " + id.getSiteId());
    }

    public static ResourceExistsException alreadyExists(String message, SiteId id, NodePath nodePath) {
        return new ResourceExistsException(message + ". Node " + nodePath + " already exists for site " + id);
    }

    public static ResourceExistsException notFound(String message, SiteId id) {
        return new ResourceExistsException(message + ". Site " + id + " does not exist.");
    }

    public static ResourceExistsException notFound(String message, PageId id) {
        return new ResourceExistsException(message + ". Page " + id.getPageName() + " does not exist for site " + id.getSiteId());
    }

    public static ResourceExistsException notFound(String message, SiteId id, NodePath nodePath) {
        return new ResourceExistsException(message + ". Node " + nodePath + " does not exist for site " + id);
    }
}
