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

package org.exoplatform.webui.form;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.validator.Validator;

import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Sep 14, 2006
 * 
 * Represents a multi value selector
 */
@ComponentConfig(events = {
   @EventConfig(listeners = UIFormMultiValueInputSet.AddActionListener.class, phase = Phase.DECODE),
   @EventConfig(listeners = UIFormMultiValueInputSet.RemoveActionListener.class, phase = Phase.DECODE)})
public class UIFormMultiValueInputSet extends UIFormInputContainer<List>
{
   /**
    * A list of validators
    */
   protected List<Validator> validators;

   /**
    * The type of items in the selector
    */
   private Class<? extends UIFormInputBase> clazz_;

   private Constructor constructor_ = null;
   
   private Object[] constructorParams_;

   /**
    * Whether this field is enabled
    */
   protected boolean enable_ = true;

   /**
    * Whether this field is in read only mode
    */
   protected boolean readonly_ = false;

   public UIFormMultiValueInputSet() throws Exception
   {
      super(null, null);
   }

   public UIFormMultiValueInputSet(String name, String bindingField) throws Exception
   {
      super(name, bindingField);
      setComponentConfig(getClass(), null);
   }

   public Class<List> getTypeValue()
   {
      return List.class;
   }

   /**
    * 
    * @param clazz
    */
   public void setType(Class<? extends UIFormInputBase> clazz)
   {
      this.clazz_ = clazz;
      Constructor[] constructors = clazz_.getConstructors();
      if (constructors.length > 0)
         constructor_ = constructors[0];
   }

   /**
    * define a <code>Constructor</code> which's invoked
    * 
    * @param constructorParameterTypes list of parameter type which is defined in constructor
    * @throws SecurityException
    * @throws NoSuchMethodException
    */
   public void setConstructorParameterTypes(Class<?>... constructorParameterTypes) throws SecurityException, NoSuchMethodException
   {
      Constructor<?> constructor = this.clazz_.getConstructor(constructorParameterTypes);
      if(constructor != null)
      {
         this.constructor_ = constructor;
      }
   }
   
   /**
    * pass values to the <code>Constructor</code>
    * You only set constructor parameter values after seted constructor by {@link #setConstructorParameterTypes(Class...)}
    * @param values
    * @throws SecurityException
    * @throws NoSuchMethodException
    */
   public void setConstructorParameterValues(Object[] values) throws SecurityException, NoSuchMethodException
   {
      this.constructorParams_ = values;
//      List<Class<?>> parameterTypes = new ArrayList<Class<?>>();
//      
//      for (Object clazz : values)
//      {
//         parameterTypes.add(clazz.getClass());
//      }
//      
//      Class<?> [] arrParameterTypes = (Class[]) parameterTypes.toArray(new Class[parameterTypes.size()]);
//      this.setConstructorParameterTypes(arrParameterTypes);
   }
   
   public Class<? extends UIFormInputBase> getUIFormInputBase()
   {
      return clazz_;
   }

   /**
    * @return the selected items in the selector
    */
   public List<?> getValue()
   {
      List<Object> values = new ArrayList<Object>();
      for (UIComponent child : getChildren())
      {
         UIFormInputBase uiInput = (UIFormInputBase)child;
         if (uiInput.getValue() == null)
            continue;
         values.add(uiInput.getValue());
      }
      return values;
   }

   @SuppressWarnings("unchecked")
   public UIFormInput setValue(List<?> values) throws Exception
   {
      getChildren().clear();
      for (int i = 0; i < values.size(); i++)
      {
         UIFormInputBase uiInput = createUIFormInput(i);
         uiInput.setValue(values.get(i));
      }
      return this;
   }

   public boolean isEnable()
   {
      return enable_;
   }

   public UIFormMultiValueInputSet setEnable(boolean enable)
   {
      enable_ = enable;
      return this;
   }

   public boolean isEditable()
   {
      return !readonly_;
   }

   public UIFormMultiValueInputSet setEditable(boolean editable)
   {
      readonly_ = !editable;
      return this;
   }

