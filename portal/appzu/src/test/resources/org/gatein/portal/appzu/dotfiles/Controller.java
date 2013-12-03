package org.gatein.portal.appzu.dotfiles;

import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        return Response.ok("pass");
    }
}
