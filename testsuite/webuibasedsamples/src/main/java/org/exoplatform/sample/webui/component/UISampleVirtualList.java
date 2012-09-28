package org.exoplatform.sample.webui.component;

import org.exoplatform.sample.webui.component.bean.User;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@ComponentConfig(lifecycle = UIContainerLifecycle.class, events = {
   @EventConfig(listeners = UISampleVirtualList.ViewActionListener.class),
   @EventConfig(listeners = UISampleVirtualList.EditActionListener.class),
   @EventConfig(listeners = UISampleVirtualList.DeleteActionListener.class)})
public class UISampleVirtualList extends UIContainer
{
   public static final String BEAN_ID = "userName";

   public static final String[] BEAN_NAMES = {BEAN_ID, "favoriteColor", "position", "dateOfBirth"};

   public static final String[] ACTIONS = {"View", "Edit", "Delete"};

   public UISampleVirtualList() throws Exception
   {
      UIRepeater uiRepeater = createUIComponent(UIRepeater.class, null, null);
      uiRepeater.configure(BEAN_ID, BEAN_NAMES, ACTIONS);      
      uiRepeater.setSource(makeDataSource());
      
      UIVirtualList uiVirtualList = addChild(UIVirtualList.class, null, null);
      uiVirtualList.setUIComponent(uiRepeater);      
   }

   public void showPopupMessage(String msg) {
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      rcontext.getUIApplication().addMessage(new ApplicationMessage(msg, null));   
   }
   
   @Override
   public void processRender(WebuiRequestContext context) throws Exception 
   {
      UIVirtualList uiVirtualList = getChild(UIVirtualList.class);
      UIRepeater uiRepeater = uiVirtualList.getRepeater();
      uiRepeater.setSource(makeDataSource());
      super.processRender(context);
   }
   
   private Iterator<List<?>> makeDataSource()
   {
      final List<User> userList = makeUserList();
      final int pageSize = 5;
      Iterator<List<?>> iterator = new Iterator<List<?>>()
      {
         int currentIndex = 0;
         
         @Override
         public boolean hasNext()
         {
            return currentIndex < userList.size();
         }

         @Override
         public List<?> next()
         {
            if(hasNext())
            {
               List<User> list = new ArrayList<User>(pageSize);
               for(int i = currentIndex; i < currentIndex + pageSize; i++)
               {
                  if(i < userList.size()) 
                  {
                     list.add(userList.get(i));
                  }
                  else
                  {
                     break;
                  }
               }
               
               //
               currentIndex += pageSize;
               return list;
            } 
            else 
            {
               return null;
            }
         }

         @Override
         public void remove()
         {
            throw new UnsupportedOperationException();
         }
      };
      return iterator;
   }

   private List<User> makeUserList()
   {
      List<User> userList = new ArrayList<User>();           
      for (int i = 0; i < 30; i++) {
         userList.add(new User("user " + i, "color " + i, "position " + i, new Date()));
      }
      return userList;
   }

   static public class ViewActionListener extends EventListener<UISampleVirtualList>
   {
      @Override
      public void execute(Event<UISampleVirtualList> event) throws Exception
      {
         event.getSource().showPopupMessage("View " + event.getRequestContext().getRequestParameter(OBJECTID));
      }
   }

   static public class EditActionListener extends EventListener<UISampleVirtualList>
   {
      @Override
      public void execute(Event<UISampleVirtualList> event) throws Exception
      {
         event.getSource().showPopupMessage("Edit " + event.getRequestContext().getRequestParameter(OBJECTID));
      }
   }

   static public class DeleteActionListener extends EventListener<UISampleVirtualList>
   {
      @Override
      public void execute(Event<UISampleVirtualList> event) throws Exception
      {
         event.getSource().showPopupMessage("Delete " + event.getRequestContext().getRequestParameter(OBJECTID));  
      }
   }   
}

