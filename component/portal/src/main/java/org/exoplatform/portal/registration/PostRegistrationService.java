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
package org.exoplatform.portal.registration;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.organization.User;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * This service contains actions, which should be performed after successful registration of new user (Sending mail, Activating
 * of user, which is disabled by default etc.)<br>
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
public class PostRegistrationService {
    private final String mailFrom;
    private final String mailTo;
    private final String mailSubject;
    private final String mailMessage;
    private final MailService mailService;
    private final Boolean sendMailEnabled; // If false, then mails won't be send.
    private static final Logger log = LoggerFactory.getLogger(PostRegistrationService.class);

    public PostRegistrationService(InitParams params, MailService mailService) {
        this.mailService = mailService;
        this.sendMailEnabled = Boolean.valueOf(params.getValueParam("sendMailAfterRegistration").getValue());
        this.mailFrom = params.getValueParam("mailFrom").getValue();
        this.mailTo = params.getValueParam("mailTo").getValue();
        this.mailSubject = params.getValueParam("mailSubject").getValue();
        this.mailMessage = params.getValueParam("mailMessage").getValue();
    }

    /**
     * This method can be used to send mail to administrator after successful registration of new user.
     *
     * @param user which just register himself to portal.
     */
    public void sendMailAfterSuccessfulRegistration(User user) {
        // return if sending mails disabled in configuration.
        if (!sendMailEnabled) {
            log.debug("Sending of mails disabled. Mail won't be send about creating of user " + user.getUserName());
            return;
        }

        try {
            String subject = replaceTokens(mailSubject, user);
            String message = replaceTokens(mailMessage, user);
            String to = replaceTokens(mailTo, user);

            log.debug("Sending mail about the creating of user " + user.getUserName());
            mailService.sendMessage(mailFrom, to, subject, message);
        } catch (Exception e) {
            log.error("Error when sending mail to admin after registration of user " + user.getUserName(), e);
        }
    }

    /**
     * Replace tokens in message with real values of user. This can be used to inform administrator about attributes of concrete
     * user.
     *
     * @param param
     * @param user
     * @return
     */
    private String replaceTokens(String param, User user) {
        String result = param.replaceAll("\\$\\{user.userName\\}", user.getUserName());
        result = result.replaceAll("\\$\\{user.firstName\\}", user.getFirstName());
        result = result.replaceAll("\\$\\{user.lastName\\}", user.getLastName());
        result = result.replaceAll("\\$\\{user.email\\}", user.getEmail());
        return result;
    }

}
