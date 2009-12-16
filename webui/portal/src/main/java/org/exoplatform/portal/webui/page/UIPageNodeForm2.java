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

package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
import org.exoplatform.webui.form.validator.Validator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Author : Dang Van Minh, Pham Tuan minhdv81@yahoo.com Jun 14, 2006
 */
public class UIPageNodeForm2 extends UIFormTabPane
{

   private PageNode pageNode_;

   private String owner_;

   private String ownerType_;

   private Object selectedParent;

   final private static String SHOW_PUBLICATION_DATE = "showPublicationDate";

   final private static String START_PUBLICATION_DATE = "startPublicationDate";

   final private static String END_PUBLICATION_DATE = "endPublicationDate";

   public UIPageNodeForm2() throws Exception
   {
      super("UIPageNodeForm");

      UIFormInputSet uiSettingSet = new UIFormInputSet("PageNodeSetting");
      UIFormCheckBoxInput<Boolean> uiDateInputCheck =
         new UIFormCheckBoxInput<Boolean>(SHOW_PUBLICATION_DATE, SHOW_PUBLICATION_DATE, false);
      uiDateInputCheck.setOnChange("SwitchPublicationDate");
      uiSettingSet.addUIFormInput(new UIFormStringInput("uri", "uri", null).setEditable(false)).addUIFormInput(
         new UIFormStringInput("name", "name", null).addValidator(MandatoryValidator.class).addValidator(
            StringLengthValidator.class, 3, 30).addValidator(IdentifierValidator.class)).addUIFormInput(
         new UIFormStringInput("label", "label", null).addValidator(StringLengthValidator.class, 3, 120))
         .addUIFormInput(new UIFormCheckBoxInput<Boolean>("visible", "visible", true).setChecked(true)).addUIFormInput(
            uiDateInputCheck).addUIFormInput(
            new UIFormDateTimeInput(START_PUBLICATION_DATE, null, null).addValidator(MandatoryValidator.class)
               .addValidator(DateTimeValidator.class)).addUIFormInput(
            new UIFormDateTimeInput(END_PUBLICATION_DATE, null, null).addValidator(MandatoryValidator.class)
               .addValidator(DateTimeValidator.class));
      addUIFormInput(uiSettingSet);
      setSelectedTab(uiSettingSet.getId());

      UIPageSelector2 uiPageSelector = createUIComponent(UIPageSelector2.class, null, null);
      uiPageSelector.configure("UIPageSelector2", "pageReference");
      addUIFormInput(uiPageSelector);

      UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "icon");
      addUIFormInput(uiIconSelector);
      setActions(new String[]{"Save", "Back"});
   }

   public PageNode getPageNode()
   {
      return pageNode_;
   }

   public void setValues(PageNode pageNode) throws Exception
   {
      pageNode_ = pageNode;
      if (pageNode == null)
      {
         getUIStringInput("name").setEditable(UIFormStringInput.ENABLE);
         getChild(UIFormInputIconSelector.class).setSelectedIcon("Default");
         setShowPublicationDate(false);
         return;
      }
      getUIStringInput("name").setEditable(UIFormStringInput.DISABLE);
      invokeGetBindingBean(pageNode_);
   }

   public void invokeGetBindingBean(Object bean) throws Exception
   {
      super.invokeGetBindingBean(bean);
      PageNode pageNode = (PageNode)bean;
      String icon = pageNode_.getIcon();
      if (icon == null || icon.length() < 0)
         icon = "Default";
      getChild(UIFormInputIconSelector.class).setSelectedIcon(icon);
      getUIStringInput("label").setValue(pageNode_.getLabel());
      getUIFormCheckBoxInput("visible").setChecked(pageNode_.isVisible());
      setShowPublicationDate(pageNode.isShowPublicationDate());
      Calendar cal = Calendar.getInstance();
      if (pageNode.getStartPublicationDate() != null)
      {
         cal.setTime(pageNode.getStartPublicationDate());
         getUIFormDateTimeInput(START_PUBLICATION_DATE).setCalendar(cal);
      }
      else
         getUIFormDateTimeInput(START_PUBLICATION_DATE).setValue(null);
      if (pageNode.getEndPublicationDate() != null)
      {
         cal.setTime(pageNode.getEndPublicationDate());
         getUIFormDateTimeInput(END_PUBLICATION_DATE).setCalendar(cal);
      }
      else
         getUIFormDateTimeInput(END_PUBLICATION_DATE).setValue(null);
   }

   public void invokeSetBindingBean(Object bean) throws Exception
   {
      super.invokeSetBindingBean(bean);
      PageNode node = (PageNode)bean;
      Calendar cal = getUIFormDateTimeInput(START_PUBLICATION_DATE).getCalendar();
      Date date = (cal != null) ? cal.getTime() : null;
      node.setStartPublicationDate(date);
      cal = getUIFormDateTimeInput(END_PUBLICATION_DATE).getCalendar();
      date = (cal != null) ? cal.getTime() : null;
      node.setEndPublicationDate(date);
   }

   public void setShowPublicationDate(boolean show)
   {
      getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).setChecked(show);
      getUIFormDateTimeInput(START_PUBLICATION_DATE).setRendered(show);
      getUIFormDateTimeInput(END_PUBLICATION_DATE).setRendered(show);
   }

   public Object getSelectedParent()
   {
      return selectedParent;
   }

   public void setSelectedParent(Object obj)
   {
      this.selectedParent = obj;
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      super.processRender(context);

      UIPageSelector2 uiPageSelector = getChild(UIPageSelector2.class);
      if (uiPageSelector == null)
         return;
      UIPopupWindow uiPopupWindowPage = uiPageSelector.getChild(UIPopupWindow.class);
      if (uiPopupWindowPage == null)
         return;
      uiPopupWindowPage.processRender(context);
   }

   public void setOwner(String owner_)
   {
      this.owner_ = owner_;
   }

   public String getOwner()
   {
      return owner_;
   }

   public void setOwnerType(String ownerType_)
   {
      this.ownerType_ = ownerType_;
   }

   public String getOwnerType()
   {
      return ownerType_;
   }

   static public class SaveActionListener extends EventListener<UIPageNodeForm2>
   {
      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
         WebuiRequestContext ctx = event.getRequestContext();
         UIPageNodeForm2 uiPageNodeForm = event.getSource();
         UIApplication uiApp = ctx.getUIApplication();
         if (uiPageNodeForm.getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked())
         {
            Calendar startCalendar =
               uiPageNodeForm.getUIFormDateTimeInput(UIWizardPageSetInfo.START_PUBLICATION_DATE).getCalendar();
            Date startDate = startCalendar.getTime();
            Calendar endCalendar =
               uiPageNodeForm.getUIFormDateTimeInput(UIWizardPageSetInfo.END_PUBLICATION_DATE).getCalendar();
            Date endDate = endCalendar.getTime();
            if (startDate.after(endDate))
            {
               uiApp.addMessage(new ApplicationMessage("UIPageNodeForm2.msg.startDateBeforeEndDate", null));
               return;
            }
         }

         PageNode pageNode = uiPageNodeForm.getPageNode();
         if (pageNode == null)
            pageNode = new PageNode();
         uiPageNodeForm.invokeSetBindingBean(pageNode);
         UIPageSelector2 pageSelector = uiPageNodeForm.getChild(UIPageSelector2.class);
         if (pageSelector.getPage() == null)
            pageNode.setPageReference(null);
         UIFormInputIconSelector uiIconSelector = uiPageNodeForm.getChild(UIFormInputIconSelector.class);
         if (uiIconSelector.getSelectedIcon().equals("Default"))
            pageNode.setIcon(null);
         else
            pageNode.setIcon(uiIconSelector.getSelectedIcon());
         if (pageNode.getLabel() == null)
            pageNode.setLabel(pageNode.getName());

         String remoteUser = ctx.getRemoteUser();
         Object selectedParent = uiPageNodeForm.getSelectedParent();
         PageNavigation pageNav = null;

         if (selectedParent instanceof PageNavigation)
         {
            pageNav = (PageNavigation)selectedParent;
            pageNav.setModifier(remoteUser);
            pageNode.setUri(pageNode.getName());
            if (!pageNav.getNodes().contains(pageNode))
            {
               if (PageNavigationUtils.searchPageNodeByUri(pageNav, pageNode.getUri()) != null)
               {
                  uiApp.addMessage(new ApplicationMessage("UIPageNodeForm2.msg.SameName", null));
                  return;
               }
               pageNav.addNode(pageNode);
            }
         }
         else if (selectedParent instanceof PageNode)
         {
            PageNode parentNode = (PageNode)selectedParent;
            List<PageNode> children = parentNode.getChildren();
            if (children == null)
            {
               children = new ArrayList<PageNode>();
               parentNode.setChildren((ArrayList<PageNode>)children);
            }
            pageNode.setUri(parentNode.getUri() + "/" + pageNode.getName());
            if (!children.contains(pageNode))
            {
               if (PageNavigationUtils.searchPageNodeByUri(parentNode, pageNode.getUri()) != null)
               {
                  uiApp.addMessage(new ApplicationMessage("UIPageNodeForm2.msg.SameName", null));
                  return;
               }
               children.add(pageNode);
            }
         }
         uiPageNodeForm.createEvent("Back", Phase.DECODE, ctx).broadcast();
      }
   }

   static public class BackActionListener extends EventListener<UIPageNodeForm2>
   {

      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
      }

   }

   static public class SwitchPublicationDateActionListener extends EventListener<UIPageNodeForm2>
   {
      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
         UIPageNodeForm2 uiForm = event.getSource();
         boolean isCheck = uiForm.getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked();
         uiForm.getUIFormDateTimeInput(START_PUBLICATION_DATE).setRendered(isCheck);
         uiForm.getUIFormDateTimeInput(END_PUBLICATION_DATE).setRendered(isCheck);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
   }

   static public class ClearPageActionListener extends EventListener<UIPageNodeForm2>
   {
      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
         UIPageNodeForm2 uiForm = event.getSource();
         UIPageSelector2 pageSelector = uiForm.findFirstComponentOfType(UIPageSelector2.class);
         pageSelector.setPage(null);
         event.getRequestContext().addUIComponentToUpdateByAjax(pageSelector);
      }
   }

   static public class CreatePageActionListener extends EventListener<UIPageNodeForm2>
   {
      public void execute(Event<UIPageNodeForm2> event) throws Exception
      {
         UIPageNodeForm2 uiForm = event.getSource();
         UIPageSelector2 pageSelector = uiForm.findFirstComponentOfType(UIPageSelector2.class);

         PortalRequestContext pcontext = Util.getPortalRequestContext();
         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();

         UIFormInputSet uiInputSet = pageSelector.getChild(UIFormInputSet.class);
         List<UIComponent> children = uiInputSet.getChildren();
         /*********************************************************************/
         for (UIComponent uiChild : children)
         {
            if (uiChild instanceof UIFormInputBase)
            {
               UIFormInputBase uiInput = (UIFormInputBase)uiChild;
               if (!uiInput.isValid())
                  continue;
               List<Validator> validators = uiInput.getValidators();
               if (validators == null)
                  continue;
               try
               {
                  for (Validator validator : validators)
                     validator.validate(uiInput);
               }
               catch (MessageException ex)
               {
                  uiPortalApp.addMessage(ex.getDetailMessage());
                  return;
               }
               catch (Exception ex)
               {
                  //TODO:  This is a  critical exception and should be handle  in the UIApplication
                  uiPortalApp.addMessage(new ApplicationMessage(ex.getMessage(), null));
                  return;
               }
            }
         }

         UserACL userACL = uiForm.getApplicationComponent(UserACL.class);

         String ownerId = uiForm.getOwner();
         String[] accessPermission = new String[1];
         accessPermission[0] = "*:" + ownerId;
         String editPermission = userACL.getMakableMT() + ":" + ownerId;
         
         if (PortalConfig.PORTAL_TYPE.equals(uiForm.getOwnerType()))
         {
            UIPortal uiPortal = Util.getUIPortal();
            accessPermission = uiPortal.getAccessPermissions();
            editPermission = uiPortal.getEditPermission();
         }
         
         
         UIFormStringInput uiPageName = uiInputSet.getChildById("pageName");
         UIFormStringInput uiPageTitle = uiInputSet.getChildById("pageTitle");

         Page page = new Page();
         page.setOwnerType(uiForm.getOwnerType());
         page.setOwnerId(ownerId);
         page.setName(uiPageName.getValue());
         String title = uiPageTitle.getValue();;
         if (title == null || title.trim().length() < 1)
            title = page.getName();
         page.setTitle(title);

         page.setShowMaxWindow(false);

         page.setAccessPermissions(accessPermission);
         page.setEditPermission(editPermission);

         userACL.hasPermission(page);

         page.setCreator(pcontext.getRemoteUser());
         page.setModifiable(true);
         if (page.getChildren() == null)
            page.setChildren(new ArrayList<ModelObject>());

         UserPortalConfigService service = uiForm.getApplicationComponent(UserPortalConfigService.class);

         // check page is exist
         DataStorage dataStorage = uiForm.getApplicationComponent(DataStorage.class);
         Page existPage = dataStorage.getPage(page.getPageId());
         if (existPage != null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageForm.msg.sameName", null));
            pcontext.addUIComponentToUpdateByAjax(uiPortalApp.getUIPopupMessages());
            return;
         }

         // save page to database
         service.create(page);
         pageSelector.setValue(page.getPageId());
      }
   }
}
