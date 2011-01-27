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

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.security.security.RemindPasswordTokenService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.gatein.wci.security.Credentials;

import java.net.URLEncoder;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jul 09, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UIForgetPassword.SendActionListener.class),
   @EventConfig(phase = Phase.DECODE, listeners = UIForgetPassword.BackActionListener.class)})
public class UIForgetPassword extends UIForm
{
   static final String Username = "username";

   static final String Email = "email";

   public UIForgetPassword() throws Exception
   {
      addUIFormInput(new UIFormStringInput(Username, null).addValidator(MandatoryValidator.class)).addUIFormInput(
         new UIFormStringInput(Email, null).addValidator(MandatoryValidator.class).addValidator(
            EmailAddressValidator.class));
   }

   static public class SendActionListener extends EventListener<UIForgetPassword>
   {
      public void execute(Event<UIForgetPassword> event) throws Exception
      {
         UIForgetPassword uiForm = event.getSource();
         UILogin uilogin = uiForm.getParent();
         WebuiRequestContext requestContext = event.getRequestContext();
         PortalRequestContext portalContext = PortalRequestContext.getCurrentInstance();
         String url = portalContext.getRequest().getRequestURL().toString();
         MailService mailSrc = uiForm.getApplicationComponent(MailService.class);
         OrganizationService orgSrc = uiForm.getApplicationComponent(OrganizationService.class);
         String userName = uiForm.getUIStringInput(Username).getValue();
         String email = uiForm.getUIStringInput(Email).getValue();
         uiForm.reset();
                  
         User user = null;

         String tokenId = null;
         
         // User provided his username
         if (userName != null)
         {
            user = orgSrc.getUserHandler().findUserByName(userName);
            if (user == null)
            {
               requestContext.getUIApplication().addMessage(
                  new ApplicationMessage("UIForgetPassword.msg.user-not-exist", null));
               return;
            }
         }
         
         // User provided his email address
         if (user == null && email != null)
         {
            Query query = new Query();
            // Querying on email won't work. PLIDM-12
            // Note that querying on email is inefficient as it loops over all users... 
            query.setEmail(email);
            PageList<User> users = orgSrc.getUserHandler().findUsers(query);
            if (users.getAll().size() > 0)
            {
            	user = users.getAll().get(0);
            }
            else
            {
               requestContext.getUIApplication().addMessage(
                  new ApplicationMessage("UIForgetPassword.msg.email-not-exist", null));
               return;
            }
         }
         
         email = user.getEmail();

         // Create token
         RemindPasswordTokenService tokenService = uiForm.getApplicationComponent(RemindPasswordTokenService.class);
         Credentials credentials = new Credentials(user.getUserName(), "");
         tokenId = tokenService.createToken(credentials);

         String portalName = URLEncoder.encode(Util.getUIPortal().getName(), "UTF-8");

         ResourceBundle res = requestContext.getApplicationResourceBundle();
         String headerMail = "headermail";
         String footerMail = "footer";
         try
         {
            headerMail =
               res.getString(uiForm.getId() + ".mail.header") + "\n\n" + res.getString(uiForm.getId() + ".mail.user")
                  + user.getUserName() + "\n"+ res.getString(uiForm.getId() + ".mail.link");
            footerMail = "\n\n\n" + res.getString(uiForm.getId() + ".mail.footer");
         }
         catch (MissingResourceException e)
         {
            e.printStackTrace();
         }
         String host = url.substring(0, url.indexOf(requestContext.getRequestContextPath()));
         String activeLink = host + requestContext.getRequestContextPath() + "/public/" + portalName
        	 		+ "?portal:componentId=UIPortal&portal:action=RecoveryPasswordAndUsername&tokenId=" 
        	 		+ tokenId;
         String mailText = headerMail + "\n" + activeLink + footerMail;
         try
         {
            mailSrc.sendMessage(res.getString("UIForgetPassword.mail.from"), email, res.getString("UIForgetPassword.mail.subject"), mailText);
         }
         catch(Exception e)
         {
            requestContext.getUIApplication().addMessage(
               new ApplicationMessage("UIForgetPassword.msg.send-mail-fail", null));
            requestContext.addUIComponentToUpdateByAjax(uilogin);
            
            return;
         }

         uilogin.getChild(UILoginForm.class).setRendered(true);
         uilogin.getChild(UIForgetPasswordWizard.class).setRendered(false);
         uilogin.getChild(UIForgetPassword.class).setRendered(false);
         requestContext.getUIApplication().addMessage(
            new ApplicationMessage("UIForgetPassword.msg.send-mail-success", null));
         requestContext.addUIComponentToUpdateByAjax(uilogin);
      }
   }

   static public class BackActionListener extends EventListener<UIForgetPassword>
   {
      public void execute(Event<UIForgetPassword> event) throws Exception
      {
         UILogin uilogin = event.getSource().getParent();
         uilogin.getChild(UILoginForm.class).setRendered(false);
         uilogin.getChild(UIForgetPasswordWizard.class).setRendered(true);
         uilogin.getChild(UIForgetPassword.class).setRendered(false);
         event.getSource().reset();
         event.getRequestContext().addUIComponentToUpdateByAjax(uilogin);
      }
   }
}
