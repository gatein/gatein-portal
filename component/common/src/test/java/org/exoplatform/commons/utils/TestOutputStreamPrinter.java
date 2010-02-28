/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.commons.utils;

import junit.framework.Assert;
import org.exoplatform.component.test.AbstractGateInTest;
import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestOutputStreamPrinter extends AbstractGateInTest
{

   static final int NOOP = -1;

   static final int WRITE = 0;

   static final int FLUSH = 1;

   static final int CLOSE = 2;

   public void testIOExceptionBlocking()
   {
      internalTest(IOFailureFlow.RETHROW, true);
   }

   public void testUndeclaredIOExceptionBlocking()
   {
      internalTest(IOFailureFlow.THROW_UNDECLARED, true);
   }

   public void testIgnoreIOExceptionBlocking()
   {
      internalTest(IOFailureFlow.IGNORE, true);
   }

   public void testIOExceptionNoBlocking()
   {
      internalTest(IOFailureFlow.RETHROW, false);
   }

   public void testUndeclaredIOExceptionNoBlocking()
   {
      internalTest(IOFailureFlow.THROW_UNDECLARED, false);
   }

   public void testIgnoreIOExceptionNoBlocking()
   {
      internalTest(IOFailureFlow.IGNORE, false);
   }

   public void internalTest(IOFailureFlow mode, boolean blockOnFailure)
   {
      int writeAfterFailure = blockOnFailure ? NOOP : WRITE;
      int flushAfterFailure = blockOnFailure ? NOOP : FLUSH;

      //
      TestOutputStream out = new TestOutputStream(true, mode, blockOnFailure);
      out.write(mode);
      out.assertOperation(WRITE).assertEmpty().wantFailure = false;
      out.flush(IOFailureFlow.IGNORE);
      out.assertOperation(flushAfterFailure).assertEmpty();
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(writeAfterFailure).assertEmpty();
      out.close(IOFailureFlow.IGNORE);
      out.assertOperation(CLOSE).assertEmpty();

      //
      out = new TestOutputStream(false, mode, blockOnFailure);
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(WRITE).assertEmpty().wantFailure = true;
      out.flush(mode);
      out.assertOperation(FLUSH).assertEmpty().wantFailure = false;
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(writeAfterFailure).assertEmpty();
      out.close(IOFailureFlow.IGNORE);
      out.assertOperation(CLOSE).assertEmpty();

      //
      out = new TestOutputStream(false, mode, blockOnFailure);
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(WRITE).assertEmpty();
      out.flush(IOFailureFlow.IGNORE);
      out.assertOperation(FLUSH).assertEmpty().wantFailure = true;
      out.write(mode);
      out.assertOperation(WRITE).assertEmpty().wantFailure = false;
      out.close(IOFailureFlow.IGNORE);
      out.assertOperation(CLOSE).assertEmpty();

      //
      out = new TestOutputStream(false, mode, blockOnFailure);
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(WRITE).assertEmpty();
      out.flush(IOFailureFlow.IGNORE);
      out.assertOperation(FLUSH).assertEmpty();
      out.write(IOFailureFlow.IGNORE);
      out.assertOperation(WRITE).assertEmpty().wantFailure = true;
      out.close(mode);
      out.assertOperation(CLOSE).assertEmpty();
   }

   private static class TestOutputStream extends OutputStream
   {

      boolean wantFailure = false;

      final LinkedList<Integer> operations = new LinkedList<Integer>();

      OutputStreamPrinter osp;

      private TestOutputStream(boolean wantFailure, IOFailureFlow mode, boolean blockOnFailure)
      {
         this.wantFailure = wantFailure;
         this.osp = new OutputStreamPrinter(CharsetTextEncoder.getUTF8(), this, mode, blockOnFailure, false, 0);
      }

      public void write(int b) throws IOException
      {
         operations.add(WRITE);
         if (wantFailure)
         {
            throw new IOException();
         }
      }

      @Override
      public void flush() throws IOException
      {
         operations.add(FLUSH);
         if (wantFailure)
         {
            throw new IOException();
         }
      }

      @Override
      public void close() throws IOException
      {
         operations.add(CLOSE);
         if (wantFailure)
         {
            throw new IOException();
         }
      }

      public void write(IOFailureFlow mode)
      {
         switch (mode)
         {
            case RETHROW :
               try
               {
                  osp.write("a");
                  fail();
               }
               catch (IOException ignore)
               {
               }
               break;
            case THROW_UNDECLARED :
               try
               {
                  osp.write("a");
                  fail();
               }
               catch (UndeclaredIOException ignore)
               {
               }
               catch (IOException expected)
               {
                  fail();
               }
               break;
            case IGNORE :
               try
               {
                  osp.write("a");
               }
               catch (UndeclaredIOException ignore)
               {
                  fail();
               }
               catch (IOException expected)
               {
                  fail();
               }
               break;
         }
      }

      public void flush(IOFailureFlow mode)
      {
         switch (mode)
         {
            case RETHROW :
               try
               {
                  osp.flush();
                  fail();
               }
               catch (IOException ignore)
               {
               }
               break;
            case THROW_UNDECLARED :
               try
               {
                  osp.flush();
                  fail();
               }
               catch (UndeclaredIOException ignore)
               {
               }
               catch (IOException e)
               {
                  fail();
               }
               break;
            case IGNORE :
               try
               {
                  osp.flush();
               }
               catch (IOException e)
               {
                  fail();
               }
               break;
         }
      }

      public void close(IOFailureFlow mode)
      {
         switch (mode)
         {
            case RETHROW :
               try
               {
                  osp.close();
                  fail();
               }
               catch (IOException ignore)
               {
               }
               break;
            case THROW_UNDECLARED :
               try
               {
                  osp.close();
                  fail();
               }
               catch (UndeclaredIOException ignore)
               {
               }
               catch (IOException e)
               {
                  fail();
               }
               break;
            case IGNORE :
               try
               {
                  osp.close();
               }
               catch (IOException e)
               {
                  fail();
               }
               break;
         }
      }

      public TestOutputStream assertEmpty()
      {
         Assert.assertTrue(operations.isEmpty());
         return this;
      }

      public TestOutputStream assertOperation(int operation)
      {
         if (operation != NOOP)
         {
            Assert.assertFalse(operations.isEmpty());
            int actual = operations.removeFirst();
            Assert.assertEquals(operation, actual);
         }
         return this;
      }
   }
}
