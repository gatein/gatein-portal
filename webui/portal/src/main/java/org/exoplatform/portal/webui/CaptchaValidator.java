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

package org.exoplatform.portal.webui;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.Validator;

import nl.captcha.Captcha;

import java.io.Serializable;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

/**
 * @author <a href="mailto:theute@redhat.com">Thomas Heute</a>
 * Validator for Captcha content.
 * Checks that the user input is equals to the content displayed by the
 * distorted image.
 */
public class CaptchaValidator implements Validator, Serializable
{

   public void validate(UIFormInput uiInput) throws Exception
   {
      PortletRequestContext ctx = PortletRequestContext.getCurrentInstance();
      PortletRequest req = ctx.getRequest();
      PortletSession session = req.getPortletSession();

      Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);

      if ((captcha != null) && (captcha.isCorrect((String) uiInput.getValue())))
      {
         return;
      }

      //modified by Pham Dinh Tan
      UIComponent uiComponent = (UIComponent) uiInput;
      UIForm uiForm = uiComponent.getAncestorOfType(UIForm.class);
      String label;
      try
      {
         label = uiForm.getLabel(uiInput.getName());
      }
      catch (Exception e)
      {
         label = uiInput.getName();
      }
      label = label.trim();
      if (label.charAt(label.length() - 1) == ':')
         label = label.substring(0, label.length() - 1);
      Object[] args =
      {label, uiInput.getBindingField()};
      throw new MessageException(new ApplicationMessage("CaptchaValidator.msg.Invalid-input", args,
            ApplicationMessage.WARNING));
   }
}
