/*
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
package org.exoplatform.commons.chromattic;

import junit.framework.Assert;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SynchronizationEventQueue implements SynchronizationListener
{

   /** . */
   private final LinkedList<SynchronizationEvent> queue = new LinkedList<SynchronizationEvent>();

   public void beforeSynchronization()
   {
      queue.add(SynchronizationEvent.BEFORE);
   }

   public void afterSynchronization(SynchronizationStatus status)
   {
      queue.add(status == SynchronizationStatus.SAVED ? SynchronizationEvent.SAVED : SynchronizationEvent.DISCARDED);
   }

   public void assertEmpty()
   {
      Assert.assertTrue(queue.isEmpty());
   }

   public void assertEvent(SynchronizationEvent event)
   {
      Assert.assertTrue(queue.size() > 0);
      Assert.assertEquals(event, queue.removeFirst());
   }
}
