/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.web.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class CompoundApplicationMessage extends AbstractApplicationMessage implements Serializable
{
   private List<AbstractApplicationMessage> messages = new ArrayList<AbstractApplicationMessage>(5);

   public CompoundApplicationMessage()
   {
      this(null);
   }

   public CompoundApplicationMessage(AbstractApplicationMessage initialMessage)
   {
      if(initialMessage != null)
      {
         messages.add(initialMessage);
      }
      setType(AbstractApplicationMessage.WARNING);
   }

   @Override
   public void setResourceBundle(ResourceBundle resourceBundle)
   {
      super.setResourceBundle(resourceBundle);
      for (AbstractApplicationMessage message : messages)
      {
         message.setResourceBundle(resourceBundle);
      }
   }

   @Override
   public String getMessage()
   {
      StringBuilder sb = new StringBuilder(255);
      for (AbstractApplicationMessage message : messages)
      {
         sb.append(message.getMessage()).append('\n');
      }

      return sb.toString();
   }

   public void addMessage(String messageKey, Object[] args)
   {
      messages.add(new ApplicationMessage(messageKey, args, AbstractApplicationMessage.WARNING));
   }

   public boolean isEmpty()
   {
      return messages.isEmpty();
   }
}
