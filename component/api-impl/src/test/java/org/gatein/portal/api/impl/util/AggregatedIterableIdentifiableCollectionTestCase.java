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

package org.gatein.portal.api.impl.util;

import org.gatein.api.id.Id;
import org.gatein.api.id.Identifiable;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.IdentifiableImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class AggregatedIterableIdentifiableCollectionTestCase
{
   private AggregatedIterableIdentifiableCollection collection;
   private static final IterableIdentifiableCollection fixture3 = new TestCollection("1", "2", "3");
   private static final IterableIdentifiableCollection fixture4 = new TestCollection("a", "b", "c", "d");
   private static final IterableIdentifiableCollection fixture1 = new TestCollection("0");


   @BeforeMethod
   public void setUp()
   {
      collection = new AggregatedIterableIdentifiableCollection();
      collection.addCollection(fixture3);
      collection.addCollection(fixture1);
      collection.addCollection(fixture4);
   }

   @Test
   public void emptyAggregatedShouldWorkAsExpected()
   {
      collection = new AggregatedIterableIdentifiableCollection();
      assert collection.size() == 0;
      IdentifiableImpl foo = createIdentifiable("foo");
      assert !collection.contains(foo);
      assert !collection.contains(foo.getId());

      Iterator iterator = collection.iterator();
      assert !iterator.hasNext();
      try
      {
         iterator.next();
         assert false;
      }
      catch (NoSuchElementException e)
      {
         // expected
      }
   }

   @Test
   public void testSize()
   {
      assert collection.size() == 8;
   }

   @Test
   public void testContains()
   {
      assert collection.contains(createIdentifiable("0"));
      assert collection.contains(createIdentifiable("2"));
      assert collection.contains(createIdentifiable("c"));
   }

   @Test
   public void testContainsId()
   {
      assert collection.contains(createIdentifiable("0").getId());
      assert collection.contains(createIdentifiable("1").getId());
      assert collection.contains(createIdentifiable("b").getId());
   }

   @Test
   public void iteratingShouldWork()
   {
      int i = 0;
      Iterator first = fixture3.iterator();
      int firstSize = fixture3.size();
      Iterator second = fixture1.iterator();
      int secondSize = fixture1.size();
      Iterator third = fixture4.iterator();
      int thirdSize = fixture4.size();

      for (Object identifiable : collection)
      {
         if (i < firstSize)
         {
            assert first.next().equals(identifiable);
         }
         else if (i < firstSize + secondSize)
         {
            assert second.next().equals(identifiable);
         }
         else if (i < firstSize + secondSize + thirdSize)
         {
            assert third.next().equals(identifiable);
         }
         else
         {
            assert false;
         }

         i++;
      }
   }

   private static IdentifiableImpl createIdentifiable(String name)
   {
      return new IdentifiableImpl(GateInImpl.GROUP_CONTEXT.create(name), name, null);
   }

   private static class TestCollection implements IterableIdentifiableCollection<Identifiable>
   {
      private LinkedHashMap<Id<Identifiable>, Identifiable> elements;

      private TestCollection(String... names)
      {
         this.elements = new LinkedHashMap<Id<Identifiable>, Identifiable>(names.length);
         for (String name : names)
         {
            IdentifiableImpl identifiable = createIdentifiable(name);
            elements.put(identifiable.getId(), identifiable);
         }
      }

      public boolean contains(Id<Identifiable> t)
      {
         return elements.containsKey(t);
      }

      public int size()
      {
         return elements.size();
      }

      public boolean contains(Identifiable identifiable)
      {
         return elements.containsKey(identifiable.getId());
      }

      public Iterator<Identifiable> iterator()
      {
         return elements.values().iterator();
      }
   }
}
