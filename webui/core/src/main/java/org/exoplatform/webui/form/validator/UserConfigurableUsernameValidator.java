/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.webui.form.validator;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.form.UIFormInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
@Serialized
public class UserConfigurableUsernameValidator extends UsernameValidator
{
   private static ValidatorConfiguration configuration = new ValidatorConfiguration();

   // needed by @Serialized
   public UserConfigurableUsernameValidator()
   {
      this.exceptionOnMissingMandatory = true;
      this.trimValue = true;
   }

   @Override
   protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput)
   {
      if (configuration.defaultConfig)
      {
         super.validate(value, label, messages, uiInput);
      }
      else
      {
         if (value.length() < configuration.minLength || value.length() > configuration.maxLength)
         {
            messages.addMessage("StringLengthValidator.msg.length-invalid", new Object[]{label, configuration.minLength.toString(), configuration.maxLength.toString()});
         }

         if (!Pattern.matches(configuration.pattern, value))
         {
            messages.addMessage("ExpressionValidator.msg.value-invalid", new Object[]{label, configuration.formatMessage});
         }
      }
   }

   private static class ValidatorConfiguration
   {
      protected static Log log = ExoLogger.getLogger("username-validator");
      private Integer minLength;
      private Integer maxLength;
      private String pattern;
      private String formatMessage;
      private boolean defaultConfig = true;

      private ValidatorConfiguration()
      {
         String gateinConfDir = System.getProperty("gatein.conf.dir");
         File conf = new File(gateinConfDir, "username-validator.properties");

         minLength = DEFAULT_MIN_LENGTH;
         maxLength = DEFAULT_MAX_LENGTH;
         pattern = Utils.USER_NAME_VALIDATOR_REGEX;
         formatMessage = pattern;

         if (conf.exists())
         {
            try
            {
               Properties properties = new Properties();
               properties.load(new FileInputStream(conf));
               minLength = Integer.valueOf(properties.getProperty("minLength", String.valueOf(DEFAULT_MIN_LENGTH)));
               maxLength = Integer.valueOf(properties.getProperty("maxLength", String.valueOf(DEFAULT_MAX_LENGTH)));
               pattern = properties.getProperty("regexp", Utils.USER_NAME_VALIDATOR_REGEX);
               formatMessage = properties.getProperty("formatMessage", formatMessage);
               defaultConfig = false;
            }
            catch (IOException e)
            {
               log.info(e.getLocalizedMessage());
               log.debug(e);
            }
         }
      }
   }
}
