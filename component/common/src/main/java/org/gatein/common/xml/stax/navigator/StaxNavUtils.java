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

import org.gatein.common.xml.stax.navigator.builder.StaxNavBuilder;
import org.gatein.common.xml.stax.navigator.builder.StaxNavBuilderImpl;
import org.staxnav.EnumElement;
import org.staxnav.Naming;
import org.staxnav.StaxNavException;
import org.staxnav.StaxNavigator;
import org.staxnav.ValueType;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxNavUtils
{
   public static <N> StaxNavigator<N> createNavigator(Naming<N> naming, InputStream in) throws StaxNavException
   {
      return buildDefaultNavigator().withInputStream(in).build(naming);
   }

   public static StaxNavigator<String> createNavigator(InputStream in) throws StaxNavException
   {
      return createNavigator(new Naming.Local(), in);
   }

   public static StaxNavigator<QName> createQualifiedNavigator(InputStream in) throws StaxNavException
   {
      return createNavigator(new Naming.Qualified(), in);
   }

   public static <E extends Enum<E> & EnumElement<E>> StaxNavigator<E> createNavigator(Class<E> enumeratedClass,
                                                                                       E noSuchElement, InputStream in) throws StaxNavException
   {
      Naming<E> naming;
      if (EnumElement.class.isAssignableFrom(enumeratedClass))
      {
         naming = new Naming.Enumerated.Mapped<E>(enumeratedClass, noSuchElement);
      }
      else
      {
         naming = new Naming.Enumerated.Simple<E>(enumeratedClass, noSuchElement);
      }

      return createNavigator(naming, in);
   }

   public static <N> StaxNavigator<N> createNavigator(Naming<N> naming, Reader reader) throws StaxNavException
   {
      return buildDefaultNavigator().withReader(reader).build(naming);
   }

   public static StaxNavigator<String> createNavigator(Reader reader) throws StaxNavException
   {
      return createNavigator(new Naming.Local(), reader);
   }

   public static StaxNavigator<QName> createQualifiedNavigator(Reader reader) throws StaxNavException
   {
      return createNavigator(new Naming.Qualified(), reader);
   }

   public static <E extends Enum<E> & EnumElement<E>> StaxNavigator<E> createNavigator(Class<E> enumeratedClass,
                                                                                       E noSuchElement, Reader reader) throws StaxNavException
   {
      Naming<E> naming;
      if (EnumElement.class.isAssignableFrom(enumeratedClass))
      {
         naming = new Naming.Enumerated.Mapped<E>(enumeratedClass, noSuchElement);
      }
      else
      {
         naming = new Naming.Enumerated.Simple<E>(enumeratedClass, noSuchElement);
      }

      return createNavigator(naming, reader);
   }

   public static <N> void requiresChild(StaxNavigator<N> navigator, N element)
   {
      if (navigator.child() != element)
      {
         throw expectedElement(navigator, element);
      }
   }

   public static <N> void requiresSibling(StaxNavigator<N> navigator, N element)
   {
      if (!navigator.sibling(element))
      {
         throw expectedElement(navigator, element);
      }
   }

   public static String getRequiredAttribute(StaxNavigator navigator, String attributeName) throws StaxNavException
   {
      String value = navigator.getAttribute(attributeName);
      if (value == null)
      {
         throw new StaxNavException(navigator.getLocation(), "Attribute '" + attributeName + "' is required for element '" + navigator.getLocalName() + "'");
      }

      return value;
   }

   public static <N> String getContent(StaxNavigator<N> navigator, boolean trim)
   {
      boolean before = navigator.getTrimContent();
      try
      {
         navigator.setTrimContent(trim);
         return navigator.getContent();
      }
      finally
      {
         navigator.setTrimContent(before);
      }
   }

   public static <N> String getRequiredContent(StaxNavigator<N> navigator, boolean trim)
   {
      String content = getContent(navigator, trim);
      if (content == null || content.length() == 0)
      {
         throw contentRequired(navigator);
      }

      return content;
   }

   public static <N, V> V parseContent(StaxNavigator<N> navigator, ValueType<V> valueType, V defaultValue)
   {
      String content = getContent(navigator, true);
      if (content != null && content.length() != 0)
      {
         return navigator.parseContent(valueType);
      }
      else
      {
         return defaultValue;
      }
   }

   public static <N, V> V parseRequiredContent(StaxNavigator<N> navigator, ValueType<V> valueType)
   {
      V value = parseContent(navigator, valueType, null);
      if (value == null)
      {
         throw contentRequired(navigator);
      }

      return value;
   }

   public static <N> Set<N> forNames(N...names)
   {
      return new HashSet<N>(Arrays.asList(names));
   }

   private static StaxNavBuilder buildDefaultNavigator()
   {
      return new StaxNavBuilderImpl();
   }

   private StaxNavUtils() {}
}
