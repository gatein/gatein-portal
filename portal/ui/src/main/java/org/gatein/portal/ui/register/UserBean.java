package org.gatein.portal.ui.register;

import juzu.Mapped;
import org.exoplatform.services.organization.User;

@Mapped
public class UserBean {
    public String userName;

    public String password;

    public String firstName;

    public String lastName;

    public String displayName;

    public String emailAddress;

    public UserBean(User user) {
        this.userName = user.getUserName();
        this.password = "";
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.displayName = user.getDisplayName();
        this.emailAddress = user.getEmail();
    }

    public UserBean() {
    }

    public UserBean(String userName, String password, String firstName, String lastName, String displayName,
                    String emailAddress) {
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
    }

    public void update(User user) {
        user.setDisplayName(displayName);
        user.setEmail(emailAddress);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPassword(password);
    }

    public String toString() {
        return "User (userName = " + this.userName + ")";
    }
}
