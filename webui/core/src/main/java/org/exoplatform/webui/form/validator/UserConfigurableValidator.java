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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
@Serialized
public class UserConfigurableValidator extends MultipleConditionsValidator
{
   protected static Log log = ExoLogger.getLogger(UserConfigurableValidator.class);

   public static final String USERNAME = "username";
   public static final String GROUPMEMBERSHIP = "groupmembership";
   public static final String DEFAULT_LOCALIZATION_KEY = "ExpressionValidator.msg.value-invalid";
   public static final String GROUP_MEMBERSHIP_VALIDATION_REGEX = "^\\p{L}[\\p{L}\\d._\\-\\s*,\\s*]+$";
   public static final String GROUP_MEMBERSHIP_LOCALIZATION_KEY = "UIGroupMembershipForm.msg.Invalid-char";

   private static Map<String, ValidatorConfiguration> configurations = new HashMap<String, ValidatorConfiguration>(3);

   public static final String KEY_PREFIX = "gatein.validators.";

   static
   {
      String gateinConfDir = System.getProperty("gatein.conf.dir");
      File conf = new File(gateinConfDir, "configuration.properties");
      if (conf.exists())
      {
         try
         {
            Properties properties = new Properties();
            properties.load(new FileInputStream(conf));
            int length = KEY_PREFIX.length();
            for (Object objectKey : properties.keySet())
            {
               String key = (String)objectKey;
               if (key.startsWith(KEY_PREFIX))
               {
                  // extract property key
                  String propertyKey = key.substring(length, key.indexOf('.', length));
                  if(!configurations.containsKey(propertyKey))
                  {
                     configurations.put(propertyKey, new ValidatorConfiguration(propertyKey, properties));
                  }
               }
            }
         }
         catch (IOException e)
         {
            log.info(e.getLocalizedMessage());
            log.debug(e);
         }
      }
   }

   private final String validatorName;
   private final String localizationKey;

   // needed by @Serialized
   public UserConfigurableValidator()
   {
      this(USERNAME, DEFAULT_LOCALIZATION_KEY);
   }

   public UserConfigurableValidator(String validatorName, String messageLocalizationKey)
   {
      this.exceptionOnMissingMandatory = true;
      this.trimValue = true;
      localizationKey = messageLocalizationKey != null ? messageLocalizationKey : DEFAULT_LOCALIZATION_KEY;
      this.validatorName = validatorName != null ? validatorName : USERNAME;
   }

   public UserConfigurableValidator(String validatorName)
   {
      this(validatorName, DEFAULT_LOCALIZATION_KEY);
   }

   @Override
   protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput)
   {
      ValidatorConfiguration configuration = configurations.get(validatorName);

      if (configuration == null)
      {
         // we don't have a user-configured validator for this validator name

         if (USERNAME.equals(validatorName))
         {
            // if the validator name is USERNAME constant, we have a username to validate with the original, non-configured behavior
            UsernameValidator.validate(value, label, messages, UsernameValidator.DEFAULT_MIN_LENGTH, UsernameValidator.DEFAULT_MAX_LENGTH);
         }
         else
         {
            // else, we assume that we need to validate a group membership, replicating original behavior
            if (!Pattern.matches(GROUP_MEMBERSHIP_VALIDATION_REGEX, value))
            {
               messages.addMessage(localizationKey, new Object[]{label});
            }
         }
      }
      else
      {
         // otherwise, use the user-provided configuration

         if (value.length() < configuration.minLength || value.length() > configuration.maxLength)
         {
            messages.addMessage("StringLengthValidator.msg.length-invalid", new Object[]{label, configuration.minLength.toString(), configuration.maxLength.toString()});
         }

         if (!Pattern.matches(configuration.pattern, value))
         {
            messages.addMessage(localizationKey, new Object[]{label, configuration.formatMessage});
         }
      }
   }

   private static class ValidatorConfiguration
   {
      private Integer minLength;
      private Integer maxLength;
      private String pattern;
      private String formatMessage;

      private ValidatorConfiguration(String propertyKey, Properties properties)
      {
         // used to assign backward compatible default values
         boolean isUser = USERNAME.equals(propertyKey);
         String prefixedKey = KEY_PREFIX + propertyKey;

         String property = properties.getProperty(prefixedKey + ".min.length");
         minLength = property != null ? Integer.valueOf(property) : (isUser ? UsernameValidator.DEFAULT_MIN_LENGTH : 0);

         property = properties.getProperty(prefixedKey + ".max.length");
         maxLength = property != null ? Integer.valueOf(property) : (isUser ? UsernameValidator.DEFAULT_MAX_LENGTH : Integer.MAX_VALUE);

         pattern = properties.getProperty(prefixedKey + ".regexp", Utils.USER_NAME_VALIDATOR_REGEX);
         formatMessage = properties.getProperty(prefixedKey + ".format.message", pattern);
      }

   }
}
