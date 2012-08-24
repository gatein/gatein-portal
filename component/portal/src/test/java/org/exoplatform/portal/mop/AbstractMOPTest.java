package org.exoplatform.portal.mop;

import org.exoplatform.portal.AbstractPortalTest;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractMOPTest extends AbstractPortalTest
{

   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      begin();
   }

   @Override
   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }
}
