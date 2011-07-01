/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

/**
 * Describes how a node should be loaded by a loading operation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class VisitMode
{

   /**
    * Include node but its children should be left appart.
    */
   public static final VisitMode NO_CHILDREN = new VisitMode("NO_CHILDREN");

   /**
    * Include node and its children.
    */
   public static final VisitMode ALL_CHILDREN = new VisitMode("ALL_CHILDREN");

   /** . */
   private final String name;

   private VisitMode(String name)
   {
      this.name = name;
   }

   @Override
   public String toString()
   {
      return "VisitMode[" + name + "]";
   }
}
