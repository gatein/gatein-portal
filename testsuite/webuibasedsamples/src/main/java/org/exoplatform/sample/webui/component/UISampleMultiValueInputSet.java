package org.exoplatform.sample.webui.component;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.exoplatform.upload.UploadResource;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormUploadInput;
import org.exoplatform.webui.form.ext.UIFormColorPicker;
import org.exoplatform.webui.form.ext.UIFormColorPicker.Colors;
import org.exoplatform.webui.form.ext.UIFormColorPicker.Colors.Color;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl", events = {@EventConfig(listeners = UISampleMultiValueInputSet.SubmitActionListener.class)})
public class UISampleMultiValueInputSet extends UIForm
{

   public static final String MULTI_UPLOAD = "UploadInput";

   public static final String MULTI_DATE = "DateTimeInput";

   public static final String MULTI_COLOR = "ColorInput";

   public static final String MULTI_STRING = "StringInput";

   public static final String MULTI_TEXTAREA = "TextAreaInput";

   public static final String[] ACTIONS = {"Submit"};

   public UISampleMultiValueInputSet() throws Exception
   {
      UIFormMultiValueInputSet uiFormMultiValueInputSet;
      
      // UIFormUploadInput
      addUIFormInput(makeMultiValueInputSet(MULTI_UPLOAD, UIFormUploadInput.class, new Class[] {String.class, String.class, int.class}));
      // UIFormDateTimeInput
      addUIFormInput(makeMultiValueInputSet(MULTI_DATE, UIFormDateTimeInput.class));

      // UIFormColorPicker
      uiFormMultiValueInputSet = new UIFormMultiValueInputSet(MULTI_COLOR, MULTI_COLOR);
      uiFormMultiValueInputSet.setType(UIFormColorPicker.class);
      uiFormMultiValueInputSet.setConstructorParameterTypes(new Class[] {String.class, String.class, String.class});
      uiFormMultiValueInputSet.setConstructorParameterValues(new Object[] {"ABC", "XYZ", null});
      addUIFormInput(uiFormMultiValueInputSet);

      // UIFormStringInput
      addUIFormInput(makeMultiValueInputSet(MULTI_STRING, UIFormStringInput.class));

      // UIFormTextAreaInput
      addUIFormInput(makeMultiValueInputSet(MULTI_TEXTAREA, UIFormTextAreaInput.class));

      setActions(ACTIONS);
   }

   @SuppressWarnings("unchecked")
   private UIFormInput makeMultiValueInputSet(String name, Class<? extends UIFormInputBase> type) throws Exception
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      return multiInput;
   }
   
   private UIFormInput makeMultiValueInputSetHasValue(String name, Class<? extends UIFormInputBase> type, Object[] parameterValues) throws Exception
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      multiInput.setConstructorParameterTypes(new Class[] {String.class, String.class, String.class});
      multiInput.setConstructorParameterValues(parameterValues);
      return multiInput;
   }
   
   private UIFormInput makeMultiValueInputSet(String name, Class<? extends UIFormInputBase> type, Class<?>... parameterTypes) throws Exception 
   {
      UIFormMultiValueInputSet multiInput = new UIFormMultiValueInputSet(name, null);
      multiInput.setType(type);
      multiInput.setConstructorParameterTypes(parameterTypes);
      return multiInput;
   }

   static public class SubmitActionListener extends EventListener<UISampleMultiValueInputSet>
   {
      @Override
      public void execute(Event<UISampleMultiValueInputSet> event) throws Exception
      {
         WebuiRequestContext rcontext = event.getRequestContext();
         rcontext.getUIApplication().addMessage(makeMsg(event.getSource()));
      }

      @SuppressWarnings("unchecked")
      private ApplicationMessage makeMsg(UISampleMultiValueInputSet uiForm)
      {
         StringBuilder msgBuild = new StringBuilder();

         for (UIComponent child : uiForm.getChildren())
         {
            UIFormMultiValueInputSet multiInput = (UIFormMultiValueInputSet)child;

            if (multiInput.getUIFormInputBase().equals(UIFormUploadInput.class))
            {
               makeUploadInputMsg(multiInput, msgBuild);
            }
            else if (multiInput.getUIFormInputBase().equals(UIFormDateTimeInput.class))
            {
               makeDateInputMsg(multiInput, msgBuild);
            }
            else
            {
               for (UIComponent multiInputChild : multiInput.getChildren())
               {
                  msgBuild.append(" " + ((UIFormInputBase)multiInputChild).getValue());
               }
            }
            msgBuild.append("<br/>");
         }

         return new ApplicationMessage(msgBuild.toString().replace(".", "*"), null);
      }

      private void makeDateInputMsg(UIFormMultiValueInputSet multiInput, StringBuilder msgBuild)
      {
         for (UIComponent multiInputChild : multiInput.getChildren())
         {
            UIFormDateTimeInput dateInput = (UIFormDateTimeInput)multiInputChild;
            Calendar calendar = dateInput.getCalendar();
            if (calendar != null)
            {
               SimpleDateFormat dateFormat = new SimpleDateFormat(dateInput.getDatePattern_());
               msgBuild.append(" " + dateFormat.format(dateInput.getCalendar().getTime()));
            }
            else
            {
               msgBuild.append("null");
            }
         }
      }

      private void makeUploadInputMsg(UIFormMultiValueInputSet multiInput, StringBuilder msgBuild)
      {
         for (UIComponent multiInputChild : multiInput.getChildren())
         {
            UploadResource uploadResource = ((UIFormUploadInput)multiInputChild).getUploadResource();
            if (uploadResource != null)
            {
               msgBuild.append(" " + uploadResource.getFileName());
            }
            else
            {
               msgBuild.append("null");
            }
         }
      }
   }
}
