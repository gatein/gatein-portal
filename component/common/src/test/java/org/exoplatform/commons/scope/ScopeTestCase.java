package org.exoplatform.commons.scope;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "org/exoplatform/commons/scope/configuration.xml")
})
public class ScopeTestCase extends AbstractKernelTest
{

   public void testKey()
   {
      ScopedKey<String> k1 = ScopedKey.create("foo", "bar");
      assertEquals("foo", k1.getScope());
      assertEquals("bar", k1.getKey());

      //
      ScopedKey<String> k2 = ScopedKey.create("foo", "bar");
      assertEquals("foo", k1.getScope());
      assertEquals("bar", k1.getKey());

      //
      ScopedKey<String> k3 = ScopedKey.create("juu", "bar");
      assertEquals("juu", k3.getScope());
      assertEquals("bar", k3.getKey());

      //
      ScopedKey<String> k4 = ScopedKey.create("juu", "daa");
      assertEquals("juu", k4.getScope());
      assertEquals("daa", k4.getKey());

      //
      assertTrue(k1.equals(k2));
      assertTrue(k2.equals(k1));
      assertEquals(k1.hashCode(), k2.hashCode());
      assertFalse(k1.equals(k3));
      assertFalse(k3.equals(k1));
      assertFalse(k3.equals(k4));
      assertFalse(k4.equals(k3));
   }

   public void testLifeCycle()
   {
      assertNull(ScopeManager.getCurrentScope());
      ScopedKey<String> key = ScopedKey.create("foo");
      assertEquals("", key.getScope());
      assertEquals("foo", key.getKey());

      //
      assertNull(ScopeManager.getCurrentScope());
      key = ScopedKey.create("foo");
      assertEquals("", key.getScope());
      assertEquals("foo", key.getKey());

      //
      begin();

      //
      assertEquals("repository", ScopeManager.getCurrentScope());
      key = ScopedKey.create("foo");
      assertEquals("repository", key.getScope());
      assertEquals("foo", key.getKey());

      //
      end();

      //
      assertNull(ScopeManager.getCurrentScope());
      key = ScopedKey.create("foo");
      assertEquals("", key.getScope());
      assertEquals("foo", key.getKey());
   }
}
