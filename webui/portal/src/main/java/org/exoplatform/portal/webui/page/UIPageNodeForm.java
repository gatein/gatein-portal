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
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.navigation.ParentChildPair;
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
public class UIPageNodeForm extends UIFormTabPane
{

   private PageNode pageNode_;

   private Object selectedParent;

   /**
    * Wrapper of editted PageNode and its parent
    */
   private ParentChildPair contextParentChildPair;
   
   /**
    * PageNavigation to which editted PageNode belongs
    */
   private PageNavigation contextPageNavigation;
   
   final private static String SHOW_PUBLICATION_DATE = "showPublicationDate";

   final private static String START_PUBLICATION_DATE = "startPublicationDate";

   final private static String END_PUBLICATION_DATE = "endPublicationDate";
   
   final private static String VISIBLE = "visible";

   public UIPageNodeForm() throws Exception
   {
      super("UIPageNodeForm");

      UIFormInputSet uiSettingSet = new UIFormInputSet("PageNodeSetting");
      UIFormCheckBoxInput<Boolean> uiDateInputCheck =
         new UIFormCheckBoxInput<Boolean>(SHOW_PUBLICATION_DATE, SHOW_PUBLICATION_DATE, false);
      UIFormCheckBoxInput<Boolean> uiVisibleCheck = new UIFormCheckBoxInput<Boolean>(VISIBLE, VISIBLE, true);
      
      uiDateInputCheck.setOnChange("SwitchPublicationDate");
      uiVisibleCheck.setOnChange("SwitchVisible");
      uiSettingSet.addUIFormInput(new UIFormStringInput("uri", "uri", null).setEditable(false))
      				.addUIFormInput(new UIFormStringInput("name", "name", null).addValidator(MandatoryValidator.class).addValidator(StringLengthValidator.class, 3, 30).addValidator(IdentifierValidator.class))
      				.addUIFormInput(new UIFormStringInput("label", "label", null).addValidator(StringLengthValidator.class, 3, 120))
      				.addUIFormInput(uiVisibleCheck.setChecked(true))
      				.addUIFormInput(uiDateInputCheck)
      				.addUIFormInput(new UIFormDateTimeInput(START_PUBLICATION_DATE, null, null).addValidator(DateTimeValidator.class))
      				.addUIFormInput(new UIFormDateTimeInput(END_PUBLICATION_DATE, null, null).addValidator(DateTimeValidator.class));
      
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
      if(pageNode.getVisibility() == Visibility.SYSTEM)
      {
         UIFormInputSet uiSettingSet = getChildById("PageNodeSetting");
         uiSettingSet.removeChildById(VISIBLE);
         uiSettingSet.removeChildById(SHOW_PUBLICATION_DATE);
         uiSettingSet.removeChildById(START_PUBLICATION_DATE);
         uiSettingSet.removeChildById(END_PUBLICATION_DATE);
      }
      else
      {
         getUIFormCheckBoxInput(VISIBLE).setChecked(pageNode_.isVisible());
         getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).setChecked(pageNode.isShowPublicationDate());
         setShowCheckPublicationDate(pageNode_.isVisible());
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
      
   }

   public void invokeSetBindingBean(Object bean) throws Exception
   {
      super.invokeSetBindingBean(bean);
      PageNode node = (PageNode) bean;
      if (node.getVisibility() != Visibility.SYSTEM)
      {
         Calendar cal = getUIFormDateTimeInput(START_PUBLICATION_DATE).getCalendar();
         Date date = (cal != null) ? cal.getTime() : null;
         node.setStartPublicationDate(date);
         cal = getUIFormDateTimeInput(END_PUBLICATION_DATE).getCalendar();
         date = (cal != null) ? cal.getTime() : null;
         node.setEndPublicationDate(date);
      }
   }

