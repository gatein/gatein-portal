package org.exoplatform.portal.mop.page;

import junit.framework.TestCase;
import org.exoplatform.portal.mop.SiteType;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class TestPageKey extends TestCase
{

   public void testParse()
   {
      PageKey key = PageKey.parse("portal::classic::home");
      assertEquals(SiteType.PORTAL, key.getSite().getType());
      assertEquals("classic", key.getSite().getName());
      assertEquals("home", key.getName());

      //
      try
      {
         PageKey.parse(null);
         fail();
      }
      catch (NullPointerException expected)
      {
      }
   }

   public void testIllegalParse()
   {
      assertIllegalParse("");
      assertIllegalParse("portal");
      assertIllegalParse("portal::");
      assertIllegalParse("portal::classic");
      assertIllegalParse("portal::classic::");
      assertIllegalParse("::classic::foo");
      assertIllegalParse("::::foo");
   }

   private void assertIllegalParse(String s)
   {
      try
      {
         PageKey.parse(s);
         fail("Was expecting " + s + " to raise a parsing exception");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }
}
