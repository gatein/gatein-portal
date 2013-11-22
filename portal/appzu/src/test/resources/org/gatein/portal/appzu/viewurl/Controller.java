package org.gatein.portal.appzu.viewurl;

import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        try {
            return Response.ok("<a id=\"target\" href=\"" + Controller_.hello("world") + "\">click</a>");
        } catch (RuntimeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    @View
    public Response.Content hello(String name) {
        try {
            return Response.ok("<div id=\"target\">hello " + name +  "</div>");
        } catch (RuntimeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }
}
