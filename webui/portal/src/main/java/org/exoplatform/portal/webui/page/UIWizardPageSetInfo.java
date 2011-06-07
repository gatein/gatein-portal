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

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.navigation.UIPageNodeSelector;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.IdentifierValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Nguyen Thi Hoa
 *          hoa.nguyen@exoplatform.com
 * Oct 31, 2006  
 */
@ComponentConfigs({@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/portal/webui/page/UIWizardPageSetInfo.gtmpl", events = {
   @EventConfig(listeners = UIWizardPageSetInfo.ChangeNodeActionListener.class, phase = Phase.DECODE),
   @EventConfig(listeners = UIWizardPageSetInfo.SwitchVisibleActionListener.class, phase = Phase.DECODE),
   @EventConfig(listeners = UIWizardPageSetInfo.SwitchPublicationDateActionListener.class, phase = Phase.DECODE)})})
public class UIWizardPageSetInfo extends UIForm
{

   final public static String PAGE_NAME = "pageName";

   final public static String PAGE_DISPLAY_NAME = "pageDisplayName";

   final public static String VISIBLE = "visible";

   final public static String SHOW_PUBLICATION_DATE = "showPublicationDate";

   final public static String START_PUBLICATION_DATE = "startPublicationDate";

   final public static String END_PUBLICATION_DATE = "endPublicationDate";

   private boolean isEditMode = false;

   private boolean firstTime = true;

   public UIWizardPageSetInfo() throws Exception
   {
   	UIFormCheckBoxInput<Boolean> uiDateInputCheck =
         new UIFormCheckBoxInput<Boolean>(SHOW_PUBLICATION_DATE, SHOW_PUBLICATION_DATE, false);
   	UIFormCheckBoxInput<Boolean> uiVisibleCheck = new UIFormCheckBoxInput<Boolean>(VISIBLE, VISIBLE, false);
   	uiDateInputCheck.setOnChange("SwitchPublicationDate");
   	uiVisibleCheck.setOnChange("SwitchVisible");
   	
      addChild(UIPageNodeSelector.class, null, null);
      addUIFormInput(new UIFormStringInput(PAGE_NAME, "name", null).addValidator(MandatoryValidator.class)
                     .addValidator(StringLengthValidator.class, 3, 30).addValidator(IdentifierValidator.class));
      addUIFormInput(new UIFormStringInput(PAGE_DISPLAY_NAME, "label", null)
      		.setMaxLength(255).addValidator(StringLengthValidator.class, 3, 120));
      addUIFormInput(uiVisibleCheck.setChecked(true));
      addUIFormInput(uiDateInputCheck);
      UIFormInputBase<String> startPubDateInput = new UIFormDateTimeInput(START_PUBLICATION_DATE, null, null).addValidator(DateTimeValidator.class);
      UIFormInputBase<String> endPubDateInput = new UIFormDateTimeInput(END_PUBLICATION_DATE, null, null).addValidator(DateTimeValidator.class);
      addUIFormInput(startPubDateInput);
      addUIFormInput(endPubDateInput);
      
      boolean isUserNav = Util.getUIPortal().getSelectedNavigation().getOwnerType().equals(PortalConfig.USER_TYPE);
      if (isUserNav)
      {
         uiVisibleCheck.setRendered(false);
         uiDateInputCheck.setRendered(false);
         startPubDateInput.setRendered(false);
         endPubDateInput.setRendered(false);
      }
   }

   public void setEditMode() throws Exception
   {
      isEditMode = true;
      UIFormStringInput uiNameInput = getChildById(PAGE_NAME);
      uiNameInput.setEditable(false);
   }

   public boolean isEditMode()
   {
      return isEditMode;
   }

   public void invokeSetBindingBean(Object bean) throws Exception
   {
      super.invokeSetBindingBean(bean);
      PageNode node = (PageNode)bean;
      if(!getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked())
      {
      	node.setVisible(getUIFormCheckBoxInput(VISIBLE).isChecked());	
      }
      Calendar cal = getUIFormDateTimeInput(START_PUBLICATION_DATE).getCalendar();
      Date date = (cal != null) ? cal.getTime() : null;
      node.setStartPublicationDate(date);
      cal = getUIFormDateTimeInput(END_PUBLICATION_DATE).getCalendar();
      date = (cal != null) ? cal.getTime() : null;
      node.setEndPublicationDate(date);
   }

