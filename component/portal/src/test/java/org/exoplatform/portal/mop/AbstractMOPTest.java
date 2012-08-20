package org.exoplatform.portal.mop;

import junit.framework.AssertionFailedError;
import org.exoplatform.portal.AbstractPortalTest;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public abstract class AbstractMOPTest extends AbstractPortalTest
{

   @Override
   protected void end(boolean save)
   {
      if (save)
      {
         try
         {
            startService();
            super.end(save);
         }
         finally
         {
            stopService();
         }
      }
      else
      {
         super.end(save);
      }
   }

   @Override
   protected void tearDown() throws Exception
   {
      end();
      super.tearDown();
   }

   private void startService()
   {
      try
      {
         begin();
         end();
      }
      catch (Exception e)
      {
         AssertionFailedError afe = new AssertionFailedError();
         afe.initCause(e);
         throw afe;
      }
   }

   private void stopService()
   {
      begin();
      end();
   }
}
