/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.sample.webui.component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.ext.UIFormColorPicker;
import org.exoplatform.webui.form.ext.UIFormComboBox;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS Author : Nguyen Duc Khoi
 * khoi.nguyen@exoplatform.com Mar 12, 2010
 */

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {
   @EventConfig(listeners = UISampleUIForm.SearchUserActionListener.class),
   @EventConfig(listeners = UISampleUIForm.SaveActionListener.class),
   @EventConfig(listeners = UISampleUIForm.ResetActionListener.class)})
public class UISampleUIForm extends UIForm
{
   private final static String[] ACTIONS = {"Save", "Reset"};

   public static final String USERNAME = "userName";

   public static final String PASSWORD = "password";

   public static final String FAVORITE_COLOR = "favoriteColor";

   public static final String POSITION = "position";

   public static final String RECEIVE_EMAIL = "receiveEmail";

   public static final String GENDER = "gender";

   private static final String DATE_OF_BIRTH = "dateOfBirth";

   private static final String DESCRIPTION = "description";
   
   private static final String COMBOBOX = "ComboBox";

   private List<String> userNames = new ArrayList<String>();

   public UISampleUIForm() throws Exception
   {
      addUIFormInput(makeUIFormInputWithActions());
      setActions(ACTIONS);
   }

   private UIFormInputWithActions makeUIFormInputWithActions() throws Exception
   {
      UIFormInputWithActions inputSet = new UIFormInputWithActions();
      addUIComponentToSet(inputSet);
      
      List<ActionData> searchUserAction = makeSearchUserActionData();
      inputSet.setActionField(USERNAME, searchUserAction);

      return inputSet;
   }

