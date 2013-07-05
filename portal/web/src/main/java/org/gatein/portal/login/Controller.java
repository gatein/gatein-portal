package org.gatein.portal.login;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import juzu.Action;
import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.template.Template;
import org.gatein.portal.mop.PropertyType;
import org.gatein.portal.servlet.Context;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.authentication.AuthenticationException;
import org.gatein.wci.security.Credentials;

/**
 * Created with IntelliJ IDEA.
 * User: tuyennt
 * Date: 7/3/13
 * Time: 3:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class Controller {
    /** . */
    private static final int AUTHENTICATED = 1;

    /** . */
    private static final int FAILED = 2;

    @Inject
    private Flash flash;

    @Inject
    @Path("login.gtmpl")
    private Template login;

    @View
    @Route(value = "/dologout", priority = 1)
    public Response doLogout() {
        try {
            ServletContainer container = ServletContainerFactory.getServletContainer();
            container.logout(Context.getCurrentRequest(), Context.getCurrentResponse());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        HttpServletRequest req = Context.getCurrentRequest();
        String initURL = req.getParameter("initURL");
        if(initURL == null || initURL.isEmpty()) {
            initURL = req.getContextPath();
        }
        return Response.redirect(initURL);
    }

    @View
    @Route(value = "/dologin", priority = 1)
    public Response doLogin() {
        try {
            HttpServletRequest req = Context.getCurrentRequest();
            int status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

            String initURL = req.getParameter("initURL");
            if(initURL == null || initURL.isEmpty()) {
                initURL = req.getContextPath();
            }

            if (status == AUTHENTICATED) {
                return Response.redirect(initURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String loginURL = Context.getCurrentRequest().getContextPath() + "/login";
        return Response.redirect(loginURL);
    }

    @Action
    @Route(value = "/actionLogin", priority = 1)
    public Response actionLogin(String username, String password) {
        try {
            Credentials credentials = new Credentials(username, password);
            ServletContainer container = ServletContainerFactory.getServletContainer();
            container.login(Context.getCurrentRequest(), Context.getCurrentResponse(), credentials);
            return Controller_.doLogin();
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticationException ex) {
            ex.printStackTrace();
        }
        flash.setError("Username or password incorrect!");
        return Controller_.login();
    }

    @View
    @Route(value = "/login", priority = 1)
    public Response login() {
        try {
            HttpServletRequest req = Context.getCurrentRequest();
            int status = req.getRemoteUser() != null ? AUTHENTICATED : FAILED;

            String initURL = req.getParameter("initURL");
            if(initURL == null || initURL.isEmpty()) {
                initURL = req.getContextPath();
            }

            if (status == AUTHENTICATED) {
                return Response.redirect(initURL);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return login.with().ok().with(juzu.PropertyType.STYLESHEET, "login-stylesheet");
    }
}
