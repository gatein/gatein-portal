package org.gatein.portal.login;

import java.io.Serializable;

import javax.inject.Named;

import juzu.RequestScoped;

/**
 * Created with IntelliJ IDEA.
 * User: tuyennt
 * Date: 7/9/13
 * Time: 2:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class OauthProviderDescriptor implements Serializable {
    private String name;
    private String link;
    private String type;

    public OauthProviderDescriptor(String name, String link, String type) {
        this.name = name;
        this.link = link;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
