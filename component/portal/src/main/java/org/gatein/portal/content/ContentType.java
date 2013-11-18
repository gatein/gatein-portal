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

package org.gatein.portal.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ServiceLoader;

import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.staxnav.StaxNavigator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class ContentType<S extends Serializable> {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(ModelUnmarshaller.class);

    /** . */
    private static final HashMap<String, ContentType<?>> REGISTRY = new HashMap<String, ContentType<?>>();

    /** . */
    private static final HashMap<String, ContentType<?>> TAG_REGISTRY = new HashMap<String, ContentType<?>>();

    /** . */
    private static final HashMap<ApplicationType<?>, ContentType<?>> APP_REGISTRY = new HashMap<ApplicationType<?>, ContentType<?>>();

    /** . */
    public static final ContentType<Serializable> UNKNOWN = new ContentType<Serializable>() {
        @Override
        public String getValue() {
            return "application/portlet";
        }
        @Override
        public ApplicationType<Serializable> getApplicationType() {
            return ApplicationType.UNKNOWN;
        }
        @Override
        public String getTagName() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Content<Serializable> readState(StaxNavigator<String> xml) {
            throw new UnsupportedOperationException();
        }
    };

    /** . */
    public static final ContentType<Portlet> PORTLET = PortletContentType.INSTANCE;

    /** . */
    public static final ContentType<org.exoplatform.portal.pom.spi.wsrp.WSRP> WSRP = WSRPContentType.INSTANCE;

    /** . */
    public static final ContentType<Gadget> GADGET = GadgetContentType.INSTANCE;

    static {
        register(PORTLET);
        register(WSRP);
        register(GADGET);
        Iterator<ContentType> i = ServiceLoader.load(ContentType.class).iterator();
        while (true) {
            try {
                if (i.hasNext()) {
                    ContentType<?> type = i.next();
                    register(type);
                } else {
                    break;
                }
            } catch (Exception e) {
                log.error("Could not load a content provider", e);
            }
        }
    }

    private static void register(ContentType<?> contentType) {
        REGISTRY.put(contentType.getValue(), contentType);
        TAG_REGISTRY.put(contentType.getTagName(), contentType);
        APP_REGISTRY.put(contentType.getApplicationType(), contentType);
    }

    protected ContentType() {
        REGISTRY.put(getValue(), this);
    }

    public static <S extends Serializable> ContentType<S> forValue(String mimeType) {
        return (ContentType<S>) REGISTRY.get(mimeType);
    }

    public static <S extends Serializable> ContentType<S> forTag(String tagName) {
        return (ContentType<S>) TAG_REGISTRY.get(tagName);
    }

    public static <S extends Serializable> ContentType<S> forApplicationType(ApplicationType<?> type) {
        return (ContentType<S>) APP_REGISTRY.get(type);
    }

    protected final void validate(boolean valid) {
        ModelUnmarshaller.validate(valid);
        if (!valid) {
            throw new RuntimeException("Parse exception");
        }
    }

    public abstract String getValue();

    /**
     * Legacy application type value.
     *
     * @return the application type
     */
    public abstract ApplicationType<S> getApplicationType();

    public abstract String getTagName();

    public abstract Content<S> readState(StaxNavigator<String> xml);
}
