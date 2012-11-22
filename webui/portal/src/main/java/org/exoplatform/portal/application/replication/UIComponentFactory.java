/*
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

package org.exoplatform.portal.application.replication;

import java.util.Map;

import org.exoplatform.commons.serialization.api.factory.CreateException;
import org.exoplatform.commons.serialization.api.factory.ObjectFactory;
import org.exoplatform.commons.serialization.model.FieldModel;
import org.exoplatform.webui.Util;
import org.exoplatform.webui.config.Component;
import org.exoplatform.webui.core.UIComponent;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UIComponentFactory extends ObjectFactory<UIComponent> {

    private <S extends UIComponent> Object getFieldValue(String fieldName, Map<FieldModel<? super S, ?>, ?> state) {
        for (Map.Entry<FieldModel<? super S, ?>, ?> entry : state.entrySet()) {
            FieldModel<? super S, ?> fieldModel = entry.getKey();
            if (fieldModel.getOwner().getJavaType() == UIComponent.class && fieldModel.getName().equals(fieldName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public <S extends UIComponent> S create(Class<S> type, Map<FieldModel<? super S, ?>, ?> state) throws CreateException {
        //
        try {
            Component config = (Component) getFieldValue("config", state);

            S instance = Util.createObject(type, config != null ? config.getInitParams() : null);

            // Now set state
            for (Map.Entry<FieldModel<? super S, ?>, ?> entry : state.entrySet()) {
                FieldModel<? super S, ?> fieldModel = entry.getKey();
                Object value = entry.getValue();
                fieldModel.castAndSet(instance, value);
            }

            //
            return instance;
        } catch (Exception e) {
            throw new CreateException(e);
        }
    }
}
