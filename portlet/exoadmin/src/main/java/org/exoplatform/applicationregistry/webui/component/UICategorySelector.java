package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormTableInputSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : LiemNC  Email:ncliam@gmail.com
 * November 09, 2009  
 */
@ComponentConfig(template = "system:/groovy/webui/form/UIForm.gtmpl", lifecycle = UIFormLifecycle.class, events = {
   @EventConfig(listeners = UICategorySelector.SaveActionListener.class),
   @EventConfig(listeners = UICategorySelector.CloseActionListener.class, phase = Phase.DECODE)})
public class UICategorySelector extends UIForm
{
   private List<ApplicationCategory> categories;

   private Application application;

   private final static String[] ACTIONS = new String[]{"Save", "Cancel"};

   private final static String[] TABLE_COLUMNS = {"choose", "categoryName"};

   public UICategorySelector() throws Exception
   {
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      categories = appRegService.getApplicationCategories();
      categories = categories != null ? categories : new ArrayList<ApplicationCategory>();
   }

   public void setup(Application app) throws Exception
   {
      setChildren(null);
      this.application = app;

      UIFormTableInputSet uiTableInputSet = createUIComponent(UIFormTableInputSet.class, null, null);
      uiTableInputSet.setName(getClass().getSimpleName());
      uiTableInputSet.setId(getClass().getSimpleName());
      uiTableInputSet.setColumns(TABLE_COLUMNS);
      addChild(uiTableInputSet);

      UIFormInputSet uiInputSet;
      UIFormCheckBoxInput<Boolean> checkBoxInput;
      UIFormInputInfo uiInfo;

      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      for (ApplicationCategory category : categories)
      {
         uiInputSet = new UIFormInputSet(category.getName());
         boolean defaultValue = appRegService.getApplication(category.getName(), app.getApplicationName()) != null;
         checkBoxInput = new UIFormCheckBoxInput<Boolean>("category_" + category.getName(), null, defaultValue);
         uiInfo = new UIFormInputInfo("categoryName", null, category.getDisplayName());
         uiInputSet.addChild(checkBoxInput);
         uiInputSet.addChild(uiInfo);
         uiTableInputSet.addChild(uiInputSet);
      }
   }

   public String[] getActions()
   {
      return ACTIONS;
   }

   public List<ApplicationCategory> getCategories()
   {
      return this.categories;
   }

   public Application getApplication()
   {
      return this.application;
   }

   static public class SaveActionListener extends EventListener<UICategorySelector>
   {
      public void execute(Event<UICategorySelector> event) throws Exception
      {
         UICategorySelector selector = event.getSource();
         List<ApplicationCategory> categories = selector.getCategories();
         UIFormCheckBoxInput<Boolean> chkInput;
         ApplicationRegistryService appRegService = selector.getApplicationComponent(ApplicationRegistryService.class);
         for (ApplicationCategory category : categories)
         {            
            chkInput = selector.getUIInput("category_" + category.getName());
            if (chkInput != null && chkInput.isChecked())
            {
               appRegService.save(category, cloneApplication(selector.getApplication()));
            }
         }
         UIGadgetInfo gadgetInfo = selector.getParent();
         gadgetInfo.getChild(UICategorySelector.class).setRendered(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(gadgetInfo);
      }

      private Application cloneApplication(Application app)
      {
         Application newApp = new Application();
         newApp.setApplicationName(app.getApplicationName());
         newApp.setDisplayName(app.getDisplayName());
         newApp.setApplicationType(app.getApplicationType());
         //newApp.setApplicationGroup(app.getApplicationGroup());
         newApp.setDescription(app.getDescription());
         newApp.setAccessPermissions(app.getAccessPermissions());
         //newApp.setUri(app.getUri());
         return newApp;
      }
   }

   static public class CloseActionListener extends EventListener<UICategorySelector>
   {
      public void execute(Event<UICategorySelector> event) throws Exception
      {
         UICategorySelector selector = event.getSource();
         UIGadgetInfo gadgetInfo = selector.getParent();
         gadgetInfo.getChild(UICategorySelector.class).setRendered(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(gadgetInfo);
      }

   }

}
