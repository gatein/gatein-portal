package org.gatein.portal.appzu.templates.inject;

import javax.inject.Inject;

import juzu.Response;
import juzu.View;
import org.exoplatform.container.PortalContainer;

/**
 * @author Julien Viet
 */
public class Controller {

    @Inject
    PortalContainer container;

    @View
    public Response.Content index() {
        return Response.ok("Running in portal container " + container.getName());
    }
}
