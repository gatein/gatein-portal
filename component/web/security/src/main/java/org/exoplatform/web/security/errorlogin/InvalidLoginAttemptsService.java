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
package org.exoplatform.web.security.errorlogin;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.mail.MailService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Service can be used to track invalid login attempts of users and do some actions when 
 * some number of successive login attempts is detected.
 * 
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public class InvalidLoginAttemptsService
{
   private static final Logger log = LoggerFactory.getLogger(InvalidLoginAttemptsService.class);

   /**
    *  If false, then feature of sending mails to administrator about bad login attempts is disabled.
    */
   private final Boolean sendMailEnabled;

   /**
    * Number of successive invalid login attempts of user, after the mail will be send.
    */
   private final Integer numberOfFailedAttempts;

   /**
    * Policy for indication that invalid login attempts are coming from same source. 
    * Valid values are SESSION, SESSION_AND_USER, SERVER. SESSION is default and it means that login attempts 
    * are coming from same HTTP session. SESSION_AND_USER is indicating login attempts of same user and from same HTTP session. 
    * SERVER means login attempts from same remote server.
    */
   private InvalidLoginPolicy invalidLoginPolicy;

   /**
    * This will be used as 'from' header in admin mail
    */
   private final String mailFrom;

   /**
    * This should be admin e-mail address, where e-mail about invalid login attempts will be send.
    */
   private final String mailTo;

   /**
    * Subject of email about invalid login attempts.
    */
   private final String mailSubject;

   /**
    * Content of mail message, which will be send to administrator. 
    * Real content will be based on value of parameter "invalidLoginPolicy". Tokens like ${username}, ${sessionId}, ${hostname} will be replaced with real values from attacker.
    */
   private final String mailMessage;

   /**
    * MailService injected by exo kernel.
    */
   private final MailService mailService;

   /**
    * Helper map to track login attempts from different users.
    */
   private final ConcurrentMap<InvalidAttemptKey, Integer> attemptMap = new ConcurrentHashMap<InvalidAttemptKey, Integer>();

   public InvalidLoginAttemptsService(InitParams params, MailService mailService)
   {
      this.mailService = mailService;
      this.sendMailEnabled = Boolean.valueOf(params.getValueParam("sendingMailsEnabled").getValue());
      this.numberOfFailedAttempts = Integer.parseInt(params.getValueParam("numberOfFailedAttempts").getValue());
      this.invalidLoginPolicy = InvalidLoginPolicy.valueOf(params.getValueParam("invalidLoginPolicy").getValue());
      this.mailFrom = params.getValueParam("mailFrom").getValue();
      this.mailTo = params.getValueParam("mailTo").getValue();
      this.mailSubject = params.getValueParam("mailSubject").getValue();
      this.mailMessage = params.getPropertiesParam("mailMessage").getProperty(invalidLoginPolicy.toString());
   }

   /**
    * This should be called each time when invalid login attempt is detected (typically from HttpFilter)
    * 
    * @param sessionId
    * @param username
    * @param hostname
    */
   public void badLoginAttempt(String sessionId, String username, String hostname)
   {
      if (log.isDebugEnabled())
      {
         log.debug(new StringBuilder("Detected invalid login attempt. Session id=").append(sessionId).append(
               ", username=").append(username).append(", hostname=").append(hostname));
      }
      if (numberOfFailedAttempts == 1)
      {
         sendMail(sessionId, username, hostname);
         return;
      }

      InvalidAttemptKey key = InvalidAttemptKey.createKey(invalidLoginPolicy, sessionId, username, hostname);

      // TODO: better synchronization
      int currentCount = 1;
      if (attemptMap.containsKey(key))
      {
         currentCount = attemptMap.get(key) + 1;
         if (currentCount == numberOfFailedAttempts)
         {
            sendMail(sessionId, username, hostname);
            attemptMap.remove(key);
         }
         else
         {
            attemptMap.put(key, currentCount);
         }
      }
      else
      {
         attemptMap.put(key, currentCount);
      }
   }

   /**
    * This should be called each time successful login attempt is detected. We need to use it to clean previous bad attempts
    * when we have good attempt.
    * 
    * @param sessionId
    * @param username
    * @param hostname
    */
   public void successfulLoginAttempt(String sessionId, String username, String hostname)
   {
      if (log.isDebugEnabled())
      {
         log.debug(new StringBuilder("Detected successful login attempt. Session id=").append(sessionId).append(
               ", username=").append(username).append(", hostname=").append(hostname));
      }
      InvalidAttemptKey key = InvalidAttemptKey.createKey(invalidLoginPolicy, sessionId, username, hostname);
      attemptMap.remove(key);
   }

   /**
    * Allows to set custom invalidLoginPolicy
    *
    * @param invalidLoginPolicy
    */
   public void setInvalidLoginPolicy(InvalidLoginPolicy invalidLoginPolicy)
   {
      this.invalidLoginPolicy = invalidLoginPolicy;
   }

   private void sendMail(String sessionId, String username, String hostname)
   {
      // return if sending mails disabled in configuration.
      if (!sendMailEnabled)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Sending of mails disabled. Mail won't be send about invalid login attempts.");
         }
         return;
      }

      // replace tokens from configuration with real values.
      String result = mailMessage.replaceAll("\\$\\{sessionId\\}", sessionId);
      result = result.replaceAll("\\$\\{username\\}", username);
      result = result.replaceAll("\\$\\{hostname\\}", hostname);
      result = result.replaceAll("\\$\\{number\\}", String.valueOf(numberOfFailedAttempts));

      try
      {
         if (log.isDebugEnabled())
         {
            log.debug("Sending mail about the invalid login attempts. Mail message is: " + result);
         }
         mailService.sendMessage(mailFrom, mailTo, mailSubject, result);
      }
      catch (Exception e)
      {
         // log exception but not throw it when sending of mail happen
         log.error("Error when sending mail to admin after detected invalid number of login attempts.", e);
      }
   }

}
