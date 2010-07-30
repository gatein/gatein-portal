package org.exoplatform.upload;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

/**
 * This listener for the purpose of cleaning up temporary files that are uploaded to the server
 * but not removed by specific actions from user
 * 
 * The listener is triggered when a session is destroyed
 * 
 * @author <a href="mailto:trongtt@gmail.com">Tran The Trong</a>
 * @version $Revision$
 */
public class UploadSessionListener extends Listener<PortalContainer, HttpSessionEvent>
{
   @Override
   public void onEvent(Event<PortalContainer, HttpSessionEvent> event) throws Exception
   {
      PortalContainer container = event.getSource();
      HttpSession session = event.getData().getSession();

      UploadService uploadService = (UploadService)container.getComponentInstanceOfType(UploadService.class);
      uploadService.cleanUp(session);
   }
}
