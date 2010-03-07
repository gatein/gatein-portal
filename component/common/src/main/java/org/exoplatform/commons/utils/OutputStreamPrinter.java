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

import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

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
 * {@link UndeclaredIOException} exception wrapping the original exception.</li>
 * </ul>
 *
 * </p>
 *
 * <p>The class provides direct write access to the underlying output stream when the client of the stream can provides
 * bytes directly.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OutputStreamPrinter extends Printer implements BinaryOutput
{

   private final IOFailureFlow failureFlow;

   private final boolean ignoreOnFailure;

   private final OutputStream out;

   private final TextEncoder encoder;

   private boolean failed;

   private final boolean flushOnClose;

   /**
    * Builds an instance with the failureFlow being {@link IOFailureFlow#RETHROW} and
    * a the ignoreOnFailure property set to false.
    *
    * @param encoder the encoder
    * @param out the output
    * @param flushOnClose flush when stream is closed
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(TextEncoder encoder, OutputStream out, boolean flushOnClose) throws IllegalArgumentException
   {
      this(encoder, out, IOFailureFlow.RETHROW, false, flushOnClose, 0, false);
   }

   /**
    * Builds an instance with the failureFlow being {@link IOFailureFlow#RETHROW} and
    * a the ignoreOnFailure property set to false.
    *
    * @param encoder the encoder
    * @param out the output
    * @param flushOnClose flush when stream is closed
    * @param bufferSize the size of the buffer
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(TextEncoder encoder, OutputStream out, boolean flushOnClose, int bufferSize) throws IllegalArgumentException
   {
      this(encoder, out, IOFailureFlow.RETHROW, false, flushOnClose, bufferSize, false);
   }

   /**
    * Builds an instance with the failureFlow being {@link IOFailureFlow#RETHROW} and
    * a the ignoreOnFailure property set to false.
    *
    * @param encoder the encoder
    * @param out the output
    * @param flushOnClose flush when stream is closed
    * @param bufferSize the initial size of the buffer
    * @param growing if the buffer should grow in size once full
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(TextEncoder encoder, OutputStream out, boolean flushOnClose, int bufferSize, boolean growing) throws IllegalArgumentException
   {
      this(encoder, out, IOFailureFlow.RETHROW, false, flushOnClose, bufferSize, growing);
   }
   
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
      this(encoder, out, IOFailureFlow.RETHROW, false, false, 0, false);
   }

   /**
    * Builds a new instance with the specified parameters and the delegate output.
    *
    * @param encoder the encoder
    * @param out the output
    * @param failureFlow the control flow failureFlow
    * @param ignoreOnFailure the behavior on failure
    * @param flushOnClose flush when stream is closed
    * @param bufferSize the buffer size
    * @throws IllegalArgumentException if any argument is null
    */
   public OutputStreamPrinter(
		      TextEncoder encoder,
		      OutputStream out,
		      IOFailureFlow failureFlow,
		      boolean ignoreOnFailure,
		      boolean flushOnClose,
		      int bufferSize)
		      throws IllegalArgumentException
   {
	   this(encoder, out, failureFlow, ignoreOnFailure, flushOnClose, bufferSize, false);
   }
   
   
   public OutputStreamPrinter(
      TextEncoder encoder,
      OutputStream out,
      IOFailureFlow failureFlow,
      boolean ignoreOnFailure,
      boolean flushOnClose,
      int bufferSize,
      boolean growing
      )
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
      if (bufferSize < 0)
      {
         throw new IllegalArgumentException("Invalid negative max buffer size: " + bufferSize);
      }

      //
      if (bufferSize > 0 && !growing)
      {
         out = new BufferingOutputStream(out, bufferSize);
      }
      else if (growing)
      {
    	 out = new GrowingOutputStream(out, bufferSize);
      }

      //
      this.encoder = encoder;
      this.out = out;
      this.failureFlow = failureFlow;
      this.failed = false;
      this.ignoreOnFailure = ignoreOnFailure;
      this.flushOnClose = flushOnClose;
   }

   public final Charset getCharset()
   {
      return encoder.getCharset();
   }

   /**
    * Returns the failure flow.
    *
    * @return the failure flow
    */
   public final IOFailureFlow getFailureFlow()
   {
      return failureFlow;
   }

   /**
    * Returns the ignore on failure property.
    * 
    * @return the ignore on failure property
    */
   public final boolean getIgnoreOnFailure()
   {
      return ignoreOnFailure;
   }

   public final boolean isFailed()
   {
      return failed;
   }

   // Bytes access interface

   public final void write(byte b) throws IOException
   {
      if (!failed)
      {
         try
         {
            out.write(b);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   public final void write(byte[] bytes) throws IOException
   {
      if (!failed)
      {
         try
         {
            out.write(bytes);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   public final void write(byte[] bytes, int off, int len) throws IOException
   {
      if (!failed)
      {
         try
         {
            out.write(bytes, off, len);
         }
         catch (IOException e)
         {
            handle(e);
         }
      }
   }

   //

   @Override
   // Note that the parent method has a synchronisation that we want to avoid
   // for performance reasons
   public final void write(int c) throws IOException
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
   public final void write(char[] cbuf) throws IOException
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
   public final void write(String str) throws IOException
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
   // Note that the parent method has a synchronisation that we want to avoid
   // for performance reasons
   public final void write(String str, int off, int len) throws IOException
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

   public final void write(char[] cbuf, int off, int len) throws IOException
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

   public final void flush() throws IOException
   {
      if (!failed && !flushOnClose)
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

   public final void close() throws IOException
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
   
   /**
    * Flush the output stream. This allows for the outputstream
    * to be independently flushed regardless of the flushOnClose setting.
    */
   public void flushOutputStream() throws IOException
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
}
