package org.gatein.portal.ui.register;

import java.util.LinkedHashMap;
import java.util.Map;

import juzu.Mapped;

@Mapped
public class User {
    public String userName;

    public String password;

    public String firstName;

    public String lastName;

    public String displayName;

    public String emailAddress;

    private static final Map<String, User> users = new LinkedHashMap<String, User>();

    public User() {
    }

    public User(String userName, String password, String firstName, String lastName, String displayName,
                String emailAddress) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    public static void saveUser(User user) {
        System.out.println("save " + user.toString());
        users.put(user.userName, user);
    }

    public static User getUser(String userName) {
        return users.get(userName);
    }

    public String toString() {
        return "User (userName = " + this.userName + ")";
    }
}
