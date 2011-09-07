/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.description;

import org.exoplatform.portal.mop.Described;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class CacheValue implements Serializable
{

   /** . */
   private static final AtomicLong SEQUENCE = new AtomicLong();

   /** . */
   final CacheKey origin;

   /** . */
   final long serial;

   /** . */
   final Described.State state;

   public CacheValue(CacheKey origin, long serial, Described.State state)
   {
      this.origin = origin;
      this.serial = serial;
      this.state = state;
   }

   public CacheValue(Described.State state)
   {
      this.origin = null;
      this.serial = SEQUENCE.incrementAndGet();
      this.state = state;
   }
}
