package org.gatein.portal.jaas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.exoplatform.services.security.jaas.RolePrincipal;
import org.exoplatform.services.security.jaas.UserPrincipal;

/**
 * Created with IntelliJ IDEA.
 * User: tuyennt
 * Date: 6/28/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class BytesLoungeLoginModule implements LoginModule {
    private CallbackHandler handler;
    private Subject subject;
    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;
    private String login;
    private List<String> userGroups;

    @Override
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map<String, ?> sharedState,
                           Map<String, ?> options) {

        handler = callbackHandler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {

        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);

        try {
            handler.handle(callbacks);
            String name = ((NameCallback) callbacks[0]).getName();
            String password = String.valueOf(((PasswordCallback) callbacks[1])
                    .getPassword());

            // Here we validate the credentials against some
            // authentication/authorization provider.
            // It can be a Database, an external LDAP,
            // a Web Service, etc.
            // For this tutorial we are just checking if
            // user is "user123" and password is "pass123"
            if (name != null &&
                    name.equals("user123") &&
                    password != null &&
                    password.equals("pass123")) {

                // We store the username and roles
                // fetched from the credentials provider
                // to be used later in commit() method.
                // For this tutorial we hard coded the
                // "admin" role
                login = name;
                userGroups = new ArrayList<String>();
                userGroups.add("admin");
                userGroups.add("users");
                return true;
            }

            // If credentials are NOT OK we throw a LoginException
            throw new LoginException("Authentication failed");

        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }

    }

    @Override
    public boolean commit() throws LoginException {

        userPrincipal = new UserPrincipal(login);
        subject.getPrincipals().add(userPrincipal);

        if (userGroups != null && userGroups.size() > 0) {
            for (String groupName : userGroups) {
                rolePrincipal = new RolePrincipal(groupName);
                subject.getPrincipals().add(rolePrincipal);
            }
        }
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return false;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        return true;
    }

}
