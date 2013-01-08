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

package org.exoplatform.portal.webui.login;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL Author : dang.tung tungcnw@gmail.com Jul 09, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/UIForgetPasswordWizard.gtmpl", events = {
        @EventConfig(listeners = UIForgetPasswordWizard.NextActionListener.class),
        @EventConfig(phase = Phase.DECODE, listeners = UIForgetPasswordWizard.BackActionListener.class) })
public class UIForgetPasswordWizard extends UIForm {
    static final String Password_Radio = "forgotpassword";

    static final String Username_Radio = "forgotusername";

    static final String Forgot = "UIForgetPasswordWizard";

    public UIForgetPasswordWizard() {
        List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>(2);
        options.add(new SelectItemOption<String>(Password_Radio, "password"));
        options.add(new SelectItemOption<String>(Username_Radio, "username"));
        addUIFormInput(new UIFormRadioBoxInput(Forgot, null, options).setAlign(UIFormRadioBoxInput.VERTICAL_ALIGN));
    }

    public static class NextActionListener extends EventListener<UIForgetPasswordWizard> {
        public void execute(Event<UIForgetPasswordWizard> event) throws Exception {
            UILogin uilogin = event.getSource().getParent();
            uilogin.getChild(UILoginForm.class).setRendered(false);
            uilogin.getChild(UIForgetPasswordWizard.class).setRendered(false);
            UIForgetPassword uiForgetpassword = (UIForgetPassword) uilogin.getChild(UIForgetPassword.class).setRendered(true);
            String value = event.getSource().getChild(UIFormRadioBoxInput.class).getValue();
            UIFormStringInput uiEmail = uiForgetpassword.getUIStringInput(UIForgetPassword.Email);
            UIFormStringInput uiUser = uiForgetpassword.getUIStringInput(UIForgetPassword.Username);
            if (value.equals("password")) {
                uiEmail.setRendered(false);
                uiUser.setRendered(true);
            } else {
                uiEmail.setRendered(true);
                uiUser.setRendered(false);
            }
            event.getRequestContext().addUIComponentToUpdateByAjax(uilogin);
        }
    }

    public static class BackActionListener extends EventListener<UIForgetPasswordWizard> {
        public void execute(Event<UIForgetPasswordWizard> event) throws Exception {
            UILogin uilogin = event.getSource().getParent();
            uilogin.getChild(UILoginForm.class).setRendered(true);
            uilogin.getChild(UIForgetPasswordWizard.class).setRendered(false);
            uilogin.getChild(UIForgetPassword.class).setRendered(false);
            event.getRequestContext().addUIComponentToUpdateByAjax(uilogin);
        }
    }
}
