package org.gatein.portal.appzu;

import javax.inject.Provider;

/**
 * @author Julien Viet
 */
public class RepositoryProvider implements Provider<ApplicationRepository> {

    @Override
    public ApplicationRepository get() {
        return ApplicationRepository.instance;
    }
}
