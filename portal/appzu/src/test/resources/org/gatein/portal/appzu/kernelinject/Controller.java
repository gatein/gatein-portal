package org.gatein.portal.appzu.kernelinject;

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
        if (container != null) {
            return Response.ok("pass");
        } else {
            return Response.ok("failed");
        }
    }
}
