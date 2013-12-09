package org.gatein.portal.appzu.templates.assets;

import juzu.Path;
import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        return Response.ok("<div class=\"custom text-center\">Click Me!</div>");
    }
}