   public void processDecode(WebuiRequestContext context) throws Exception
   {
      super.processDecode(context);
      UIForm uiForm = getAncestorOfType(UIForm.class);
      String action = uiForm.getSubmitAction();
      Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
      if (event == null)
         return;
      event.broadcast();
   }

   public void processRender(WebuiRequestContext context) throws Exception
   {
      if (getChildren() == null || getChildren().size() < 1)
         createUIFormInput(0);

      Writer writer = context.getWriter();

      UIForm uiForm = getAncestorOfType(UIForm.class);
      int size = getChildren().size();
      
      ResourceBundle res = context.getApplicationResourceBundle() ;
      String addItem = res.getString("UIFormMultiValueInputSet.label.add");
      String removeItem = res.getString("UIFormMultiValueInputSet.label.remove");
      
      for (int i = 0; i < size; i++)
      {
         UIFormInputBase uiInput = getChild(i);
         writer.append("<div class=\"MultiValueContainer\">");

         uiInput.setReadOnly(readonly_);
         uiInput.setDisabled(!enable_);

         uiInput.processRender(context);

         if ((size >= 2) || ((size == 1) && (uiInput.getValue() != null)))
         {
            writer.append("<img onclick=\"");
            writer.append(uiForm.event("Remove", uiInput.getId())).append("\" title=\"" + removeItem + "\" alt=\"\"");
            writer
               .append(" class=\"MultiFieldAction Remove16x16Icon\" src=\"/eXoResources/skin/sharedImages/Blank.gif\" />");
         }
         if (i == size - 1)
         {

            writer.append("<img onclick=\"");
            writer.append(uiForm.event("Add", getId())).append("\" title=\"" + addItem + "\" alt=\"\"");
            writer
               .append(" class=\"MultiFieldAction AddNewNodeIcon\" src=\"/eXoResources/skin/sharedImages/Blank.gif\" />");
         }
         writer.append("</div>");
      }
   }

   public UIFormInputBase createUIFormInput(int idx) throws Exception
   {
	   
	  if(constructor_  == null) return  null;
      Class[] classes = constructor_.getParameterTypes();
      UIFormInputBase inputBase;      
      String compName = getId() + String.valueOf(idx);
      if (classes.length > 0) {
         if (constructorParams_ == null)
         {
            Object[] params = new Object[classes.length];
            for (int i = 0; i < classes.length; i++)
            {
               if (classes[i].isPrimitive())
               {
                  if (classes[i] == boolean.class)
                     params[i] = false;
                  else
                     params[i] = 0;
               }
            }
            params[0] = compName;
            inputBase = (UIFormInputBase) constructor_.newInstance(params);
         }
         else
         {
            inputBase = (UIFormInputBase) constructor_.newInstance(constructorParams_);
         }
      } else {
         inputBase = (UIFormInputBase)constructor_.newInstance();
      }
      
      inputBase.setName(compName);
      inputBase.setId(compName);
      List<Validator> validators = this.getValidators();
      if (validators != null)
      {
         for (Validator validator : validators)
         {
            inputBase.addValidator(validator.getClass());
         }
      }
      addChild(inputBase);
      return inputBase;
   }

   static public class AddActionListener extends EventListener<UIFormMultiValueInputSet>
   {
      public void execute(Event<UIFormMultiValueInputSet> event) throws Exception
      {
         UIFormMultiValueInputSet uiSet = event.getSource();
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         if (uiSet.getId().equals(id))
         {
            // get max id
            List<UIComponent> children = uiSet.getChildren();
            if (children.size() > 0)
            {
               UIFormInputBase uiInput = (UIFormInputBase)children.get(children.size() - 1);
               String index = uiInput.getId();
               int maxIndex = Integer.parseInt(index.replaceAll(id, ""));
               uiSet.createUIFormInput(maxIndex + 1);
            }
         }
      }
   }

   static public class RemoveActionListener extends EventListener<UIFormMultiValueInputSet>
   {
      public void execute(Event<UIFormMultiValueInputSet> event) throws Exception
      {
         UIFormMultiValueInputSet uiSet = event.getSource();
         String id = event.getRequestContext().getRequestParameter(OBJECTID);
         uiSet.removeChildById(id);
      }
   }

}
