package org.exoplatform.services.organization.idm;

import org.gatein.common.logging.LogLevel;
import org.gatein.common.logging.Logger;

import java.util.Arrays;
import java.util.Collection;

/*
* JBoss, a division of Red Hat
* Copyright 2010, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

/**
 * Some helper methods
 */
public class Tools
{
   public static void logMethodIn(Logger log, LogLevel level, String methodName, Object[] args)
   {
      try
      {
         StringBuilder sb = new StringBuilder();
         sb.append("Method '")
            .append(methodName)
            .append("' called with arguments: ");

         if (args != null)
         {
            for (Object arg : args)
            {
               if (arg != null && arg instanceof Object[])
               {
                  sb.append(Arrays.toString((Object[])arg))
                     .append("; ");
               }
               else
               {
                  sb.append(arg)
                     .append("; ");
               }
            }
         }
         else
         {
            sb.append(args);
         }

         log.log(level, sb.toString());
      }
      catch (Throwable t)
      {
         log.log(level, "Error in logging code block (not related to application code): ", t);
      }

   }

   public static void logMethodOut(Logger log, LogLevel level, String methodName, Object result)
   {
      try
      {
         StringBuilder sb = new StringBuilder();
         sb.append("Method '")
            .append(methodName)
            .append("' returning object: ");

         if (result != null && result instanceof Collection)
         {
            sb.append("Collection of size: ").append(((Collection)result).size());
         }
         else
         {
            if (result != null)
            {
               sb.append("[").append(result.getClass().getCanonicalName()).append("]");
            }
            sb.append(result);
         }

         log.log(level, sb.toString());

      }
      catch (Throwable t)
      {
         log.log(level, "Error in logging code block (not related to application code): ", t);
      }
   }

}
