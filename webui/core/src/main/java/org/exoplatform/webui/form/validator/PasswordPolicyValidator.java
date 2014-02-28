package org.exoplatform.webui.form.validator;

import org.exoplatform.webui.form.UIFormInput;

/**
 * This validator checks the password against a policy named "passwordpolicy" from the validator configuration.
 * If no such entry is available, it validates that the password has a minimum of 6 and maximum of 30 chars, which
 * was the GateIn behavior before the introduction of this validator.
 *
 * The entries in the configuration file should look like this:
 * <pre>
 *      gatein.validators.passwordpolicy.length.min=5
 *      gatein.validators.passwordpolicy.length.max=50
 *      gatein.validators.passwordpolicy.regexp=...
 *      gatein.validators.passwordpolicy.format.message=Minimum 5 chars, max 50 chars, upper/lower case required, number required.
 * </pre>
 *
 * @see org.exoplatform.webui.form.validator.UserConfigurableValidator
 * @see org.exoplatform.webui.form.validator.PasswordStringLengthValidator
 * @author <a href="mailto:jpkroehling@redhat.com">Juraci Paixão Kröhling</a>
 */
public class PasswordPolicyValidator extends AbstractValidator {

    private static final String POLICY_CONFIG_ENTRY = "passwordpolicy";
    private AbstractValidator validator;

    public PasswordPolicyValidator() {

        // if we have a policy configured, we use it, otherwise, we do the simple verification
        // as we had before this change
        if (UserConfigurableValidator.getConfigurationNames().contains(POLICY_CONFIG_ENTRY)) {
            validator = new UserConfigurableValidator(POLICY_CONFIG_ENTRY, null);
        } else {
            validator = new PasswordStringLengthValidator(6, 30);
        }

    }

    @Override
    protected String getMessageLocalizationKey() {
        return validator.getMessageLocalizationKey();
    }

    @Override
    public void validate(UIFormInput uiInput) throws Exception {
        // this is needed, as the UserConfigurableValidator calls this one directly, which then skips the isValid
        validator.validate(uiInput);
    }

    @Override
    protected boolean isValid(String value, UIFormInput uiInput) {
        // this is needed, as the StringLengthValidator calls this one
        return validator.isValid(value, uiInput);
    }

    @Override
    protected Object[] getMessageArgs(String value, UIFormInput uiInput) throws Exception {
        // this is needed for proper formatting of the message, for the StringLengthValidator
        return validator.getMessageArgs(value, uiInput);
    }

}
