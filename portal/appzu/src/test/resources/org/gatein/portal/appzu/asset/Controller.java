package org.gatein.portal.appzu.asset;

import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        return Response.ok("<div id=\"foo\">pass</div>").withAssets("test.js", "test.css");
    }
}
