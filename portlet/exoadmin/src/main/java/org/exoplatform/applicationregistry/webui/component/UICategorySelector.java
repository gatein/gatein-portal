package org.exoplatform.applicationregistry.webui.component;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.applicationregistry.webui.Util;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.commons.utils.SerializablePageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormPageIterator;

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
@Serialized
public class UICategorySelector extends UIForm
{
   private Application application;

   private final static String[] ACTIONS = new String[]{"Save", "Cancel"};

   private final static String[] TABLE_COLUMNS = {"choose", "categoryName"};

   public UICategorySelector() throws Exception
   {
      init();
   }

   public List<ApplicationCategory> getAllCategories()
   {
      try
      {
         ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
         List<ApplicationCategory> categories = appRegService.getApplicationCategories(new Util.CategoryComparator());
         categories = categories != null ? categories : new ArrayList<ApplicationCategory>();
         return categories;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   public boolean isRendered()
   {
      return (getAllCategories().size() > 0 && super.isRendered());
   }

   public void init() throws Exception
   {      
      UIFormTableIteratorInputSet uiTableInputSet = createUIComponent(UIFormTableIteratorInputSet.class, null, null);
      uiTableInputSet.setName(getClass().getSimpleName());
      uiTableInputSet.setId(getClass().getSimpleName());
      uiTableInputSet.setColumns(TABLE_COLUMNS);
      addChild(uiTableInputSet);

      UIFormInputSet uiInputSet;
      UIFormCheckBoxInput<Boolean> checkBoxInput;
      UIFormInputInfo uiInfo;

      HTMLEntityEncoder encoder = HTMLEntityEncoder.getInstance();

      //
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> categories = getAllCategories();
      List<UIFormInputSet> uiInputSetList = new ArrayList<UIFormInputSet>();
      for (ApplicationCategory category : categories)
      {
         uiInputSet = new UIFormInputSet(category.getName());
         boolean defaultValue = false;
         if (application != null)
         {
            String definitionName = application.getDisplayName().replace(' ', '_');
            defaultValue = appRegService.getApplication(category.getName(), definitionName) != null;
         }
         checkBoxInput = new UIFormCheckBoxInput<Boolean>("category_" + category.getName(), null, defaultValue);
         uiInfo = new UIFormInputInfo("categoryName", null, encoder.encode(category.getDisplayName(true)));
         uiInputSet.addChild(checkBoxInput);
         uiInputSet.addChild(uiInfo);
         uiTableInputSet.addChild(uiInputSet);
         uiInputSetList.add(uiInputSet);
      }
      
      UIFormPageIterator uiIterator = uiTableInputSet.getChild(UIFormPageIterator.class);
      SerializablePageList<UIFormInputSet> pageList = new SerializablePageList<UIFormInputSet>(
         UIFormInputSet.class, uiInputSetList, 5
      );
      uiIterator.setPageList(pageList);
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
               Application newApp = cloneApplication(selector.getApplication());
               UIApplicationRegistryPortlet.setPermissionToEveryone(newApp);
               appRegService.save(category, newApp);
            }
         }
         UIContainer appInfo = selector.getParent();
         appInfo.getChild(UICategorySelector.class).setRendered(false);
         UIApplicationRegistryPortlet uiPortlet = appInfo.getAncestorOfType(UIApplicationRegistryPortlet.class);
         UIApplicationOrganizer uiOrganizer = uiPortlet.getChild(UIApplicationOrganizer.class);
         UIGadgetManagement uiGadgetManagement = uiPortlet.getChild(UIGadgetManagement.class);

         uiOrganizer.reload();
         if (uiGadgetManagement != null) 
         {
            uiGadgetManagement.setSelectedGadget(selector.getApplication().getApplicationName());
         } 

         event.getRequestContext().addUIComponentToUpdateByAjax(appInfo);
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
         UIContainer appInfo = selector.getParent();
         appInfo.getChild(UICategorySelector.class).setRendered(false);
         event.getRequestContext().addUIComponentToUpdateByAjax(appInfo);
      }

   }

   static public class ShowPageActionListener extends EventListener<UICategorySelector>
   {
      public void execute(Event<UICategorySelector> event) throws Exception
      {
         UICategorySelector selector = event.getSource();
         int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
         UIFormTableIteratorInputSet inputSet = selector.getChild(UIFormTableIteratorInputSet.class);
         inputSet.getUIFormPageIterator().setCurrentPage(page);
         selector.init();
         event.getRequestContext().addUIComponentToUpdateByAjax(selector);
      }
   }

}
