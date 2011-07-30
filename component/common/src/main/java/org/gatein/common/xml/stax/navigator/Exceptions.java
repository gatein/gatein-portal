/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.common.xml.stax.navigator;

import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Exceptions
{
   public static <N> StaxNavException expectedElement(StaxNavigator<N> navigator, N expected)
   {
      return expectedElement(navigator, navigator.getNaming().getLocalPart(expected));
   }

   public static StaxNavException expectedElement(StaxNavigator navigator, String expected)
   {
      StringBuilder message = new StringBuilder().append("Expected '").append(expected)
         .append("' but found '").append(navigator.getLocalName()).append("' instead.");

      return new StaxNavException(navigator.getLocation(), message.toString());
   }

   public static StaxNavException unexpectedElement(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Unexpected element '" + navigator.getLocalName() + "'");
   }

   public static StaxNavException unknownElement(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Unknown element '" + navigator.getLocalName() + "'");
   }

   public static StaxNavException invalidSequence(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Element '" + navigator.getLocalName() + "' is out of sequence.");
   }

   public static StaxNavException contentRequired(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Content for element '" + navigator.getLocalName() + "' is required.");
   }

   public static StaxNavException invalidParent(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Invalid parent for element '" + navigator.getLocalName() + "'");
   }

   public static StaxNavException unexpectedEndOfFile(StaxNavigator navigator)
   {
      return new StaxNavException(navigator.getLocation(), "Unexpected end of file.");
   }
}
