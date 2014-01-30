package org.gatein.api.application;

import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.PortalContainer;
import org.gatein.api.ApiException;
import org.gatein.api.Util;
import org.gatein.api.security.Permission;

import java.util.ArrayList;
import java.util.List;

/**
 * This service is the implementation of the public API part, for retrieving Application entries from the permanent
 * storage.
 *
 * This service is mostly a thin wrapper around the more complete ApplicationRegistryService, which is not part of the
 * public API.
 *
 * @see org.gatein.api.application.ApplicationRegistry
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ApplicationRegistryImpl implements ApplicationRegistry {

    private ApplicationRegistryService applicationRegistryService;

    public ApplicationRegistryImpl() {

        /*
        At the time of the implementation, there were two options here for
        retrieving applications from the permanent storage:

        1) use the existing service and expose only the methods we want as part of the public API;
        2) use the existing service only as a reference, making a completely independent call to the storage,
        parsing the results by ourselves.

        The chosen approach is, at least for now, the approach 1.

        The advantage of the approach 1 is that this place here is "clean", in the sense that we reuse everything that
        is being done already, and don't need to implement possible changes in two places, possibly having differences
        in the implementation, causing inconsistent behavior.

        The disadvantage is that the current logic is not a perfect fit for our needs, and might get more data retrieved
        than we need. For instance, applications are grouped into categories, and then iterated over to return a list of
        applications. The concept of categories is absent from the public API, so, it's not used here.
        Also, there's already a place to convert raw data into an Application object, a logic that is
        useless to us, as we'll convert it again into a representation that we want to expose as part of the API.

        As mentioned, the "reuse" argument seems the strongest right now, and we might revisit this decision in the
        future, should it present severe performance problems.
         */
        this.applicationRegistryService = PortalContainer
                .getInstance()
                .getComponentInstanceOfType(ApplicationRegistryService.class);
    }

    /**
     * Returns all known applications from the permanent storage.
     * @return a list of Application's
     * @throws ApiException
     */
    @Override
    public List<Application> getApplications() throws ApiException {
        try {
            return adaptApplications(applicationRegistryService.getAllApplications());
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves an specific application, based on its ID.
     * @param id the id of the application to retrieve
     * @return the application or null, if none is found
     * @throws ApiException
     */
    @Override
    public Application getApplication(String id) throws ApiException {
        try {
            return adaptApplication(applicationRegistryService.getApplication(id));
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    /**
     * Imports the applications there were deployed but not available yet. For instance, upon deployment of a
     * portlet, it might become available only after the execution of this method.
     *
     * @throws ApiException in case of problems with the permanent storage.
     */
    @Override
    public void importApplications() throws ApiException {
        try {
            applicationRegistryService.importAllPortlets();
            applicationRegistryService.importExoGadgets();
        } catch (Exception e) {
            throw new ApiException(e.getMessage(), e);
        }
    }

    /**
     * Helper method, to convert a list of org.exoplatform.application.registry.Application into a list of
     * org.gatein.api.application.Application
     *
     * @see org.gatein.api.application.ApplicationRegistryImpl#adaptApplication(org.exoplatform.application.registry.Application)
     * @param originalApplications a list of org.exoplatform.application.registry.Application
     * @return a list of org.gatein.api.application.Application
     */
    private List<Application> adaptApplications(List<org.exoplatform.application.registry.Application> originalApplications) {
        List<Application> applications = new ArrayList<Application>(originalApplications.size());

        for (org.exoplatform.application.registry.Application originalApplication : originalApplications) {
            applications.add(adaptApplication(originalApplication));
        }

        return applications;
    }

    /**
     * Converts an org.exoplatform.application.registry.Application into a org.gatein.api.application.Application .
     * This is required as we don't want to leak the internal representation of an application to consumers
     * of the public API. The downside is that we'll have one more place to change in case we add more properties
     * to the original application, but the benefit is that we also don't need to provide *all* data to the consumer.
     * In fact, some properties are deliberately *not* shared, as they are probably not useful to the consumer:
     *
     * org.exoplatform.application.registry.Application#storageId
     * org.exoplatform.application.registry.Application#contentId
     *
     * And possibly others.
     *
     * This is the key part on the conversion between a persisted application and an application that is available
     * via the public API.
     *
     * @param originalApplication the org.exoplatform.application.registry.Application to be converted
     * @return the application converted into a org.gatein.api.application.Application or null,
     *         if the originalApplication is null
     */
    private Application adaptApplication(org.exoplatform.application.registry.Application originalApplication) {

        if (null == originalApplication) {
            return null;
        }

        ApplicationImpl application = new ApplicationImpl();

        application.setAccessPermission(Util.from(originalApplication.getAccessPermissions()));
        application.setApplicationName(originalApplication.getApplicationName());
        application.setCategoryName(originalApplication.getCategoryName());
        application.setDescription(originalApplication.getDescription());
        application.setDisplayName(originalApplication.getDisplayName());
        application.setIconURL(originalApplication.getIconURL());
        application.setId(originalApplication.getId());
        application.setType(ApplicationType.valueOf(originalApplication.getType().getName().toUpperCase()));

        return application;
    }
}
