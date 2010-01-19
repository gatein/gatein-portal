package org.exoplatform.portal.config;

/**
 * @author <a href="mailto:trong.tran@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */
public class StorageException extends RuntimeException
{
   public StorageException()
   {
   }

   public StorageException(String message)
   {
      super(message);
   }

   public StorageException(Throwable cause)
   {
      super(cause);
   }

   public StorageException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
