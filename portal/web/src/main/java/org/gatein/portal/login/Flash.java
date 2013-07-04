package org.gatein.portal.login;

import javax.inject.Named;
import juzu.FlashScoped;

@Named("flash")
@FlashScoped
public class Flash {

    private String success = "";

    private String error = "";


    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
