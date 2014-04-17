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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;

/**
 * A user-configurable validator. Several aspects of this validator can be configured via properties in the
 * configuration.properties file found in the GateIn configuration directory (${gatein.conf.dir}). The validator supports
 * several configurations that can be activated when a validator instance is created by passing it the name of the configuration
 * to be activated. A configuration is created by adding an entry in configuration.properties using the {@link #KEY_PREFIX}
 * prefix followed by the name of the configuration, a period '.' and the name of the validation aspect to modify.
 * <p/>
 * Currently supported validation aspects, where {configuration} is a configuration's name:
 * <ul>
 * <li>{@link #KEY_PREFIX}{configuration}.length.min: the minimal length of the validated field</li>
 * <li>{@link #KEY_PREFIX}{configuration}.length.max: the maximal length of the validated field</li>
 * <li>{@link #KEY_PREFIX}{configuration}.regexp: the regular expression to which the validated field must conform</li>
 * <li>{@link #KEY_PREFIX}{configuration}.format.message: a message to display providing details about the format of values the
 * regular expression allows in case the validated field doesn't conform to it</li>
 * </ul>
 *
 * Currently used configurations in the code are defined by the {@link #USERNAME} and {@link #GROUPMEMBERSHIP} names.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
@Serialized
public class UserConfigurableValidator extends MultipleConditionsValidator {
    protected static Log log = ExoLogger.getLogger(UserConfigurableValidator.class);

    public static final String USERNAME = "username";
    public static final String GROUPMEMBERSHIP = "groupmembership";
    public static final String PAGE_NAME = "pagename";
    public static final String DEFAULT_LOCALIZATION_KEY = "ExpressionValidator.msg.value-invalid";
    /**
     * Note that this regular expression should actually validate comma-separated usernames. This is not the case as some
     * constraints (consecutive symbols for examples) are not taken into account.
     */
    public static final String GROUP_MEMBERSHIP_VALIDATION_REGEX = "^(\\p{Lower}[\\p{Lower}\\d\\._]+)(\\s*,\\s*(\\p{Lower}[\\p{Lower}\\d\\._]+))*$";
    public static final String GROUP_MEMBERSHIP_LOCALIZATION_KEY = "UIGroupMembershipForm.msg.Invalid-char";

    private static Map<String, ValidatorConfiguration> configurations = new HashMap<String, ValidatorConfiguration>(3);

    public static final String KEY_PREFIX = "gatein.validators.";

    static {
        String gateinConfDir = System.getProperty("gatein.conf.dir");
        File conf = new File(gateinConfDir, "configuration.properties");
        if (conf.exists()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(conf));
                int length = KEY_PREFIX.length();
                for (Object objectKey : properties.keySet()) {
                    String key = (String) objectKey;
                    if (key.startsWith(KEY_PREFIX)) {
                        // extract property key
                        String propertyKey = key.substring(length, key.indexOf('.', length));
                        if (!configurations.containsKey(propertyKey)) {
                            configurations.put(propertyKey, new ValidatorConfiguration(propertyKey, properties));
                        }
                    }
                }
            } catch (IOException e) {
                log.info(e.getLocalizedMessage());
                log.debug(e);
            }
        }
    }

    private final String validatorName;
    private final String localizationKey;

    // needed by @Serialized
    public UserConfigurableValidator() {
        this(USERNAME, DEFAULT_LOCALIZATION_KEY);
    }

    public UserConfigurableValidator(String configurationName, String messageLocalizationKey) {
        this(configurationName, messageLocalizationKey, true);
    }

    public UserConfigurableValidator(String configurationName, String messageLocalizationKey,
            Boolean exceptionOnMissingMandatory) {
        this.exceptionOnMissingMandatory = exceptionOnMissingMandatory;
        this.trimValue = true;
        localizationKey = messageLocalizationKey != null ? messageLocalizationKey : DEFAULT_LOCALIZATION_KEY;
        this.validatorName = configurationName != null ? configurationName : USERNAME;
    }

    public UserConfigurableValidator(String configurationName) {
        this(configurationName, DEFAULT_LOCALIZATION_KEY);
    }

    @Override
    public void validate(UIFormInput uiInput) throws Exception {
        if (exceptionOnMissingMandatory) {
            super.validate(uiInput);
        } else {
            String label = getLabelFor(uiInput);

            CompoundApplicationMessage messages = new CompoundApplicationMessage();

            validate((String) uiInput.getValue(), label, messages, uiInput);

            if (!messages.isEmpty()) {
                throw new MessageException(messages);
            }

        }
    }

    @Override
    protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput) {
        ValidatorConfiguration configuration = configurations.get(validatorName);

        if (value == null) {
            value = "";
        }

        if (configuration == null) {
            // we don't have a user-configured validator for this validator name

            if (USERNAME.equals(validatorName)) {
                // if the validator name is USERNAME constant, we have a username to validate with the original, non-configured
                // behavior
                UsernameValidator.validate(value, label, messages, UsernameValidator.DEFAULT_MIN_LENGTH,
                        UsernameValidator.DEFAULT_MAX_LENGTH);
            } else if (GROUPMEMBERSHIP.equals(validatorName)) {
                // else, we assume that we need to validate a group membership, replicating original behavior
                if (!Pattern.matches(GROUP_MEMBERSHIP_VALIDATION_REGEX, value)) {
                    messages.addMessage(localizationKey, new Object[] { label });
                }
            } else if (PAGE_NAME.equals(validatorName)) {
               ConfigurableIdentifierValidator.validate(value, label, messages, uiInput, ConfigurableIdentifierValidator.DEFAULT_MIN_LENGTH, ConfigurableIdentifierValidator.DEFAULT_MAX_LENGTH);
            }
        } else {
            // otherwise, use the user-provided configuration
            if (value.length() < configuration.minLength || value.length() > configuration.maxLength) {
                messages.addMessage("StringLengthValidator.msg.length-invalid",
                        new Object[] { label, configuration.minLength.toString(), configuration.maxLength.toString() });
            }

            if (!Pattern.matches(configuration.pattern, value)) {
                messages.addMessage(localizationKey, new Object[] { label, configuration.formatMessage });
            }
        }
    }

    private static class ValidatorConfiguration {
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private String formatMessage;

        private ValidatorConfiguration(String propertyKey, Properties properties) {
            // used to assign backward compatible default values

            boolean isUser = USERNAME.equals(propertyKey);
            String prefixedKey = KEY_PREFIX + propertyKey;
            String minProperty = properties.getProperty(prefixedKey + ".length.min");
            String maxProperty = properties.getProperty(prefixedKey + ".length.max");
            if (USERNAME.equals(propertyKey)) {
                minLength = minProperty != null ? Integer.valueOf(minProperty) : UsernameValidator.DEFAULT_MIN_LENGTH;
                maxLength = maxProperty != null ? Integer.valueOf(maxProperty) : UsernameValidator.DEFAULT_MAX_LENGTH;
                pattern = properties.getProperty(prefixedKey + ".regexp", Utils.USER_NAME_VALIDATOR_REGEX);
            } else if (PAGE_NAME.equals(propertyKey)) {
                minLength = minProperty != null ? Integer.valueOf(minProperty) : ConfigurableIdentifierValidator.DEFAULT_MIN_LENGTH;
                maxLength = maxProperty != null ? Integer.valueOf(maxProperty) : ConfigurableIdentifierValidator.DEFAULT_MAX_LENGTH;
                pattern = properties.getProperty(prefixedKey + ".regexp", ConfigurableIdentifierValidator.IDENTIFER_VALIDATOR_REGEX);
            } else {
                minLength = minProperty != null ? Integer.valueOf(minProperty) : 0;
                maxLength = maxProperty != null ? Integer.valueOf(maxProperty) : Integer.MAX_VALUE;
                pattern = properties.getProperty(prefixedKey + ".regexp", Utils.USER_NAME_VALIDATOR_REGEX);
            }
            formatMessage = properties.getProperty(prefixedKey + ".format.message", pattern);
        }

    }
}
