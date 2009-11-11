package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.applicationregistry.webui.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
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
   @EventConfig(listeners = UICategorySelector.CancelActionListener.class, phase = Phase.DECODE)})
public class UICategorySelector extends UIForm
{
   private List<ApplicationCategory> categories;

   private Application application;

   private final static String[] ACTIONS = new String[]{"Save", "Cancel"};

   private final static String[] TABLE_COLUMNS = {"choose", "categoryName"};

   public UICategorySelector()
   {
   }

   @Override
   public boolean isRendered()
   {
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> categories;
      try
      {
         categories = appRegService.getApplicationCategories();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      categories = categories != null ? categories : new ArrayList<ApplicationCategory>();
      if (categories.isEmpty())
         return false;
      return super.isRendered();
   }

   /**
    * @see Refresh each render time
    */
   @Override
   public void processRender(WebuiRequestContext context) throws Exception
   {
      setChildren(null);
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> categories = appRegService.getApplicationCategories(new Util.CategoryComparator());
      categories = categories != null ? categories : new ArrayList<ApplicationCategory>();

      UIFormTableInputSet uiTableInputSet = createUIComponent(UIFormTableInputSet.class, null, null);
      uiTableInputSet.setName(getClass().getSimpleName());
      uiTableInputSet.setId(getClass().getSimpleName());
      uiTableInputSet.setColumns(TABLE_COLUMNS);
      addChild(uiTableInputSet);

      UIFormInputSet uiInputSet;
      UIFormCheckBoxInput<Boolean> checkBoxInput;
      UIFormInputInfo uiInfo;
      for (ApplicationCategory category : categories)
      {
         uiInputSet = new UIFormInputSet(category.getName());
         boolean defaultValue = false;
         if (application != null)
         {
            String definitionName = application.getDisplayName().replace(' ', '_');
            defaultValue =
               appRegService.getApplication(category.getName(), definitionName) != null;
         }
         checkBoxInput = new UIFormCheckBoxInput<Boolean>("category_" + category.getName(), null, defaultValue);
         uiInfo = new UIFormInputInfo("categoryName", null, category.getDisplayName());
         uiInputSet.addChild(checkBoxInput);
         uiInputSet.addChild(uiInfo);
         uiTableInputSet.addChild(uiInputSet);
      }

      super.processRender(context);
   }

   public String[] getActions()
   {
      return ACTIONS;
   }   

   public void setApplication(Application app)
   {
      this.application = app;
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
         ApplicationRegistryService appRegService = selector.getApplicationComponent(ApplicationRegistryService.class);
         List<ApplicationCategory> categories = appRegService.getApplicationCategories();
         categories = categories != null ? categories : new ArrayList<ApplicationCategory>();
         UIFormCheckBoxInput<Boolean> chkInput;         
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
         newApp.setType(app.getType());
         newApp.setDescription(app.getDescription());
         newApp.setAccessPermissions(app.getAccessPermissions());
         newApp.setContentId(app.getContentId());
         return newApp;
      }
   }

   static public class CancelActionListener extends EventListener<UICategorySelector>
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
