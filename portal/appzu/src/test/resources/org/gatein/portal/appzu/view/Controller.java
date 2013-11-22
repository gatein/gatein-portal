package org.gatein.portal.appzu.view;

import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        return Response.ok("<div id=\"target\">THE VIEW</div>");
    }
}
