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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * TODO: fix logic, adding empty collections shouldn't break the iteration process, just move to the next collection
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class AggregatedIterableIdentifiableCollection<T extends Identifiable<T>> implements IterableIdentifiableCollection<T>
{
   private List<IterableIdentifiableCollection> aggregated = new ArrayList<IterableIdentifiableCollection>(7);

   public void addCollection(IterableIdentifiableCollection<? extends T> collection)
   {
      if (collection.size() != 0)
      {
         aggregated.add(collection);
      }
   }


   public boolean contains(Id<T> t)
   {
      for (IterableIdentifiableCollection<T> collection : aggregated)
      {
         if (collection.contains(t))
         {
            return true;
         }
      }

      return false;
   }

   public int size()
   {
      int size = 0;
      for (IterableIdentifiableCollection<T> collection : aggregated)
      {
         size += collection.size();
      }

      return size;
   }

   public boolean contains(T t)
   {
      return contains(t.getId());
   }

   public Iterator<T> iterator()
   {
      final Iterator<IterableIdentifiableCollection> iterator = aggregated.iterator();

      if (iterator.hasNext())
      {
         return new Iterator<T>()
         {
            private Iterator<T> current = iterator.next().iterator();

            public boolean hasNext()
            {
               Iterator<T> currentIterator = getCurrent();
               return currentIterator != null && currentIterator.hasNext();
            }

            public T next()
            {
               Iterator<T> currentIterator = getCurrent();
               if (currentIterator != null)
               {
                  return currentIterator.next();
               }
               else
               {
                  throw new NoSuchElementException();
               }
            }

            public void remove()
            {
               throw new UnsupportedOperationException();
            }

            private Iterator<T> getCurrent()
            {
               if (current.hasNext())
               {
                  return current;
               }
               else
               {
                  if (iterator.hasNext())
                  {
                     current = iterator.next().iterator();
                     return current;
                  }
                  else
                  {
                     return null;
                  }
               }
            }
         };
      }
      else
      {
         return new Iterator<T>()
         {
            public boolean hasNext()
            {
               return false;
            }

            public T next()
            {
               throw new NoSuchElementException();
            }

            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }
   }
}
