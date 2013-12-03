package org.gatein.portal.appzu.ui;

import javax.inject.Named;

import juzu.FlashScoped;

/**
 * @author Julien Viet
 */
@FlashScoped
@Named("flash")
public class Flash {

    /** . */
    public String error;

    /** . */
    public String success;

}
