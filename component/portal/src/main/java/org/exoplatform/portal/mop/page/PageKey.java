package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The immutable key for a page.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageKey implements Serializable
{

   /** . */
   private static final HashMap<String, SiteType> map = new HashMap<String, SiteType>();

   static
   {
      map.put("portal", SiteType.PORTAL);
      map.put("group", SiteType.GROUP);
      map.put("user", SiteType.USER);
   }

   /**
    * Parse the string representation of a page key.
    *
    * @param s the string representation
    * @return the corresponding page key
    * @throws NullPointerException if the string argument is null
    * @throws IllegalArgumentException if the key does not have the good format
    */
   public static PageKey parse(String s) throws NullPointerException, IllegalArgumentException
   {
      if (s == null)
      {
         throw new NullPointerException("No null string argument allowed");
      }
      int pos1 = s.indexOf("::");
      if (pos1 != -1)
      {
         SiteType siteType = map.get(s.substring(0, pos1));
         if (siteType != null)
         {
            pos1 += 2;
            int pos2 = s.indexOf("::", pos1);
            if (pos2 != -1)
            {
               String siteName = s.substring(pos1, pos2);
               pos2 += 2;
               if (pos2 > pos1 && pos2 < s.length())
               {
                  String pageName = s.substring(pos2);
                  return siteType.key(siteName).page(pageName);
               }
            }
         }
      }
      throw new IllegalArgumentException("Invalid page reference: " + s);
   }

   /** . */
   final SiteKey site;

   /** . */
   final String name;

   /** . */
   private String ref;

   public PageKey(SiteKey site, String name) throws NullPointerException
   {
      if (site == null)
      {
         throw new NullPointerException("No null site accepted");
      }
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      this.site = site;
      this.name = name;
      this.ref = null;
   }

   public SiteKey getSite()
   {
      return site;
   }

   public String getName()
   {
      return name;
   }

   public PageKey sibling(String name)
   {
      return new PageKey(site, name);
   }

   public String format()
   {
      if (ref == null)
      {
         ref = site.getType().getName() + "::" + site.getName() + "::" + name;
      }
      return ref;
   }

   @Override
   public int hashCode()
   {
      return site.hashCode() ^ name.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }

      // We need to use class equality here
      if (obj != null && getClass().equals(obj.getClass()))
      {
         PageKey that = (PageKey)obj;
         return site.equals(that.site) && name.equals(that.name);
      }

      //
      return false;
   }

   @Override
   public String toString()
   {
      return "PageKey[site=" + site + ",name=" + name + "]";
   }
}