   public PageNode getPageNode() throws Exception
   {
      if (isEditMode)
      {
         PageNode pageNode = getSelectedPageNode();
         PageNode clonedNode = (pageNode != null) ? pageNode.clone() : null;
         invokeSetBindingBean(clonedNode);
         if (clonedNode.getLabel() == null || clonedNode.getLabel().trim().length() == 0)
         {
            clonedNode.setLabel(clonedNode.getName());
         }
         return clonedNode;
      }

      PageNode pageNode = new PageNode();
      invokeSetBindingBean(pageNode);
      if (pageNode.getLabel() == null || pageNode.getLabel().trim().length() == 0)
      {
         pageNode.setLabel(pageNode.getName());
      }

      UIPageNodeSelector uiNodeSelector = getChild(UIPageNodeSelector.class);
      PageNode selectedNode = uiNodeSelector.getSelectedPageNode();
      PageNavigation nav = uiNodeSelector.getSelectedNavigation();
      if(nav.getOwnerType().equals(PortalConfig.USER_TYPE))
         pageNode.setUri(pageNode.getName());
      else
      {
         if (selectedNode != null)
         {
            pageNode.setUri(selectedNode.getUri() + "/" + pageNode.getName());
         }
         else
            pageNode.setUri(pageNode.getName());
      }
      return pageNode;
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

   public void setPageNode(PageNode pageNode) throws Exception
   {
      if (pageNode.getName() != null)
         getUIStringInput(PAGE_NAME).setValue(pageNode.getName());
      if (pageNode.getLabel() != null)
         getUIStringInput(PAGE_DISPLAY_NAME).setValue(pageNode.getLabel());
      getUIFormCheckBoxInput(VISIBLE).setChecked(pageNode.isVisible());
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

   public PageNode getSelectedPageNode()
   {
      UIPageNodeSelector uiPageNodeSelector = getChild(UIPageNodeSelector.class);
      PageNavigation nav = uiPageNodeSelector.getSelectedNavigation();
      if(nav.getOwnerType().equals(PortalConfig.USER_TYPE))
         return null;
      return uiPageNodeSelector.getSelectedPageNode();
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      if (isEditMode && getChild(UIPageNodeSelector.class).getSelectedPageNode() == null)
         reset();
      super.processRender(context);
   }

   public void processDecode(WebuiRequestContext context) throws Exception
   {
      super.processDecode(context);
      String action = context.getRequestParameter(UIForm.ACTION);
      Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
      if (event != null)
         event.broadcast();
   }

   public boolean isFirstTime()
   {
      return firstTime;
   }

   public void setFirstTime(boolean firstTime)
   {
      this.firstTime = firstTime;
   }

   static public class ChangeNodeActionListener extends EventListener<UIWizardPageSetInfo>
   {
      public void execute(Event<UIWizardPageSetInfo> event) throws Exception
      {
         String uri = event.getRequestContext().getRequestParameter(OBJECTID);
         UIWizardPageSetInfo uiForm = event.getSource();

         UIPageNodeSelector uiPageNodeSelector = event.getSource().getChild(UIPageNodeSelector.class);
         UITree tree = uiPageNodeSelector.getChild(UITree.class);

         if (tree.getParentSelected() == null && (uri == null || uri.length() < 1))
         {
            uiPageNodeSelector.selectNavigation(uiPageNodeSelector.getSelectedNavigation());
         }
         else
         {
            uiPageNodeSelector.selectPageNodeByUri(uri);
         }

         UIPortalApplication uiPortalApp = uiPageNodeSelector.getAncestorOfType(UIPortalApplication.class);
         UIWizard uiWizard = uiPortalApp.findFirstComponentOfType(UIWizard.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiWizard);

         if (!event.getSource().isEditMode())
         {
            return;
         }
         PageNode pageNode = uiPageNodeSelector.getSelectedPageNode();

         if (pageNode == null && uiForm.isFirstTime())
         {
            uiForm.setShowPublicationDate(false);
            uiForm.setFirstTime(false);
            UIPortal uiPortal = Util.getUIPortal();
            uiPageNodeSelector.selectNavigation(uiPortal.getSelectedNavigation());
            if (uiPortal.getSelectedNode() != null)
            {
               uiPageNodeSelector.selectPageNodeByUri(uiPortal.getSelectedNode().getUri());
            }
            pageNode = uiPageNodeSelector.getSelectedPageNode();
         }

         if (pageNode == null)
         {
            uiForm.setShowPublicationDate(false);
            return;
         }
         UserPortalConfigService configService = uiWizard.getApplicationComponent(UserPortalConfigService.class);
         String accessUser = event.getRequestContext().getRemoteUser();
         Page page = null;
         if (pageNode.getPageReference() != null)
            page = configService.getPage(pageNode.getPageReference(), accessUser);
         if (page == null)
         {
            uiPortalApp.addMessage(new ApplicationMessage("UIWizardPageSetInfo.msg.null", null));
            uiForm.reset();
            uiForm.setShowPublicationDate(false);
            return;
         }
         uiForm.setPageNode(pageNode);
      }
   }

   static public class SwitchPublicationDateActionListener extends EventListener<UIWizardPageSetInfo>
   {
      public void execute(Event<UIWizardPageSetInfo> event) throws Exception
      {
         UIWizardPageSetInfo uiForm = event.getSource();
         boolean isCheck = uiForm.getUIFormCheckBoxInput(SHOW_PUBLICATION_DATE).isChecked();
         uiForm.getUIFormDateTimeInput(START_PUBLICATION_DATE).setRendered(isCheck);
         uiForm.getUIFormDateTimeInput(END_PUBLICATION_DATE).setRendered(isCheck);
         UIWizard uiWizard = uiForm.getAncestorOfType(UIWizard.class);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiWizard);
      }

   }
   
   static public class SwitchVisibleActionListener extends EventListener<UIWizardPageSetInfo>
   {
		@Override
		public void execute(Event<UIWizardPageSetInfo> event) throws Exception
		{
			UIWizardPageSetInfo uiForm = event.getSource();
			boolean isCheck = uiForm.getUIFormCheckBoxInput(VISIBLE).isChecked();
			uiForm.setShowCheckPublicationDate(isCheck);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
		}
   }

}
