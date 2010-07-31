package org.exoplatform.sample.webui.component;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "app:/groovy/webui/component/UISampleDownloadUpload.gtmpl", events = {@EventConfig(listeners = UISampleDownloadUpload.SubmitActionListener.class)})
public class UISampleDownloadUpload extends UIForm
{

   Map<String, String> data = new HashMap<String, String>();

   private String[] downloadLink;

   private String[] fileName;

   private String[] inputName;

   public UISampleDownloadUpload() throws Exception
   {
      addUIFormInput(new UIFormUploadInput("name0", "value0"));
      addUIFormInput(new UIFormUploadInput("name1", "value1", 100));
      addUIFormInput(new UIFormUploadInput("name2", "value2", 200));
   }

   public void setDownloadLink(String[] downloadLink)
   {
      this.downloadLink = downloadLink;
   }

   public String[] getDownloadLink()
   {
      return downloadLink;
   }

   public void setFileName(String[] fileName)
   {
      this.fileName = fileName;
   }

   public String[] getFileName()
   {
      return fileName;
   }

   public void setInputName(String[] inputName)
   {
      this.inputName = inputName;
   }

   public String[] getInputName()
   {
      return inputName;
   }

   static public class SubmitActionListener extends EventListener<UISampleDownloadUpload>
   {

      public void execute(Event<UISampleDownloadUpload> event) throws Exception
      {
         UISampleDownloadUpload uiForm = event.getSource();
         DownloadService dservice = uiForm.getApplicationComponent(DownloadService.class);
         String[] downloadLink = new String[3];
         String[] fileName = new String[3];
         String[] inputName = new String[3];
         for (int index = 0; index <= 2; index++)
         {
            UIFormUploadInput input = uiForm.getChildById("name" + index);
            UploadResource uploadResource = input.getUploadResource();
            if (uploadResource != null)
            {
               DownloadResource dresource =
                  new InputStreamDownloadResource(input.getUploadDataAsStream(), uploadResource.getMimeType());
               dresource.setDownloadName(uploadResource.getFileName());
               downloadLink[index] = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
               fileName[index] = uploadResource.getFileName();
               inputName[index] = "name" + index;
            }
         }

         uiForm.setDownloadLink(downloadLink);
         uiForm.setFileName(fileName);
         uiForm.setInputName(inputName);

         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent());
      }
   }
}
