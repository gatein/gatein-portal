package org.exoplatform.sample.webui.component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
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
      uiRepeater.setDataSource(makeDataSource());
      
      UIVirtualList uiVirtualList = addChild(UIVirtualList.class, null, null);
      uiVirtualList.setUIComponent(uiRepeater);      
   }

   public void showPopupMessage(String msg) {
      WebuiRequestContext rcontext = WebuiRequestContext.getCurrentInstance();
      rcontext.getUIApplication().addMessage(new ApplicationMessage(msg, null));   
   }
   
   private LazyPageList<User> makeDataSource()
   {
      List<User> userList = makeUserList();
      return new LazyPageList<User>(new ListAccessImpl<User>(User.class, userList), 5);
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

