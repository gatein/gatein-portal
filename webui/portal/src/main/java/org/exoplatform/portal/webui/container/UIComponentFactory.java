/**
 * Copyright (C) 2014 eXo Platform SAS.
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

package org.exoplatform.portal.webui.container;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;

@SuppressWarnings("unchecked")
public abstract class UIComponentFactory<T extends UIComponent> {
    public static String DEFAULT_FACTORY_ID = "";

    protected static Map<Class<?>, List<UIComponentFactory<?>>> componentFactory = new HashMap<Class<?>, List<UIComponentFactory<?>>>();

    static {
        @SuppressWarnings("rawtypes")
        ServiceLoader<UIComponentFactory> loader = ServiceLoader.load(UIComponentFactory.class);
        for (UIComponentFactory<? extends UIComponent> factory : loader) {
            Type genericSuper = factory.getClass().getGenericSuperclass();

            Class<?> supportedType = UIComponent.class;
            if (genericSuper instanceof ParameterizedType) {
                supportedType = (Class<?>) ((ParameterizedType) genericSuper).getActualTypeArguments()[0];
            }

            List<UIComponentFactory<?>> factories = componentFactory.get(supportedType);
            if (factories == null) {
                factories = new LinkedList<UIComponentFactory<?>>();
                componentFactory.put(supportedType, factories);
            }

            factories.add(factory);
        }
    }

    public static <T extends UIComponent> UIComponentFactory<? extends T> getInstance(Class<T> type) {
        if (type == null) {
            throw new NullPointerException(type + " is null");
        }

        final List<UIComponentFactory<? extends T>> list = new LinkedList<UIComponentFactory<? extends T>>();
        for (Class<?> t : componentFactory.keySet()) {
            if (type.isAssignableFrom(t)) {
                for (UIComponentFactory<?> factory : componentFactory.get(t)) {
                    list.add((UIComponentFactory<? extends T>)factory);
                }
            }
        }

        return new UIComponentFactory<T>() {
            @Override
            public T createUIComponent(String factoryID, WebuiRequestContext context) {
                T uiComponent = null;
                for (UIComponentFactory<? extends T> f : list) {
                    uiComponent = f.createUIComponent(factoryID, context);
                    if (uiComponent != null) {
                        break;
                    }
               }
                return uiComponent;
            }
        };
    }

    protected T create(Class<? extends T> type, WebuiRequestContext context) {
        try {
            if (context == null) {
                return type.newInstance();
            }

            WebuiApplication app = (WebuiApplication) context.getApplication();
            return app.createUIComponent(type, null, null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public T createUIComponent(String factoryID) {
        return createUIComponent(factoryID, WebuiRequestContext.<WebuiRequestContext> getCurrentInstance());
    }

    public abstract T createUIComponent(String factoryID, WebuiRequestContext context);
}