   private void addUIComponentToSet(UIFormInputWithActions inputSet) throws Exception
   {
      inputSet.addUIFormInput(new UIFormStringInput(USERNAME, null, null).addValidator(MandatoryValidator.class));
      inputSet.addUIFormInput(new UIFormStringInput(PASSWORD, null, null).setType(UIFormStringInput.PASSWORD_TYPE));
      inputSet.addUIFormInput(new UIFormColorPicker(FAVORITE_COLOR, null, UIFormColorPicker.Colors.N_RED));
      inputSet.addUIFormInput(new UIFormRadioBoxInput(GENDER, "Male", makeRadioOptions())
         .addValidator(MandatoryValidator.class));
      inputSet.addUIFormInput(new UIFormDateTimeInput(DATE_OF_BIRTH, null, new Date()));
      inputSet.addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, null, ""));

      List<SelectItemOption<String>> selectItemOptions = makeSelectItemOptions();
      inputSet.addUIFormInput(new UIFormSelectBox(POSITION, null, selectItemOptions));
      inputSet.addUIFormInput(new UICheckBoxInput(RECEIVE_EMAIL, null, false));
      
      List<SelectItemOption<String>> comboBoxItemOptions = new ArrayList<SelectItemOption<String>>();
      comboBoxItemOptions.add(new SelectItemOption<String>(("VI")));
      comboBoxItemOptions.add(new SelectItemOption<String>(("UK")));
      comboBoxItemOptions.add(new SelectItemOption<String>(("FR")));
      inputSet.addUIFormInput(new UIFormComboBox(COMBOBOX, COMBOBOX, comboBoxItemOptions));
   }

   private List<SelectItemOption<String>> makeRadioOptions()
   {
      List<SelectItemOption<String>> selectItemOptions = new ArrayList<SelectItemOption<String>>();
      selectItemOptions.add(new SelectItemOption<String>("Male"));
      selectItemOptions.add(new SelectItemOption<String>("Female"));
      return selectItemOptions;
   }

   private List<SelectItemOption<String>> makeSelectItemOptions()
   {
      List<SelectItemOption<String>> selectItemOptions = new ArrayList<SelectItemOption<String>>();
      selectItemOptions.add(new SelectItemOption<String>("Boss"));
      selectItemOptions.add(new SelectItemOption<String>("Employee"));
      return selectItemOptions;
   }

   private List<ActionData> makeSearchUserActionData()
   {
      List<ActionData> actions = new ArrayList<ActionData>();
      ActionData searchUser = new ActionData();
      searchUser.setActionListener("SearchUser");
      searchUser.setActionType(ActionData.TYPE_ICON);
      searchUser.setActionName("SearchUser");
      searchUser.setCssIconClass("SearchIcon");
      actions.add(searchUser);
      return actions;
   }
   
   public boolean isUserExist(String userName)
   {
      return userNames.contains(userName);
   }

   public void saveUser(String userName)
   {
      userNames.add(userName);
   }

   static public class SearchUserActionListener extends EventListener<UISampleUIForm>
   {

      @Override
      public void execute(Event<UISampleUIForm> event) throws Exception
      {
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();

         UISampleUIForm uiForm = event.getSource();
         String inputUserName = uiForm.getUIStringInput(USERNAME).getValue().trim();
         String[] i18nParams = new String[]{inputUserName};

         if (inputUserName.equals(""))
         {
            uiApp.addMessage(new ApplicationMessage("UISampleUIForm.msg.empty-input", null, ApplicationMessage.ERROR));
         }
         else if (uiForm.isUserExist(inputUserName))
         {
            uiApp.addMessage(new ApplicationMessage("UISampleUIForm.msg.user-exist", i18nParams,
               ApplicationMessage.WARNING));
         }
         else
         {
            uiApp.addMessage(new ApplicationMessage("UISampleUIForm.msg.user-not-exist", i18nParams,
               ApplicationMessage.INFO));
         }
      }
   }

   static public class SaveActionListener extends EventListener<UISampleUIForm>
   {

      @Override
      public void execute(Event<UISampleUIForm> event) throws Exception
      {
         WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
         UIApplication uiApp = context.getUIApplication();

         UISampleUIForm uiForm = event.getSource();
         String inputUserName = uiForm.getUIStringInput(USERNAME).getValue();
         String[] i18nParams = new String[]{inputUserName};

         if (uiForm.isUserExist(inputUserName))
         {
            uiApp.addMessage(new ApplicationMessage("UISampleUIForm.msg.user-exist", i18nParams,
               ApplicationMessage.WARNING));
         }
         else
         {
            uiForm.saveUser(inputUserName);
            uiApp.addMessage(new ApplicationMessage("UISampleUIForm.msg.user-saved", i18nParams,
               ApplicationMessage.INFO));
         }

         String traceMsg = makeTraceMsg(event);
         uiApp.addMessage(new ApplicationMessage(traceMsg, null, ApplicationMessage.INFO));
      }

      private String makeTraceMsg(Event<UISampleUIForm> event) throws Exception
      {
         UISampleUIForm uiForm = event.getSource();
         String userName = uiForm.getUIStringInput(USERNAME).getValue();
         String password = uiForm.getUIStringInput(PASSWORD).getValue();
         boolean receiveEmail = uiForm.getUICheckBoxInput(RECEIVE_EMAIL).isChecked();
         String favoriteColor = (String)uiForm.getUIInput(FAVORITE_COLOR).getValue();
         String position = (String)uiForm.getUIInput(POSITION).getValue();
         String gender = (String)uiForm.getUIInput(GENDER).getValue();

         UIFormDateTimeInput dateInput = uiForm.getUIFormDateTimeInput(DATE_OF_BIRTH);
         SimpleDateFormat dateFormat = new SimpleDateFormat(dateInput.getDatePattern_());
         Calendar calendar = dateInput.getCalendar();
         String dateOfBirth = calendar == null ? null : dateFormat.format(calendar.getTime());
         

         StringBuilder strBuilder = new StringBuilder();
         strBuilder.append(userName);
         strBuilder.append("<br/>");
         strBuilder.append(password);        
         strBuilder.append("<br/>");
         strBuilder.append(favoriteColor);
         strBuilder.append("<br/>");
         strBuilder.append(gender);
         strBuilder.append("<br/>");
         strBuilder.append(dateOfBirth);
         strBuilder.append("<br/>");
         strBuilder.append(position);                 
         strBuilder.append("<br/>");
         strBuilder.append(receiveEmail);
         return strBuilder.toString();
      }
   }

   static public class ResetActionListener extends EventListener<UISampleUIForm>
   {

      @SuppressWarnings("unchecked")
      @Override
      public void execute(Event<UISampleUIForm> event) throws Exception
      {
         UIFormInputSet inputSet = event.getSource().getChild(UIFormInputSet.class);
         for (UIComponent child : inputSet.getChildren())
         {
            if (child instanceof UIFormColorPicker)
            {
               continue;
            }
            ((UIFormInput)child).reset();
         }
      }
   }  
}
