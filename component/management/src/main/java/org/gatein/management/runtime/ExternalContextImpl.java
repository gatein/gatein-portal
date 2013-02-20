package org.gatein.management.runtime;

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.gatein.management.api.ExternalContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class ExternalContextImpl implements ExternalContext {

    @Override
    public String getRemoteUser() {
        Identity identity = getIdentity();
        if (identity != null) {
            String user = identity.getUserId();

            // Returning null implies it's an anonymous user
            if (IdentityConstants.ANONIM.equals(user)) {
                return null;
            }
            return user;
        }

        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (role == null) return false;

        Identity identity = getIdentity();
        if (identity != null) {
            for (String r : identity.getRoles()) {
                if (role.equals(r)) return true;
            }
            return false;
        } else {
            // In order for export/import gadget to work (conversation/identity is not set) we must return true here
            return true;
        }
    }

    private static Identity getIdentity() {
        ConversationState conversation = ConversationState.getCurrent();
        if (conversation == null) return null;

        return conversation.getIdentity();
    }
}
