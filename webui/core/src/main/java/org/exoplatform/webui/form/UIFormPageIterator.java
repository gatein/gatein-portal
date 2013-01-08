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

package org.exoplatform.webui.form;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jul 9, 2007
 *
 * Represents a multi page iterator
 */

import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.validator.Validator;

@ComponentConfig(template = "system:/groovy/webui/core/UIPageIterator.gtmpl", events = @EventConfig(listeners = UIFormPageIterator.ShowPageActionListener.class, phase = Phase.DECODE))
@Serialized
public class UIFormPageIterator extends UIPageIterator implements UIFormInput<Object> {

    public void processDecode(WebuiRequestContext context) throws Exception {
        UIForm uiForm = getAncestorOfType(UIForm.class);
        String action = uiForm.getSubmitAction();
        if (action == null)
            return;
        Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
        if (event != null)
            event.broadcast();
    }

    public String event(String name, String beanId) throws Exception {
        UIForm uiForm = getAncestorOfType(UIForm.class);
        return uiForm.event(name, getId(), beanId);
    }

    @SuppressWarnings("unused")
    public static class ShowPageActionListener extends EventListener<UIFormPageIterator> {
        public void execute(Event<UIFormPageIterator> event) throws Exception {
            UIFormPageIterator uiPageIterator = event.getSource();
            int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
            uiPageIterator.setCurrentPage(page);
            UIForm uiForm = uiPageIterator.getAncestorOfType(UIForm.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
        }
    }

    @SuppressWarnings("unused")
    public <E extends Validator> UIFormInput addValidator(Class<E> clazz, Object... params) throws Exception {
        return this;
    }

    public String getBindingField() {
        return null;
    }

    public List getValidators() {
        return null;
    }

    public Object getValue() throws Exception {
        return null;
    }

    public void reset() {
    }

    public String getLabel() {
        return getName();
    }

    public Class getTypeValue() {
        return null;
    }

    @SuppressWarnings("unused")
    public UIFormInput setValue(Object value) throws Exception {
        return null;
    }

}
