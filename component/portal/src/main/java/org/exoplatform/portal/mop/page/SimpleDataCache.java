package org.exoplatform.portal.mop.page;

import org.exoplatform.commons.serialization.MarshalledObject;
import org.exoplatform.portal.pom.config.POMSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple implementation for unit testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache
{

   /** . */
   protected Map<MarshalledObject<PageKey>, MarshalledObject<PageData>> pages;

   public SimpleDataCache()
   {
      this.pages = new ConcurrentHashMap<MarshalledObject<PageKey>, MarshalledObject<PageData>>();
   }

   @Override
   protected PageData getPage(POMSession session, PageKey key)
   {
      MarshalledObject<PageKey> marshalledKey = MarshalledObject.marshall(key);
      MarshalledObject<PageData> marshalledPage = pages.get(marshalledKey);
      if (marshalledPage == null)
      {
         PageData page = loadPage(session, key);
         if (page != null)
         {
            pages.put(marshalledKey, MarshalledObject.marshall(page));
            return page;
         }
         else
         {
            return null;
         }
      }
      else
      {
         return marshalledPage.unmarshall();
      }
   }

   @Override
   protected void removePage(POMSession session, PageKey key)
   {
      pages.remove(MarshalledObject.marshall(key));
   }

   @Override
   protected void putPage(PageData data)
   {
      pages.put(MarshalledObject.marshall(data.key), MarshalledObject.marshall(data));
   }

   @Override
   protected void clear()
   {
      pages.clear();
   }
}
