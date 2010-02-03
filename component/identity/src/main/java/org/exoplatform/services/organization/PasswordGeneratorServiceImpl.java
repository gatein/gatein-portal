/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.exoplatform.services.organization;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:theute@redhat.com">Thomas Heute</a>
 * @version $Revision$
 */
public class PasswordGeneratorServiceImpl implements PasswordGeneratorService
{

   private String passwordCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_][{};:?.-+";

   public String generatePassword()
   {
      Logger logger = LoggerFactory.getLogger(PasswordGeneratorServiceImpl.class);
      int length = 12;
      StringBuffer buffer = new StringBuffer();
      char[] characterMap = passwordCharacters.toCharArray();
      SecureRandom secureRandom;
      try
      {
         secureRandom = SecureRandom.getInstance("SHA1PRNG");
      }
      catch (NoSuchAlgorithmException e)
      {
         logger.warn("SHA1PRNG algorithm isn't available, falling back to insecure password");
         return "" + System.currentTimeMillis();
      }

      for (int i = 0; i <= length; i++)
      {
         byte[] bytes = new byte[512];
         secureRandom.nextBytes(bytes);
         double number = secureRandom.nextDouble();
         int b = ((int) (number * characterMap.length));
         buffer.append(characterMap[b]);
      }

      return buffer.toString();
   }

}
