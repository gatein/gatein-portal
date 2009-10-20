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

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>An extension of {@link Printer} that encodes the text with a provided encoder and sends the resulting
 * bytes on an {@link java.io.OutputStream}. The instance can be configured to have different behavior
 * on failure of the output stream.</p>
 *
 * <p>The <tt>ignoreOnFailure</tt> property will stop to make further invocations to the output stream if an exception
 * is thrown by the output stream except for the {@link #close()} method.</p>
 *
 * <p>The <tt>failureFlow</tt> property modifies the control flow of the method invocation when the output stream throws an
 * {@link java.io.IOException}.
 *
 * <ul>
 * <li>The {@link IOFailureFlow#IGNORE} value ignores the exception.</li>
 * <li>The {@link IOFailureFlow#RETHROW} value rethrows the exception.</li>
 * <li>The {@link IOFailureFlow#THROW_UNDECLARED} value throws instead a
 * {@link org.exoplatform.commons.utils.UndeclaredIOException} exception wrapping the original exception.</li>
 * </ul>
 *
 * </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OutputStreamPrinter extends Printer
{

   private final IOFailureFlow failureFlow;

   private final boolean ignoreOnFailure;

   private final OutputStream out;

   private final TextEncoder encoder;

   private boolean failed;

   /**
    * Builds an instance with the failureFlow being {@link IOFailureFlow#RETHROW} and
    * a the ignoreOnFailure property set to false.
    *
    * @param encoder the encoder
    * @param out the output
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(TextEncoder encoder, OutputStream out) throws IllegalArgumentException
   {
      this(encoder, out, IOFailureFlow.RETHROW, false);
   }

   /**
    * Builds a new instance.
    *
    * @param encoder the encoder
    * @param out the output
    * @param failureFlow the control flow failureFlow
    * @param ignoreOnFailure the behavior on failure
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(TextEncoder encoder, OutputStream out, IOFailureFlow failureFlow, boolean ignoreOnFailure)
      throws IllegalArgumentException
   {
      if (encoder == null)
      {
         throw new IllegalArgumentException("No null encoder accepted");
      }
      if (out == null)
      {
         throw new IllegalArgumentException("No null output stream accepted");
      }
      if (failureFlow == null)
      {
         throw new IllegalArgumentException("No null control flow mode accepted");
      }
      this.encoder = encoder;
      this.out = out;
      this.failureFlow = failureFlow;
      this.failed = false;
      this.ignoreOnFailure = ignoreOnFailure;
   }

   /**
    * Returns the failure flow.
    *
    * @return the failure flow
    */
   public IOFailureFlow getFailureFlow()
   {
      return failureFlow;
   }

   /**
    * Returns the ignore on failure property.
    * 
    * @return the ignore on failure property
    */
   public boolean getIgnoreOnFailure()
   {
      return ignoreOnFailure;
   }

   public boolean isFailed()
   {
      return failed;
   }

   @Override
   public void write(int c) throws IOException
   {
      if (!failed)
      {
         try
         {
            encoder.encode((char)c, out);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   @Override
   public void write(char[] cbuf) throws IOException
   {
      if (!failed)
      {
         try
         {
            encoder.encode(cbuf, 0, cbuf.length, out);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   @Override
   public void write(String str) throws IOException
   {
      if (!failed)
      {
         try
         {
            encoder.encode(str, 0, str.length(), out);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   @Override
   public void write(String str, int off, int len) throws IOException
   {
      if (!failed)
      {
         try
         {
            encoder.encode(str, off, len, out);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   public void write(char[] cbuf, int off, int len) throws IOException
   {
      if (!failed)
      {
         try
         {
            encoder.encode(cbuf, off, len, out);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   public void flush() throws IOException
   {
      if (!failed)
      {
         try
         {
            out.flush();
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   public void close() throws IOException
   {
      try
      {
         out.close();
      }
      catch (IOException e)
      {
         handle(e);
      }
   }

   private void handle(IOException e) throws IOException
   {
      if (ignoreOnFailure)
      {
         failed = true;
      }
      switch (failureFlow)
      {
         case IGNORE :
            break;
         case THROW_UNDECLARED :
            throw new UndeclaredIOException(e);
         case RETHROW :
            throw e;
      }
   }
}
