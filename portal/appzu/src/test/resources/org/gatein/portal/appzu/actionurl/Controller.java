package org.gatein.portal.appzu.actionurl;

import juzu.Action;
import juzu.Response;
import juzu.View;

/**
 * @author Julien Viet
 */
public class Controller {

    @View
    public Response.Content index() {
        try {
            return Response.ok("<a id=\"target\" href=\"" + Controller_.greet("world") + "\">greet</div>");
        } catch (RuntimeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            throw e;
        }
    }

    @Action
    public Response.View greet(String name) {
        try {
            return Controller_.hello(name);
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