   public void setShowCheckPublicationDate(boolean show)
   {
   	getUIFormCheckBoxInput(VISIBLE).setChecked(show);
   	UIFormCheckBoxInput<Boolean> uiForm = getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE);
   	uiForm.setRendered(show);
   	setShowPublicationDate(show && uiForm.isChecked());
   }
   
   public void setShowPublicationDate(boolean show)
   {
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

   public void setContextParentChildPair(ParentChildPair _contextParentChildPair)
   {
      this.contextParentChildPair = _contextParentChildPair;
   }
   
   public ParentChildPair getContextParentChildPair()
   {
      return this.contextParentChildPair;
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

   public String getOwner()
   {
      return contextPageNavigation.getOwnerId();
   }

   public String getOwnerType()
   {
      return contextPageNavigation.getOwnerType();
   }
   
   public void setContextPageNavigation(PageNavigation _contextPageNav)
   {
      this.contextPageNavigation = _contextPageNav;
   }
   
   public PageNavigation getContextPageNavigation()
   {
      return this.contextPageNavigation;
   }

   static public class SaveActionListener extends EventListener<UIPageNodeForm>
   {
      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
         WebuiRequestContext ctx = event.getRequestContext();
         UIPageNodeForm uiPageNodeForm = event.getSource();
         UIApplication uiPortalApp = ctx.getUIApplication();
         PageNode pageNode = uiPageNodeForm.getPageNode();
         if (pageNode == null)
            pageNode = new PageNode();
         
         if (pageNode.getVisibility() != Visibility.SYSTEM && uiPageNodeForm.getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked())
         {
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH), currentCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            Date currentDate = currentCalendar.getTime();
            
            Calendar startCalendar =
               uiPageNodeForm.getUIFormDateTimeInput(UIWizardPageSetInfo.START_PUBLICATION_DATE).getCalendar();
            Date startDate = startCalendar != null ? startCalendar.getTime() : currentDate;
            Calendar endCalendar =
               uiPageNodeForm.getUIFormDateTimeInput(UIWizardPageSetInfo.END_PUBLICATION_DATE).getCalendar();
            Date endDate = endCalendar != null ? endCalendar.getTime() : null;
            
            // Case 1: current date after start date
            if (currentDate.after(startDate))
            {
               Object[] args = {};
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeStartDate", args, ApplicationMessage.WARNING));
               return;
            }
            // Case 2: start date after end date
            else if ((endCalendar != null) && (startCalendar != null) && (startDate.after(endDate)))
            {
               Object[] args = {};
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.startDateBeforeEndDate", args, ApplicationMessage.WARNING));
               return;
            }
            // Case 3: start date is null and current date after end date
            else if((endCalendar != null) && (currentDate.after(endDate)))
            {
               Object[] args = {};
               uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.currentDateBeforeEndDate", args, ApplicationMessage.WARNING));
               return;
            }
            
         }
         
         UIPageSelector2 pageSelector = uiPageNodeForm.getChild(UIPageSelector2.class);
         if (pageSelector.getPage() == null)
         {
            pageSelector.setValue(null);
         }
         else
         {
            Page page = pageSelector.getPage();
            DataStorage storage = uiPageNodeForm.getApplicationComponent(DataStorage.class);
            if (storage.getPage(page.getPageId()) == null)
            {
               storage.create(page);
               pageSelector.setValue(page.getPageId());
            }
         }
         
         uiPageNodeForm.invokeSetBindingBean(pageNode);
         UIFormInputIconSelector uiIconSelector = uiPageNodeForm.getChild(UIFormInputIconSelector.class);
         if (uiIconSelector.getSelectedIcon().equals("Default"))
            pageNode.setIcon(null);
         else
            pageNode.setIcon(uiIconSelector.getSelectedIcon());
         if (pageNode.getLabel() == null)
            pageNode.setLabel(pageNode.getName());

         Object selectedParent = uiPageNodeForm.getSelectedParent();
         PageNavigation pageNav = null;

         if (selectedParent instanceof PageNavigation)
         {
            pageNav = (PageNavigation)selectedParent;
            pageNode.setUri(pageNode.getName());
            if (!pageNav.getNodes().contains(pageNode))
            {
               if (PageNavigationUtils.searchPageNodeByUri(pageNav, pageNode.getUri()) != null)
               {
                  uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.SameName", null));
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
                  uiPortalApp.addMessage(new ApplicationMessage("UIPageNodeForm.msg.SameName", null));
                  return;
               }
               children.add(pageNode);
            }
         }
         uiPageNodeForm.createEvent("Back", Phase.DECODE, ctx).broadcast();
      }
   }

   static public class BackActionListener extends EventListener<UIPageNodeForm>
   {

      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
      }

   }

   static public class SwitchPublicationDateActionListener extends EventListener<UIPageNodeForm>
   {
      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
         UIPageNodeForm uiForm = event.getSource();
         boolean isCheck = uiForm.getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked();
         uiForm.setShowPublicationDate(isCheck);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      }
   }
   
   static public class SwitchVisibleActionListener extends EventListener<UIPageNodeForm>
   {
		@Override
		public void execute(Event<UIPageNodeForm> event) throws Exception
		{
			UIPageNodeForm uiForm = event.getSource();
			boolean isCheck = uiForm.getUIFormCheckBoxInput(VISIBLE).isChecked();
			uiForm.setShowCheckPublicationDate(isCheck);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
		}
   }

   static public class ClearPageActionListener extends EventListener<UIPageNodeForm>
   {
      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
         UIPageNodeForm uiForm = event.getSource();
         UIPageSelector2 pageSelector = uiForm.findFirstComponentOfType(UIPageSelector2.class);
         pageSelector.setPage(null);
         event.getRequestContext().addUIComponentToUpdateByAjax(pageSelector);
      }
   }

   static public class CreatePageActionListener extends EventListener<UIPageNodeForm>
   {
      public void execute(Event<UIPageNodeForm> event) throws Exception
      {
         UIPageNodeForm uiForm = event.getSource();
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

         page.setModifiable(true);
         if (page.getChildren() == null)
            page.setChildren(new ArrayList<ModelObject>());

         // check page is exist
         DataStorage dataService = uiForm.getApplicationComponent(DataStorage.class);
         Page existPage = dataService.getPage(page.getPageId());
         if (existPage != null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIPageForm.msg.sameName", null));
            pcontext.addUIComponentToUpdateByAjax(uiPortalApp.getUIPopupMessages());
            return;
         }

         pageSelector.setPage(page);
      }
   }
}
