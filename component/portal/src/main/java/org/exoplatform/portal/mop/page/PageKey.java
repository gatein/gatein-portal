package org.exoplatform.portal.mop.page;

import org.exoplatform.portal.mop.SiteKey;

import java.io.Serializable;

/**
 * The immutable key for a page.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageKey implements Serializable
{

   /** . */
   final SiteKey site;

   /** . */
   final String name;

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
