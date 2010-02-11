package org.exoplatform.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GrowingOutputStream extends OutputStream{

	private final OutputStream out;
	
	private ByteArrayOutputStream buffer;

	private boolean open;
	
	public GrowingOutputStream(OutputStream out, int initialBufferSize) {
	      if (out == null)
	      {
	         throw new NullPointerException("No null output stream");
	      }
	      if (initialBufferSize < 0)
	      {
	         throw new IllegalArgumentException("No initial buffer size under 0");
	      }
	      
	      this.out = out;
	      this.buffer = new ByteArrayOutputStream(initialBufferSize);
	      this.open = true;
	}

	   @Override
	   public void write(int b) throws IOException
	   {
	      if (!open)
	      {
	         throw new IOException("closed");
	      }
	      buffer.write(b);
	   }

	   @Override
	   public void write(byte[] b, int off, int len) throws IOException
	   {
	      if (!open)
	      {
	         throw new IOException("closed");
	      }
	      buffer.write(b, off, len);
	   }

	   @Override
	   public void flush() throws IOException
	   {
	      if (!open)
	      {
	         throw new IOException("closed");
	      }

	      //
	      out.write(buffer.toByteArray());

	      //
	      out.flush();
	   }

	   @Override
	   public void close() throws IOException
	   {
	      if (!open)
	      {
	         throw new IOException("closed");
	      }

	      //
	      out.write(buffer.toByteArray());
	      
	      //
	      open = false;
	      out.close();
	   }

}
