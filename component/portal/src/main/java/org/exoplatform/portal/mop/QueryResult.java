package org.exoplatform.portal.mop;

import java.util.Iterator;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public final class QueryResult<T> implements Iterable<T>
{

   /** The result first item offset. */
   private final int from;

   /** The result item size. */
   private final int size;

   /** The result item count (i.e the global size). */
   private final int hits;

   /** The items. */
   private final Iterable<T> items;

   public QueryResult(int hits, int from, int size, Iterable<T> items)
   {
      this.from = from;
      this.hits = hits;
      this.items = items;
      this.size = size;
   }

   /**
    * Returns the index of the first item in the result.
    *
    * @return the first index
    */
   public int getFrom()
   {
      return from;
   }

   /**
    * Returns the number of items returned.
    *
    * @return the number of items
    */
   public int getSize()
   {
      return size;
   }

   /**
    * Returns the index of the item following the last item in the result.
    *
    * @return the last index
    */
   public int getTo()
   {
      return from + size;
   }

   /**
    * Return the result count.
    *
    * @return the result count
    */
   public int getHits()
   {
      return hits;
   }

   @Override
   public Iterator<T> iterator()
   {
      return items.iterator();
   }
}
